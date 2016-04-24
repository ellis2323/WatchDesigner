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
import java.io.IOException;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;
import android.widget.Toast;

import org.jraf.android.util.handler.HandlerUtil;
import org.jraf.android.util.log.Log;
import org.jraf.android.util.log.LogUtil;

import com.iopixel.library.Storage;
import com.iopixel.library.Wear;
import com.iopixel.watchface.wear.R;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceContentValues;
import com.iopixel.watchface.wear.library.FileUtil;
import com.iopixel.watchface.wear.library.GWDReader;

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
                    onSuccess(context, description, downloadedFilePath);
                    break;
            }
        } finally {
            cursor.close();
        }
    }

    private void onFail(Context context, String description) {
        Log.d("description=%s", description);
        String toastText = context.getString(R.string.download_failToast_download, description);
        showToast(context, toastText);
    }

    private void onSuccess(Context context, String description, String downloadedFilePath) {
        Log.d("downloadedFilePath=%s", downloadedFilePath);
        // Copy the downloaded file to the storage
        File downloadedFile = new File(downloadedFilePath);
        String fileName = downloadedFile.getName();
        File destination = Storage.getGwdStorageFile(context, fileName);
        try {
            org.jraf.android.util.file.FileUtil.copy(downloadedFile, destination);
        } catch (IOException e) {
            Log.w(e, "Could not copy file");
            String toastText = context.getString(R.string.download_failToast_install, fileName);
            showToast(context, toastText);
            return;
        }
        // Extract icon and return watchface name
        String watchfaceName = GWDReader.loadGWD(destination);
        if (watchfaceName == null) {
            Log.w("Could not extract the icon: give up");
            String toastText = context.getString(R.string.download_failToast_extract, description);
            showToast(context, toastText);
            return;
        }

        // Insert into content provider
        insert(context, destination, watchfaceName);

        // Send the file to the watch
        Wear.sendAFile(destination);

        // Success toast
        String toastText = context.getString(R.string.download_successToast, fileName);
        showToast(context, toastText);
    }

    private void insert(Context context, File gwdFile, String watchfaceName) {
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

    private void showToast(final Context context, final String toastText) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
            }
        });
    }
}
