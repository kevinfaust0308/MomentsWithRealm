package com.monsoonblessing.moments.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.monsoonblessing.moments.R;
import com.monsoonblessing.moments.RealmDatabaseHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;

/**
 * Created by Kevin on 2016-06-20.
 */
public class UpdateMoment extends MomentConfig {

    private static final String TAG = "UpdateMoment";

    private String originalPhotoFilePath;


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
        Dialog d = super.onCreateDialog(savedInstanceState);
        originalPhotoFilePath = chosenPhotoFilePath;
        return d;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        if (chosenPhotoFilePath != null) {
            // determine if we submitted
            if (submittedEntry) {
                // if chosen photo file path differs from original, delete the original file
                if (!originalPhotoFilePath.equals(chosenPhotoFilePath)) {
                    File origFile = new File(originalPhotoFilePath);
                    if (origFile.delete()) {
                        Log.d(TAG, "Deleted original file to store new file");
                    }
                }
            } else {
                // if we are leaving popup without saving, only delete the chosen image file
                // if it differs from our original photo file path
                if (!originalPhotoFilePath.equals(chosenPhotoFilePath)) {
                    super.onDismiss(dialog);
                }
            }
        }
    }



    @Override
    public void removePreviewPictureAndDeleteImageFile() {
        // if the current preview picture is not the original one, we can delete it
        // we dont delete the original because if user removes it but doesn't submit, we want everything to remain the same as before
        if (!originalPhotoFilePath.equals(chosenPhotoFilePath)) {
            Log.d(TAG, "Preview picture differs from original image. Deleting preview picture file");
            super.removePreviewPictureAndDeleteImageFile();
        } else {
            Log.d(TAG, "Preview picture is the original image. Did not delete");
            mImageViewSwitcher.setDisplayedChild(0);
            chosenPhotoFilePath = null;
        }
    }

}

