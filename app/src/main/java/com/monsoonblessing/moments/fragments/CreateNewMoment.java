package com.monsoonblessing.moments.fragments;

import android.view.View;
import android.widget.Toast;

import com.monsoonblessing.moments.DatabaseHelper;

/**
 * Created by Kevin on 2016-06-15.
 */
public class CreateNewMoment extends MomentConfig {


    /*
    1. Check if our Moment has a photo
    2. Update our Moment with information user wants
    3. Check if we inserted row into database successfully
     */
    @Override
    public void onClick(View v) {
        if (mMoment.getPhotoUri() == null) {
            Toast.makeText(getActivity(), "Please choose a photo!", Toast.LENGTH_SHORT).show();
        } else {
            //update Moment object
            updateMomentFields();
            //make sure row was inserted successfully
            if (insertRow()) {
                //callback to Main Activity to refresh adapter
                try {
                    OnSubmitListener l = (OnSubmitListener) getActivity();
                    l.OnSubmit();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getActivity(), "Moment created successfully~", Toast.LENGTH_LONG).show();
                dismiss();
            } else {
                Toast.makeText(getActivity(), "Error trying to save entry :/", Toast.LENGTH_LONG).show();
            }
        }
    }


    public boolean insertRow() {
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        boolean isInserted = dbHelper.insertData(
                mMoment.getTitle(),
                String.valueOf(mMoment.getPhotoUri()),
                mMoment.getDate(),
                mMoment.getMonth(),
                mMoment.getYear());
        dbHelper.closeDatabase();
        return isInserted;
    }


}
