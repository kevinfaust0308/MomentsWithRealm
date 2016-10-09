package com.monsoonblessing.moments;

/**
 * Created by Kevin on 2016-06-25.
 */
public abstract class CalendarUtil {

    public static String[] months = {
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
    };

    public static String monthNumberToText(int monthNum) {
        return months[monthNum];
    }

}
