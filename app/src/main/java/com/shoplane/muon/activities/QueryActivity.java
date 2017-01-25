package com.shoplane.muon.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.IoniconsIcons;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.shoplane.muon.R;
import com.shoplane.muon.adapters.FilterPagerAdapter;
import com.shoplane.muon.adapters.QueryAdapter;
import com.shoplane.muon.common.Constants;
import com.shoplane.muon.common.handler.WebSocketRequestHandler;
import com.shoplane.muon.common.helper.FilterHelper;
import com.shoplane.muon.common.utils.UniqueIdGenerator;
import com.shoplane.muon.common.utils.userinterface.MaterialEditText.MaterialEditText;
import com.shoplane.muon.common.utils.userinterface.SlidingTabLayout;
import com.shoplane.muon.interfaces.UpdateUITask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryActivity extends AppCompatActivity implements UpdateUITask {
    private static final String TAG = QueryActivity.class.getSimpleName();

    private static final String ENTER_QUERY = "Please enter a query to search";
    private static final String LAST_SEARCHED_QUERY = "last_searched_query";

    private String mQuery;
    private static List<String> mSuggestedQueryItems;
    private List<String> mTotalSuggestedItems;
    private List<Boolean> mStyleItemSelectionList;
    private List<String> mStyleFilters;
    private List<String> mStyleSelected;
    //private List<String> mStyleToFilter;
    private Map<Long, List<String>> mFilterIdToStylesMap;
    private List<String> mFilterSelectionItemList;
    private int mNumStylesSelected;
    private int mFilterPosition;
    private boolean mIsStyleDisplayed;
    private String mUniqueQueryId;
    WeakReference<UpdateUITask> mActRef;
    private boolean mIsActivityAvailable;
    private boolean mIsFilterIntent;
    private boolean mShouldUpdateUI;


    private QueryAdapter mQueryAdapter;
    //private StylesToFilterListAdapter mStyleToFilterListAdapter;
    private MaterialEditText mSearchText;
    private ViewGroup mSearchTextWrapper;
    private int mSelectedBkgColor;
    private int mUnselectedBkgColor;
    private FilterPagerAdapter mFilterPagerAdapter;
    private TextView mCurrentStyleView;
    private QuerySendScheduler mQuerySendScheduler;
    //private FilterSelectionListAdapter mFilterSelectionAdapter;
    private FloatingActionButton filterButton;
    private ViewGroup mQueryMainLayout;
    private ViewGroup mStyleNavLayout;
    private TextView mSuggestionTypeText;
    private SlidingTabLayout mFilterSlidingTabLayout;
    private ViewPager mFilterPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        // setupUI(findViewById(R.id.query_main_layout));

        // Set background image for window to avoid resizing when keyboard appears
        getWindow().setBackgroundDrawableResource(R.drawable.splash_search_3);


        mSearchTextWrapper = (ViewGroup) findViewById(R.id.searchbox_search_query_wrapper);
        mSearchText = (MaterialEditText) findViewById(R.id.searchbox_search_query);
        mSearchText.requestFocus();

        // set lines and
        mSearchText.setHorizontallyScrolling(false);
        mSearchText.setMaxLines(5);

        // Bold font for floatinglabel text
        Typeface typeFaceFloatLabel = Typeface.createFromAsset(this.getAssets(),
                "fonts/Roboto-Bold.ttf");
        mSearchText.setAccentTypeface(typeFaceFloatLabel);
        Typeface typeFaceMainText = Typeface.createFromAsset(this.getAssets(),
                "fonts/Roboto-Medium.ttf");
        mSearchText.setTypeface(typeFaceMainText);

        // Set search for soft keyboard search button
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchForQuery();
                    return true;
                }
                return false;
            }
        });

        final Long mFilterId = 0L;

        mFilterPager = (ViewPager) findViewById(R.id.filter_pager);
        mFilterSlidingTabLayout = (SlidingTabLayout) findViewById(
                R.id.filter_tabs);
        mFilterPager.setOffscreenPageLimit(6);
        mFilterPagerAdapter = new FilterPagerAdapter(getSupportFragmentManager(), mFilterId,
                mFilterSlidingTabLayout);
        mFilterPager.setAdapter(mFilterPagerAdapter);


        mFilterSlidingTabLayout.setDistributeEvenly(true);
        mFilterSlidingTabLayout.setViewPager(mFilterPager);

        mCurrentStyleView = (TextView) findViewById(R.id.style_title);

        mSuggestedQueryItems = new ArrayList<>();
        mTotalSuggestedItems = new ArrayList<>();
        mStyleItemSelectionList = new ArrayList<>();
        mStyleSelected = new ArrayList<>();
        mStyleFilters = new ArrayList<>();
        //mStyleToFilter = new ArrayList<>();
        mFilterSelectionItemList = new ArrayList<>();
        mFilterIdToStylesMap = new HashMap<>();
        mNumStylesSelected = 0;
        mFilterPosition = 0;
        mUniqueQueryId = getUniqueQueryIdentifier();
        mIsStyleDisplayed = false;
        mIsFilterIntent = false;
        mShouldUpdateUI = false;

        // use this reference to determine whether activity exist or not
        mIsActivityAvailable = true;
        mActRef = new WeakReference<UpdateUITask>(this);


        ListView mQuerySuggestionView = (ListView) findViewById(R.id.query_suggestion_view);
        /*DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        LinearLayout.LayoutParams querySuggestionViewLayout = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        int dpValue = 12; // margin in dips
        float screenDensity = displaymetrics.density;
        int margin = (int)(dpValue * screenDensity); // margin in pixels
        querySuggestionViewLayout.setMargins(0, margin, 0, 0);
        mQuerySuggestionView.setLayoutParams(querySuggestionViewLayout);*/

        mQueryAdapter = new QueryAdapter(this,
                mSuggestedQueryItems, mStyleItemSelectionList, mIsStyleDisplayed);
        mQuerySuggestionView.setAdapter(mQueryAdapter);

        //ListView styleToFilterListView = (ListView) findViewById(R.id.style_to_filter_list);
        //mStyleToFilterListAdapter = new StylesToFilterListAdapter(this, mStyleToFilter);
        //styleToFilterListView.setAdapter(mStyleToFilterListAdapter);

        mSelectedBkgColor = getResources().getColor(R.color.orange);
        mUnselectedBkgColor = getResources().getColor(R.color.transparent);

        // Setup iconify module
        Iconify.with(new IoniconsModule());
        filterButton = (FloatingActionButton) findViewById(
                R.id.floating_filter_button);
        filterButton.setImageDrawable(new IconDrawable(this,
                IoniconsIcons.ion_funnel).colorRes(R.color.white).actionBarSize());

        mSuggestionTypeText = (TextView) findViewById(R.id.list_content_text);
        mQuerySuggestionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mIsStyleDisplayed) {
                    TextView textView = (TextView) view.findViewById(R.id.suggested_query_value);
                    String suggestedQuery = textView.getText().toString().trim();
                    mSearchText.setText(suggestedQuery);
                    mSearchText.setSelection(suggestedQuery.length());
                    mSearchText.requestFocus();
                    //showKeyboard(mSearchText);
                } else {
                    if (!mStyleItemSelectionList.get(position)) {
                        mStyleItemSelectionList.set(position, true);
                        view.setBackgroundColor(mSelectedBkgColor);
                        mStyleSelected.add(mSuggestedQueryItems.get(position));
                        mNumStylesSelected++;
                        if (1 == mNumStylesSelected) {
                            // update filters only when styles changes
                            filterButton.setVisibility(View.VISIBLE);
                        }
                        updateFilterMapForSelection(mStyleSelected);
                        mQuerySendScheduler.resetTask();
                    } else {
                        mStyleItemSelectionList.set(position, false);
                        view.setBackgroundColor(mUnselectedBkgColor);
                        mStyleSelected.remove(mSuggestedQueryItems.get(position));
                        mNumStylesSelected--;
                        if (0 == mNumStylesSelected) {
                            filterButton.setVisibility(View.GONE);
                        }
                        updateFilterMapForSelection(mStyleSelected);
                        mQuerySendScheduler.resetTask();
                    }
                }
            }
        });

        FloatingActionButton searchButton = (FloatingActionButton) findViewById(
                R.id.floating_search_button);
        searchButton.setImageDrawable(new IconDrawable(this,
                IoniconsIcons.ion_ios_search).colorRes(R.color.white).actionBarSize());

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get text from searchbox
                searchForQuery();
            }
        });

        // Adapter for filter selection list
        //RecyclerView filterSelectionView = (RecyclerView) findViewById(R.id.filter_selection_gridview);
        //LinearLayoutManager filterSelectionViewLayoutManager = new LinearLayoutManager(this);
        //filterSelectionViewLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //filterSelectionView.setLayoutManager(filterSelectionViewLayoutManager);
        //mFilterSelectionAdapter = new FilterSelectionListAdapter(this, mFilterSelectionItemList);
        //filterSelectionView.setAdapter(mFilterSelectionAdapter);

        // View groups
        mQueryMainLayout = (ViewGroup) findViewById(R.id.query_main_layout);
        mStyleNavLayout = (ViewGroup) findViewById(R.id.style_navigator_layout);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterPosition = 0;
                updateFilterLayout();
                hideKeyboard(mSearchText);
                mQueryMainLayout.setVisibility(View.GONE);

