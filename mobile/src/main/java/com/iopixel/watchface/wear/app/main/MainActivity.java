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
import java.util.Set;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.collection.CollectionUtil;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.dialog.AlertDialogListener;
import org.jraf.android.util.log.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;
import com.iopixel.library.Storage;
import com.iopixel.library.Wear;
import com.iopixel.watchface.wear.R;
import com.iopixel.watchface.wear.app.main.grid.WatchfaceGridFragment;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceContentValues;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceSelection;
import com.iopixel.watchface.wear.databinding.MainBinding;


public class MainActivity extends AppCompatActivity implements WatchfaceCallbacks, ActionMode.Callback, AlertDialogListener {
    private static final int DIALOG_DELETE_CONFIRM = 0;

    private MainBinding mBinding;
    private String mGwdToSendPublicId;
    private ActionMode mActionMode;
    private WatchfaceGridFragment mWatchfaceGridFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.main);
        WearManager.getInstance().addWearConsumer(mWearConsumer);
    }

    @Override
    protected void onDestroy() {
        WearManager.getInstance().removeWearConsumer(mWearConsumer);
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

                    Toast.makeText(MainActivity.this, R.string.main_watchfaceSetToast, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private WatchfaceGridFragment getWatchfaceGridFragment() {
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


    //
    //region AlertDialogListener.
    //

    @Override
    public void onDialogClickPositive(int tag, Object payload) {
        new TaskFragment(new Task<MainActivity>() {
            public int mSize;

            @Override
            protected void doInBackground() throws Throwable {
                WatchfaceSelection selection = new WatchfaceSelection();
                long[] selectedIds = CollectionUtil.unwrapLong(getWatchfaceGridFragment().getSelection());
                mSize = selectedIds.length;
                selection.id(selectedIds);
                selection.delete(getActivity());
            }

            @Override
            protected void onPostExecuteOk() {
                if (mActionMode != null) mActionMode.finish();
                String message = getResources().getQuantityString(R.plurals.main_delete_success, mSize, mSize);
                Snackbar.make(mBinding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
            }
        }).execute(this);
    }

    @Override
    public void onDialogClickNegative(int tag, Object payload) {}

    @Override
    public void onDialogClickListItem(int tag, int index, Object payload) {}

    //endregion
}
