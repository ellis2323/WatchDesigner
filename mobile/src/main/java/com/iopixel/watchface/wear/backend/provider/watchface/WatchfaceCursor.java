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
package com.iopixel.watchface.wear.backend.provider.watchface;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.iopixel.watchface.wear.backend.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code watchface} table.
 */
public class WatchfaceCursor extends AbstractCursor implements WatchfaceModel {
    public WatchfaceCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    @Override
    public long getId() {
        Long res = getLongOrNull(WatchfaceColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Public id of the watchface, which is used to build file names (e.g. "xyw_superwf_v2").
     * Cannot be {@code null}.
     */
    @Override
    @NonNull
    public String getPublicId() {
        String res = getStringOrNull(WatchfaceColumns.PUBLIC_ID);
        if (res == null)
            throw new NullPointerException("The value of 'public_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Display name (e.g. "Super Duper Watchface v2").
     * Cannot be {@code null}.
     */
    @Override
    @NonNull
    public String getDisplayName() {
        String res = getStringOrNull(WatchfaceColumns.DISPLAY_NAME);
        if (res == null)
            throw new NullPointerException("The value of 'display_name' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Is it the currently selected watchface?<br/>At any time, the value should be {@code true} for one and only one row.
     */
    @Override
    public boolean getIsSelected() {
        Boolean res = getBooleanOrNull(WatchfaceColumns.IS_SELECTED);
        if (res == null)
            throw new NullPointerException("The value of 'is_selected' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * When was the watchface installed.
     * Cannot be {@code null}.
     */
    @Override
    @NonNull
    public Date getInstallDate() {
        Date res = getDateOrNull(WatchfaceColumns.INSTALL_DATE);
        if (res == null)
            throw new NullPointerException("The value of 'install_date' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Is this watchface a 'bundled' one?  If yes it cannot be deleted?
     */
    @Override
    public boolean getIsBundled() {
        Boolean res = getBooleanOrNull(WatchfaceColumns.IS_BUNDLED);
        if (res == null)
            throw new NullPointerException("The value of 'is_bundled' in the database was null, which is not allowed according to the model definition");
        return res;
    }
}