//                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//                if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                    // Do something for lollipop and above versions
//                    // get the center for the clipping circle
//                    float cx = filterButton.getX() + filterButton.getWidth() / 2;
//                    float cy = filterButton.getY() + filterButton.getHeight() / 2;
//
//                    // get the final radius for the clipping circle
//                    int finalRadius = Math.max(mStyleNavLayout.getWidth(),
//                            mStyleNavLayout.getHeight());
//
//                    // create the animator for this view (the start radius is zero)
//                    Animator anim =
//                            ViewAnimationUtils.createCircularReveal(mStyleNavLayout,
//                                    (int) cx, (int) cy, 0, finalRadius);
//
//                    // make the view visible and start the animation
//                    mStyleNavLayout.setVisibility(View.VISIBLE);
//                    anim.start();
//                } else {
//                    // do something for phones running an SDK before lollipop
//                    mStyleNavLayout.setVisibility(View.VISIBLE);
//                }
                mStyleNavLayout.setVisibility(View.VISIBLE);


                //Animation filterAnimation = AnimationUtils.loadAnimation(QueryActivity.this,
                //      R.anim.push_in_up);

                //mStyleNavLayout.startAnimation(filterAnimation);
                //mQueryMainLayout.animate().translationY(-mQueryMainLayout.getHeight());
                //mStyleNavLayout.animate().translationY(-mStyleNavLayout.getHeight());
            }
        });

        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // if timer exxpired then then send query otherwise reset timer
                if (0 == s.length()) {
                    getDefaultQuerySuggestion();

                    // adapter is notified if style not displayed
                    mIsStyleDisplayed = false;
                    mQueryAdapter.setStyleDisplayed(false);

                    filterButton.setVisibility(View.GONE);
                    //loadMoreButton.setVisibility(View.GONE);
                    mStyleSelected.clear();
                    mNumStylesSelected = 0;
                    mQuerySendScheduler.stopTask();
                    mShouldUpdateUI = false;
                } else {
                    mShouldUpdateUI = true;
                    mQuerySendScheduler.resetTask();
                }
            }
        });

        // Style navigation
        ImageButton styleNavigateBackButton = (ImageButton) findViewById(R.id.style_back_button);
        styleNavigateBackButton.setImageDrawable(new IconDrawable(this,
                IoniconsIcons.ion_android_arrow_back).colorRes(R.color.primary).actionBarSize());

        ImageButton styleNavigateForwardButton = (ImageButton) findViewById(
                R.id.style_forward_button);
        styleNavigateForwardButton.setImageDrawable(new IconDrawable(this,
                IoniconsIcons.ion_android_arrow_forward).colorRes(R.color.primary).actionBarSize());

        ImageButton filterCloseButton = (ImageButton) findViewById(R.id.style_down_button);
        filterCloseButton.setImageDrawable(new IconDrawable(this,
                IoniconsIcons.ion_android_close).colorRes(R.color.primary).actionBarSize());

        FloatingActionButton filterApplyButton = (FloatingActionButton) findViewById(
                R.id.filter_apply_button);
        filterApplyButton.setImageDrawable(new IconDrawable(this,
                IoniconsIcons.ion_ios_search).colorRes(R.color.white).actionBarSize());


        styleNavigateBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (0 == mFilterPosition) {
                    return;
                }
                mFilterPosition--;
                updateFilterLayout();

            }
        });

        styleNavigateForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mStyleFilters.size() - 1) == mFilterPosition) {
                    return;
                }
                mFilterPosition++;
                // Indicate fragment to change data
                updateFilterLayout();
            }
        });

        filterCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mStyleNavLayout.animate().translationY(0);
                mStyleNavLayout.setVisibility(View.GONE);
                mQueryMainLayout.setVisibility(View.VISIBLE);
                //mQueryMainLayout.animate().translationY(0);
                mSearchText.requestFocus();
                if (!mIsFilterIntent) {
                    showKeyboard();
                }
            }
        });

        filterApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForQuery();
            }
        });

        getDefaultQuerySuggestion();
        mQuerySendScheduler = new QuerySendScheduler(500);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyboard(mSearchText);
    }

    @Override
    public void onStop() {
        super.onStop();
        mQuerySendScheduler.stopTask();
    }

    @Override
    public void onDestroy() {
        mIsActivityAvailable = false;
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //store the new intent unless getIntent() will return the old one
        setIntent(intent);
        Boolean isSearchIntent = intent.getExtras().getBoolean(Constants.OPEN_QUERY_BOX);
        Boolean isFilterIntent = intent.getExtras().getBoolean(Constants.OPEN_QUERY_FILTER);

        if (!isSearchIntent && !isFilterIntent) {
            return;
        }

        if (isSearchIntent) {
            mStyleNavLayout.setVisibility(View.GONE);
            mQueryMainLayout.setVisibility(View.VISIBLE);
            mSearchTextWrapper.setVisibility(View.VISIBLE);
            mSearchText.requestFocus();
            showKeyboard();
        }

        if (isFilterIntent) {
            mIsFilterIntent = true;
            mSearchTextWrapper.setVisibility(View.GONE);
            if (0 == mNumStylesSelected) {
                mStyleNavLayout.setVisibility(View.GONE);
                mQueryMainLayout.setVisibility(View.VISIBLE);
            } else {
                mFilterPosition = 0;
                updateFilterLayout();
                mQueryMainLayout.setVisibility(View.GONE);
                mStyleNavLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean getActivityAvailableStatus() {
        return mIsActivityAvailable;
    }

    private void getDefaultQuerySuggestion() {
        mSuggestedQueryItems.clear();
        mStyleItemSelectionList.clear();
        mSuggestedQueryItems.add("Something comfortable to wear on a sunny day in goa");
        mStyleItemSelectionList.add(false);
        mSuggestedQueryItems.add("A dress to wear at nightclub party");
        mStyleItemSelectionList.add(false);
        mSuggestedQueryItems.add("Something trending to wear this Diwali");
        mStyleItemSelectionList.add(false);
        mSuggestedQueryItems.add("A party wear for cold weather");
        mStyleItemSelectionList.add(false);
        mSuggestedQueryItems.add("A birthday present for my wife who is turning 30");
        mStyleItemSelectionList.add(false);

        mQueryAdapter.notifyDataSetChanged();

        mSuggestionTypeText.setVisibility(View.GONE);
    }

    private void getStyleSuggestionList() {

        mSuggestedQueryItems.clear();
        mStyleItemSelectionList.clear();

        // flag should be set before notifying adapter
        mIsStyleDisplayed = true;
        mQueryAdapter.setStyleDisplayed(true);

        mSuggestedQueryItems.addAll(mTotalSuggestedItems);
        mStyleItemSelectionList.add(false);

        mQueryAdapter.notifyDataSetChanged();
        mSuggestionTypeText.setText(R.string.styles_suggested_for_you_text);
        mSuggestionTypeText.setVisibility(View.VISIBLE);
    }

    private void resetStyleSelection() {
        mStyleItemSelectionList.clear();
        mTotalSuggestedItems.clear();
        mStyleSelected.clear();
        mStyleFilters.clear();
        //mStyleToFilter.clear();
        mFilterSelectionItemList.clear();
        mFilterIdToStylesMap.clear();
        mNumStylesSelected = 0;
        mIsStyleDisplayed = false;
        mQueryAdapter.setStyleDisplayed(false);
        mFilterPosition = 0;

        filterButton.setVisibility(View.GONE);
    }

    private void updateFilterMapForSelection(final List<String> stylesSelected) {
        // get filter ids
        if (null == stylesSelected || 0 == stylesSelected.size()) {
            return;
        }
        mFilterIdToStylesMap.clear();
        mStyleFilters.clear();
        try {
            mFilterIdToStylesMap = FilterHelper.getFilterHelperInstance().getFiltersForStyles(
                    stylesSelected);
        } catch (Exception e) {
            Log.e(TAG, "filters not found for styles");
        }

        // set filter titles
        for (Long filterId : mFilterIdToStylesMap.keySet()) {
            String filterTitle = "";
            try {
                filterTitle = FilterHelper.getFilterHelperInstance().
                        getFilterTitleForFilterId(filterId);
            } catch (Exception e) {
                Log.e(TAG, "could not get filter title");
            }
            mStyleFilters.add(filterTitle);
        }
    }

    public Long getCurrentFilterId() {
        Long filterId = 0L;
        try {
            filterId = FilterHelper.getFilterHelperInstance().getFilterIdForFilterTitle(
                    mStyleFilters.get(mFilterPosition));
        } catch (Exception e) {
            Log.e(TAG, "could not get filterid");
        }
        return filterId;
    }

    private void updateFilterLayout() {
        String currentFilter = mStyleFilters.get(mFilterPosition).toLowerCase().trim();
        Long filterId = getCurrentFilterId();
        // get styles for filterid
        List<String> styleList = mFilterIdToStylesMap.get(filterId);
        // Show styles for filter
        StringBuilder filterAndStyles = new StringBuilder();

        //mStyleToFilter.clear();
        for (String style : styleList) {
            //mStyleToFilter.add(style);
            filterAndStyles.append(changeToUpperCase(style));
            filterAndStyles.append(" ");
        }
        //mStyleToFilterListAdapter.notifyDataSetChanged();
        // Indicate fragment to change data
        mFilterPagerAdapter.setFilterId(filterId);
        mFilterPagerAdapter.updateFilterTitles();
        mFilterSlidingTabLayout.setViewPager(mFilterPager);
        mFilterPagerAdapter.notifyDataSetChanged();

//        mCurrentStyleView.setText(Html.fromHtml("<big>" + currentFilter.toUpperCase() + "</big>" + "<br />" +
//                "<small>" + filterAndStyles + "</small>"));
        String stylesForFilter = filterAndStyles.toString();
        int len = stylesForFilter.length();
        SpannableString spanStr = new SpannableString(stylesForFilter);
        spanStr.setSpan(new RelativeSizeSpan(0.75f), 0, len, 0);
        spanStr.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.primary_light)),
                0, len, 0);
        mCurrentStyleView.setText(TextUtils.concat(currentFilter.toUpperCase(), "\n", spanStr));

        Log.i(TAG, "style text updated");
        // Update filters
        mFilterSelectionItemList.clear();
        Set<String> selectedFilterTypes = FilterHelper.getFilterHelperInstance().
                getSelectedFiltersKeys(filterId);
        for (String key : selectedFilterTypes) {
            mFilterSelectionItemList.addAll(FilterHelper.getFilterHelperInstance().
                    getFilterSelectionList(filterId, key));
        }
        //mFilterSelectionAdapter.notifyDataSetChanged();
    }


    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                this.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof MaterialEditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(v);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    private void searchForQuery() {
        String mQuery = mSearchText.getText().toString().trim();
        if (null == mQuery || mQuery.equalsIgnoreCase(Constants.EMPTY_STRING)) {
            Toast.makeText(QueryActivity.this, ENTER_QUERY, Toast.LENGTH_SHORT).show();
        } else {
            // Send query to search activity
            Intent searchIntent = new Intent(QueryActivity.this, SearchActivity.class);
            searchIntent.putExtra(Constants.SEARCH_ID, mUniqueQueryId);
            // get new id
            mUniqueQueryId = getUniqueQueryIdentifier();
            mIsFilterIntent = false;
            // hide soft keyboard
            //hideKeyboard(mSearchText);
            startActivity(searchIntent);
        }
    }

    private void sendQueryToServer() {
        // get filters
        // get query text
        // create json
        // send it to server

        JSONObject updatequery = new JSONObject();
        try {
            updatequery.put("reqid", Integer.MAX_VALUE + "");
            updatequery.put("type", "post");
            updatequery.put("uri", "/query/update");

            JSONObject params = new JSONObject();
            params.put("sruid", mUniqueQueryId);

            JSONObject query = new JSONObject();
            query.put("queryStr", mSearchText.getText());

            JSONObject styles = new JSONObject();
            for (String style : mStyleSelected) {
                styles.put(style, FilterHelper.getFilterHelperInstance().
                        getFilterIdForStyle(style) + "");
            }
            query.put("styles", styles);

            JSONObject filters = new JSONObject();
            for (String styleFilter : mStyleFilters) {
                JSONObject filterIdObj = new JSONObject();

                Long filterId = FilterHelper.getFilterHelperInstance().
                        getFilterIdForFilterTitle(styleFilter);

                filterIdObj.put("filterTitle", FilterHelper.getFilterHelperInstance().
                        getFilterTitleForFilterId(filterId));

                Set<String> filterTypesSet = new HashSet<>();
                filterTypesSet.addAll(FilterHelper.getFilterHelperInstance().
                        getSelectedFiltersKeys(filterId));

                List<String> values = new ArrayList<>();
                for (String filterType : filterTypesSet) {
                    values.clear();
                    JSONArray filterTypeJsonArray = new JSONArray();
                    values.addAll(FilterHelper.getFilterHelperInstance().
                            getFilterSelectionList(filterId, filterType));
                    for (String value : values) {
                        filterTypeJsonArray.put(value);
                    }
                    filterIdObj.put(filterType, filterTypeJsonArray);
                }
                filters.put(filterId.toString(), filterIdObj);
            }

            query.put("filters", filters);
            params.put("query", query);
            updatequery.put("params", params);

        } catch (JSONException je) {
            Log.e(TAG, "Failed to create request");
        }
        Log.e(TAG, "Query is " + updatequery.toString());
        WebSocketRequestHandler.getInstance().createAndSendPostRequestToServer(
                updatequery.toString(), mActRef, Constants.QUERY_SUGGESTION_STYLES_PATH);
        Log.e(TAG, "Query sent");
    }

