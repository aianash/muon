package com.shoplane.muon.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.shoplane.muon.common.helper.FilterHelper;
import com.shoplane.muon.common.utils.userinterface.SlidingTabLayout;
import com.shoplane.muon.fragments.FilterPagerFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ravmon on 16/9/15.
 */
public class FilterPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = FilterPagerAdapter.class.getSimpleName();


    private List<String> mFilterFieldTitle = new ArrayList<>();
    private Long mFilterId;
    private SlidingTabLayout mSlidingTabLayout;

    public FilterPagerAdapter(FragmentManager fm, Long filterId, SlidingTabLayout slidingTabLayout) {
        super(fm);
        this.mFilterId = filterId;
        this.mSlidingTabLayout = slidingTabLayout;
    }

    @Override
    public Fragment getItem(int position) {
        Log.e(TAG, "filter title position = " +  mFilterFieldTitle.get(position));
        FilterPagerFragment filterPagerFragment = FilterPagerFragment.getInstance(position,
                mFilterFieldTitle.get(position), mFilterId);
        return filterPagerFragment;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFilterFieldTitle.get(position);
    }

    @Override
    public int getCount() {
        return mFilterFieldTitle.size();
    }

    public void setFilterId(Long filterId) {
        mFilterId = filterId;
    }

    public void updateFilterTitles() {
        Set<String> filterTypesSet = new HashSet<>();
        filterTypesSet.addAll(FilterHelper.getFilterHelperInstance().
                getFiltersKeys(mFilterId));
        mFilterFieldTitle.clear();
        for (String filterType : filterTypesSet) {
            Log.e(TAG, "filter title = " + filterType );
            mFilterFieldTitle.add(filterType);
        }
    }
}
