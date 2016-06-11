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

import java.io.File;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;

import org.jraf.android.util.log.Log;
import org.jraf.android.util.log.LogUtil;

import com.iopixel.watchface.wear.R;
import com.iopixel.watchface.wear.library.InstallUtil;

public class DownloadBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            final long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            // Do the work in a background thread because it does disk i/o
            final PendingResult result = goAsync();
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    handleDownloadComplete(context, downloadId);
                    result.finish();
                    return null;
                }
            }.execute();
        }
    }

    @WorkerThread
    private void handleDownloadComplete(Context context, long downloadId) {
        Log.d("downloadId=%s", downloadId);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        try {
            cursor.moveToFirst();
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            Log.d("status=%s", LogUtil.getConstantName(DownloadManager.class, status, "STATUS"));
            String description = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));
            switch (status) {
                case DownloadManager.STATUS_FAILED:
                    onFail(context, description);
                    break;

                case DownloadManager.STATUS_SUCCESSFUL:
                    String downloadedFilePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    onSuccess(context, downloadedFilePath);
                    break;
            }
        } finally {
            cursor.close();
        }
    }

    private void onFail(Context context, String description) {
        Log.d("description=%s", description);
        String infoText = context.getString(R.string.download_fail_download, description);
        InstallUtil.showInfo(context, infoText);
    }

    private void onSuccess(Context context, String downloadedFilePath) {
        Log.d("downloadedFilePath=%s", downloadedFilePath);
        // Copy the downloaded file to the storage
        File downloadedFile = new File(downloadedFilePath);
        InstallUtil.installExternalFile(context, downloadedFile);
    }
}
