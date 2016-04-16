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
package com.iopixel.watchface.wear.app.download;

import android.app.Activity;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import org.jraf.android.util.log.Log;

import com.iopixel.watchface.wear.R;

public class DownloadActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();
        Log.d("uri=%s", uri);
        String fileName = uri.getLastPathSegment();

        if (fileName == null) {
            // Handle special case for gearfaces.com
            if (uri.toString().contains("wpdmdl")) {
                // Use a generic file name, because we don't have a real one
                fileName = "watchface.gwd";
            } else {
                // This is not a download link: silently ignore
                finish();
                return;
            }
        }

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(getString(R.string.app_name));
        request.setDescription(getString(R.string.download_description, fileName));
        request.setVisibleInDownloadsUi(false);
        downloadManager.enqueue(request);

        // Show a toast
        Toast.makeText(this, getString(R.string.download_startedToast, fileName), Toast.LENGTH_LONG).show();

        finish();
    }
}
