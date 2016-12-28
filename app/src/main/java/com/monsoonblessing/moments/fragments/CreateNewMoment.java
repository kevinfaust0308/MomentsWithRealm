package com.monsoonblessing.moments.Fragments;

import android.app.Dialog;
import android.os.Bundle;

/**
 * Created by Kevin on 2016-06-15.
 */
public class CreateNewMoment extends MomentConfig {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

/*
        isNewMoment = true;

        // set preset settings
        momentPhotoUri = null;
        CurrentDate cd = new CurrentDate();
        // make date long contain current date
        momentDateLong = cd.getCurrentLongDate();

        // update date text
        setDateTextView(cd.getYear(), cd.getMonth(), cd.getDay());
*/

        // creates dialog
        return super.onCreateDialog(savedInstanceState);
    }


}
