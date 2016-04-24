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

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.jraf.android.util.log.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;
import com.iopixel.library.Storage;
import com.iopixel.library.Wear;
import com.iopixel.watchface.wear.R;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceContentValues;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceSelection;
import com.iopixel.watchface.wear.databinding.MainBinding;

public class MainActivity extends AppCompatActivity implements WatchfaceCallbacks {
    private MainBinding mBinding;
    private String mGwdToSendPublicId;

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


    //
    //region WatchfaceCallbacks.
    //

    @Override
    public void onWatchfaceClicked(String publicId) {
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

    //endregion
}
