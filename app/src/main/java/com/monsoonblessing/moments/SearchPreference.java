package com.monsoonblessing.moments;

import com.monsoonblessing.moments.enums.SortingOptions;

/**
 * Created by Kevin on 2016-06-24.
 */
public class SearchPreference {

    private String mMonth;
    private String mYear;
    private SortingOptions mSortOption;


    public SearchPreference() {
        mMonth = null;
        mYear = null;
        mSortOption = SortingOptions.ORDER_ADDED;
    }


    public SearchPreference(String month, String year, SortingOptions sortOption) {
        mMonth = month;
        mYear = year;
        mSortOption = sortOption;
    }


    public void reset() {
        mMonth = null;
        mYear = null;
        mSortOption = SortingOptions.ORDER_ADDED;
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
