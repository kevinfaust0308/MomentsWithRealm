package com.monsoonblessing.moments.Activities;

import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;
import com.monsoonblessing.moments.MetricUtils;
import com.monsoonblessing.moments.MomentModel;
import com.monsoonblessing.moments.R;
import com.monsoonblessing.moments.R2;
import com.monsoonblessing.moments.RealmDatabaseHelper;
import com.monsoonblessing.moments.PreCachingLayoutManager;
import com.monsoonblessing.moments.RecyclerItemListener;
import com.monsoonblessing.moments.SearchPreference;
import com.monsoonblessing.moments.Adapters.MyRecyclerViewAdapter;
import com.monsoonblessing.moments.Enums.SortingOptions;
import com.monsoonblessing.moments.Fragments.CreateNewMoment;
import com.monsoonblessing.moments.Fragments.SettingsDialog;
import com.monsoonblessing.moments.Fragments.UpdateMoment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SEARCH_PREFS = "SearchPreference";

    @BindView(R2.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R2.id.viewSwitcher)
    ViewSwitcher mEntriesViewSwitcher;
    @BindView(R2.id.noEntriesText)
    TextView mNoEntriesFoundText;
    @BindView(R2.id.my_toolbar)
    Toolbar mToolbar;

    private MyRecyclerViewAdapter mAdapter;
    private SearchPreference mSearchPreference;

    private SharedPreferences mSharedPreferences;

    private Realm realm;

    //search layout
    @BindView(R2.id.search_layout)
    LinearLayout searchLayout;
    @BindView(R2.id.spinner_month)
    Spinner mSpinnerMonth;
    @BindView(R2.id.spinner_year)
    Spinner mSpinnerYear;
    @BindView(R2.id.spinner_sort)
    Spinner mSpinnerSort;
    @BindView(R2.id.search_button)
    ImageButton mSearchButton;
    //adapters
    private ArrayAdapter<CharSequence> monthsAdapter;
    private ArrayAdapter<String> yearsAdapter;
    private ArrayAdapter<String> sortOptionsAdapter;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        realm = Realm.getDefaultInstance();

        mSharedPreferences = getSharedPreferences("com.monsoonblessing.moments", MODE_PRIVATE);
        String searchPreference = mSharedPreferences.getString(SEARCH_PREFS, null);
        if (searchPreference != null) {
            mSearchPreference = new Gson().fromJson(searchPreference, SearchPreference.class);
        } else {
            mSearchPreference = new SearchPreference();
        }

        // create an adapter with null data
        mAdapter = new MyRecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        // fetch our data and update the adapter and add listener
        reloadMomentsRealmResults(mSearchPreference);


        PreCachingLayoutManager manager = new PreCachingLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemViewCacheSize(9999);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemListener(this, mRecyclerView, new RecyclerItemListener.OnItemClickListener() {

            @Override
            public void OnItemLongClick(View v, final int position) {
                ImageButton edit = (ImageButton) v.findViewById(R.id.edit_btn);
                ImageButton delete = (ImageButton) v.findViewById(R.id.delete_btn);
                ImageButton close = (ImageButton) v.findViewById(R.id.close_text);
                final ViewSwitcher cardViewSwitcher = (ViewSwitcher) v.findViewById(R.id.cardViewSwitcher);

                cardViewSwitcher.setDisplayedChild(1);
                hideSearchLayout();

                final MomentModel moment = mAdapter.getDataAtPosition(position);

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UpdateMoment f = UpdateMoment.newInstance(moment.getId(), moment.getPhotoUri(), moment.getTitle(), moment.getDateLong());
                        f.show(getFragmentManager(), "updateMoment");
                        cardViewSwitcher.setDisplayedChild(0);
                    }
                });

                //removes moment object from database (adapter data auto-updates due to data source being a RealmResult)
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Id to delete: " + moment.getId());
                        RealmDatabaseHelper.deleteRow(moment.getId());
                        cardViewSwitcher.setDisplayedChild(0);
                    }

                });

                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cardViewSwitcher.setDisplayedChild(0);
                    }
                });
            }
        }


        ));

        setUpSearchLayout();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            //add new moment popup
            case R.id.action_add:
                //hide search frag if visible
                hideSearchLayout();
                DialogFragment frag = new CreateNewMoment();
                frag.show(getFragmentManager(), "addMoment");
                return true;
            //toggle fragment visibility
            case R.id.action_launch_filter_screen:
                toggleSearchLayout();
                return true;
            case R.id.action_settings:
                SettingsDialog sd = new SettingsDialog();
                sd.show(getFragmentManager(), "Settings Dialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void showOrHideMomentEntriesUI() {
        if (mAdapter.getItemCount() == 0) {
            showNoEntryUI();
        } else {
            showEntryUI();
        }
    }



    /*
    Only need to reload entire data source on first app launch or during filtering
     */
    public void reloadMomentsRealmResults(SearchPreference searchPreference) {
        Log.d(TAG, "Reloading moments from realm");

        // re-query data
        RealmResults<MomentModel> requeriedData = RealmDatabaseHelper.searchEntries(searchPreference);
        // update the data used in the recyclerview
        mAdapter.updateData(requeriedData);
        // update moments ui
        showOrHideMomentEntriesUI();

        // add listener to the newly updated data source to listen to adds/edits/deletes
        requeriedData.addChangeListener(new RealmChangeListener<RealmResults<MomentModel>>() {
            @Override
            public void onChange(RealmResults<MomentModel> changedData) {
                Log.d(TAG, "Data has been added/edited/deleted. mMomentsRealmResults change listener running");
                mAdapter.updateData(changedData);
                showOrHideMomentEntriesUI();
            }
        });
    }


    public void showNoEntryUI() {
        mEntriesViewSwitcher.setDisplayedChild(1);
        String monthFilter = mSearchPreference.getMonth();
        String yearFilter = mSearchPreference.getYear();
        String text = "";

        Log.d(TAG, "month filter: " + monthFilter);
        Log.d(TAG, "year filter: " + yearFilter);

        if (monthFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES) && yearFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES)) {
            text = "Add a new entry to get started";
        } else if (!monthFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES) && yearFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES)) {
            text = "No entries for " + monthFilter;
        } else if (monthFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES) && !yearFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES)) {
            text = "No entries for " + yearFilter;
        } else if (!monthFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES) && !yearFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES)) {
            text = "No entries for " + monthFilter + " " + yearFilter;
        }
        mNoEntriesFoundText.setText(text);
    }

    public void showEntryUI() {
        mEntriesViewSwitcher.setDisplayedChild(0);
    }


    /******************** Search layout things ************************/

    public void setUpSearchLayout() {
         /*
        Set up adapter and make dropdown box select preconfigured settings (if any)
        --> preconfigured settings will be stored in member variables
        --> these get updated when user performs a filter request
         */
        //set up months dropdown
        monthsAdapter = ArrayAdapter.createFromResource(this, R.array.months_array, R.layout.textview);
        monthsAdapter.setDropDownViewResource(R.layout.simple_dropdown_box);
        mSpinnerMonth.setAdapter(monthsAdapter);
        //set selected month value
        mSpinnerMonth.setSelection(monthsAdapter.getPosition(mSearchPreference.getMonth()), false);


        //set up sort dropdown
        sortOptionsAdapter = new ArrayAdapter<>(this, R.layout.textview, getSortingOptions());
        sortOptionsAdapter.setDropDownViewResource(R.layout.simple_dropdown_box);
        mSpinnerSort.setAdapter(sortOptionsAdapter);
        //set selected sort option
        mSpinnerSort.setSelection(sortOptionsAdapter.getPosition(mSearchPreference.getSortOption().getDescription()), false);


        // set up years dropdown
        yearsAdapter = new ArrayAdapter<>(this, R.layout.textview, RealmDatabaseHelper.getYears());
        yearsAdapter.setDropDownViewResource(R.layout.simple_dropdown_box);
        mSpinnerYear.setAdapter(yearsAdapter);
        //set selected year value
        mSpinnerYear.setSelection(yearsAdapter.getPosition(mSearchPreference.getYear()), false);


        //updated search field variables, save in shared prefs, and reload UI
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String monthFilter = mSpinnerMonth.getSelectedItem().toString();
                String yearFilter = mSpinnerYear.getSelectedItem().toString();
                String sortFilter = mSpinnerSort.getSelectedItem().toString();

                Log.d(TAG, "mSearchPreference: " + monthFilter + " " + yearFilter + " " + sortFilter);

                mSearchPreference = new SearchPreference(monthFilter, yearFilter, SortingOptions.getEnum(sortFilter));
                reloadMomentsRealmResults(mSearchPreference);
            }
        });
    }

    public void updateYearDropdownOptions() {
        yearsAdapter.clear();
        yearsAdapter.addAll(RealmDatabaseHelper.getYears());
        yearsAdapter.notifyDataSetChanged();

        //set selected year value. when submitting, we store the year filter value
        mSpinnerYear.setSelection(yearsAdapter.getPosition(mSearchPreference.getYear()), false);
    }


    public List<String> getSortingOptions() {
        List<String> list = new ArrayList<>();
        for (SortingOptions option : SortingOptions.values()) {
            list.add(option.getDescription());
        }
        return list;
    }

    public void toggleSearchLayout() {
        if (searchLayout.getY() == 0) {
            hideSearchLayout();
        } else {
            showSearchLayout();
        }
    }

    private void hideSearchLayout() {
        ObjectAnimator.ofFloat(searchLayout, "y", MetricUtils.dpToPx(-330)).start();
    }

    private void showSearchLayout() {
        ObjectAnimator.ofFloat(searchLayout, "y", 0f).start();
        // update year dropdown
        updateYearDropdownOptions();

    }

    /***************** END OF SEARCH LAYOUT THINGS ******************/

    public void saveSearchPreferences() {
        Log.d(TAG, "Storing search prefs: " + mSearchPreference.getMonth() + " " + mSearchPreference.getYear() + " " + mSearchPreference.getSortOption());

        // change search preference object to json to store
        String pref = new Gson().toJson(mSearchPreference);
        mSharedPreferences.edit().putString(SEARCH_PREFS, pref).apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSearchPreferences();
    }
}


























