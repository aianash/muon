package com.shoplane.muon.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.shoplane.muon.R;
import com.shoplane.muon.activities.QueryActivity;
import com.shoplane.muon.common.helper.FilterHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ravmon on 16/9/15.
 */
public class FilterPagerFragment extends Fragment {
    private static final String TAG = FilterPagerFragment.class.getSimpleName();
    private static final String SELECTION_STATE = "selection_state";

    private String mTitle;
    private int mPosition;
    private Long mFilterId;
//    private List<String> mFilterItemList;
    private List<String> mFilterItemList;
    //private List<String> mFilterItemListValues;
    private int mSelectedBkgColor;
    private int mUnselectedBkgColor;
    private QueryActivity mActivity;

    public static FilterPagerFragment getInstance(int position, String title,
                                                  Long filterId) {
        FilterPagerFragment filterPagerFragment = new FilterPagerFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("title", title);
        args.putLong("filterid", filterId);
        filterPagerFragment.setArguments(args);
        return filterPagerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments().getInt("position", 0);
        mTitle = getArguments().getString("title");
        mFilterId = getArguments().getLong("filterid");

        try {
            mFilterItemList = new ArrayList<>(FilterHelper.getFilterHelperInstance().
                    getFilterFields(mFilterId, mTitle));
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Fields not valid");
        } catch (IllegalStateException ise) {
            Log.e(TAG, "Filter not available");
        } catch (Exception e) {
            Log.e(TAG, "Error while feting filter");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.query_filter_pager_fragment, container, false);
        //final TextView mFilterTitle = (TextView) view.findViewById(R.id.filter_title);
        //mFilterTitle.setText(mTitle);
        GridView filterList = (GridView) view.findViewById(R.id.filter_list);
        mSelectedBkgColor = getResources().getColor(R.color.orange);
        mUnselectedBkgColor = getResources().getColor(R.color.white_translucent);

        FilterAdapter mFilterListAdapter = new FilterAdapter(this.getActivity(),
                mFilterItemList);
        filterList.setAdapter(mFilterListAdapter);
        filterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!FilterHelper.getFilterHelperInstance().getFilterValueSelectionStatus(
                        mFilterId, mTitle, position)){
                    FilterHelper.getFilterHelperInstance().setFilterValueSelectionStatus(
                            mFilterId, mTitle, position, true);
                    view.setBackgroundColor(mSelectedBkgColor);
                    FilterHelper.getFilterHelperInstance().addFilterToSelectionList(mFilterId,
                            mTitle, mFilterItemList.get(position));
                    // change layout
//                    mActivity.getFilterSelectionList().add(mFilterItemList.get(position));
                    mActivity.getFilterSelectionList().add(
                            mFilterItemList.get(position));
                    //mActivity.getFilterSelectionAdapter().notifyDataSetChanged();
                    // set task to send query to servergetQuerySendScheduler().resetTask();
                    mActivity.resetQuerySchedulerForFilterChange();
                } else {
                    FilterHelper.getFilterHelperInstance().setFilterValueSelectionStatus(
                            mFilterId, mTitle, position, false);
                    view.setBackgroundColor(mUnselectedBkgColor);
                    FilterHelper.getFilterHelperInstance().removeFilterFromSelectionList(mFilterId,
                            mTitle, mFilterItemList.get(position));
                    //change layout
                    mActivity.getFilterSelectionList().remove(mFilterItemList.get(position));
                    //mActivity.getFilterSelectionAdapter().notifyDataSetChanged();
                    mActivity.resetQuerySchedulerForFilterChange();
                }
            }
        });
        return view;
    }

    /*@Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putBooleanArray(SELECTION_STATE, mFilterItemSelectionList);
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Fragment destroyed");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (QueryActivity) activity;
    }

    public void updateFilterData() {

    }

    // Custom filter adapter
    private class FilterAdapter extends BaseAdapter {
        private ViewHolder viewHolder;
        private Context mContext;
        private List<String> mFilterItemAdapterList;
        private LayoutInflater mInflater;

        public FilterAdapter(Context context, List<String> arrayListItems) {
            mFilterItemAdapterList = arrayListItems;
            this.mContext = context;
        }

        // class for caching the views in a row
        private class ViewHolder {
            TextView textView;
        }

        @Override
        public int getCount() {
            return mFilterItemAdapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return mFilterItemAdapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            if (mInflater == null) {
                mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.filter_row, null);
                final TextView textView = (TextView) convertView.findViewById(R.id.filter_field);
                viewHolder = new ViewHolder();
                viewHolder.textView = textView;
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Fill textview
            viewHolder.textView.setText(mFilterItemAdapterList.get(position));

            if(FilterHelper.getFilterHelperInstance().getFilterValueSelectionStatus(
                    mFilterId, mTitle, position)) {
                convertView.setBackgroundColor(mSelectedBkgColor);
            } else {
                convertView.setBackgroundColor(mUnselectedBkgColor);
            }
            return convertView;
        }
    }

}
