package com.shoplane.muon.common.helper;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ravmon on 2/9/15.
 */
public class FilterHelper {
    private static final String TAG = FilterHelper.class.getSimpleName();

    private static FilterHelper mFilterHelperInstance;
    private Map<String, Long> mStyleToFilterIdMap;
    private Map<Long, Map<String, List<String>>> mFilterIdToFiltersMap;
    private Map<Long, Map<String, List<Boolean>>> mFiltertSelectionMap;
    private Map<Long, Map<String, List<String>>> mFilterSelecttionListMap;
    private Map<Long, String> mFilterIdToTitleMap;
    private Map<String, Long> mFilterTitleToIdMap;

    public static FilterHelper getFilterHelperInstance() {
        if (null == mFilterHelperInstance) {
            mFilterHelperInstance = new FilterHelper();
        }
        return mFilterHelperInstance;
    }

    private FilterHelper() {

        mStyleToFilterIdMap = new HashMap<>();
        mFilterIdToFiltersMap = new HashMap<>();
        mFiltertSelectionMap = new HashMap<>();
        mFilterSelecttionListMap = new HashMap<>();
        mFilterIdToTitleMap = new HashMap<>();
        mFilterTitleToIdMap = new HashMap<>();
    }

    public List<String> getFilterFields(Long filterId, String filterType) {

        return mFilterIdToFiltersMap.get(filterId).get(filterType);
    }

    public void updateFilters(Map<String, Long> styleToFilterId,
                              Map<Long, Map<String, List<String>>> filterIdToFilterMap,
                              Map<Long, String> filterIdToTitleMap,
                              Map<String, Long> filterTitleToIdMap) {
        mStyleToFilterIdMap.clear();
        mFilterIdToFiltersMap.clear();
        mFilterIdToTitleMap.clear();
        mFiltertSelectionMap.clear();
        mFilterSelecttionListMap.clear();

        mStyleToFilterIdMap.putAll(styleToFilterId);
        mFilterIdToFiltersMap.putAll(filterIdToFilterMap);
        mFilterIdToTitleMap.putAll(filterIdToTitleMap);
        mFilterTitleToIdMap.putAll(filterTitleToIdMap);

        for (long filterIdKey : filterIdToFilterMap.keySet() ) {
            mFiltertSelectionMap.put(filterIdKey, new HashMap<String, List<Boolean>>());
            mFilterSelecttionListMap.put(filterIdKey, new HashMap<String, List<String>>());
            for (String filterTypeKey : mFilterIdToFiltersMap.get(filterIdKey).keySet()) {
                // add type to selection map
                mFilterSelecttionListMap.get(filterIdKey).put(filterTypeKey,
                        new ArrayList<String>());
                // addboolean values
                int lenOfTypeValues = mFilterIdToFiltersMap.get(filterIdKey).
                        get(filterTypeKey).size();

                List<Boolean> boolValues = new ArrayList();
                for (int i = 0; i < lenOfTypeValues; i++) {
                    boolValues.add(false);
                }

                mFiltertSelectionMap.get(filterIdKey).put(filterTypeKey,
                        new ArrayList<>(boolValues));
            }
        }

    }

    public Map<Long, List<String>> getFiltersForStyles(List<String> styles) {
        Map<Long, List<String>> filterToStylesMap = new HashMap<>();
        for (String style : styles) {
            style = style.toLowerCase().trim();
            long filterIdForStyle = mStyleToFilterIdMap.get(style);
            if (filterToStylesMap.containsKey(filterIdForStyle)) {
                filterToStylesMap.get(filterIdForStyle).add(style);
            } else {
                List<String> styleList = new ArrayList<>();
                styleList.add(style);
                filterToStylesMap.put(filterIdForStyle, new ArrayList<>(styleList));
            }
        }
        return filterToStylesMap;
    }

    public String getFilterTitleForFilterId(Long filterId) {
        return mFilterIdToTitleMap.get(filterId);
    }

    public Long getFilterIdForFilterTitle(String filterTitle) {
        return mFilterTitleToIdMap.get(filterTitle.trim().toLowerCase());
    }

    public Boolean getFilterValueSelectionStatus(Long filterId, String filterType,
                                                 int filterValuePosition) {
        return mFiltertSelectionMap.get(filterId).get(filterType.trim().toLowerCase()).
                get(filterValuePosition);
    }

    public void setFilterValueSelectionStatus(Long filterId, String filterType,
                                                 int filterValuePosition, boolean value) {
        mFiltertSelectionMap.get(filterId).get(filterType.trim().toLowerCase()).
                set(filterValuePosition, value);
    }

    public void addFilterToSelectionList(Long filterId, String filterGroup, String filterValue){
        mFilterSelecttionListMap.get(filterId).get(filterGroup.trim().toLowerCase()).
                add(filterValue.trim().toLowerCase());
        Log.i(TAG, mFilterSelecttionListMap.toString());
    }

    public void removeFilterFromSelectionList(Long filterId, String filterGroup,
                                              String filterValue) {
        mFilterSelecttionListMap.get(filterId).get(filterGroup.trim().toLowerCase()).
                remove(filterValue.trim().toLowerCase());
    }

    public List<String> getFilterSelectionList(Long filterId, String filterGroup) {
        return mFilterSelecttionListMap.get(filterId).get(filterGroup.trim().toLowerCase());
    }

    public long getFilterIdForStyle(String style) {
        return mStyleToFilterIdMap.get(style.trim().toLowerCase());
    }

    public Set<String> getSelectedFiltersKeys(Long filterId) {
        return mFilterSelecttionListMap.get(filterId).keySet();
    }

    public Set<String> getFiltersKeys(Long filterId) {
        return mFilterIdToFiltersMap.get(filterId).keySet();
    }
}
