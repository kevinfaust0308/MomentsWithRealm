package com.monsoonblessing.moments;

import java.util.Calendar;

/**
 * Created by Kevin on 2016-06-17.
 */
public class CurrentDate {

    private Calendar mCalendar;
    private int mMonth;
    private int mDay;
    private int mYear;


    public CurrentDate() {
        mCalendar = Calendar.getInstance();
        mMonth = mCalendar.get(Calendar.MONTH);
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mYear = mCalendar.get(Calendar.YEAR);
    }

    public long getCurrentLongDate() {
        return mCalendar.getTime().getTime();
    }


    public int getMonth() {
        return mMonth;
    }


    public int getDay() {
        return mDay;
    }


    public int getYear() {
        return mYear;
    }

}
