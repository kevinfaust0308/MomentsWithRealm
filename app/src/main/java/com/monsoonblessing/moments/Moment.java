package com.monsoonblessing.moments;

import android.net.Uri;

/**
 * Created by Kevin on 2016-06-18.
 */
public class Moment {

    private int mId;
    private String mTitle;
    private Uri mPhotoUri;
    private Long mDate;
    private String mMonth;
    private int mYear;


    public Moment(int id, String title, Uri photoUri, Long date) {
        mId = id;
        mTitle = title;
        mPhotoUri = photoUri;
        mDate = date;
    }


    public Moment() {
        //default: set all values to null
    }


    public int getId() {
        return mId;
    }


    public void setId(int id) {
        mId = id;
    }


    public String getTitle() {
        return mTitle;
    }


    public void setTitle(String title) {
        mTitle = title;
    }


    public Uri getPhotoUri() {
        return mPhotoUri;
    }


    public void setPhotoUri(Uri photoUri) {
        mPhotoUri = photoUri;
    }


    public Long getDate() {
        return mDate;
    }


    public void setDate(Long date) {
        mDate = date;
    }


    public String getMonth() {
        return mMonth;
    }


    public void setMonth(String month) {
        mMonth = month;
    }


    public int getYear() {
        return mYear;
    }


    public void setYear(int year) {
        mYear = year;
    }
}
