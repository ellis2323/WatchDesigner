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
package com.iopixel.watchface.wear.library;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.Log;

import com.iopixel.library.Storage;
import com.iopixel.library.Wear;
import com.iopixel.watchface.wear.R;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceContentValues;
import com.iopixel.watchface.wear.prefs.MainPrefs;

public class InstallUtil {
    private static final String PREFIX = InstallUtil.class.getName() + ".";
    public static final String ACTION_SHOW_INFO = PREFIX + "ACTION_SHOW_INFO";
    public static final String EXTRA_INFO_TEXT = PREFIX + "EXTRA_INFO_TEXT";

    public static void showInfo(Context context, String infoText) {
        Intent intent = new Intent(ACTION_SHOW_INFO).putExtra(EXTRA_INFO_TEXT, infoText);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void installExternalFile(Context context, File externalFile) {
        String fileName = externalFile.getName();
        File destinationFile = Storage.getGwdStorageFile(context, fileName);
        try {
            org.jraf.android.util.file.FileUtil.copy(externalFile, destinationFile);
        } catch (IOException e) {
            Log.w(e, "Could not copy file");
            String infoText = context.getString(R.string.download_fail_install, fileName);
            showInfo(context, infoText);
            return;
        }
        installInternalFile(context, fileName, destinationFile);
    }

    public static void installInputStream(Context context, InputStream inputStream) {
        String fileName = getUniqueFileName(context);

        File destinationFile = Storage.getGwdStorageFile(context, fileName);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(destinationFile);
            IoUtil.copy(inputStream, fileOutputStream);
        } catch (IOException e) {
            Log.w(e, "Could not copy input");
            String infoText = context.getString(R.string.download_fail_install, fileName);
            showInfo(context, infoText);
            return;
        } finally {
            IoUtil.closeSilently(inputStream, fileOutputStream);
        }
        installInternalFile(context, fileName, destinationFile);
    }

    private static String getUniqueFileName(Context context) {
        MainPrefs mainPrefs = MainPrefs.get(context);
        int uniqueId = mainPrefs.getUniqueId();
        mainPrefs.putUniqueId(uniqueId + 1);
        return String.format(Locale.US, "watchface-%d.gwd", uniqueId);
    }

    private static void installInternalFile(Context context, String fileName, File destination) {
        // Extract icon and return watchface name
        String watchfaceName = GWDReader.loadGWD(destination);
        if (watchfaceName == null) {
            Log.w("Could not extract the watchface name: give up");
            String infoText = context.getString(R.string.download_fail_extract, fileName);
            showInfo(context, infoText);
            return;
        }

        // Insert into content provider
        insert(context, destination, watchfaceName);

        // Send the file to the watch
        Wear.sendAFile(destination);

        // Success toast
        String infoText = context.getString(R.string.download_success, fileName);
        showInfo(context, infoText);
    }

    private static void insert(Context context, File gwdFile, String watchfaceName) {
        // First, deselect any previously selected watchface
        WatchfaceContentValues values = new WatchfaceContentValues();
        values.putIsSelected(false);
        values.update(context, null);

        // Now insert the new watchface
        values.putDisplayName(watchfaceName);
        String id = FileUtil.removeExtension(gwdFile);
        values.putPublicId(id);
        values.putInstallDate(System.currentTimeMillis());
        values.putIsSelected(true);
        values.insert(context);
    }
}
