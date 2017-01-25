package com.shoplane.muon.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;

import com.shoplane.muon.R;
import com.shoplane.muon.common.handler.SessionHandler;
import com.shoplane.muon.common.utils.NetworkConnectionUtil;
import com.shoplane.muon.interfaces.AuthStatus;

public class SplashActivity extends Activity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    // Time to display splash in mili seconds
    private final int SPLASH_DISPLAY_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // grand hotel font for welcome quote
        Typeface typeFaceSplashAppName = Typeface.createFromAsset(this.getAssets(),
                "fonts/Pacifico.ttf");
        TextView splashAppNameTextView = (TextView) findViewById(R.id.splash_app_name);
        splashAppNameTextView.setTypeface(typeFaceSplashAppName);
        TextView splashAppTagTextView = (TextView) findViewById(R.id.splash_app_tag);
        splashAppTagTextView.setTypeface(typeFaceSplashAppName);

        if (!(NetworkConnectionUtil.checkNetworkConnection(this))) {
            alertNoConnection();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkIfUserLoggedIn();
                }
            }, SPLASH_DISPLAY_TIME);
        }

        // crash handling. Restarting from splash as static data will be lost
        /*Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                // Close session handler editor
                SessionHandler.getInstance(SplashActivity.this).closeSessionHandler();
                // Close websocket connection when home activity is destroyed

                if(WebSocketRequestHandler.getInstance().getWebsocket() != null) {
                    WebSocketRequestHandler.getInstance().getWebsocket().close();
                }

                Intent intent = new Intent(SplashActivity.this, SplashActivity.class);
                SplashActivity.this.startActivity(intent);

                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });*/
    }

    private void checkNetworkConnection() {
        if (!(NetworkConnectionUtil.checkNetworkConnection(this))) {
            alertNoConnection();
        } else {
            checkIfUserLoggedIn();
        }
    }

    private void alertNoConnection() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("No Internet Connection");

        alertDialogBuilder
                .setMessage("Please enable network connection and Retry!!!")
                .setCancelable(false)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        checkNetworkConnection();
                    }
                })
                .setNegativeButton("Close App", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        SplashActivity.this.finish();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void openLoginActivity() {
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
        finish();
    }

    private void openFeedActivity() {
        Intent feedActivityIntent = new Intent(this, FeedActivity.class);
        startActivity(feedActivityIntent);
        finish();
    }

    private void checkIfUserLoggedIn() {
        // Check for credentials
        SessionHandler.getInstance(this).checkLoginSessionOnStart(new AuthStatus() {
            @Override
            public void authStatus(boolean status) {
                if (status) {
                    Log.i(TAG, "Session initialized on start");
                    openFeedActivity();
                } else {
                    // login not successful
                    Log.i(TAG, "Session not initialized on start");
                    openLoginActivity();
                }
            }
        });

    }
}
