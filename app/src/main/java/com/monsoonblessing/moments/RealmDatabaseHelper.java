package com.monsoonblessing.moments;

import com.monsoonblessing.moments.enums.SortingOptions;

import java.util.LinkedHashSet;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Updated by Kevin on 2016-12-25.
 */
public class RealmDatabaseHelper {
    /*
    Helper class for database related operations
     */

    private static final String TAG = RealmDatabaseHelper.class.getSimpleName();


    //drop down box of all available data year entries for filtering
    public Set<String> getYears() {
        /*
        Get all years of our entries
         */

        Realm realm = Realm.getDefaultInstance();

        // set to hold all unique years
        Set<String> years = new LinkedHashSet<>();

        // get all the entries sorted by year old to new
        SearchPreference searchPreference = new SearchPreference(null, null, SortingOptions.OLD_TO_RECENT_DATE);
        RealmResults<MomentModel> yearSortedResults = searchEntries(searchPreference);
        // loop through each resut and add to our set
        for (MomentModel moment : yearSortedResults) {
            years.add(String.valueOf(moment.getYear()));
        }

        // close database
        realm.close();

        // return set of years
        return years;
    }


    public RealmResults<MomentModel> searchEntries(SearchPreference searchPreference) {
        /*
        Inserts a new row into the database with the passed in data
         */

        Realm realm = Realm.getDefaultInstance();

        // Build the query looking at all users:
        RealmQuery<MomentModel> query = realm.where(MomentModel.class);
        // if we have a month filter, add that to the query
        if (searchPreference.getMonth() != null) {
            query.equalTo("month", searchPreference.getMonth());
        }
        // if we have a year filter, add that to the query
        if (searchPreference.getYear() != null) {
            query.equalTo("year", searchPreference.getYear());
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


    public void insertNewMomentIntoDB(final String title, final String photoUri, final Long date, final String month, final int year) {
        /*
        Inserts a new row into the database with the passed in data
         */

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // create new row
                MomentModel moment = realm.createObject(MomentModel.class);
                // populate row with data
                moment.setAllFields(getNextPrimaryKeyValue(), title, photoUri, date, month, year);
            }
        });
        // close database
        realm.close();
    }


    public void updateRow(final int id, final String title, final String photoUri, final Long date, final String month, final int year) {
        /*
        Updates a row with the specified data
         */

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // get the row with the specified id
                MomentModel moment = realm.where(MomentModel.class).equalTo("id", id).findFirst();
                // update its fields
                moment.setAllFields(title, photoUri, date, month, year);

            }
        });
        // close database
        realm.close();
    }


    public void deleteRow(final int id) {
        /*
        Deletes a row from the database with the specified id
         */

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // get the row with the specified id
                MomentModel moment = realm.where(MomentModel.class).equalTo("id", id).findFirst();
                // delete the row
                moment.deleteFromRealm();

            }
        });
        // close database
        realm.close();
    }


    public void deleteAllEntries() {
        /*
        Deletes everything
         */

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


    private int getNextPrimaryKeyValue() {
        /*
        Get the next available primary key value for row insertion
         */

        Realm realm = Realm.getDefaultInstance();
        Number curr_max = realm.where(MomentModel.class).findAll().max("id");
        int new_max = 1;

        // if num is not null (meaning we actually have a row), our primary key should have value 1
        if (curr_max != null) {
            new_max = curr_max.intValue();
            new_max++;
        }
        return new_max;
    }

}








