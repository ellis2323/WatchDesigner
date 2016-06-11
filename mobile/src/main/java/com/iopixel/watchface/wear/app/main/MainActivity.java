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
package com.iopixel.watchface.wear.app.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.jraf.android.util.about.AboutActivityIntentBuilder;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.dialog.AlertDialogListener;
import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.Log;
import org.jraf.android.util.string.StringUtil;

import com.google.android.gms.wearable.MessageEvent;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;
import com.iopixel.library.Bundled;
import com.iopixel.library.Storage;
import com.iopixel.library.Wear;
import com.iopixel.watchface.wear.BuildConfig;
import com.iopixel.watchface.wear.R;
import com.iopixel.watchface.wear.app.main.grid.WatchfaceGridFragment;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceContentValues;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceSelection;
import com.iopixel.watchface.wear.databinding.MainBinding;
import com.iopixel.watchface.wear.library.GWDReader;
import com.iopixel.watchface.wear.library.InstallUtil;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements WatchfaceCallbacks, ActionMode.Callback, AlertDialogListener {
    private static final int DIALOG_DELETE_CONFIRM = 0;

    MainBinding mBinding;
    private String mGwdToSendPublicId;
    ActionMode mActionMode;
    private WatchfaceGridFragment mWatchfaceGridFragment;
    private boolean mInfoBroadcastReceiverRegistered;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("intent=%s", StringUtil.toString(getIntent()));
        mBinding = DataBindingUtil.setContentView(this, R.layout.main);
        WearManager.getInstance().addWearConsumer(mWearConsumer);

        registerInfoBroadcastReceiver();

        // Check if the bundled watchfaces are installed (first time case)
        checkForBundledWatchfaces();

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            // Called from link: start a download or use the provided file
            handleViewAction(getIntent());

            // Forget this action, to not trigger the download again when the activity is recreated
            setIntent(getIntent().setAction(Intent.ACTION_MAIN));
        }
    }

    private void handleViewAction(Intent intent) {
        Uri uri = intent.getData();
        Log.d("uri=%s", uri);
        String scheme = uri.getScheme();
        Log.d("scheme=%s", scheme);

        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            // Download the file with the Download Manager - it will be installed when the download ends
            String fileName = uri.getLastPathSegment();
            if (fileName == null) {
                // Handle special case for gearfaces.com
                if (uri.toString().contains("wpdmdl")) {
                    // Use a generic file name, because we don't have a real one
                    fileName = "watchface.gwd";
                } else {
                    // This is not a download link: silently ignore
                    return;
                }
            }

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(getString(R.string.app_name));
            request.setDescription(getString(R.string.download_description, fileName));
            request.setVisibleInDownloadsUi(false);
            downloadManager.enqueue(request);

            // Show a snackbar
            Snackbar.make(mBinding.getRoot(), getString(R.string.main_download_started, fileName), Snackbar.LENGTH_LONG).show();
        } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(scheme)) {
            // File case: use it directly
            MainActivityPermissionsDispatcher.handleViewActionWithFileWithCheck(this, uri);
        } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(scheme)) {
            InputStream inputStream;
            try {
                inputStream = getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                Log.w(e, "Could not open %s", uri);
                // Silently ignore
                return;
            }
            InstallUtil.installInputStream(this, inputStream);
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void handleViewActionWithFile(Uri uri) {
        File file = new File(uri.getPath());
        Log.d("file=%s", file);
        InstallUtil.installExternalFile(this, file);
    }


    //
    //region Permissions.
    //

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        // Show a snackbar
        Snackbar.make(mBinding.getRoot(), R.string.main_permissionDenied_readExternalStorage, Snackbar.LENGTH_LONG).show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        // Show a snackbar
        Snackbar.make(mBinding.getRoot(), R.string.main_permissionDenied_readExternalStorage, Snackbar.LENGTH_LONG).show();
    }

    //endregion


    //
    //region Install info broadcast receiver.
    //

    private void registerInfoBroadcastReceiver() {
        if (!mInfoBroadcastReceiverRegistered) {
            IntentFilter filter = new IntentFilter(InstallUtil.ACTION_SHOW_INFO);
            LocalBroadcastManager.getInstance(this).registerReceiver(mInfoBroadcastReceiver, filter);
            mInfoBroadcastReceiverRegistered = true;
        }
    }

    private void unregisterInfoBroadcastReceiver() {
        if (mInfoBroadcastReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mInfoBroadcastReceiver);
            mInfoBroadcastReceiverRegistered = false;
        }
    }

    private BroadcastReceiver mInfoBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String infoText = intent.getStringExtra(InstallUtil.EXTRA_INFO_TEXT);
            Snackbar.make(mBinding.getRoot(), infoText, Snackbar.LENGTH_LONG).show();
        }
    };

    //endregion


    //
    //region Menu.
    //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                onAboutClicked();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onAboutClicked() {
        AboutActivityIntentBuilder builder = new AboutActivityIntentBuilder();
        builder.setAppName(getString(R.string.app_name));
        builder.setBuildDate(BuildConfig.BUILD_DATE);
        builder.setGitSha1(BuildConfig.GIT_SHA1);
        builder.setAuthorCopyright(getString(R.string.about_authorCopyright));
        builder.setLicense(getString(R.string.about_License));
        builder.setShareTextSubject(getString(R.string.about_shareText_subject));
        builder.setShareTextBody(getString(R.string.about_shareText_body));
        builder.setBackgroundResId(R.drawable.about_bg);
        builder.addLink(getString(R.string.about_email_uri), getString(R.string.about_email_text));
        builder.addLink(getString(R.string.about_web_uri), getString(R.string.about_web_text));
        builder.addLink(getString(R.string.about_sources_uri), getString(R.string.about_sources_text));
        builder.setIsLightIcons(true);
        builder.setAuthorPlayStoreName(getString(R.string.about_authorPlaystoreName));
        startActivity(builder.build(this));
    }

    //endregion

    private void checkForBundledWatchfaces() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                File bundledGwd0File = Storage.getGwdFile(MainActivity.this, Bundled.WF_BUNDLED_0_PUBLIC_ID);
                File bundledGwd1File = Storage.getGwdFile(MainActivity.this, Bundled.WF_BUNDLED_1_PUBLIC_ID);
                if (bundledGwd0File.exists() && bundledGwd1File.exists()) {
                    // Bundled watchfaces are already installed, stop here
                    return false;
                }

                // Show a "loading" panel during the installation
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBinding.conLoading.setVisibility(View.VISIBLE);
                    }
                });

                // Copy the watchfaces from the assets folder to the external storage
                try {
                    InputStream bundledGwd0In = getAssets().open("bundled_0.gwd");
                    InputStream bundledGwd1In = getAssets().open("bundled_1.gwd");
                    OutputStream bundledGwd0Out = new FileOutputStream(bundledGwd0File);
                    OutputStream bundledGwd1Out = new FileOutputStream(bundledGwd1File);
                    IoUtil.copy(bundledGwd0In, bundledGwd0Out);
                    IoUtil.copy(bundledGwd1In, bundledGwd1Out);
                } catch (IOException e) {
                    Log.e(e, "Could not install bundled watchfaces");
                }

                // Extract icons
                GWDReader.loadGWD(bundledGwd0File);
                GWDReader.loadGWD(bundledGwd1File);

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    mBinding.conLoading.setVisibility(View.GONE);
                    getWatchfaceGridFragment().reload();
                }

            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        WearManager.getInstance().removeWearConsumer(mWearConsumer);
        unregisterInfoBroadcastReceiver();
        super.onDestroy();
    }

    private AbstractWearConsumer mWearConsumer = new AbstractWearConsumer() {
        @Override
        public void onWearableMessageReceived(MessageEvent messageEvent) {
            String path = messageEvent.getPath();
            Log.d("path=%s", path);
            switch (path) {
                case Wear.PATH_MESSAGE_SET_GWD_REPLY:
                    boolean ok = Wear.isOk(messageEvent);
                    if (!ok) {
                        // File not present: send it now
                        File gwdFile = Storage.getGwdFile(MainActivity.this, mGwdToSendPublicId);
                        Wear.sendAFile(gwdFile);
                    }

                    Snackbar.make(mBinding.getRoot(), R.string.main_set_success, Snackbar.LENGTH_LONG).show();
                    break;
            }
        }
    };

    WatchfaceGridFragment getWatchfaceGridFragment() {
        if (mWatchfaceGridFragment == null) {
            mWatchfaceGridFragment = (WatchfaceGridFragment) getSupportFragmentManager().findFragmentById(R.id.fraGrid);
        }
        return mWatchfaceGridFragment;
    }


    //
    //region WatchfaceCallbacks.
    //

    @Override
    public void onWatchfaceClick(String publicId) {
        mGwdToSendPublicId = publicId;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // First, deselect any previously selected watchface
                WatchfaceContentValues values = new WatchfaceContentValues();
                values.putIsSelected(false);
                values.update(MainActivity.this, null);

                // Now select the given one
                WatchfaceSelection selection = new WatchfaceSelection().publicId(mGwdToSendPublicId);
                values.putIsSelected(true);
                values.update(MainActivity.this, selection);

                // Ask the watch to set the watchface
                Wear.sendMessage(Wear.PATH_MESSAGE_SET_GWD_REQUEST, mGwdToSendPublicId);
                return null;
            }
        }.execute();
    }

    @Override
    public void onWatchfacesSelected(Set<Long> selectedPublicIds) {
        if (selectedPublicIds.isEmpty()) {
            mActionMode.finish();
        } else {
            // Start action mode
            if (mActionMode == null) mActionMode = startSupportActionMode(this);
            assert mActionMode != null;
            mActionMode.invalidate();
        }
    }

    //endregion


    //
    //region ActionMode.Callback.
    //

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.main_contextual, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        int quantity = getWatchfaceGridFragment().getSelection().size();
        mode.setTitle("" + quantity);
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                int quantity = getWatchfaceGridFragment().getSelection().size();
                String message = getResources().getQuantityString(R.plurals.main_delete_confirm, quantity, quantity);
                AlertDialogFragment.newInstance(DIALOG_DELETE_CONFIRM).message(message).positiveButton(android.R.string.ok)
                        .negativeButton(android.R.string.cancel)
                        .show(this);
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        getWatchfaceGridFragment().stopSelectionMode();
    }

    //endregion


    //
    //region AlertDialogListener.
    //

    @Override
    public void onDialogClickPositive(int tag, Object payload) {
        new TaskFragment(new DeleteTask()).execute(this);
    }

    @Override
    public void onDialogClickNegative(int tag, Object payload) {}

    @Override
    public void onDialogClickListItem(int tag, int index, Object payload) {}

    //endregion


    public void onGetWatchfacesClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_getWatchfaces)));
        startActivity(browserIntent);
    }
}
