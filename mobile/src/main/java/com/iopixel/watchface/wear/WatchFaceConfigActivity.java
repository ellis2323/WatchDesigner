/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iopixel.watchface.wear;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.iopixel.library.Native;
import com.iopixel.library.Storage;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**

 */
public class WatchFaceConfigActivity extends Activity {

    public static final String TAG = "iopixel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Native.InitWithNullDriver(Storage.getAPKFileName(this), Storage.getInternalStoragePath(this));

        try {
            UnzipAssets("data/text.gwd", "toto");
            File outputDir = getOutputDir("toto");
            Native.CreateWD(outputDir.getAbsolutePath());
            deletePngInDirectory(outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        String action = intent.getAction();

        if (action.compareTo(Intent.ACTION_VIEW) == 0) {
            String scheme = intent.getScheme();
            ContentResolver resolver = getContentResolver();

            if (scheme.compareTo(ContentResolver.SCHEME_CONTENT) == 0) {
                Uri uri = intent.getData();
                String name = getContentName(resolver, uri);

                Log.v("tag" , "Content intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);

            }
            else if (scheme.compareTo(ContentResolver.SCHEME_FILE) == 0) {
                Uri uri = intent.getData();
                String name = uri.getLastPathSegment();

                Log.v("tag" , "File intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);

            }
            else if (scheme.compareTo("http") == 0) {
                // TODO Import from HTTP!
            }
            else if (scheme.compareTo("ftp") == 0) {
                // TODO Import from FTP!
            }
        }

    }

    private String getContentName(ContentResolver resolver, Uri uri){
        Cursor cursor = resolver.query(uri, null, null, null, null);
        cursor.moveToFirst();
        int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
        if (nameIndex >= 0) {
            return cursor.getString(nameIndex);
        } else {
            return null;
        }
    }


    private boolean UnzipAssets(String filepath, String dirName) throws IOException {
        File outputDir = getOutputDir(dirName);
        FileUtils.deleteDirectory(outputDir);
        outputDir.mkdir();
        // create spritesheets
        File sps = new File(outputDir.getAbsolutePath() + File.separator + "spritesheets");
        sps.mkdir();
        AssetManager am = getAssets();
        InputStream is = am.open(filepath);
        ZipUtil.unpack(is, outputDir);
        return true;
    }

    private File getOutputDir(String dirName) {
        String basepath = getFilesDir().getAbsolutePath();
        String dirpath = basepath + File.separator + dirName;
        return new File(dirpath);
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }

    private void deletePngInDirectory(File dir) {
        String[] exts = { "png" };
        Iterator it = FileUtils.iterateFiles(dir, exts, true);
        while (it.hasNext()) {
            File file = (File) it.next();
            if (file != null) {
                file.delete();
            }
        }

    }



}
