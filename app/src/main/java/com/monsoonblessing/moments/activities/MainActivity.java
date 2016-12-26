package com.monsoonblessing.moments.activities;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;
import com.monsoonblessing.moments.CurrentDate;
import com.monsoonblessing.moments.MomentModel;
import com.monsoonblessing.moments.RealmDatabaseHelper;
import com.monsoonblessing.moments.PreCachingLayoutManager;
import com.monsoonblessing.moments.R;
import com.monsoonblessing.moments.RecyclerItemListener;
import com.monsoonblessing.moments.SearchPreference;
import com.monsoonblessing.moments.adapters.MyRecyclerViewAdapter;
import com.monsoonblessing.moments.fragments.CreateNewMoment;
import com.monsoonblessing.moments.fragments.MomentConfig;
import com.monsoonblessing.moments.fragments.SearchFragment;
import com.monsoonblessing.moments.fragments.SettingsDialog;
import com.monsoonblessing.moments.fragments.UpdateMoment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity
        implements MomentConfig.OnSubmitListener, SearchFragment.OnFilterListener, SettingsDialog.OnDeleteListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ViewSwitcher mViewSwitcher;
    private FrameLayout mFragmentContainer;
    private TextView mNoEntriesFoundText;
    private MenuItem searchToolbarItem;

    private MyRecyclerViewAdapter mAdapter;
    private SearchFragment mSearchFragment;
    private SearchPreference mSearchPreference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activateToolbar();

        new QueryAndCreateAdapter().execute();

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        mFragmentContainer = (FrameLayout) findViewById(R.id.fragmentContainer);
        mNoEntriesFoundText = (TextView) findViewById(R.id.noEntriesText);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
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
                        UpdateMoment f = UpdateMoment.newInstance(moment.getId(), moment.getPhotoUri().toString(), moment.getTitle(), moment.getDate());
                        f.show(getFragmentManager(), "updateMoment");
                        cardViewSwitcher.setDisplayedChild(0);
                    }
                });

                //removes moment object from database and from adapter
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dbHelper.deleteRow(moment.getId())) {
                            mAdapter.removeData(position);
                            mAdapter.notifyItemRemoved(position);
                            Log.d(TAG, "deleted moment with id of " + moment.getId());
                            if (mAdapter.getLengthOfData() == 0) {
                                Log.d(TAG, "No entries in our adapter. Updating no entry UI");
                                updateNoEntryUI();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Error deleting entry. Please try again", Toast.LENGTH_SHORT).show();
                        }
                        dbHelper.closeDatabase();
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
        }));

        mSearchFragment = new SearchFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.fragmentContainer, mSearchFragment, "searchFragment");
        ft.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchToolbarItem = menu.findItem(R.id.action_launch_filter_screen);

        /*if (mSearchPreference.getMonth() == null && mSearchPreference.getYear() == null) {
            searchToolbarItem.setVisible(false); //no search filter but no results
        }*/

        if (mAdapter != null && mAdapter.getLengthOfData() == 0) {
            updateNoEntryUI();
        }

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
                if (mFragmentContainer.getVisibility() == View.VISIBLE) {
                    hideSearchFragment();
                } else {
                    mSearchFragment.refreshYearsDropdown();
                    mFragmentContainer.setVisibility(View.VISIBLE);
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
        if (mFragmentContainer.getVisibility() == View.VISIBLE) {
            mFragmentContainer.setVisibility(View.GONE);
        }
    }


    /*
    Called when we add/update an entry
    Callback interface implementation. Declaration in CreateNewMoment.java
    Re-sort and retrieve data and then update UI by updating the adapter
    */
    @Override
    public void OnSubmit() {
        mViewSwitcher.setDisplayedChild(0);
        new QueryAndUpdateAdapter().execute();
    }


    /*
    Called when we filter our entries
    Callback interface implementation. Declaration in SearchFragment.java
    Re-sort and retrieve data and then update UI by updating the adapter
    */
    @Override
    public void OnFilter() {
        hideSearchFragment();
        new QueryAndUpdateAdapter().execute();
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
        mAdapter.updateData(new ArrayList<MomentModel>());
        mAdapter.notifyDataSetChanged();

        //reset search preferences
        SharedPreferences.Editor sharedPreferences = getSharedPreferences("com.monsoonblessing.moments", MODE_PRIVATE).edit();
        sharedPreferences.putString("SearchPreference", new Gson().toJson(new SearchPreference())).apply();
        mSearchPreference.reset();

        //display "Add entry text" because search preference is now null (must be after the search pref reset ^^)
        //we call this here because OnDelete means there are 0 entries
        updateNoEntryUI();
        //reset search fragment dropdown
        mSearchFragment.reset();
    }


    /******************************************************
     * IF WE HAVE NO ENTRIES THEN THIS WILL UPDATE THE UI
     *********************/
    /*if we have no entries found:
    1. no search filter --> no entries in database
    2. search filter --> no entries for that search filter
     */
