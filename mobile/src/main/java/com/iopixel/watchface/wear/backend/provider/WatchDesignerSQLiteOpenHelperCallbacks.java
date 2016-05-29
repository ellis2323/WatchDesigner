/*
 * Copyright (C) 2016 The WatchDesigner team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iopixel.watchface.wear.backend.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.iopixel.library.Bundled;
import com.iopixel.watchface.wear.BuildConfig;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceColumns;

/**
 * Implement your custom database creation or upgrade code here.
 *
 * This file will not be overwritten if you re-run the content provider generator.
 */
public class WatchDesignerSQLiteOpenHelperCallbacks {
    private static final String TAG = WatchDesignerSQLiteOpenHelperCallbacks.class.getSimpleName();

    private static final String WF_BUNDLED_0_DISPLAY_NAME = "Basic 1";
    private static final String INSERT_BUNDLED_WF_0 = "INSERT INTO "
            + WatchfaceColumns.TABLE_NAME
            + " ( "
            + WatchfaceColumns.PUBLIC_ID + ", "
            + WatchfaceColumns.DISPLAY_NAME + ", "
            + WatchfaceColumns.IS_SELECTED + ", "
            + WatchfaceColumns.INSTALL_DATE + ", "
            + WatchfaceColumns.IS_BUNDLED
            + " ) "
            + " VALUES "
            + " ( "
            + "'" + Bundled.WF_BUNDLED_0_PUBLIC_ID + "', "
            + "'" + WF_BUNDLED_0_DISPLAY_NAME + "', "
            + "1" + ", "
            + System.currentTimeMillis() + ", "
            + "1"
            + " ) ";

    private static final String WF_BUNDLED_1_DISPLAY_NAME = "Basic 2";
    private static final String INSERT_BUNDLED_WF_1 = "INSERT INTO "
            + WatchfaceColumns.TABLE_NAME
            + " ( "
            + WatchfaceColumns.PUBLIC_ID + ", "
            + WatchfaceColumns.DISPLAY_NAME + ", "
            + WatchfaceColumns.IS_SELECTED + ", "
            + WatchfaceColumns.INSTALL_DATE + ", "
            + WatchfaceColumns.IS_BUNDLED
            + " ) "
            + " VALUES "
            + " ( "
            + "'" + Bundled.WF_BUNDLED_1_PUBLIC_ID + "', "
            + "'" + WF_BUNDLED_1_DISPLAY_NAME + "', "
            + "0" + ", "
            + (System.currentTimeMillis() + 1) + ", "
            + "1"
            + " ) ";


    public void onOpen(Context context, SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onOpen");
        // Insert your db open code here.
    }

    public void onPreCreate(Context context, SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPreCreate");
        // Insert your db creation code here. This is called before your tables are created.
    }

    public void onPostCreate(Context context, SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPostCreate");
        // Insert special 'bundled' watchfaces.
        db.execSQL(INSERT_BUNDLED_WF_0);
        db.execSQL(INSERT_BUNDLED_WF_1);
    }

    public void onUpgrade(Context context, SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        // Insert your upgrading code here.
    }
}
