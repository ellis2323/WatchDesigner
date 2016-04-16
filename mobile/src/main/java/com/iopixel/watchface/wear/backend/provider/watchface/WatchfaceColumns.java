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
package com.iopixel.watchface.wear.backend.provider.watchface;

import android.net.Uri;
import android.provider.BaseColumns;

import com.iopixel.watchface.wear.backend.provider.WatchDesignerProvider;

/**
 * Watchface.
 */
public class WatchfaceColumns implements BaseColumns {
    public static final String TABLE_NAME = "watchface";
    public static final Uri CONTENT_URI = Uri.parse(WatchDesignerProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    /**
     * Id of the watchface, which is used to build file names (e.g. "xyw_superwf_v2").
     */
    public static final String ID = "id";

    /**
     * Display name (e.g. "Super Duper Watchface v2").
     */
    public static final String DISPLAY_NAME = "display_name";

    /**
     * Is it the currently selected watchface?<br/>At any time, the value should be {@code true} for one and only one row.
     */
    public static final String IS_SELECTED = "is_selected";

    /**
     * When was the watchface installed.
     */
    public static final String INSTALL_DATE = "install_date";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            ID,
            DISPLAY_NAME,
            IS_SELECTED,
            INSTALL_DATE
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(ID) || c.contains("." + ID)) return true;
            if (c.equals(DISPLAY_NAME) || c.contains("." + DISPLAY_NAME)) return true;
            if (c.equals(IS_SELECTED) || c.contains("." + IS_SELECTED)) return true;
            if (c.equals(INSTALL_DATE) || c.contains("." + INSTALL_DATE)) return true;
        }
        return false;
    }

}
