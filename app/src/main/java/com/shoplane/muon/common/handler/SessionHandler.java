package com.shoplane.muon.common.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.shoplane.muon.common.Constants;
import com.shoplane.muon.interfaces.AuthStatus;
import com.shoplane.muon.interfaces.WebsocketConnectionStatus;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ravmon on 24/8/15.
 */
public class SessionHandler {
    private static final String TAG = SessionHandler.class.getSimpleName();


    private static SessionHandler mSessionHandler;
    private boolean mIsUserLoggedIn;
    private String mServerAuthToken;
    private String mLoginMode;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    Context mContext;

    AuthStatus mAuthStatus;
    private int mModeOfLogin;
    private String mUid;

    private SessionHandler(Context context) {
        this.mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREF_FILE_NAME,
                Constants.SHAREF_PREF_PRIVATE_MODE);
        mEditor = mSharedPreferences.edit();
        mIsUserLoggedIn = false;
        mServerAuthToken = null;
        mModeOfLogin = -1;
        mUid = null;
        mAuthStatus = null;
    }

    public synchronized static SessionHandler getInstance(Context context) {
        if (null == mSessionHandler) {
            mSessionHandler = new SessionHandler(context);
        }
        return mSessionHandler;
    }

    public void checkLoginSessionOnStart(AuthStatus status) {
        // Get authtoken
        mAuthStatus = status;
        mLoginMode =  mSharedPreferences.getString(Constants.SHARED_PREF_LOGIN_MODE, null);
        mServerAuthToken = mSharedPreferences.getString(Constants.SHARED_PREF_USERAUTHTOKEN, null);
        if (null == mServerAuthToken ||
                null == mLoginMode ||
                mServerAuthToken.trim().equalsIgnoreCase(Constants.EMPTY_STRING) ||
                mLoginMode.trim().equalsIgnoreCase(Constants.EMPTY_STRING)) {
            Log.i(TAG, "Server authentication not done");
            mIsUserLoggedIn = false;
            mAuthStatus.authStatus(false);
            mAuthStatus = null;
        } else {
            Log.i(TAG, "Connect to Server with sharedpref token " + mServerAuthToken);
            WebSocketRequestHandler.getInstance().connectToServer(mServerAuthToken,
                    new WebsocketConnectionStatus() {
                        @Override
                        public void onWebsocketConnected() {
                            if (WebSocketRequestHandler.getInstance().isConnectedToServer()) {
                                Log.i(TAG, "Reconnect succesful");
                                mIsUserLoggedIn = true;
                                mAuthStatus.authStatus(true);
                                mAuthStatus = null;
                            } else {
                                Log.i(TAG, "Websocket connection failed for reconnection");
                                mIsUserLoggedIn = false;
                                mAuthStatus.authStatus(false);
                                mAuthStatus = null;
                            }
                        }
                    });
        }

    }

    public int getLoginModeFromSP() {
        return  mSharedPreferences.getInt(Constants.SHARED_PREF_LOGIN_MODE, -1);
    }

    public String getLoginUidFromSP() {
        return  mSharedPreferences.getString(Constants.SHARED_PREF_USERID, null);
    }

    public boolean isUserLoggedIn() {
        return mIsUserLoggedIn;
    }

    //Create login session
    public void createUserLoginSession(int mode, String uid, String authToken, AuthStatus status) {
        mAuthStatus = status;
        mUid = uid;
        mModeOfLogin = mode;

        if (Constants.FB_LOGIN == mModeOfLogin) {
            // FB login
            Map<String, String> loginRequest = new HashMap<String, String>();
            loginRequest.put("fbuid", uid);
            loginRequest.put("fbauthtoken", authToken);
            String json = new GsonBuilder().create().toJson(loginRequest, Map.class);
            Log.e(TAG, "FB login request " + json);

            new InitiateUserLogin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    "http://128.199.237.71:9000/v1/oauth/with-facebook", json);
        }

        if (Constants.GUEST_LOGIN == mModeOfLogin) {
            new InitiateGuestLogin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    "http://128.199.237.71:9000/v1/guest/token");
        }

    }

    public void logoutUser() {
        // clear token
        Log.i(TAG, "Log out user");
        mEditor.clear();
        mEditor.commit();

        // close websocket connection
        WebSocketRequestHandler.getInstance().closeWebsocketConnection();

        // set flags to null
        mIsUserLoggedIn = false;
        mServerAuthToken = null;
    }

    public void closeSessionHandler() {
        mEditor.clear();
    }

    private class InitiateUserLogin extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                HttpPost httpPost = new HttpPost(uri[0]);
                httpPost.setEntity(new StringEntity(uri[1]));
                httpPost.setHeader("Content-type", "application/json");

                Log.d(TAG, "Call " + uri[0]);
                response = httpclient.execute(httpPost);
                StatusLine statusLine = response.getStatusLine();
                Log.d(TAG, "Call " + uri[0] + " Status Code = " + statusLine.getStatusCode());
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                    Log.d(TAG, "Call to " + uri[0] + " Response body = " + responseString);
                } else if (statusLine.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                    Log.e(TAG, "Call to " + uri[0] + " received no content.");
                } else {
                    Log.e(TAG, "Exception while calling  " + uri[0] + " " +
                            statusLine.getReasonPhrase());
                }

            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "UnsupportedEncodingException while calling  " + uri[0] + " " + e);
            } catch (ClientProtocolException e) {
                Log.e(TAG, "ClientProtocolException while calling  " + uri[0] + " " + e);
            } catch (IOException e) {
                Log.e(TAG, "IOException while calling  " + uri[0] + " " + e);
            } catch (Exception e) {
                Log.e(TAG, "Exception while calling  " + uri[0] + " " + e);
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null == result) {
                Log.e(TAG, "Server token is null");
                mAuthStatus.authStatus(false);
                return;
            }

            JsonObject jobj = new Gson().fromJson(result, JsonObject.class);

            mServerAuthToken = jobj.get("token").toString();

            Log.i(TAG, mServerAuthToken);

            if (null == mServerAuthToken || mServerAuthToken.trim().
                    equalsIgnoreCase(Constants.EMPTY_STRING)) {
                Log.d(TAG, "Sever auth token is null");
                mAuthStatus.authStatus(false);
                return;
            }

            // Create websocket connection
            WebSocketRequestHandler.getInstance().connectToServer(mServerAuthToken,
                    new WebsocketConnectionStatus() {
                        @Override
                        public void onWebsocketConnected() {
                            if (WebSocketRequestHandler.getInstance().isConnectedToServer()) {
                                    Log.i(TAG, "Write to shared pref token " + mServerAuthToken);
                                    // Storing server token, mode and id in sharedpreference
                                    mEditor.putString(Constants.SHARED_PREF_USERAUTHTOKEN,
                                            mServerAuthToken);
                                    mEditor.putString(Constants.SHARED_PREF_USERID, mUid);
                                    mEditor.putInt(Constants.SHARED_PREF_LOGIN_MODE, mModeOfLogin);
                                    // commit changes
                                    mEditor.commit();

                                    Log.i(TAG, "Commit changes in shared pref");
                                    mIsUserLoggedIn = true;
                                    mAuthStatus.authStatus(true);
                            } else {
                                Log.i(TAG, "Websocket connection failed for user login");
                                mIsUserLoggedIn = false;
                                mAuthStatus.authStatus(false);
                            }
                        }
                    });
        }
    }

    private class InitiateGuestLogin extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                HttpGet httpGet = new HttpGet(uri[0]);

                Log.d(TAG, "Call " + uri[0]);
                response = httpclient.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                Log.d(TAG, "Call " + uri[0] + " Status Code = " + statusLine.getStatusCode());
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                    Log.d(TAG, "Call to " + uri[0] + " Response body = " + responseString);
                } else if (statusLine.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                    Log.e(TAG, "Call to " + uri[0] + " received no content.");
                } else {
                    Log.e(TAG, "Exception while calling  " + uri[0] + " " +
                            statusLine.getReasonPhrase());
                }

            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "UnsupportedEncodingException while calling  " + uri[0] + " " + e);
            } catch (ClientProtocolException e) {
                Log.e(TAG, "ClientProtocolException while calling  " + uri[0] + " " + e);
            } catch (IOException e) {
                Log.e(TAG, "IOException while calling  " + uri[0] + " " + e);
            } catch (Exception e) {
                Log.e(TAG, "Exception while calling  " + uri[0] + " " + e);
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null == result) {
                Log.e(TAG, "Server token is null");
                mAuthStatus.authStatus(false);
                return;
            }

            JsonObject jobj = new Gson().fromJson(result, JsonObject.class);
            mServerAuthToken = jobj.get("token").toString();
            Log.i(TAG, mServerAuthToken);

            if (null == mServerAuthToken || mServerAuthToken.trim().
                    equalsIgnoreCase(Constants.EMPTY_STRING)) {
                Log.d(TAG, "Sever auth token is null");
                mAuthStatus.authStatus(false);
                return;
            }

            // Create websocket connection
            WebSocketRequestHandler.getInstance().connectToServer(mServerAuthToken,
                    new WebsocketConnectionStatus() {
                        @Override
                        public void onWebsocketConnected() {
                            if (WebSocketRequestHandler.getInstance().isConnectedToServer()) {
                                Log.i(TAG, "Logged as guest user");
                                mAuthStatus.authStatus(true);
                            } else {
                                Log.i(TAG, "Websocket connection failed for guest login");
                                mAuthStatus.authStatus(false);
                            }
                        }
                    });
        }
    }

}