/*    public void updateNoEntryUI() {
        //mViewSwitcher.setDisplayedChild(1);

        updateNoEntryText();

        String monthFilter = mSearchPreference.getMonth();
        String yearFilter = mSearchPreference.getYear();

        if (monthFilter == null && yearFilter == null)  {
            searchToolbarItem.setVisible(false); //no search filter but no results
        } else if (monthFilter != null && yearFilter == null) {
            searchToolbarItem.setVisible(true); //no results because of filter
        } else if (monthFilter != null && yearFilter != null) {
            searchToolbarItem.setVisible(true); //no results because of filter
        }
    }

    public void updateNoEntryText() {
        mViewSwitcher.setDisplayedChild(1);
        String monthFilter = mSearchPreference.getMonth();
        String yearFilter = mSearchPreference.getYear();
        String text = "";

        if (monthFilter == null && yearFilter == null) {
            text = "Add a new entry to get started";
        } else if (monthFilter != null && yearFilter == null) {
            text = "No entries for " + monthFilter;
        } else if (monthFilter != null && yearFilter != null) {
            text = "No entries for " + monthFilter + " " + yearFilter;
        }
        Log.d(TAG, "No entry text: " + text);
        mNoEntriesFoundText.setText(text);
    }*/
    public void updateNoEntryUI() {
        mViewSwitcher.setDisplayedChild(1);
        String monthFilter = mSearchPreference.getMonth();
        String yearFilter = mSearchPreference.getYear();
        String text = "";

        if (monthFilter == null && yearFilter == null) {
            text = "Add a new entry to get started";
            searchToolbarItem.setVisible(false); //app has 0 entries regardless of filter
        } else if (monthFilter != null && yearFilter == null) {
            text = "No entries for " + monthFilter;
            searchToolbarItem.setVisible(true); //no results because of filter
        } else if (monthFilter == null && yearFilter != null) {
            text = "No entries for " + yearFilter;
            searchToolbarItem.setVisible(true); //no results because of filter
        } else if (monthFilter != null && yearFilter != null) {
            text = "No entries for " + monthFilter + " " + yearFilter;
            searchToolbarItem.setVisible(true); //no results because of filter
        }
        mNoEntriesFoundText.setText(text);
    }


