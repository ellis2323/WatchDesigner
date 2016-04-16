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
import android.database.Cursor;
import android.net.Uri;

import com.iopixel.watchface.wear.backend.provider.base.AbstractSelection;

/**
 * Selection for the {@code watchface} table.
 */
public class WatchfaceSelection extends AbstractSelection<WatchfaceSelection> {
    @Override
    protected Uri baseUri() {
        return WatchfaceColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code WatchfaceCursor} object, which is positioned before the first entry, or null.
     */
    public WatchfaceCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new WatchfaceCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public WatchfaceCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code WatchfaceCursor} object, which is positioned before the first entry, or null.
     */
    public WatchfaceCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new WatchfaceCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public WatchfaceCursor query(Context context) {
        return query(context, null);
    }


    public WatchfaceSelection id(long... value) {
        addEquals("watchface." + WatchfaceColumns._ID, toObjectArray(value));
        return this;
    }

    public WatchfaceSelection idNot(long... value) {
        addNotEquals("watchface." + WatchfaceColumns._ID, toObjectArray(value));
        return this;
    }

    public WatchfaceSelection orderById(boolean desc) {
        orderBy("watchface." + WatchfaceColumns._ID, desc);
        return this;
    }

    public WatchfaceSelection orderById() {
        return orderById(false);
    }

    public WatchfaceSelection publicId(String... value) {
        addEquals(WatchfaceColumns.PUBLIC_ID, value);
        return this;
    }

    public WatchfaceSelection publicIdNot(String... value) {
        addNotEquals(WatchfaceColumns.PUBLIC_ID, value);
        return this;
    }

    public WatchfaceSelection publicIdLike(String... value) {
        addLike(WatchfaceColumns.PUBLIC_ID, value);
        return this;
    }

    public WatchfaceSelection publicIdContains(String... value) {
        addContains(WatchfaceColumns.PUBLIC_ID, value);
        return this;
    }

    public WatchfaceSelection publicIdStartsWith(String... value) {
        addStartsWith(WatchfaceColumns.PUBLIC_ID, value);
        return this;
    }

    public WatchfaceSelection publicIdEndsWith(String... value) {
        addEndsWith(WatchfaceColumns.PUBLIC_ID, value);
        return this;
    }

    public WatchfaceSelection orderByPublicId(boolean desc) {
        orderBy(WatchfaceColumns.PUBLIC_ID, desc);
        return this;
    }

    public WatchfaceSelection orderByPublicId() {
        orderBy(WatchfaceColumns.PUBLIC_ID, false);
        return this;
    }

    public WatchfaceSelection displayName(String... value) {
        addEquals(WatchfaceColumns.DISPLAY_NAME, value);
        return this;
    }

    public WatchfaceSelection displayNameNot(String... value) {
        addNotEquals(WatchfaceColumns.DISPLAY_NAME, value);
        return this;
    }

    public WatchfaceSelection displayNameLike(String... value) {
        addLike(WatchfaceColumns.DISPLAY_NAME, value);
        return this;
    }

    public WatchfaceSelection displayNameContains(String... value) {
        addContains(WatchfaceColumns.DISPLAY_NAME, value);
        return this;
    }

    public WatchfaceSelection displayNameStartsWith(String... value) {
        addStartsWith(WatchfaceColumns.DISPLAY_NAME, value);
        return this;
    }

    public WatchfaceSelection displayNameEndsWith(String... value) {
        addEndsWith(WatchfaceColumns.DISPLAY_NAME, value);
        return this;
    }

    public WatchfaceSelection orderByDisplayName(boolean desc) {
        orderBy(WatchfaceColumns.DISPLAY_NAME, desc);
        return this;
    }

    public WatchfaceSelection orderByDisplayName() {
        orderBy(WatchfaceColumns.DISPLAY_NAME, false);
        return this;
    }

    public WatchfaceSelection isSelected(boolean value) {
        addEquals(WatchfaceColumns.IS_SELECTED, toObjectArray(value));
        return this;
    }

    public WatchfaceSelection orderByIsSelected(boolean desc) {
        orderBy(WatchfaceColumns.IS_SELECTED, desc);
        return this;
    }

    public WatchfaceSelection orderByIsSelected() {
        orderBy(WatchfaceColumns.IS_SELECTED, false);
        return this;
    }

    public WatchfaceSelection installDate(Date... value) {
        addEquals(WatchfaceColumns.INSTALL_DATE, value);
        return this;
    }

    public WatchfaceSelection installDateNot(Date... value) {
        addNotEquals(WatchfaceColumns.INSTALL_DATE, value);
        return this;
    }

    public WatchfaceSelection installDate(long... value) {
        addEquals(WatchfaceColumns.INSTALL_DATE, toObjectArray(value));
        return this;
    }

    public WatchfaceSelection installDateAfter(Date value) {
        addGreaterThan(WatchfaceColumns.INSTALL_DATE, value);
        return this;
    }

    public WatchfaceSelection installDateAfterEq(Date value) {
        addGreaterThanOrEquals(WatchfaceColumns.INSTALL_DATE, value);
        return this;
    }

    public WatchfaceSelection installDateBefore(Date value) {
        addLessThan(WatchfaceColumns.INSTALL_DATE, value);
        return this;
    }

    public WatchfaceSelection installDateBeforeEq(Date value) {
        addLessThanOrEquals(WatchfaceColumns.INSTALL_DATE, value);
        return this;
    }

    public WatchfaceSelection orderByInstallDate(boolean desc) {
        orderBy(WatchfaceColumns.INSTALL_DATE, desc);
        return this;
    }

    public WatchfaceSelection orderByInstallDate() {
        orderBy(WatchfaceColumns.INSTALL_DATE, false);
        return this;
    }
}
