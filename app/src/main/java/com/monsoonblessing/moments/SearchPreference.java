package com.monsoonblessing.moments;

import com.monsoonblessing.moments.Enums.SortingOptions;

/**
 * Created by Kevin on 2016-06-24.
 */
public class SearchPreference {

    public static final String DEFAULT_ALL_DATE_ENTRIES = "All";
    public static final SortingOptions DEFAULT_SORT_ORDER = SortingOptions.ORDER_ADDED;

    private String mMonth;
    private String mYear;
    private SortingOptions mSortOption;


    public SearchPreference() {
        setDefaultSearchSettings();
    }


    public SearchPreference(String month, String year, SortingOptions sortOption) {
        mMonth = month;
        mYear = year;
        mSortOption = sortOption;
    }


    private void setDefaultSearchSettings() {
        mMonth = DEFAULT_ALL_DATE_ENTRIES;
        mYear = DEFAULT_ALL_DATE_ENTRIES;
        mSortOption = DEFAULT_SORT_ORDER;
    }


    public String getMonth() {
        return mMonth;
    }


    public void setMonth(String month) {
        mMonth = month;
    }


    public String getYear() {
        return mYear;
    }


    public void setYear(String year) {
        mYear = year;
    }


    public SortingOptions getSortOption() {
        return mSortOption;
    }


    public void setSortOption(SortingOptions sortOption) {
        mSortOption = sortOption;
    }
}
