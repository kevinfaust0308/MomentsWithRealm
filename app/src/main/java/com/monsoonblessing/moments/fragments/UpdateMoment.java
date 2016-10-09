package com.monsoonblessing.moments.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.monsoonblessing.moments.DatabaseHelper;

/**
 * Created by Kevin on 2016-06-20.
 */
public class UpdateMoment extends MomentConfig {

    public static UpdateMoment newInstance(int id, String photoUri, String title, Long date) {
        UpdateMoment f = new UpdateMoment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putString("uri", photoUri);
        args.putString("title", title);
        args.putLong("date", date);
        f.setArguments(args);
        return f;
    }


    /*
    1. Check if our Moment has a photo
    2. Update our Moment with information user wants
    3. Check if we updated database successfully
     */
    @Override
    public void onClick(View v) {
        if (mMoment.getPhotoUri() == null) {
            Toast.makeText(getActivity(), "Please choose a photo!", Toast.LENGTH_SHORT).show();
        } else {
            //update Moment object
            updateMomentFields();
            //make sure row was updated successfully
            if (updateRow()) {
                //callback to Main Activity to refresh adapter
                try {
                    OnSubmitListener l = (OnSubmitListener) getActivity();
                    l.OnSubmit();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getActivity(), "Moment updated successfully~", Toast.LENGTH_LONG).show();
                dismiss();
            } else {
                Toast.makeText(getActivity(), "Error trying to update entry :/", Toast.LENGTH_LONG).show();
            }
        }
    }


    public boolean updateRow() {
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        Boolean isUpdated = dbHelper.updateRow(
                mMoment.getId(),
                mMoment.getTitle(),
                String.valueOf(mMoment.getPhotoUri()),
                mMoment.getDate(),
                mMoment.getMonth(),
                mMoment.getYear());
        dbHelper.closeDatabase();
        return isUpdated;
    }
}

