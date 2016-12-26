package com.monsoonblessing.moments;

import android.net.Uri;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Kevin on 2016-06-18.
 */
public class MomentModel extends RealmObject {

    @PrimaryKey
    private int id;

    private String title;
    private String photoUri;
    private Long dateLong;
    private String month;
    private int year;


    public void setAllFields(int id, String title, String photoUri, Long dateLong, String month, int year) {
        this.id = id;
        setAllFields(title, photoUri, dateLong, month, year);
    }


    public void setAllFields(String title, String photoUri, Long dateLong, String month, int year) {
        this.title = title;
        this.photoUri = photoUri;
        this.dateLong = dateLong;
        this.month = month;
        this.year = year;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getPhotoUri() {
        return photoUri;
    }


    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }


    public Long getDateLong() {
        return dateLong;
    }


    public void setDateLong(Long dateLong) {
        this.dateLong = dateLong;
    }


    public String getMonth() {
        return month;
    }


    public void setMonth(String month) {
        this.month = month;
    }


    public int getYear() {
        return year;
    }


    public void setYear(int year) {
        this.year = year;
    }
}