/*
package com.monsoonblessing.moments.Activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;
import com.monsoonblessing.moments.MetricUtils;
import com.monsoonblessing.moments.MomentModel;
import com.monsoonblessing.moments.R;
import com.monsoonblessing.moments.R2;
import com.monsoonblessing.moments.RealmDatabaseHelper;
import com.monsoonblessing.moments.PreCachingLayoutManager;
import com.monsoonblessing.moments.RecyclerItemListener;
import com.monsoonblessing.moments.SearchPreference;
import com.monsoonblessing.moments.Adapters.MyRecyclerViewAdapter;
import com.monsoonblessing.moments.Enums.SortingOptions;
import com.monsoonblessing.moments.Fragments.CreateNewMoment;
import com.monsoonblessing.moments.Fragments.SettingsDialog;
import com.monsoonblessing.moments.Fragments.UpdateMoment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SEARCH_PREFS = "SearchPreference";

    @BindView(R2.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R2.id.viewSwitcher)
    ViewSwitcher mEntriesViewSwitcher;
    @BindView(R2.id.noEntriesText)
    TextView mNoEntriesFoundText;
    @BindView(R2.id.my_toolbar)
    Toolbar mToolbar;

    private MyRecyclerViewAdapter mAdapter;
    private SearchPreference mSearchPreference;

    private RealmResults<MomentModel> mMomentsRealmResults;

    private SharedPreferences mSharedPreferences;

    private Realm realm;

    //search layout
    @BindView(R2.id.search_layout)
    LinearLayout searchLayout;
    @BindView(R2.id.spinner_month)
    Spinner mSpinnerMonth;
    @BindView(R2.id.spinner_year)
    Spinner mSpinnerYear;
    @BindView(R2.id.spinner_sort)
    Spinner mSpinnerSort;
    @BindView(R2.id.search_button)
    ImageButton mSearchButton;
    //adapters
    private ArrayAdapter<CharSequence> monthsAdapter;
    private ArrayAdapter<String> yearsAdapter;
    private ArrayAdapter<String> sortOptionsAdapter;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        realm = Realm.getDefaultInstance();

        mSharedPreferences = getSharedPreferences("com.monsoonblessing.moments", MODE_PRIVATE);
        String searchPreference = mSharedPreferences.getString(SEARCH_PREFS, null);
        if (searchPreference != null) {
            mSearchPreference = new Gson().fromJson(searchPreference, SearchPreference.class);
        } else {
            mSearchPreference = new SearchPreference();
        }

        // create an adapter with no data
        //TEMPORARILY CREATING AND SETTING ADAPTER EVER TIME UI CHANGES BECUZ OF SOME BUG WHERE IT ISNT UPDATING SOMETIMES
        mAdapter = new MyRecyclerViewAdapter(this, new ArrayList<MomentModel>(0));
        mRecyclerView.setAdapter(mAdapter);
        // fetch our data and update the adapter and add listener
        reloadMomentsRealmResults(mSearchPreference);


        PreCachingLayoutManager manager = new PreCachingLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemViewCacheSize(9999);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemListener(this, mRecyclerView, new RecyclerItemListener.OnItemClickListener() {

            @Override
            public void OnItemLongClick(View v, final int position) {
                ImageButton edit = (ImageButton) v.findViewById(R.id.edit_btn);
                ImageButton delete = (ImageButton) v.findViewById(R.id.delete_btn);
                ImageButton close = (ImageButton) v.findViewById(R.id.close_text);
                final ViewSwitcher cardViewSwitcher = (ViewSwitcher) v.findViewById(R.id.cardViewSwitcher);

                cardViewSwitcher.setDisplayedChild(1);
                hideSearchLayout();

                final MomentModel moment = mAdapter.getData(position);

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UpdateMoment f = UpdateMoment.newInstance(moment.getId(), moment.getPhotoUri(), moment.getTitle(), moment.getDateLong());
                        f.show(getFragmentManager(), "updateMoment");
                        cardViewSwitcher.setDisplayedChild(0);
                    }
                });

                //removes moment object from database and from adapter
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Id to delete: " + moment.getId());
                        RealmDatabaseHelper.deleteRow(moment.getId());
                        mAdapter.removeData(position);
                        //mAdapter.notifyItemRemoved(position);
                        if (mAdapter.getItemCount() == 0) {
                            Log.d(TAG, "No entries in our adapter. Updating no entry UI");
                            showNoEntryUI();
                        }
                    }

                });

                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cardViewSwitcher.setDisplayedChild(0);
                    }
                });
            }
        }


        ));

        setUpSearchLayout();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            //add new moment popup
            case R.id.action_add:
                //hide search frag if visible
                hideSearchLayout();
                DialogFragment frag = new CreateNewMoment();
                frag.show(getFragmentManager(), "addMoment");
                return true;
            //toggle fragment visibility
            case R.id.action_launch_filter_screen:
                toggleSearchLayout();
                return true;
            case R.id.action_settings:
                SettingsDialog sd = new SettingsDialog();
                sd.show(getFragmentManager(), "Settings Dialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public void showOrHideMomentEntriesUI() {
        if (mMomentsRealmResults.size() == 0) {
            //hide recyclerview and tell user there are no entries
            showNoEntryUI();
        } else {
            // temporary fix. just updating the adapter doesnt seem to work sometimes
            //mAdapter = new MyRecyclerViewAdapter(this, new ArrayList<>(mMomentsRealmResults));
            //mRecyclerView.setAdapter(mAdapter);
            mAdapter.updateData(mMomentsRealmResults);
            showEntryUI();
        }
    }



    public void reloadMomentsRealmResults(SearchPreference searchPreference) {
        Log.d(TAG, "Reloading moments from realm");
        // remove current listener if not first launch
        if (mMomentsRealmResults != null) {
            mMomentsRealmResults.removeChangeListeners();
        }
        // change data source
        mMomentsRealmResults = RealmDatabaseHelper.searchEntries(searchPreference);
        // update moments ui
        showOrHideMomentEntriesUI();
        // add listener to new data source
        mMomentsRealmResults.addChangeListener(new RealmChangeListener<RealmResults<MomentModel>>() {
            @Override
            public void onChange(RealmResults<MomentModel> element) {
                Log.d(TAG, "mMomentsRealmResults change listener running");
                showOrHideMomentEntriesUI();
            }
        });
    }


    public void showNoEntryUI() {
        mEntriesViewSwitcher.setDisplayedChild(1);
        String monthFilter = mSearchPreference.getMonth();
        String yearFilter = mSearchPreference.getYear();
        String text = "";

        Log.d(TAG, "month filter: " + monthFilter);
        Log.d(TAG, "year filter: " + yearFilter);

        if (monthFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES) && yearFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES)) {
            text = "Add a new entry to get started";
        } else if (!monthFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES) && yearFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES)) {
            text = "No entries for " + monthFilter;
        } else if (monthFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES) && !yearFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES)) {
            text = "No entries for " + yearFilter;
        } else if (!monthFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES) && !yearFilter.equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES)) {
            text = "No entries for " + monthFilter + " " + yearFilter;
        }
        mNoEntriesFoundText.setText(text);
    }

    public void showEntryUI() {
        mEntriesViewSwitcher.setDisplayedChild(0);
    }


    */
