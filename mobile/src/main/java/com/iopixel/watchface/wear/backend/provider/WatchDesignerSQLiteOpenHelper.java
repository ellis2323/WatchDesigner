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
 * imitations under the License.
 */
package com.iopixel.watchface.wear.backend.provider;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.DefaultDatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.iopixel.watchface.wear.BuildConfig;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceColumns;

public class WatchDesignerSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = WatchDesignerSQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_FILE_NAME = "watchdesigner.db";
    private static final int DATABASE_VERSION = 1;
    private static WatchDesignerSQLiteOpenHelper sInstance;
    private final Context mContext;
    private final WatchDesignerSQLiteOpenHelperCallbacks mOpenHelperCallbacks;

    // @formatter:off
    public static final String SQL_CREATE_TABLE_WATCHFACE = "CREATE TABLE IF NOT EXISTS "
            + WatchfaceColumns.TABLE_NAME + " ( "
            + WatchfaceColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WatchfaceColumns.PUBLIC_ID + " TEXT NOT NULL, "
            + WatchfaceColumns.DISPLAY_NAME + " TEXT NOT NULL, "
            + WatchfaceColumns.IS_SELECTED + " INTEGER NOT NULL DEFAULT 0, "
            + WatchfaceColumns.INSTALL_DATE + " INTEGER NOT NULL "
            + ", CONSTRAINT unique_public_id UNIQUE (public_id) ON CONFLICT REPLACE"
            + " );";

    // @formatter:on

    public static WatchDesignerSQLiteOpenHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = newInstance(context.getApplicationContext());
        }
        return sInstance;
    }

    private static WatchDesignerSQLiteOpenHelper newInstance(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return newInstancePreHoneycomb(context);
        }
        return newInstancePostHoneycomb(context);
    }


    /*
     * Pre Honeycomb.
     */
    private static WatchDesignerSQLiteOpenHelper newInstancePreHoneycomb(Context context) {
        return new WatchDesignerSQLiteOpenHelper(context);
    }

    private WatchDesignerSQLiteOpenHelper(Context context) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
        mContext = context;
        mOpenHelperCallbacks = new WatchDesignerSQLiteOpenHelperCallbacks();
    }


    /*
     * Post Honeycomb.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static WatchDesignerSQLiteOpenHelper newInstancePostHoneycomb(Context context) {
        return new WatchDesignerSQLiteOpenHelper(context, new DefaultDatabaseErrorHandler());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private WatchDesignerSQLiteOpenHelper(Context context, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION, errorHandler);
        mContext = context;
        mOpenHelperCallbacks = new WatchDesignerSQLiteOpenHelperCallbacks();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        mOpenHelperCallbacks.onPreCreate(mContext, db);
        db.execSQL(SQL_CREATE_TABLE_WATCHFACE);
        mOpenHelperCallbacks.onPostCreate(mContext, db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            setForeignKeyConstraintsEnabled(db);
        }
        mOpenHelperCallbacks.onOpen(mContext, db);
    }

    private void setForeignKeyConstraintsEnabled(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setForeignKeyConstraintsEnabledPreJellyBean(db);
        } else {
            setForeignKeyConstraintsEnabledPostJellyBean(db);
        }
    }

    private void setForeignKeyConstraintsEnabledPreJellyBean(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setForeignKeyConstraintsEnabledPostJellyBean(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mOpenHelperCallbacks.onUpgrade(mContext, db, oldVersion, newVersion);
    }
}
