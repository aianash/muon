package com.shoplane.muon.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.shoplane.muon.R;
import com.shoplane.muon.common.Constants;
import com.shoplane.muon.common.handler.SessionHandler;
import com.shoplane.muon.common.handler.WebSocketRequestHandler;
import com.shoplane.muon.common.utils.userinterface.CircleButton;
import com.shoplane.muon.interfaces.AuthStatus;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    // Tag for logcat
    private static final String TAG = "LoginActivity";
    private static final String USER_NOT_LOGGEDIN = "User not loggedin";
    private static final String GPLUS_CONNECTION_FAILED =
            "Error while connecting to play services. Please try again";


    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    //request code for sign in non negative
    private static final int RC_SIGN_IN = 9001;

    // Flag to check Sign In button clicked
    private boolean mGPlusSignInClicked;
    private boolean mFbSignInClicked;
    private String mGPlusAccessToken;
    private String mGPlusUserId;
    private String mGPlusIdToken;

    private ConnectionResult mConnectionResult;

    // Facebook Login
    private LoginButton mfbLoginButton;
    private CallbackManager mfbCallbackManager;

    private ProgressDialog mProgressdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // fb initialize
        FacebookSdk.sdkInitialize(getApplicationContext());
        mfbCallbackManager = CallbackManager.Factory.create();

        // Setup icon module
        Iconify.with(new IoniconsModule());
        setContentView(R.layout.activity_login);

        // grand hotel font for welcome quote
        Typeface typeFaceWelcomeQuote = Typeface.createFromAsset(this.getAssets(),
                "fonts/Pacifico.ttf");
        TextView welcomeTextView = (TextView) findViewById(R.id.welcome_quote);
        welcomeTextView.setTypeface(typeFaceWelcomeQuote);

        setupGplusSignIn();
        setupFBSignIn();

        // Setup button listeners
        Button skipLoginButton = (Button) findViewById(R.id.skip_login_button);
        skipLoginButton.setOnClickListener(this);

        mProgressdialog = new ProgressDialog(this);
        mProgressdialog.setIndeterminate(true);

        // If logged then handle logout as we may have come after pressing signout button
        handleLogout();
    }

    private void setupGplusSignIn() {
        //SignInButton btnSignIn = (SignInButton) findViewById(R.id.gplus_signin_button);
        // Button click listeners
        CircleButton gplusLoginButton = (CircleButton) findViewById(R.id.gplus_login_button);
        gplusLoginButton.setImageDrawable(new IconDrawable(this,
                IoniconsIcons.ion_social_googleplus).colorRes(R.color.white).actionBarSize());
        gplusLoginButton.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN).build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void setupFBSignIn() {

        //mfbLoginButton = (LoginButton) findViewById(R.id.fb_signin_button);
        //mfbLoginButton.setOnClickListener(this);
        //mfbLoginButton.setReadPermissions(Arrays.asList("public_profile", "user_friends", "email",
        //        "user_location"));
        CircleButton fbLoginButton = (CircleButton) findViewById(R.id.fb_login_button);
        fbLoginButton.setImageDrawable(new IconDrawable(this,
                IoniconsIcons.ion_social_facebook).colorRes(R.color.white).actionBarSize());
        fbLoginButton.setOnClickListener(this);

        LoginManager.getInstance().registerCallback(mfbCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // Fb login successful
                        AccessToken accessToken = loginResult.getAccessToken();
                        String uid = accessToken.getUserId();
                        String fbAuthToken = accessToken.getToken();
                        // Call server with this information and get login token from it
                        Log.i(TAG, "FB token and id " + fbAuthToken + uid);
                        createSession(Constants.FB_LOGIN, uid, fbAuthToken);
                    }

                    @Override
                    public void onCancel() {
                        if (mProgressdialog.isShowing()) {
                            mProgressdialog.dismiss();
                        }
                        Log.e(TAG, "FB login attempt cancelled");
                        Toast.makeText(LoginActivity.this, "Login Failed",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException e) {
                        if (mProgressdialog.isShowing()) {
                            mProgressdialog.dismiss();
                        }
                        Log.e(TAG, "FB login attempt failed");
                        Toast.makeText(LoginActivity.this, "Login Failed",
                                Toast.LENGTH_LONG).show();
                    }
                });

    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.v(TAG, "onconnectedfailed");

        if (mProgressdialog.isShowing()) {
            mProgressdialog.dismiss();
        }

        Toast.makeText(this, GPLUS_CONNECTION_FAILED, Toast.LENGTH_LONG).show();
        mGPlusSignInClicked = false;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mFbSignInClicked) {
            mfbCallbackManager.onActivityResult(requestCode, resultCode, data);
            mFbSignInClicked = false;
        }

        if (mGPlusSignInClicked) {
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    // Signed in successfully, show authenticated UI.
                    GoogleSignInAccount acct = result.getSignInAccount();
                    createSession(1, "", mGPlusAccessToken);
                } else {
                    // Signed out, show unauthenticated UI.
                }
                mGPlusSignInClicked = false;
            }
        }
    }

    // 3 modes for signing in
    // 1 for UserPass Login
    // 2 for GPlus login
    // 3 for FB login
    private void createSession(int mode, String userid, String userAuthToken) {
        SessionHandler.getInstance(this).createUserLoginSession(mode,
                userid, userAuthToken, new AuthStatus() {
                    @Override
                    public void authStatus(boolean status) {
                        if (mProgressdialog.isShowing()) {
                            mProgressdialog.dismiss();
                        }

                        if (status) {
                            Log.i(TAG, "Login successful. Open feed activity");
                            openFeedActivity();
                        } else {
                            Log.i(TAG, "Login failed. Stay on Login activity");
                            // Logout from Facebook is session is created
                            if (LoginManager.getInstance() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                });
    }

    // Button onclick listener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gplus_login_button:
                if (SessionHandler.getInstance(this).isUserLoggedIn()) {
                    // do nothing
                } else {
                    mProgressdialog.setMessage("Logging in with GPlus");
                    mProgressdialog.show();
                    mGPlusSignInClicked = true;
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
                // signInWithGplus();
                break;
            case R.id.fb_login_button:
                //signInWithFb();
                if (SessionHandler.getInstance(this).isUserLoggedIn()) {
                    // do nothing
                } else {
                    mProgressdialog.setMessage("Logging in with FaceBook");
                    mProgressdialog.show();
                    mFbSignInClicked = true;
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this,
                            Arrays.asList("public_profile", "user_friends",
                                    "email", "user_location"));
                }
                break;
            case R.id.skip_login_button:
                mProgressdialog.setMessage("Logging in as Guest user");
                mProgressdialog.show();
                signInAsGuest();
                break;
        }
    }

    private void signInAsGuest() {
        createSession(Constants.GUEST_LOGIN, "", "");
    }

    private void handleLogout() {
        mProgressdialog.show();

        if (SessionHandler.getInstance(this).isUserLoggedIn()) {
            if (loginmode == fb) {
                Log.i(TAG, "Log out from facebook");
                // logout from facebook
                LoginManager.getInstance().logOut();
            } else {
                // gplus
                Log.i(TAG, "Log out from gplus");
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            }
            // delete token
            SessionHandler.getInstance(this).logoutUser();
        } else {
            if (WebSocketRequestHandler.getInstance().isConnectedToServer()) {
                Log.i(TAG, "Guest session, close websocket connection");
                WebSocketRequestHandler.getInstance().closeWebsocketConnection();
            } else {
                Log.i(TAG, "New Session");
            }
        }

        if (mProgressdialog.isShowing()) {
            mProgressdialog.dismiss();
        }

    }

    private void openFeedActivity() {
        Intent feedActivityIntent = new Intent(this, FeedActivity.class);
        startActivity(feedActivityIntent);
        finish();
    }
}