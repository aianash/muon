package com.shoplane.muon.activities;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.shoplane.muon.R;
import com.shoplane.muon.adapters.ItemDetailImagePagerAdapter;
import com.shoplane.muon.common.Constants;
import com.shoplane.muon.common.utils.userinterface.CircleButton;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ItemDetailActivity extends AppCompatActivity {
    private static  final  String TAG = ItemDetailActivity.class.getSimpleName();

    private long mItemId;
    private String mItemLink;
    private ProgressDialog mProgressDialog;
    private List<String> mImageUrls;
    private ItemDetailImagePagerAdapter mItemImagePagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        Iconify.with(new IoniconsModule());

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_home);
            actionBar.setHomeAsUpIndicator(new IconDrawable(this,
                    IoniconsIcons.ion_ios_home).colorRes(R.color.white).actionBarSize());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        ViewPager itemImagePager = (ViewPager) findViewById(R.id.item_image_pager);
        itemImagePager.setOffscreenPageLimit(5);

        // Set proper size for search list columns
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenHeight = displaymetrics.heightPixels;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (screenHeight * 0.65));
        itemImagePager.setLayoutParams(layoutParams);

        mImageUrls = new ArrayList<>();
        mItemImagePagerAdapter = new ItemDetailImagePagerAdapter(
                getSupportFragmentManager(), mImageUrls);
        itemImagePager.setAdapter(mItemImagePagerAdapter);

        CirclePageIndicator circlePageIndicator = (CirclePageIndicator)findViewById(
                R.id.item_image_pager_indicator);
        circlePageIndicator.setViewPager(itemImagePager);


        final CircleButton openItemInBrowser = (CircleButton) findViewById(
                R.id.open_browser_button);
        openItemInBrowser.setImageDrawable(new IconDrawable(this,
                IoniconsIcons.ion_link).colorRes(R.color.materialblue_light).
                actionBarSize());

        openItemInBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openItemInBrowser();
            }
        });

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);

        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_detail, menu);

        menu.findItem(R.id.action_back).setIcon(
                new IconDrawable(this, IoniconsIcons.ion_android_close).
                        colorRes(R.color.white).actionBarSize());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_back:
                onBackPressed();
                return true;
            case android.R.id.home:
                openFeedActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    private void openItemInBrowser() {
        String itemPage = mItemLink;
        if (!itemPage.startsWith("http://") && !itemPage.startsWith("https://"))
            itemPage = "https://" + itemPage;

        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(itemPage));
            startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ItemDetailActivity.this, "Please install a webbrowser",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void handleIntent(Intent intent) {

        TextView itemTitleBrand = (TextView) findViewById(R.id.item_title_brand);
        TextView productTitle = (TextView) findViewById(R.id.product_title_group_price_gender);
        TextView productSizes = (TextView) findViewById(R.id.product_sizes);
        TextView productColors = (TextView) findViewById(R.id.product_colors);
        TextView productDescription = (TextView) findViewById(R.id.product_description);
        TextView productStyletips = (TextView) findViewById(R.id.product_styletips);
        TextView productSizesHead = (TextView) findViewById(R.id.product_sizes_head);
        TextView productColorsHead = (TextView) findViewById(R.id.product_colors_head);
        TextView productDescriptionHead = (TextView) findViewById(R.id.product_description_head);
        TextView productStyletipsHead = (TextView) findViewById(R.id.product_styletips_head);


        String itemData = intent.getStringExtra(Constants.ITEM_DATA);
        Log.i(TAG, itemData);


        try {
            StringBuilder text = new StringBuilder();
            int len = 0;

            JSONObject item = new JSONObject(itemData);
            Log.i(TAG, item.toString());

            // Style and brand
            JSONArray jsonArr = item.getJSONArray("styles");
            len = jsonArr.length();
            for (int i = 0; i < len - 1; i++) {
                text.append(jsonArr.getString(i));
                text.append("-");
            }
            text.append(jsonArr.getString(len - 1));
            text.append("\n");
            text.append(item.getString("brand"));
            itemTitleBrand.setText(text.toString());

            // images
            JSONObject imageUrl = item.getJSONObject("images");
            mImageUrls.add(imageUrl.getString("primary"));

            jsonArr = imageUrl.getJSONArray("alt");
            len = jsonArr.length();
            for (int i = 0; i < len; i++) {
                mImageUrls.add(jsonArr.getString(i));
            }

            // group, title, price and gender
            text.setLength(0);
            if(item.has("groups")) {
                jsonArr = item.getJSONArray("groups");
                len = jsonArr.length();
                for (int i = 0; i < len - 1; i++) {
                    text.append(jsonArr.getString(i));
                    text.append("->");
                }
                text.append(jsonArr.getString(len - 1));
                text.append("\n");
            }

            if(item.has("title")) {
                text.append("Title - ");
                text.append(item.getString("title"));
                text.append("\n");
            }

            if(item.has("price")) {
                text.append("Price - ");
                text.append(item.getString("price"));
                text.append("\n");
            }

            if(item.has("gender")) {
                text.append("Gender - ");
                text.append(item.getString("gender"));
            }
            text.append("\n");
            productTitle.setText(text.toString());



            // colors
            text.setLength(0);
            if(item.has("colors")) {
                jsonArr = item.getJSONArray("colors");
                len = jsonArr.length();
                for (int i = 0; i < len - 1; i++) {
                    text.append(jsonArr.getString(i));
                    text.append("-");
                }
                text.append(jsonArr.getString(len - 1));
                text.append("\n");
                productColors.setText(text.toString());
            } else {
                productColors.setVisibility(View.GONE);
                productColorsHead.setVisibility(View.GONE);
            }

            // sizes
            text.setLength(0);
            if(item.has("sizes")) {
                jsonArr = item.getJSONArray("sizes");
                len = jsonArr.length();
                for (int i = 0; i < len - 1; i++) {
                    text.append(jsonArr.getString(i));
                    text.append("-");
                }
                text.append(jsonArr.getString(len - 1));
                text.append("\n");
                productSizes.setText(text.toString());
            } else {
                productSizes.setVisibility(View.GONE);
                productSizesHead.setVisibility(View.GONE);
            }

            // description
            text.setLength(0);
            if(item.has("descr")) {
                text.append(item.getString("descr").trim());
                text.append("\n");
                productDescription.setText(text.toString());
            } else {
                productDescription.setVisibility(View.GONE);
                productDescriptionHead.setVisibility(View.GONE);
            }

            // styling tips
            text.setLength(0);
            if(item.has("stylingTips")) {
                text.append(item.getString("stylingTips").trim());
                text.append("\n");
                productStyletips.setText(text.toString());
            } else {
                productStyletips.setVisibility(View.GONE);
                productStyletipsHead.setVisibility(View.GONE);
            }

            mItemId = item.getLong("cuid");
            mItemLink = item.getString("itemUrl");

        } catch (JSONException je) {
            Log.e(TAG, "Failed to load item details");
        }

        mItemImagePagerAdapter.notifyDataSetChanged();
        if(mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    private void openFeedActivity() {
        Intent feedActivityIntent = new Intent(this, FeedActivity.class);
        startActivity(feedActivityIntent);
    }
}
