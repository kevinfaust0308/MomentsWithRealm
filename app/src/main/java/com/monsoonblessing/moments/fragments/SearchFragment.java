package com.monsoonblessing.moments.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.monsoonblessing.moments.RealmDatabaseHelper;
import com.monsoonblessing.moments.R;
import com.monsoonblessing.moments.SearchPreference;
import com.monsoonblessing.moments.Enums.SortingOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Kevin on 2016-06-24.
 */
public class SearchFragment extends Fragment {

    public interface OnFilterListener {
        void OnFilter(SearchPreference searchPreference);
    }

    private static final String TAG = SearchFragment.class.getSimpleName();

    @BindView(R.id.textView)
    TextView monthText;
    @BindView(R.id.textView1)
    TextView yearText;
    @BindView(R.id.textView2)
    TextView filterText;
    @BindView(R.id.spinner_month)
    Spinner mSpinnerMonth;
    @BindView(R.id.spinner_year)
    Spinner mSpinnerYear;
    @BindView(R.id.spinner_sort)
    Spinner mSpinnerSort;
    @BindView(R.id.search_button)
    ImageButton mSearchButton;
    //filter selections
    private String monthFilter;
    private String yearFilter;
    private String sortFilter;
    //load preconfigured search preference
    private SearchPreference mSearchPreference;
    //adapters
    private ArrayAdapter<CharSequence> monthsAdapter;
    private ArrayAdapter<String> yearsAdapter;
    private ArrayAdapter<String> sortOptionsAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_layout, container, false);
        ButterKnife.bind(this, v);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("com.monsoonblessing.moments", Context.MODE_PRIVATE);

        //load preconfigured search preference
        //our fragment is only ever created once. so the prefs we have here won't change even if we modify it
        String savedSearchPref = sharedPreferences.getString("SearchPreference", null);
        if (savedSearchPref != null) {
            mSearchPreference = new Gson().fromJson(savedSearchPref, SearchPreference.class);
        } else {
            mSearchPreference = new SearchPreference();
        }


        /*
        Set up adapter and make dropdown box select preconfigured settings (if any)
        --> preconfigured settings will be stored in member variables
        --> these get updated when user performs a filter request
         */
        //set up months dropdown
        monthsAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.months_array, R.layout.textview);
        monthsAdapter.setDropDownViewResource(R.layout.simple_dropdown_box);
        mSpinnerMonth.setAdapter(monthsAdapter);
        //set selected month value. if we have a search preference use that, otherwise default to 'All'
        monthFilter = mSearchPreference.getMonth();
        mSpinnerMonth.setSelection(monthsAdapter.getPosition(monthFilter), false);


        //set up sort dropdown
        sortOptionsAdapter = new ArrayAdapter<>(getActivity(), R.layout.textview, getSortingOptions());
        sortOptionsAdapter.setDropDownViewResource(R.layout.simple_dropdown_box);
        mSpinnerSort.setAdapter(sortOptionsAdapter);
        //set selected sort option. will never be null because a new obj is defaulted to "Order added"
        sortFilter = mSearchPreference.getSortOption().getDescription();
        mSpinnerSort.setSelection(sortOptionsAdapter.getPosition(sortFilter), false);


        // set up years dropdown
        yearsAdapter = new ArrayAdapter<>(getActivity(), R.layout.textview, RealmDatabaseHelper.getYears());
        yearsAdapter.setDropDownViewResource(R.layout.simple_dropdown_box);
        mSpinnerYear.setAdapter(yearsAdapter);
        //set selected year value. when submitting, we store the year filter value
        yearFilter = mSearchPreference.getYear();
        mSpinnerYear.setSelection(yearsAdapter.getPosition(yearFilter), false);


        //updated search field variables, save in shared prefs, and reload UI
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monthFilter = mSpinnerMonth.getSelectedItem().toString();
                yearFilter = mSpinnerYear.getSelectedItem().toString();
                sortFilter = mSpinnerSort.getSelectedItem().toString();

                //if our selection is "All", then we store null (null = all when doing sql querying)
                SearchPreference searchPref = new SearchPreference(
                        (monthFilter.equals("All")) ? null : monthFilter,
                        (yearFilter.equals("All")) ? null : yearFilter,
                        SortingOptions.getEnum(sortFilter)
                );

                ((OnFilterListener) getActivity()).OnFilter(searchPref);
            }
        });


        //custom font for the text descriptions
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Raleway-Regular.ttf");
        monthText.setTypeface(myTypeface);
        yearText.setTypeface(myTypeface);
        filterText.setTypeface(myTypeface);

        return v;
    }


    public void updateYearDropdownOptions() {
        yearsAdapter.clear();
        yearsAdapter.addAll(RealmDatabaseHelper.getYears());
        yearsAdapter.notifyDataSetChanged();

        //set selected year value. when submitting, we store the year filter value
        mSpinnerYear.setSelection(yearsAdapter.getPosition(yearFilter), false);
    }


    public List<String> getSortingOptions() {
        List<String> list = new ArrayList<>();
        for (SortingOptions option : SortingOptions.values()) {
            list.add(option.getDescription());
        }
        return list;
    }


    //when deleting all entries, setDefaultSearchSettings the dropdown selection to defaults (year auto updates)
    public void reset() {
        monthFilter = "All";
        mSpinnerMonth.setSelection(monthsAdapter.getPosition(monthFilter), false);

        sortFilter = SortingOptions.ORDER_ADDED.getDescription();
        mSpinnerSort.setSelection(sortOptionsAdapter.getPosition(sortFilter), false);
    }

}
