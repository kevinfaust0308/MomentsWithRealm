package com.monsoonblessing.moments.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Kevin on 2016-06-15.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {


    public interface OnDateChosenListener {
        void OnDateChose(int year, int month, int day);
    }

    public static DatePickerFragment newInstance(long date) {
        DatePickerFragment f = new DatePickerFragment();
        Bundle b = new Bundle();
        b.putLong("date", date);
        f.setArguments(b);
        return f;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("DatePickerFragment", "OnCreateDialog");

        Date d = new Date(getArguments().getLong("date"));
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        return new DatePickerDialog(getActivity(), this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }


    //DatePickerDialog.OnDateSetListener overridden method
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        /*
        Callback interface. Implementation in CreateNewMoment.java
        When date is chosen, get callback fragment and call its callback method to update UI with selected date
        */
        OnDateChosenListener listener = null;
        if (getFragmentManager().findFragmentByTag("addMoment") != null) {
            listener = (OnDateChosenListener) getFragmentManager().findFragmentByTag("addMoment");
        } else if (getFragmentManager().findFragmentByTag("updateMoment") != null) {
            listener = (OnDateChosenListener) getFragmentManager().findFragmentByTag("updateMoment");
        }
        if (listener != null) listener.OnDateChose(year, monthOfYear, dayOfMonth);

    }


}
