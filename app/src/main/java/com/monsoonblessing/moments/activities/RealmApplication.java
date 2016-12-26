package com.monsoonblessing.moments.activities;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by Kevin on 2016-12-25.
 */

public class RealmApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
