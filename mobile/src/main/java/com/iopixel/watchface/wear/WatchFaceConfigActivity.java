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
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**

 */
public class WatchFaceConfigActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    private void InputStreamToFile(InputStream in, String file) {
        try {
            OutputStream out = new FileOutputStream(new File(file));

            int size = 0;
            byte[] buffer = new byte[1024];

            while ((size = in.read(buffer)) != -1) {
                out.write(buffer, 0, size);
            }

            out.close();
        }
        catch (Exception e) {
            Log.e("MainActivity", "InputStreamToFile exception: " + e.getMessage());
        }
    }
}
