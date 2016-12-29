package com.monsoonblessing.moments.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.view.View;
import android.widget.CheckBox;

import com.monsoonblessing.moments.Activities.MainActivity;
import com.monsoonblessing.moments.RealmDatabaseHelper;
import com.monsoonblessing.moments.R;

/**
 * Created by Kevin on 2016-06-28.
 */
public class SettingsDialog extends DialogFragment {

    private static final String TAG = SettingsDialog.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.settings_popup, null);
        final CheckBox display_checkbox = (CheckBox) v.findViewById(R.id.display_mode_toggle);

        SharedPreferences sp = getActivity().getSharedPreferences("com.monsoonblessing.moments", Context.MODE_PRIVATE);
        final SharedPreferences.Editor spEdit = sp.edit();

        if (!sp.getBoolean("DefaultDisplay", true)) {
            display_checkbox.setChecked(false);
        }

        b.setView(v).setPositiveButton("Apply Changes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // save preference
                spEdit.putBoolean("DefaultDisplay", display_checkbox.isChecked()).apply();
                // save whatever search preferences our main activity had
                ((MainActivity) getActivity()).saveSearchPreferences();
                // restart app
                Intent i = getActivity().getPackageManager()
                        .getLaunchIntentForPackage(getActivity().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

            }
        }).setNeutralButton("Dismiss", null);
        return b.create();
    }

}
