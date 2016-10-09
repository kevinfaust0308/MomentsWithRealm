package com.monsoonblessing.moments.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.monsoonblessing.moments.DatabaseHelper;
import com.monsoonblessing.moments.R;

/**
 * Created by Kevin on 2016-06-23.
 */
public class BaseActivity extends AppCompatActivity {

    protected Toolbar mToolbar;
    protected DatabaseHelper dbHelper;

    protected Toolbar activateToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        }
        setSupportActionBar(mToolbar);
        return mToolbar;
    }

    protected Toolbar activateToolbarWithHomeEnabled() {
        activateToolbar();
        if (mToolbar!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return mToolbar;
    }

}
