package com.monsoonblessing.moments;

import com.monsoonblessing.moments.Enums.SortingOptions;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * <p>Helper class for database related operations</p>
 * Updated by Kevin on 2016-12-25.
 */
public class RealmDatabaseHelper {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PHOTO_URI = "photoUri";
    public static final String COLUMN_DATE_LONG = "dateLong";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_YEAR = "year";

    private static final String TAG = RealmDatabaseHelper.class.getSimpleName();


    /**
     * <p>Get all years of our entries</p>
     * <p>Used for drop down box of all available data year entries for filtering</p>
     */
    public static List<String> getYears() {


        Realm realm = Realm.getDefaultInstance();

        // list to hold all unique years
        List<String> years = new ArrayList<>();

        // add "All" as first option
        years.add("All");

        // get all the entries sorted by year old to new
        SearchPreference searchPreference = new SearchPreference(
                SearchPreference.DEFAULT_ALL_DATE_ENTRIES,
                SearchPreference.DEFAULT_ALL_DATE_ENTRIES,
                SortingOptions.OLD_TO_RECENT_DATE
        );
        RealmResults<MomentModel> yearSortedResults = searchEntries(searchPreference);
        // get all distinct years
        yearSortedResults = yearSortedResults.distinct(COLUMN_YEAR);

        // loop through each resut and add to our list
        for (MomentModel moment : yearSortedResults) {
            years.add(String.valueOf(moment.getYear()));
        }

        // close database
        realm.close();

        // return set of years
        return years;
    }


    /**
     * Inserts a new row into the database with the passed in data
     *
     * @param searchPreference describes how to filter and sort data
     */
    public static RealmResults<MomentModel> searchEntries(SearchPreference searchPreference) {
        Realm realm = Realm.getDefaultInstance();

        // Build the query looking at all users:
        RealmQuery<MomentModel> query = realm.where(MomentModel.class);
        // if we have a month filter, add that to the query
        if (!searchPreference.getMonth().equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES)) {
            query.equalTo(COLUMN_MONTH, searchPreference.getMonth());
        }
        // if we have a year filter, add that to the query
        if (!searchPreference.getYear().equals(SearchPreference.DEFAULT_ALL_DATE_ENTRIES)) {
            // since we're not searching for "All" years, we have an integer year
            int year = Integer.valueOf(searchPreference.getYear());
            query.equalTo(COLUMN_YEAR, year);
        }

        // execute query for filtered results
        RealmResults<MomentModel> results = query.findAll();

        // get the sort options
        SortingOptions options = searchPreference.getSortOption();
        // execute query for sorted results
        results = results.sort(options.getCol(), options.getSort());

        // close database
        realm.close();

        // return results
        return results;
    }


    /**
     * Inserts a new row into the database with the passed in data
     *
     * @param title    title of new entry
     * @param photoUri photo uri of new entry
     * @param date     date, in milliseconds, of new entry
     * @param month    month of new entry
     * @param year     year of new entry
     */
    public static void insertNewMomentIntoDB(final String title, final String photoUri, final Long date, final String month, final int year) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // create new row
                MomentModel moment = realm.createObject(MomentModel.class, getNextPrimaryKeyValue());
                // populate row with data
                moment.setAllFields(title, photoUri, date, month, year);
            }
        });
        // close database
        realm.close();
    }


    /**
     * Updates the row with the passed in ID with the new data
     *
     * @param id       id of the row to update
     * @param title    title of new entry
     * @param photoUri photo uri of new entry
     * @param date     date, in milliseconds, of new entry
     * @param month    month of new entry
     * @param year     year of new entry
     */
    public static void updateRow(final int id, final String title, final String photoUri, final Long date, final String month, final int year) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // get the row with the specified id
                MomentModel moment = realm.where(MomentModel.class).equalTo(COLUMN_ID, id).findFirst();
                // update its fields
                moment.setAllFields(title, photoUri, date, month, year);

            }
        });
        // close database
        realm.close();
    }


    /**
     * Deletes the row with the specified id
     *
     * @param id id of the row to delete
     */
    public static void deleteRow(final int id) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // get the row with the specified id
                MomentModel moment = realm.where(MomentModel.class).equalTo(COLUMN_ID, id).findFirst();
                // delete the row
                moment.deleteFromRealm();

            }
        });
        // close database
        realm.close();
    }


    /**
     * Delete all entries
     */
    public static void deleteAllEntries() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // get all rows
                RealmResults<MomentModel> results = realm.where(MomentModel.class).findAll();
                // delete all
                results.deleteAllFromRealm();

            }
        });
        // close database
        realm.close();
    }


    /**
     * Returns the next available primary key value for row insertion
     */
    private static int getNextPrimaryKeyValue() {


        Realm realm = Realm.getDefaultInstance();
        Number curr_max = realm.where(MomentModel.class).findAll().max(COLUMN_ID);
        int new_max = 1;

        // if num is not null (meaning we actually have a row), our primary key should have value 1
        if (curr_max != null) {
            new_max = curr_max.intValue();
            new_max++;
        }
        return new_max;
    }

}








