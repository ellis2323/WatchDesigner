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

import com.iopixel.library.Storage;
import com.iopixel.watchface.wear.R;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceContentValues;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceSelection;
import com.iopixel.watchface.wear.databinding.MainBinding;
import com.iopixel.watchface.wear.library.WearUtil;

public class MainActivity extends AppCompatActivity implements WatchfaceCallbacks {
    private MainBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.main);
    }

    /*
     * WatchfaceCallbacks.
     */
    //region

    @Override
    public void onWatchfaceClicked(final String publicId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // First, deselect any previously selected watchface
                WatchfaceContentValues values = new WatchfaceContentValues();
                values.putIsSelected(false);
                values.update(MainActivity.this, null);

                // Now select the given one
                WatchfaceSelection selection = new WatchfaceSelection().publicId(publicId);
                values.putIsSelected(true);
                values.update(MainActivity.this, selection);

                // Send the watchface to the watch
                File gwdFile = Storage.getGwdFile(MainActivity.this, publicId);
                WearUtil.sendAFile(gwdFile);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Toast.makeText(MainActivity.this, R.string.main_watchfaceSetToast, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    //endregion
}
