package com.monsoonblessing.moments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Kevin on 2016-06-17.
 * <p/>
 * For help regarding the overriden methods:
 * http://stackoverflow.com/questions/21881992/when-is-sqliteopenhelper-oncreate-onupgrade-run
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int VERSION_NUMBER = 1;
    public static final String DATABASE_NAME = "moments.db";
    public static final String TABLE_NAME = "moments_table";
    public static final String COL_1_ID = "ID";
    public static final String COL_2_TITLE = "TITLE";
    public static final String COL_3_URI = "URI";
    public static final String COL_4_DATE = "DATE";
    public static final String COL_5_MONTH = "MONTH";
    public static final String COL_6_YEAR = "YEAR";
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private SQLiteDatabase db;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_NUMBER);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "
                + TABLE_NAME
                + " ("
                + COL_1_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_2_TITLE + " VARCHAR(255), "
                + COL_3_URI + " TEXT, "
                + COL_4_DATE + " INTEGER, "
                + COL_5_MONTH + " VARCHAR(9), "
                + COL_6_YEAR + " SMALLINT UNSIGNED);");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public void closeDatabase() {
        if (db != null) db.close();
    }


    /*
    Querying
    Inserting
    Updating
    Deleting
     */


    public Cursor getRow(long rowId) {
        db = getReadableDatabase();
        return db.query(TABLE_NAME,
                new String[]{COL_1_ID, COL_2_TITLE, COL_3_URI, COL_4_DATE},
                "? = ?",
                new String[]{COL_1_ID, Long.toString(rowId)},
                null,
                null,
                null);
    }

    //drop down box of all available data year entries for filtering
    public Cursor getYears() {
        db = getReadableDatabase();
        return db.query(true, TABLE_NAME, new String[] {COL_6_YEAR}, null, null, null, null, COL_6_YEAR + " ASC", null);
    }


    /*specify how to get the data
    1. Pass in null to get all columns
    2. Pass in a month/year to get filtered results
     */
/*    public Cursor getAllData(SortingOptions sortOption, String selection) {
        db = getReadableDatabase();

        String sortingOption;
        switch (sortOption) {
            case ORDER_ADDED:
                sortingOption = "ID DESC";
                break;
            case RECENT_TO_OLD_DATE:
                sortingOption = "DATE DESC";
                break;
            case OLD_TO_RECENT_DATE:
                sortingOption = "DATE ASC";
                break;
            default:
                sortingOption = null;
        }


        //use rawQuery or query
        //SELECT * FROM TABLE_NAMEs
        return db.query(TABLE_NAME,
                new String[]{COL_1_ID, COL_2_TITLE, COL_3_URI, COL_4_DATE},
                selection,
                null,
                null,
                null,
                sortingOption);
        //OR db.rawQuery("SELECT * FROM ?", new String[]{TABLE_NAME});
        //OR db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }*/


    public Cursor getAllData(SearchPreference searchPreference) {
        db = getReadableDatabase();

        String selectionMonth = null;
        String selectionYear = null;
        String selection = null;
        if (searchPreference.getMonth() != null) {
            selectionMonth = COL_5_MONTH + " = '" + searchPreference.getMonth() + "'";
            selection = selectionMonth;
        }
        if (searchPreference.getYear() != null) {
            selectionYear = COL_6_YEAR + " = '" + searchPreference.getYear() + "'";
            selection = selectionYear;
        }
        if (selectionMonth != null && selectionYear != null) {
            selection = (selectionMonth + " AND " + selectionYear);
        }

        Log.d(TAG, "Selection string: " + selection);


        String sortingOption;
        switch (searchPreference.getSortOption()) {
            case ORDER_ADDED:
                sortingOption = "ID DESC";
                break;
            case RECENT_TO_OLD_DATE:
                sortingOption = "DATE DESC";
                break;
            case OLD_TO_RECENT_DATE:
                sortingOption = "DATE ASC";
                break;
            default:
                sortingOption = null;
        }


        //use rawQuery or query
        //SELECT * FROM TABLE_NAMEs
        return db.query(TABLE_NAME,
                new String[]{COL_1_ID, COL_2_TITLE, COL_3_URI, COL_4_DATE},
                selection,
                null,
                null,
                null,
                sortingOption);
        //OR db.rawQuery("SELECT * FROM ?", new String[]{TABLE_NAME});
        //OR db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }


    public boolean insertData(String title, String uri, Long date, String month, int year) {
        db = getWritableDatabase();

        //this map contains the column values for the row. The keys should be the column names and the values the column values
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2_TITLE, title);
        contentValues.put(COL_3_URI, uri);
        contentValues.put(COL_4_DATE, date);
        contentValues.put(COL_5_MONTH, month);
        contentValues.put(COL_6_YEAR, year);

        //the insert convenience method returns the row ID of the newly inserted row, or -1 if an error occurred
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }


    public boolean updateRow(int id, String title, String uri, Long date, String month, int year) {
        db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2_TITLE, title);
        contentValues.put(COL_3_URI, uri);
        contentValues.put(COL_4_DATE, date);
        contentValues.put(COL_5_MONTH, month);
        contentValues.put(COL_6_YEAR, year);

        int result = db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{String.valueOf(id)});
        return result == 1;
    }


    public boolean deleteRow(int id) {
        db = getWritableDatabase();

        int result = db.delete(TABLE_NAME, "ID = ?", new String[]{String.valueOf(id)});
        return result == 1;
    }


    public void deleteAllEntries() {
        db = getWritableDatabase();

        db.delete(TABLE_NAME, null, null);
    }

}








