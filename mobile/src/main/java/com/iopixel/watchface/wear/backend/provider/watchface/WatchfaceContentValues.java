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

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.iopixel.watchface.wear.backend.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code watchface} table.
 */
public class WatchfaceContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return WatchfaceColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable WatchfaceSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable WatchfaceSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Public id of the watchface, which is used to build file names (e.g. "xyw_superwf_v2").
     */
    public WatchfaceContentValues putPublicId(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("publicId must not be null");
        mContentValues.put(WatchfaceColumns.PUBLIC_ID, value);
        return this;
    }


    /**
     * Display name (e.g. "Super Duper Watchface v2").
     */
    public WatchfaceContentValues putDisplayName(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("displayName must not be null");
        mContentValues.put(WatchfaceColumns.DISPLAY_NAME, value);
        return this;
    }


    /**
     * Is it the currently selected watchface?<br/>At any time, the value should be {@code true} for one and only one row.
     */
    public WatchfaceContentValues putIsSelected(boolean value) {
        mContentValues.put(WatchfaceColumns.IS_SELECTED, value);
        return this;
    }


    /**
     * When was the watchface installed.
     */
    public WatchfaceContentValues putInstallDate(@NonNull Date value) {
        if (value == null) throw new IllegalArgumentException("installDate must not be null");
        mContentValues.put(WatchfaceColumns.INSTALL_DATE, value.getTime());
        return this;
    }


    public WatchfaceContentValues putInstallDate(long value) {
        mContentValues.put(WatchfaceColumns.INSTALL_DATE, value);
        return this;
    }

    /**
     * Is this watchface a 'bundled' one?  If yes it cannot be deleted?
     */
    public WatchfaceContentValues putIsBundled(boolean value) {
        mContentValues.put(WatchfaceColumns.IS_BUNDLED, value);
        return this;
    }

}
