package com.shoplane.muon.common.handler;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.shoplane.muon.common.Constants;
import com.shoplane.muon.common.communication.Ring;
import com.shoplane.muon.interfaces.UpdateUITask;
import com.shoplane.muon.interfaces.WebsocketConnectionStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by ravmon on 24/8/15.
 */
public class WebSocketRequestHandler {
    private static final String TAG = WebSocketRequestHandler.class.getSimpleName();
    private static final String WEB_SOCKET_CONNECTION_URL = "128.199.237.71";
    private static final int SERVER_PORT = 9000;
    private static final String CONNECTION_TOKEN_FORMAT = "/stream?accessToken=";


    private boolean mIsConnectedToServer;
    private WebSocket mWebsocket;
    private Ring mRing;
    private String mAuthServerToken;

    private static WebSocketRequestHandler mWebSocketHandlerInstance;

    public synchronized static WebSocketRequestHandler getInstance() {
        if (null == mWebSocketHandlerInstance) {
            mWebSocketHandlerInstance = new WebSocketRequestHandler();
        }
        return mWebSocketHandlerInstance;
    }

    public void setAuthServerToken(String authServerToken) {
        this.mAuthServerToken = authServerToken;
    }

    private WebSocketRequestHandler() {
        mIsConnectedToServer = false;

        mWebsocket = null;
        mRing = new Ring(4);
        this.mAuthServerToken = null;
    }

    public boolean isConnectedToServer() {
        return mIsConnectedToServer;
    }

    public WebSocket getWebsocket() {
        return mWebsocket;
    }

    public void connectToServer(String authToken,
                                final WebsocketConnectionStatus websocketConnectionStatus) {
        String hostAddr = "ws://" + WEB_SOCKET_CONNECTION_URL + ":" + SERVER_PORT +
                CONNECTION_TOKEN_FORMAT + authToken;
        mAuthServerToken = authToken;

        AsyncHttpClient.WebSocketConnectCallback mWebSocketConnectCallback =
                new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    Log.e(TAG, "Failed to connect to Server");
                    mIsConnectedToServer = false;
                    websocketConnectionStatus.onWebsocketConnected();
                    return;
                }
                mIsConnectedToServer = true;
                mWebsocket = webSocket;

                webSocket.setStringCallback(new RingStringCallback());

                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception e) {
                        Log.i(TAG, "websocket connection closed " + e);
                        mIsConnectedToServer = false;
                        mWebsocket = null;
                    }
                });

                webSocket.setEndCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception e) {
                        Log.i(TAG, "websocket connection ended " + e);
                        mIsConnectedToServer = false;
                        mWebsocket = null;
                    }
                });

                websocketConnectionStatus.onWebsocketConnected();
            }
        };

        AsyncHttpClient mAsyncHttpClient = AsyncHttpClient.getDefaultInstance();
        mAsyncHttpClient.websocket(hostAddr, null, mWebSocketConnectCallback);
    }

    public void createAndSendGetRequestToServer(final JSONObject requestJson,
                                                WeakReference<UpdateUITask> actRef,
                                                final String queryPath) {

        // get and put request id in json
        final String request = mRing.registerRequest(requestJson, actRef, queryPath);
        if (!mIsConnectedToServer || null == mWebsocket) {
            connectToServer(mAuthServerToken, new WebsocketConnectionStatus() {
                @Override
                public void onWebsocketConnected() {
                    if(mIsConnectedToServer && mWebsocket != null) {
                        Log.i(TAG, "Get Request is " + request);
                        mWebsocket.send(request);
                    } else {
                        Log.e(TAG, "Failed to send get request to server");
                    }
                }
            });
        } else {
            Log.i(TAG, "Get Request is " + request);
            mWebsocket.send(request);
        }

    }

    public void createAndSendPostRequestToServer(final String request,
                                                 final WeakReference<UpdateUITask> actWeakRef,
                                                 final String queryPath) {

        if (!mIsConnectedToServer || null == mWebsocket) {
            connectToServer(mAuthServerToken, new WebsocketConnectionStatus() {
                @Override
                public void onWebsocketConnected() {
                    if(mIsConnectedToServer && mWebsocket != null) {
                        mRing.addTaskToUpdateUIForPostRequest(queryPath.trim().toLowerCase(),
                                actWeakRef);
                        mWebsocket.send(request);
                    } else {
                        Log.e(TAG, "Failed to send post request to server");
                    }
                }
            });
        } else {
            mRing.addTaskToUpdateUIForPostRequest(queryPath.trim().toLowerCase(),
                    actWeakRef);
            mWebsocket.send(request);
        }
    }

    private void processPostResponse(final JSONObject jsonResponse, final String messageType) {

        if (messageType.trim().equalsIgnoreCase(Constants.QUERY_SUGGESTION_STYLES_PATH)) {
            // check if activity exists or not
            final UpdateUITask task =  mRing.getTaskToUpdateUIForPostRequest(
                    Constants.QUERY_SUGGESTION_STYLES_PATH).get();
            if (task != null && task.getActivityAvailableStatus()) {

                //run update task asociated with the request
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("UI thread", "Run query activity update task on UI thread " +
                        messageType);
                        task.updateUI(jsonResponse);
                    }
                });
            }

        }
    }

    private void processGetResponse(final JSONObject jsonResponse, final String responseType,
                                    final int reqid) {
        if (mRing.isRequestActive(reqid)) {
            //TODO race codition resolution
            WeakReference<UpdateUITask> taskRef = mRing.getTaskToUpdateUIForGetRequest(reqid);
            if (taskRef != null) {
                final UpdateUITask task = taskRef.get();
                if (task != null && task.getActivityAvailableStatus()) {

                    //run update task asociated with the request
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Run get request update task on UI thread " + reqid +
                                    " " + responseType);
                            task.updateUI(jsonResponse);
                        }
                    });
                } else {
                    Log.i(TAG, "Activity not available");
                }
            } else {
                Log.i(TAG, "UpdateUI reference cleared");
            }
            // clear request
            Log.i(TAG, "Clear get request after update");
            mRing.clearRequest(reqid);
        }
    }

    private class RingStringCallback implements WebSocket.StringCallback {

        @Override
        public void onStringAvailable(String response) {
            // get requestid from string
            //final int reqId;
            JSONObject responseObject;
            try {
                responseObject = new JSONObject(response);

                // Post Request
                Log.i(TAG, response);

                boolean requestType = responseObject.has("messageType");
                if (requestType) {
                    Log.i(TAG, "Process post request");
                    String messageType = responseObject.getString("messageType");
                    processPostResponse(responseObject.getJSONObject("data"),
                            messageType.trim().toLowerCase());
                    return;
                }

                // Get Request
                requestType = responseObject.has("responseType");
                if (requestType) {
                    Log.i(TAG, "Process get request");
                    String responseType = responseObject.getString("responseType");
                    int reqid = Integer.parseInt(responseObject.getString("reqid"));
                    processGetResponse(responseObject.getJSONObject("data"),
                            responseType.trim().toLowerCase(), reqid);
                    return;
                }

                Log.e(TAG, "Bad Response. Ignore");
            } catch (JSONException je) {
                Log.e(TAG, "Failed to process response header");
            }

        }
    }

    public void closeWebsocketConnection() {
        Log.i(TAG, "Close Websocket connection if it is there");
        if(mWebsocket != null) {
            mWebsocket.close();
        }

        mIsConnectedToServer = false;
        mWebsocket = null;
        mAuthServerToken = null;
    }

}
