package com.monsoonblessing.moments.Fragments;

import android.app.Dialog;
import android.os.Bundle;

import com.monsoonblessing.moments.RealmDatabaseHelper;

/**
 * Created by Kevin on 2016-06-20.
 */
public class UpdateMoment extends MomentConfig {

    public static UpdateMoment newInstance(int id, String photoUri, String title, Long date) {
        UpdateMoment f = new UpdateMoment();
        Bundle args = new Bundle();
        args.putInt(RealmDatabaseHelper.COLUMN_ID, id);
        args.putString(RealmDatabaseHelper.COLUMN_PHOTO_URI, photoUri);
        args.putString(RealmDatabaseHelper.COLUMN_TITLE, title);
        args.putLong(RealmDatabaseHelper.COLUMN_DATE_LONG, date);
        f.setArguments(args);
        return f;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

/*
        isNewMoment = false;

        Bundle bundle = getArguments();

        // get preset settings
        int id = bundle.getInt(RealmDatabaseHelper.COLUMN_ID);
        String uri = bundle.getString(RealmDatabaseHelper.COLUMN_PHOTO_URI);
        String title = bundle.getString(RealmDatabaseHelper.COLUMN_TITLE);
        Long date = bundle.getLong(RealmDatabaseHelper.COLUMN_DATE_LONG);

        // set preset settings
        momentID = id;
        momentPhotoUri = Uri.parse(uri);
        momentDateLong = date;

        // update title text
        mTitleText.setText(title);

        // update date text
        Date dateObj = new Date(date);
        SimpleDateFormat f = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        mDateText.setText(f.format(dateObj));

        // update preview picture
        addPreviewPicture(momentPhotoUri);
*/

        // creates dialog
        return super.onCreateDialog(savedInstanceState);
    }


}

