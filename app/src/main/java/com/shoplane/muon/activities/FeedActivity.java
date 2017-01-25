package com.shoplane.muon.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.emilsjolander.components.StickyScrollViewItems.StickyScrollView;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.shoplane.muon.R;
import com.shoplane.muon.common.Constants;
import com.shoplane.muon.common.handler.SessionHandler;
import com.shoplane.muon.common.handler.VolleyRequestHandler;
import com.shoplane.muon.common.handler.WebSocketRequestHandler;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;


public class FeedActivity extends AppCompatActivity {

    private final String TAG = FeedActivity.class.getSimpleName();

    private CircleImageView mProfilePic;
    private TextView mWelcomeTextview;

    private String[] mServerUrls = {
            "http://imagizer.imageshack.us/v2/1080x720q90/661/b7B7Aa.jpg",
            "http://imagizer.imageshack.us/v2/1080x720q90/661/ydjkV8.jpg",
            "http://imagizer.imageshack.us/v2/1080x720q90/673/dUsfNv.jpg",
            "http://imagizer.imageshack.us/v2/1080x720q90/673/lQJ1xH.jpg",
            "http://imagizer.imageshack.us/v2/1080x720q90/673/j0tCUL.jpg",
            "http://imagizer.imageshack.us/v2/1080x720q90/901/yDq86D.jpg",
            "http://imagizer.imageshack.us/v2/1080x720q90/661/73Bv58.jpg"};

    private int[] mFeedImageViews = {
            R.id.adv_imageView2,
            R.id.adv_imageView3,
            R.id.adv_imageView4,
            R.id.adv_imageView5,
            R.id.adv_imageView6,
            R.id.adv_imageView7,
            R.id.adv_imageView8
    };

    private void openQueryActivity() {
        Intent queryActivityIntent = new Intent(this, QueryActivity.class);
        startActivity(queryActivityIntent);
    }

    private void openLoginActivity() {
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
    }

    private void fetchNewFeeds() {

        ImageLoader imageLoader = VolleyRequestHandler.getVolleyRequestHandlerInstance(this).
                getImageLoader();

        for (int i = 0; i < 7; i++) {
            NetworkImageView feedImgView = (NetworkImageView) findViewById(mFeedImageViews[i]);

            feedImgView.setDefaultImageResId(R.drawable.ic_no_image);
            feedImgView.setErrorImageResId(R.drawable.ic_no_image);
            feedImgView.setAdjustViewBounds(true);
            feedImgView.setImageUrl(mServerUrls[i], imageLoader);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        View advView = findViewById(R.id.adv_image_layout1);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        advView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (height * 0.6)));


        final ViewGroup profileView = (ViewGroup) findViewById(R.id.adv_image_layout1);
        final ScrollView scrollView = (StickyScrollView) findViewById(R.id.sticky_scrollview);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {

                        int scrollY = scrollView.getScrollY();
                        profileView.setTranslationY(scrollY / 2);
                    }
                });

        Iconify.with(new IoniconsModule());
        mProfilePic = (CircleImageView) findViewById(R.id.profile_image);
        mWelcomeTextview = (TextView) findViewById(R.id.welcome_text);

        // initialize fb sdk to get profile information and access token
        FacebookSdk.sdkInitialize(getApplicationContext());

        scrollView.smoothScrollTo(0, 0);
        fetchNewFeeds();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // fetch profile info and set it
        setProfileInfo();
    }

    private void setProfileInfo() {
        if (SessionHandler.getInstance(this).isUserLoggedIn()) {
            // user logged in get profile info and set it
            // get mode of login
            Log.i(TAG, "get profile info");
            int mode = SessionHandler.getInstance(this).getLoginModeFromSP();
            if (-1 == mode) {
                Log.e(TAG, "Login mode is not correct");
                return;
            }

            String uid = SessionHandler.getInstance(this).getLoginUidFromSP();
            if (null == uid) {
                Log.e(TAG, "Login uid is not correct");
                return;
            }


            if (Constants.FB_LOGIN == mode) {
                Log.i(TAG, "Get profile pic from fb");
                getProfilePicFromFb(uid.trim());
                getUserNameFromFb();
            }

        } else {
            // user not logged in show guest text
            mProfilePic.setImageDrawable(new IconDrawable(this,
                    IoniconsIcons.ion_android_person).colorRes(R.color.white).sizeDp(100));
            mWelcomeTextview.setText(R.string.welcome_text_guest);
        }
    }

    private void getUserNameFromFb() {
        AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        Log.i(TAG, "fbAccessToken = " + fbAccessToken.toString());
        GraphRequestAsyncTask request = GraphRequest.newMeRequest(fbAccessToken,
                new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                Log.i(TAG, user.optString("first_name"));
                mWelcomeTextview.setText("Hello " + user.optString("first_name"));
            }
        }).executeAsync();
    }

    private void getProfilePicFromFb(final String uid) {
        AsyncTask<Void, Void, Bitmap> getFBPictask = new AsyncTask<Void, Void, Bitmap>() {

            @Override
            public Bitmap doInBackground(Void... params) {
                URL fbPicUrl = null;
                Bitmap fbPicBitmap = null;
                try {
                    fbPicUrl = new URL("https://graph.facebook.com/" + uid + "/picture?type=large");
                    fbPicBitmap = BitmapFactory.decodeStream(fbPicUrl.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                   Log.e(TAG, "Malformedurl exception while getting fb profile pic " + e);
                } catch (IOException e) {
                    Log.e(TAG, "IOexception while getting fb profile pic " + e);
                }
                return fbPicBitmap;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (null != result) {
                    mProfilePic.setImageBitmap(result);
                } else {
                    Log.e(TAG, "Profile pic not received");
                }
            }
        };

        getFBPictask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feed, menu);
        menu.findItem(R.id.action_search).setIcon(
                new IconDrawable(this, IoniconsIcons.ion_ios_search).
                        colorRes(R.color.white).actionBarSize());
        showRightSigningOption(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        showRightSigningOption(menu);
        return true;
    }

    private void showRightSigningOption(Menu menu) {
        MenuItem signInOption = (MenuItem) menu.findItem(R.id.action_signin);
        MenuItem signOutOption = (MenuItem) menu.findItem(R.id.action_signout);

        if (SessionHandler.getInstance(this).isUserLoggedIn()) {
            // user logged in show sign out option
            signInOption.setVisible(false);
            signOutOption.setVisible(true);
        } else {
            // user not logged in show sign in option
            signOutOption.setVisible(false);
            signInOption.setVisible(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Close session handler editor
        SessionHandler.getInstance(this).closeSessionHandler();

        // Close websocket connection when home activity is destroyed
        if (WebSocketRequestHandler.getInstance().getWebsocket() != null) {
            WebSocketRequestHandler.getInstance().getWebsocket().close();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                openQueryActivity();
                return true;
            case R.id.action_signout:
                openLoginActivity();
                return true;
            case R.id.action_signin:
                openLoginActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
