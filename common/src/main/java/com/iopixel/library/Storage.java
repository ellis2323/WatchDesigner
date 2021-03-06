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
package com.iopixel.library;

import java.io.File;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.jraf.android.util.log.Log;

public class Storage {
    private static final String PATH_GWD = "gwd";
    public static final String PREFIX_GWD = ".gwd";

    public static String getAPKFileName(Context ctx) {
        try {
            ApplicationInfo appInfo = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), 0);
            return appInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(e, "Error when locating the apk filename");
        }
        return "";
    }

    public static String getInternalStoragePath(Context ctx) {
        if (ctx.getFilesDir() == null) {
            Log.e("error, getFilesDir is null");
        }

        if (!ctx.getFilesDir().exists()) {
            ctx.getFilesDir().mkdir();
        }
        return ctx.getFilesDir().getAbsolutePath();
    }

    public static File getGwdStorageFile(Context ctx) {
        File result = null;
        File[] files = ctx.getExternalFilesDirs(PATH_GWD);
        for (File f: files) {
            if (f.exists()) {
                result = f;
                break;
            }
        }
        return result;
    }

    public static File getGwdStorageFile(Context ctx, String fileName) {
        File gwdStorage = getGwdStorageFile(ctx);
        return new File(gwdStorage, fileName);
    }

    public static File getPreviewImageFile(Context ctx, String publicId) {
        return getGwdStorageFile(ctx, publicId + ".png");
    }

    public static File getGwdFile(Context ctx, String publicId) {
        return getGwdStorageFile(ctx, publicId + ".gwd");
    }

    public static File getInternalGwdFile(Context ctx, String publicId) {
        return new File(ctx.getFilesDir(), publicId + PREFIX_GWD);
    }
}