/*    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Permissions.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Permissions.isCameraMode) {
                        cameraIntent();
                    } else {
                        //code for deny
                    }
                    break;
                }
        }

    }*/




    /*
    Querying the data from the database
    1. get the data sorted, create/set adapter
    2. get the data sorted, update adapter
    3. base class that fetches the data sorted
     */
    private class QueryAndCreateAdapter extends QueryData {

        @Override
        protected void onPostExecute(List<MomentModel> moments) {
            super.onPostExecute(moments);
            mAdapter = new MyRecyclerViewAdapter(MainActivity.this, moments);
            mRecyclerView.setAdapter(mAdapter);

            //do we have any entries?
            if (searchToolbarItem != null && moments.size() == 0) {
                //hide recyclerview and tell user there are no entries
                updateNoEntryUI(); //only update text. menu items have not been created yet
            }
        }
    }

    private class QueryAndUpdateAdapter extends QueryData {

        @Override
        protected void onPostExecute(List<MomentModel> moments) {
            super.onPostExecute(moments);
            Log.d(TAG, "data refreshed");

            //do we have any entries?
            if (moments.size() == 0) {
                //hide recyclerview and tell user there are no entries
                updateNoEntryUI();
            } else {
                mAdapter.updateData(moments);
                mAdapter.notifyDataSetChanged();
                mViewSwitcher.setDisplayedChild(0);
                searchToolbarItem.setVisible(true); //only allow filtering if we have entries
            }
        }
    }

    private class QueryData extends AsyncTask<Void, Void, List<MomentModel>> {

        private ProgressDialog pd;
        private long preTime;
        private long postTime;


        @Override
        protected List<MomentModel> doInBackground(Void... params) {

            Log.d(TAG, "async task do in background running");

            SharedPreferences sharedPreferences = getSharedPreferences("com.monsoonblessing.moments", MODE_PRIVATE);
            Gson gson = new Gson();
            String searchPreference = sharedPreferences.getString("SearchPreference", null);
            if (searchPreference != null) {
                mSearchPreference = gson.fromJson(searchPreference, SearchPreference.class);
            } else {
                mSearchPreference = new SearchPreference();
            }


/*            for (int i=0;i<500;i++) {
                dbHelper.insertNewMomentIntoDB("for loop #" + (i+1), "file:///storage/emulated/0/DCIM/Camera/IMG_20160712_033246.jpg", new CurrentDate().getCurrentLongDate(), CalendarUtil.monthNumberToText(i%12), 1950 + i);
            }*/


            //populate an array list by getting all the data sorted
            Cursor cursor = dbHelper.getAllData(mSearchPreference);
            List<MomentModel> momentsList = new ArrayList<>();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //get data from each row and add the row to our array list
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(RealmDatabaseHelper.COL_1_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(RealmDatabaseHelper.COL_2_TITLE));
                    String uri = cursor.getString(cursor.getColumnIndexOrThrow(RealmDatabaseHelper.COL_3_URI));
                    //check if file still exists. user could have deleted pic (ex: deleted from gallery)
                    /*File photoFile = new File(uri);
                    if (!photoFile.exists()) {
                        uri = null;
                    }*/

                    //even if image in file path was deleted, the app won't crash. picasso placeholder image
                    //as set in the adapter will show

                    Long date = cursor.getLong(cursor.getColumnIndexOrThrow(RealmDatabaseHelper.COL_4_DATE));
/*                String month = cursor.getString(cursor.getColumnIndexOrThrow(RealmDatabaseHelper.COL_5_MONTH));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow(RealmDatabaseHelper.COL_6_YEAR));*/
                    Log.d(TAG, "MomentModel retrieved from database(id, title, uri, date): " + id + ", " + title + ", " + uri + ", " + date);
                    MomentModel moment = new MomentModel(id, title, Uri.parse(uri), date);
                    momentsList.add(moment);
                }
                cursor.close();
            }
            dbHelper.closeDatabase();


            return momentsList;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.show();
            preTime = new CurrentDate().getCurrentLongDate();
        }


        @Override
        protected void onPostExecute(List<MomentModel> moments) {
            super.onPostExecute(moments);
            postTime = new CurrentDate().getCurrentLongDate();
            pd.dismiss();
            Log.d(TAG, "It took " + (postTime - preTime) / 1000.0 + " seconds to load " + moments.size() + " rows of data from database");
        }
    }
}




/*      Alternative to fetching database data using thread instead of async task

new Thread(new Runnable() {
            @Override
            public void run() {
                //populate our moments list and pass it to adapter
                Cursor data = dbHelper.searchEntries(RealmDatabaseHelper.RECENT_TO_OLD);
                final List<MomentModel> momentsList = new ArrayList<>();

                if (data != null) {
                    while (data.moveToNext()) {
                        //get data from each row and add the row to our array list
                        String title = data.getString(0);
                        String uri = data.getString(1);
                        Long date = data.getLong(2);
                        MomentModel moment = new MomentModel(title, Uri.parse(uri), date);
                        momentsList.add(moment);
                    }
                    data.close();
                }
                dbHelper.closeDatabase();

                //update ui on main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new MyRecyclerViewAdapter(MainActivity.this, momentsList);
                        mRecyclerView.setAdapter(mAdapter);

                        //do we have any entries?
                        if (momentsList.size() == 0) {
                            //hide recyclerview and tell user to add a new entry
                            mViewSwitcher.showNext();
                        }
                    }
                });

            }
        }).start();*/
