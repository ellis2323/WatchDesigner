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
package com.iopixel.watchface.wear.library.install;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.Log;

import com.iopixel.library.Storage;
import com.iopixel.library.Wear;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceContentValues;
import com.iopixel.watchface.wear.library.FileUtil;
import com.iopixel.watchface.wear.library.GWDReader;
import com.iopixel.watchface.wear.prefs.MainPrefs;

public class InstallUtil {
    public static void installExternalFile(Context context, File externalFile) {
        String fileName = externalFile.getName();
        File gwdStorageFile = Storage.getGwdStorageFile(context, fileName);
        try {
            org.jraf.android.util.file.FileUtil.copy(externalFile, gwdStorageFile);
        } catch (IOException e) {
            Log.w(e, "Could not copy file");
            EventBus.getDefault().post(new InstallFailedEvent(InstallFailedEvent.Reason.CANNOT_COPY_TO_GWD_STORAGE, fileName));
            return;
        }
        installGwdStorageFile(context, fileName, gwdStorageFile);
    }

    public static void installInputStream(Context context, InputStream inputStream) {
        String fileName = getUniqueFileName(context);
        File gwdStorageFile = Storage.getGwdStorageFile(context, fileName);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(gwdStorageFile);
            IoUtil.copy(inputStream, fileOutputStream);
        } catch (IOException e) {
            Log.w(e, "Could not copy input");
            EventBus.getDefault().post(new InstallFailedEvent(InstallFailedEvent.Reason.CANNOT_COPY_TO_GWD_STORAGE, fileName));
            return;
        } finally {
            IoUtil.closeSilently(inputStream, fileOutputStream);
        }
        installGwdStorageFile(context, fileName, gwdStorageFile);
    }

    private static String getUniqueFileName(Context context) {
        MainPrefs mainPrefs = MainPrefs.get(context);
        int uniqueId = mainPrefs.getUniqueId();
        mainPrefs.putUniqueId(uniqueId + 1);
        return String.format(Locale.US, "watchface-%d.gwd", uniqueId);
    }

    private static void installGwdStorageFile(Context context, String fileName, File gwdStorageFile) {
        // Extract icon and return watchface name
        String watchfaceName = GWDReader.loadGWD(gwdStorageFile);
        if (watchfaceName == null) {
            Log.w("Could not extract the watchface name: give up");
            EventBus.getDefault().post(new InstallFailedEvent(InstallFailedEvent.Reason.CANNOT_READ_GWD, fileName));
            return;
        }

        // Insert into content provider
        String publicId = insert(context, gwdStorageFile, watchfaceName);

        // Send the file to the watch
        Wear.sendAFile(gwdStorageFile);

        EventBus.getDefault().post(new InstallSuccessEvent(publicId));
    }

    private static String insert(Context context, File gwdFile, String watchfaceName) {
        // First, deselect any previously selected watchface
        WatchfaceContentValues values = new WatchfaceContentValues();
        values.putIsSelected(false);
        values.update(context, null);

        // Now insert the new watchface
        values.putDisplayName(watchfaceName);
        String publicId = FileUtil.removeExtension(gwdFile);
        values.putPublicId(publicId);
        values.putInstallDate(System.currentTimeMillis());
        values.putIsSelected(true);
        values.insert(context);

        return publicId;
    }
}