//    public FilterSelectionListAdapter getFilterSelectionAdapter() {
//        return mFilterSelectionAdapter;
//    }

    public void resetQuerySchedulerForFilterChange() {
        mQuerySendScheduler.resetTask();
    }

    public List<String> getFilterSelectionList() {
        return mFilterSelectionItemList;
    }


    @Override
    public void updateUI(JSONObject jsonResponse) {

        if (!mShouldUpdateUI || mQueryMainLayout.getVisibility() == View.GONE) {
            // do not process
            return;
        }

        // process query suggestions
        final Map<String, Long> styleToFilterId;
        final Map<Long, Map<String, List<String>>> filterIdToFilterMap;
        final Map<Long, String> filterIdToFilterTitleMap;
        final Map<String, Long> filterTitleToFilterIdMap;
        final List<String> styleList;

        Log.e(TAG, jsonResponse.toString());
        try {
            String searchId = jsonResponse.getString("sruid");
            // styles and filterid
            JSONObject styles = jsonResponse.getJSONObject("styles");
            styleToFilterId = new HashMap<>();
            styleList = new ArrayList<>();
            Iterator<String> styleIt = styles.keys();
            while (styleIt.hasNext()) {
                String key = styleIt.next();
                styleList.add(key.trim().toLowerCase());
                styleToFilterId.put(key.trim().toLowerCase(), styles.getLong(key));
            }

            // filter id and filters
            JSONObject filters = jsonResponse.getJSONObject("filters");
            filterIdToFilterMap = new HashMap<>();
            filterIdToFilterTitleMap = new HashMap<>();
            filterTitleToFilterIdMap = new HashMap<>();

            Iterator<String> filterIt = filters.keys();
            while (filterIt.hasNext()) {
                String key = filterIt.next();
                Long keyInLong = Long.parseLong(key);
                filterIdToFilterMap.put(keyInLong, new HashMap<String, List<String>>());

                JSONObject singleFilter = filters.getJSONObject(key);

                String filterTiltle = singleFilter.getString("filterTitle");
                filterIdToFilterTitleMap.put(keyInLong, filterTiltle.trim().toLowerCase());
                filterTitleToFilterIdMap.put(filterTiltle.trim().toLowerCase(), keyInLong);

                Iterator<String> filterTypesIt = singleFilter.keys();
                // skip title
                filterTypesIt.next();

                while (filterTypesIt.hasNext()) {
                    String filterType = filterTypesIt.next();
                    JSONArray filterValuesJson = singleFilter.getJSONArray(filterType);

                    List<String> filterValues = new ArrayList<>();
                    int len = filterValuesJson.length();
                    for (int i = 0; i < len; i++) {
                        filterValues.add(filterValuesJson.get(i).toString().trim().toLowerCase());
                    }

                    filterIdToFilterMap.get(keyInLong).put(filterType.trim().toLowerCase(),
                            new ArrayList<>(filterValues));
                }
            }

        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse query suggestions");
            // do not update anything if there is parsing error
            return;
        }

        resetStyleSelection();
        mTotalSuggestedItems.addAll(styleList);
        FilterHelper.getFilterHelperInstance().updateFilters(styleToFilterId,
                filterIdToFilterMap, filterIdToFilterTitleMap, filterTitleToFilterIdMap);
        getStyleSuggestionList();
    }

    private String changeToUpperCase(String inputString) {
        if (inputString != null && inputString.trim().length() > 0) {
            String[] splitString = inputString.split("\\s+");
            int length = splitString.length;
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < length; i++) {
                String convertedString = splitString[i];
                stringBuffer.append(Character.toUpperCase(convertedString
                        .charAt(0)));
                stringBuffer.append(convertedString.substring(1).toLowerCase());
                stringBuffer.append(" ");
            }
            return stringBuffer.toString();
        }
        // some issue with string
        return "";
    }

    // Schedular for query sent to server
    private class QuerySendScheduler {

        private int mInterval;
        private Handler mHandler;

        public QuerySendScheduler(int interval) {
            this.mHandler = new Handler();
            this.mInterval = interval;
        }

        Runnable querySenderRunnable = new Runnable() {
            @Override
            public void run() {
                sendQueryToServer();
            }
        };

        void startTask() {
            if (mHandler != null) {
                if (querySenderRunnable != null) {
                    mHandler.postDelayed(querySenderRunnable, mInterval);
                }
            }
        }

        void stopTask() {
            if (mHandler != null) {
                if (querySenderRunnable != null) {
                    mHandler.removeCallbacks(querySenderRunnable);
                }
            }
        }

        public void resetTask() {
            if (mHandler != null) {
                if (querySenderRunnable != null) {
                    mHandler.removeCallbacks(querySenderRunnable);
                    mHandler.postDelayed(querySenderRunnable, mInterval);
                }
            }
        }

    }

    // Query sending to server
    private String getUniqueQueryIdentifier() {
        return UniqueIdGenerator.getInstance().getUniqueId();
    }
}



