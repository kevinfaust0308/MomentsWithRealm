package com.monsoonblessing.moments.Activities;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;
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
import com.monsoonblessing.moments.Fragments.MomentConfig;
import com.monsoonblessing.moments.Fragments.SearchFragment;
import com.monsoonblessing.moments.Fragments.SettingsDialog;
import com.monsoonblessing.moments.Fragments.UpdateMoment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity
        implements SearchFragment.OnFilterListener, SettingsDialog.OnDeleteListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R2.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R2.id.viewSwitcher)
    ViewSwitcher mEntriesViewSwitcher;
    @BindView(R2.id.fragmentContainer)
    FrameLayout mFilterSearchFragmentContainer;
    @BindView(R2.id.noEntriesText)
    TextView mNoEntriesFoundText;
    @BindView(R2.id.my_toolbar)
    Toolbar mToolbar;

    private MyRecyclerViewAdapter mAdapter;
    private SearchFragment mSearchFragment;
    private SearchPreference mSearchPreference;

    private RealmResults<MomentModel> mMomentsRealmResults;

    private SharedPreferences mSharedPreferences;

    private Realm realm;


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
        Gson gson = new Gson();
        String searchPreference = mSharedPreferences.getString("SearchPreference", null);
        if (searchPreference != null) {
            mSearchPreference = gson.fromJson(searchPreference, SearchPreference.class);
        } else {
            mSearchPreference = new SearchPreference();
        }

        mMomentsRealmResults = RealmDatabaseHelper.searchEntries(mSearchPreference);
        if (mMomentsRealmResults.size() == 0) {
            //hide recyclerview and tell user there are no entries
            Log.d(TAG, "View switcher: " + mEntriesViewSwitcher);
            showNoEntryUI();
            mAdapter = new MyRecyclerViewAdapter(MainActivity.this, new ArrayList<MomentModel>(0));
        } else {
            // debugging. see all entries on launch
            for (MomentModel m : mMomentsRealmResults) {
                Log.d(TAG, m.toString());
            }
            mAdapter = new MyRecyclerViewAdapter(MainActivity.this, mMomentsRealmResults);
        }
        mRecyclerView.setAdapter(mAdapter);


        // basically when we submit new entries, this listener will take care of everything
        mMomentsRealmResults.addChangeListener(new RealmChangeListener<RealmResults<MomentModel>>() {
            @Override
            public void onChange(RealmResults<MomentModel> results) {
                if (results.size() == 0) {
                    //hide recyclerview and tell user there are no entries
                    showNoEntryUI();
                } else {
                    mAdapter.updateData(mMomentsRealmResults);
                    mAdapter.notifyDataSetChanged();
                    showEntryUI();
                }
            }
        });


        PreCachingLayoutManager manager = new PreCachingLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemViewCacheSize(9999);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemListener(this, mRecyclerView, new RecyclerItemListener.OnItemClickListener() {

            @Override
            public void OnItemLongClick(View v, final int position) {
                TextView edit = (TextView) v.findViewById(R.id.edit_text);
                TextView delete = (TextView) v.findViewById(R.id.delete_text);
                ImageButton close = (ImageButton) v.findViewById(R.id.close_text);
                final ViewSwitcher cardViewSwitcher = (ViewSwitcher) v.findViewById(R.id.cardViewSwitcher);

                cardViewSwitcher.setDisplayedChild(1);
                hideSearchFragment();

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
                        mAdapter.notifyItemRemoved(position);
                        if (mAdapter.getItemCount() == 0) {
                            Log.d(TAG, "No entries in our adapter. Updating no entry UI");
                            showNoEntryUI();
                        }
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

        mSearchFragment = new SearchFragment();

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.fragmentContainer, mSearchFragment, "searchFragment");
        ft.commit();
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
                hideSearchFragment();
                DialogFragment frag = new CreateNewMoment();
                frag.show(getFragmentManager(), "addMoment");
                return true;
            //toggle fragment visibility
            case R.id.action_launch_filter_screen:
                if (mFilterSearchFragmentContainer.getVisibility() == View.VISIBLE) {
                    hideSearchFragment();
                } else {
                    mSearchFragment.updateYearDropdownOptions();
                    mFilterSearchFragmentContainer.setVisibility(View.VISIBLE);
                }
                return true;
            case R.id.action_settings:
                SettingsDialog sd = new SettingsDialog();
                sd.show(getFragmentManager(), "Settings Dialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void hideSearchFragment() {
        if (mFilterSearchFragmentContainer.getVisibility() == View.VISIBLE) {
            mFilterSearchFragmentContainer.setVisibility(View.GONE);
        }
    }


    /*
    Called when we filter our entries
    Callback interface implementation. Declaration in SearchFragment.java
    Re-sort and retrieve data and then update UI by updating the adapter
    */
    @Override
    public void OnFilter(SearchPreference searchPreference) {
        hideSearchFragment();

        mMomentsRealmResults = RealmDatabaseHelper.searchEntries(searchPreference);
        if (mMomentsRealmResults.size() == 0) {
            //hide recyclerview and tell user there are no entries
            showNoEntryUI();
        } else {
            mAdapter.updateData(mMomentsRealmResults);
            mAdapter.notifyDataSetChanged();
            showEntryUI();
        }

    }


    /*
    Called when we delete all entries
    Callback interface implementation. Declaration in SettingsDialog.java
    Update UI prompting user to add an entry and remove filter menu item
    */
    @Override
    public void OnDelete() {
        hideSearchFragment();

        //when adding new entries after deletion, images are still temporarily cached and those old photos will flicker briefly
        mAdapter.clearData();
        mAdapter.notifyDataSetChanged();

        //setDefaultSearchSettings search preferences
        // SharedPreferences.Editor sharedPreferences = getSharedPreferences("com.monsoonblessing.moments", MODE_PRIVATE).edit();
        // sharedPreferences.putString("SearchPreference", new Gson().toJson(new SearchPreference())).apply();
        mSearchPreference.setDefaultSearchSettings();

        //display "Add entry text" because search preference is now null (must be after the search pref setDefaultSearchSettings ^^)
        //we call this here because OnDelete means there are 0 entries
        showNoEntryUI();
        //setDefaultSearchSettings search fragment dropdown
        mSearchFragment.reset();
    }


    public void showNoEntryUI() {
        mEntriesViewSwitcher.setDisplayedChild(1);
        String monthFilter = mSearchPreference.getMonth();
        String yearFilter = mSearchPreference.getYear();
        String text = "";

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


    @Override
    protected void onStop() {
        super.onStop();

        // get search preference fields
        String monthFilter = mSearchPreference.getMonth();
        String yearFilter = mSearchPreference.getYear();
        SortingOptions sortingOptions = mSearchPreference.getSortOption();

        // create the search preference object
        SearchPreference sp = new SearchPreference(
                (monthFilter.equals("All")) ? null : monthFilter,
                (yearFilter.equals("All")) ? null : yearFilter,
                sortingOptions
        );

        Log.d(TAG, "Storing search prefs: " + monthFilter + " " + yearFilter + " " + sortingOptions);

        // change search preference object to json to store
        String pref = new Gson().toJson(sp);
        mSharedPreferences.edit().putString("SearchPreference", pref).apply();
    }
}


