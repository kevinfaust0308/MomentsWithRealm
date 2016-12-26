package com.monsoonblessing.moments.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.monsoonblessing.moments.CurrentDate;
import com.monsoonblessing.moments.RealmDatabaseHelper;
import com.monsoonblessing.moments.R;

/**
 * Created by Kevin on 2016-06-28.
 */
public class SettingsDialog extends DialogFragment {

    public interface OnDeleteListener {
        void OnDelete();
    }

    private static final String TAG = SettingsDialog.class.getSimpleName();


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.settings_popup, null);
        final CheckBox delete_checkbox = (CheckBox) v.findViewById(R.id.delete_all_entry_checkbox);
        final CheckBox display_checkbox = (CheckBox) v.findViewById(R.id.default_display_checkbox);

        SharedPreferences sp = getActivity().getSharedPreferences("com.monsoonblessing.moments", Context.MODE_PRIVATE);
        final SharedPreferences.Editor spEdit = sp.edit();

        if (!sp.getBoolean("DefaultDisplay", true)) {
            display_checkbox.setChecked(false);
        }

        b.setView(v).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (delete_checkbox.isChecked()) {
                    new DeleteAllEntries().execute();
                } else {
                    dismiss();
                }
            }
        }).setNegativeButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (display_checkbox.isChecked()) {
                    spEdit.putBoolean("DefaultDisplay", true);
                } else {
                    spEdit.putBoolean("DefaultDisplay", false);
                }
                spEdit.apply();
                //Toast.makeText(getActivity(), "App refreshed", Toast.LENGTH_LONG).show();
                Intent i = getActivity().getPackageManager()
                        .getLaunchIntentForPackage(getActivity().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

            }
        }).setNeutralButton("Dismiss", null);
        return b.create();
    }


    private class DeleteAllEntries extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pd;
        private long preTime;
        private long postTime;


        @Override
        protected Void doInBackground(Void... params) {
            RealmDatabaseHelper dbHelper = new RealmDatabaseHelper(getActivity());
            dbHelper.deleteAllEntries();
            dbHelper.closeDatabase();
            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getActivity());
            pd.setMessage("Please wait");
            pd.show();
            preTime = new CurrentDate().getCurrentLongDate();
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            postTime = new CurrentDate().getCurrentLongDate();
            pd.dismiss();
            Log.d(TAG, "It took " + (postTime - preTime) / 1000.0 + " seconds to delete all data from database");
            Toast.makeText(getActivity(), "All entries have been deleted~", Toast.LENGTH_SHORT).show();
            ((OnDeleteListener) getActivity()).OnDelete(); //update UI in main
        }
    }
}
