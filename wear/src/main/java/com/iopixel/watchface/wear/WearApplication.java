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
package com.iopixel.watchface.wear;

import java.io.File;

import android.app.Application;

import org.jraf.android.util.log.Log;
import org.jraf.android.util.log.LogUtil;

import com.google.android.gms.wearable.WearableStatusCodes;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;

public class WearApplication extends Application {
    private static final String TAG = "WatchDesigner";

    @Override
    public void onCreate() {
        super.onCreate();

        // Log
        Log.init(this, TAG);

        // Wear Companion Library
        WearManager.initialize(this);

        WearManager.getInstance().addWearConsumer(new AbstractWearConsumer() {
            @Override
            public void onWearableFileReceivedResult(int statusCode, String requestId, File savedFile, String originalName) {
                String statusCodeStr = LogUtil.getConstantName(WearableStatusCodes.class, statusCode);
                Log.d("statusCode=%s requestId=%s savedFile=%s originalName=%s", statusCodeStr, requestId, savedFile, originalName);
                Log.d("File size=%d", savedFile.length());
            }
        });
    }
}
