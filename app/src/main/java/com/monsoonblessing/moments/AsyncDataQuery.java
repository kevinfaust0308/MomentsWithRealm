package com.monsoonblessing.moments;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 2016-06-24.
 */
public class AsyncDataQuery extends AsyncTask<SearchPreference, Void, List<Moment>> {

    private static final String TAG = AsyncDataQuery.class.getSimpleName();
    private DatabaseHelper dbHelper;


    public AsyncDataQuery(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }


    @Override
    protected List<Moment> doInBackground(SearchPreference... params) {

        Log.d(TAG, "async task do in background running");

        //populate an array list by getting all the data sorted
        Cursor cursor = dbHelper.getAllData(params[0]);
        List<Moment> momentsList = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                //get data from each row and add the row to our array list
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_1_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_2_TITLE));
                String uri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_3_URI));
                //check if file still exists. user could have deleted pic (ex: deleted from gallery)
                    /*File photoFile = new File(uri);
                    if (!photoFile.exists()) {
                        uri = null;
                    }*/

                //even if image in file path was deleted, the app won't crash. picasso placeholder image
                //as set in the adapter will show

                Long date = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_4_DATE));
/*                String month = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_5_MONTH));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_6_YEAR));*/
                Log.d(TAG, "Moment retrieved from database(id, title, uri, date): " + id + ", " + title + ", " + uri + ", " + date);
                Moment moment = new Moment(id, title, Uri.parse(uri), date);
                momentsList.add(moment);
            }
            cursor.close();
        }
        dbHelper.closeDatabase();
        return momentsList;
    }


    @Override
    protected void onPostExecute(List<Moment> moments) {
        super.onPostExecute(moments);
        Log.d(TAG, "Loaded " + moments.size() + " data from database");
    }
}