/******************** Search layout things ************************//*


    public void setUpSearchLayout() {
         */
/*
        Set up adapter and make dropdown box select preconfigured settings (if any)
        --> preconfigured settings will be stored in member variables
        --> these get updated when user performs a filter request
         *//*

        //set up months dropdown
        monthsAdapter = ArrayAdapter.createFromResource(this, R.array.months_array, R.layout.textview);
        monthsAdapter.setDropDownViewResource(R.layout.simple_dropdown_box);
        mSpinnerMonth.setAdapter(monthsAdapter);
        //set selected month value
        mSpinnerMonth.setSelection(monthsAdapter.getPosition(mSearchPreference.getMonth()), false);


        //set up sort dropdown
        sortOptionsAdapter = new ArrayAdapter<>(this, R.layout.textview, getSortingOptions());
        sortOptionsAdapter.setDropDownViewResource(R.layout.simple_dropdown_box);
        mSpinnerSort.setAdapter(sortOptionsAdapter);
        //set selected sort option
        mSpinnerSort.setSelection(sortOptionsAdapter.getPosition(mSearchPreference.getSortOption().getDescription()), false);


        // set up years dropdown
        yearsAdapter = new ArrayAdapter<>(this, R.layout.textview, RealmDatabaseHelper.getYears());
        yearsAdapter.setDropDownViewResource(R.layout.simple_dropdown_box);
        mSpinnerYear.setAdapter(yearsAdapter);
        //set selected year value
        mSpinnerYear.setSelection(yearsAdapter.getPosition(mSearchPreference.getYear()), false);


        //updated search field variables, save in shared prefs, and reload UI
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String monthFilter = mSpinnerMonth.getSelectedItem().toString();
                String yearFilter = mSpinnerYear.getSelectedItem().toString();
                String sortFilter = mSpinnerSort.getSelectedItem().toString();

                Log.d(TAG, "mSearchPreference: " + monthFilter + " " + yearFilter + " " + sortFilter);

                mSearchPreference = new SearchPreference(monthFilter, yearFilter, SortingOptions.getEnum(sortFilter));
                reloadMomentsRealmResults(mSearchPreference);
            }
        });
    }

    public void updateYearDropdownOptions() {
        yearsAdapter.clear();
        yearsAdapter.addAll(RealmDatabaseHelper.getYears());
        yearsAdapter.notifyDataSetChanged();

        //set selected year value. when submitting, we store the year filter value
        mSpinnerYear.setSelection(yearsAdapter.getPosition(mSearchPreference.getYear()), false);
    }


    public List<String> getSortingOptions() {
        List<String> list = new ArrayList<>();
        for (SortingOptions option : SortingOptions.values()) {
            list.add(option.getDescription());
        }
        return list;
    }

    public void toggleSearchLayout() {
        if (searchLayout.getY() == 0) {
            hideSearchLayout();
        } else {
            showSearchLayout();
        }
    }

    private void hideSearchLayout() {
        ObjectAnimator.ofFloat(searchLayout, "y", MetricUtils.dpToPx(-330)).start();
    }

    private void showSearchLayout() {
        ObjectAnimator.ofFloat(searchLayout, "y", 0f).start();
        // update year dropdown
        updateYearDropdownOptions();

    }

    */
/***************** END OF SEARCH LAYOUT THINGS ******************//*


    public void saveSearchPreferences() {
        Log.d(TAG, "Storing search prefs: " + mSearchPreference.getMonth() + " " + mSearchPreference.getYear() + " " + mSearchPreference.getSortOption());

        // change search preference object to json to store
        String pref = new Gson().toJson(mSearchPreference);
        mSharedPreferences.edit().putString(SEARCH_PREFS, pref).apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSearchPreferences();
    }
}


*/
