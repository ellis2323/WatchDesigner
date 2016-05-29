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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.GLES20;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.Gles2WatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.Log;
import org.jraf.android.util.log.LogUtil;
import org.jraf.android.util.serializable.SerializableUtil;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableStatusCodes;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;
import com.iopixel.library.Bundled;
import com.iopixel.library.I18NEngine;
import com.iopixel.library.Native;
import com.iopixel.library.Storage;
import com.iopixel.library.Wear;

/**
 * Created by ellis on 13/02/16.
 */
public class IOWatchfaceService extends Gles2WatchFaceService {

    public static final boolean DEBUG = true;
    public static Engine sEngine;

    private AbstractWearConsumer mWearConsumer = new AbstractWearConsumer() {
        @Override
        public void onWearableFileReceivedResult(int statusCode, String requestId, File savedFile, String originalName) {
            String statusCodeStr = LogUtil.getConstantName(WearableStatusCodes.class, statusCode);
            Log.d("statusCode=%s requestId=%s savedFile=%s originalName=%s", statusCodeStr, requestId, savedFile, originalName);
            Log.d("File size=%d", savedFile.length());
            try {
                Native.LoadGWD(savedFile.getCanonicalPath());
            } catch (IOException e) {
                Native.LoadGWD(savedFile.getAbsolutePath());
            }
            if (sEngine != null) {
                sEngine.invalidate();
            }
        }

        @Override
        public void onWearableMessageReceived(MessageEvent messageEvent) {
            String path = messageEvent.getPath();
            Log.d("path=%s", path);
            switch (path) {
                case Wear.PATH_MESSAGE_SET_GWD_REQUEST:
                    String publicId = null;
                    try {
                        publicId = new String(messageEvent.getData(), "utf-8");
                    } catch (UnsupportedEncodingException ignored) {}
                    assert publicId != null;
                    Log.d("publicId=%s", publicId);
                    File gwdFile = Storage.getInternalGwdFile(IOWatchfaceService.this, publicId);
                    if (gwdFile.exists()) {
                        // We already have the gwd file
                        Log.d("gwd exists");
                        // Load it now
                        Native.LoadGWD(gwdFile.getAbsolutePath());

                        // Reply OK
                        WearManager.getInstance().sendMessage(messageEvent.getSourceNodeId(), Wear.PATH_MESSAGE_SET_GWD_REPLY, Wear.DATA_OK);
                    } else {
                        // We don't have it
                        if (Bundled.WF_BUNDLED_0_PUBLIC_ID.equals(publicId) || Bundled.WF_BUNDLED_1_PUBLIC_ID.equals(publicId)) {
                            Log.d("gwd is bundled");
                            // Special case: bundled gwd files
                            // Copy the watchface from the assets folder to the external storage
                            try {
                                InputStream bundledGwdIn = getAssets().open(publicId + Storage.PREFIX_GWD);
                                OutputStream bundledGwdOut = new FileOutputStream(gwdFile);
                                IoUtil.copy(bundledGwdIn, bundledGwdOut);
                            } catch (IOException e) {
                                Log.e(e, "Could not install bundled watchfaces");
                            }

                            // Load it now
                            Native.LoadGWD(gwdFile.getAbsolutePath());

                            // Reply OK
                            WearManager.getInstance().sendMessage(messageEvent.getSourceNodeId(), Wear.PATH_MESSAGE_SET_GWD_REPLY, Wear.DATA_OK);
                        } else {
                            // Ask for the transfer
                            Log.d("gwd doesn't exist");
                            WearManager.getInstance().sendMessage(messageEvent.getSourceNodeId(), Wear.PATH_MESSAGE_SET_GWD_REPLY, Wear.DATA_KO);
                        }
                    }
                    break;

                case Wear.PATH_MESSAGE_DELETE_GWDS_REQUEST:
                    String[] publicIds = SerializableUtil.deserialize(messageEvent.getData());
                    Log.d("publicIds=%s", Arrays.toString(publicIds));
                    // Delete the files
                    for (String id : publicIds) {
                        gwdFile = Storage.getInternalGwdFile(IOWatchfaceService.this, id);
                        gwdFile.delete();
                    }

                    // Reply OK
                    WearManager.getInstance().sendMessage(messageEvent.getSourceNodeId(), Wear.PATH_MESSAGE_DELETE_GWDS_REPLY, Wear.DATA_OK);
                    break;
            }
        }
    };

    @Override
    public Engine onCreateEngine() {
        WearManager.getInstance().addWearConsumer(mWearConsumer);
        sEngine = new Engine();
        return sEngine;
    }

    @Override
    public void onDestroy() {
        WearManager.getInstance().removeWearConsumer(mWearConsumer);
        super.onDestroy();
    }

    private class Engine extends Gles2WatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            ResultCallback<DailyTotalResult> {

        private GoogleApiClient mGoogleApiClient;
        private boolean mStepsRequested = false;
        private boolean mRegisteredTimeInfoReceiver = false;
        private boolean mRegisteredBatteryInfoReceiver = false;
        private int mStepsTotal = 0;
        private int mBatteryWear = 0;
        private int mBatteryPhone = 0;


        private final BroadcastReceiver mTimeInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                invalidate();
            }
        };

        private final BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mBatteryWear = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                Log.d("IOPIXEL === Wear Battery " + mBatteryWear);
                Native.WearBattery(mBatteryWear);
            }
        };

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            mStepsRequested = false;
            mGoogleApiClient = new GoogleApiClient.Builder(IOWatchfaceService.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.RECORDING_API)
                    // When user has multiple accounts, useDefaultAccount() allows Google Fit to
                    // associated with the main account for steps. It also replaces the need for
                    // a scope request.
                    .useDefaultAccount()
                    .build();


            setWatchFaceStyle(new WatchFaceStyle.Builder(IOWatchfaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setStatusBarGravity(Gravity.RIGHT | Gravity.TOP)
                    .setHotwordIndicatorGravity(Gravity.LEFT | Gravity.TOP)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());
            I18NEngine.attach(getApplicationContext());
        }

        @Override
        public void onDestroy() {
            Log.d("onDestroy");
            Native.Destroy();
        }

        @Override
        public void onGlContextCreated() {
            super.onGlContextCreated();
        }

        @Override
        public void onGlSurfaceCreated(int width, int height) {
            super.onGlSurfaceCreated(width, height);
            Native.OnSurfaceCreated(Storage.getAPKFileName(IOWatchfaceService.this), Storage.getInternalStoragePath(IOWatchfaceService.this), width, height);
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            Native.AmbientMode(inAmbientMode);
            invalidate();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            // burnin protection
            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                mGoogleApiClient.connect();
                registerReceiver();
            } else {
                unregisterReceiver();
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
            }
            Native.VisibilityChanged(visible);
        }

        private void registerReceiver() {
            if (!mRegisteredTimeInfoReceiver) {
                mRegisteredTimeInfoReceiver = true;
                IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                IOWatchfaceService.this.registerReceiver(mTimeInfoReceiver, filter);
            }
            if (!mRegisteredBatteryInfoReceiver) {
                mRegisteredBatteryInfoReceiver = true;
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                IOWatchfaceService.this.registerReceiver(mBatteryInfoReceiver, filter);
            }
        }

        private void unregisterReceiver() {
            if (mRegisteredTimeInfoReceiver) {
                mRegisteredTimeInfoReceiver = false;
                IOWatchfaceService.this.unregisterReceiver(mTimeInfoReceiver);
            }
            if (mRegisteredBatteryInfoReceiver) {
                mRegisteredBatteryInfoReceiver = false;
                IOWatchfaceService.this.unregisterReceiver(mBatteryInfoReceiver);
            }
        }


        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            if (insets.isRound()) {
                Native.Shape(1);
            } else {
                Native.Shape(0);
            }

        }

        @Override
        public void onDraw() {
            super.onDraw();
            GLES20.glClearColor(0.5f, 0.2f, 0.2f, 1);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            Native.OnDraw();
            if (isVisible() && !isInAmbientMode()) {
                try {
                    Thread.sleep(10, 0);
                } catch (InterruptedException e) {}
                invalidate();
            }
        }

        @Override
        public void onTapCommand(@TapType int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case WatchFaceService.TAP_TYPE_TAP:
                    Log.d("tap " + x + " " + y);
                    Native.SendEvent(x, y);
                    break;

                case WatchFaceService.TAP_TYPE_TOUCH:
                    break;

                case WatchFaceService.TAP_TYPE_TOUCH_CANCEL:
                    break;

                default:
                    super.onTapCommand(tapType, x, y, eventTime);
                    break;
            }
        }

        private void getTotalSteps() {
            Log.d("getTotalSteps()");

            if ((mGoogleApiClient == null) || (!mGoogleApiClient.isConnected()) || (mStepsRequested)) {
                return;
            }

            mStepsRequested = true;
            PendingResult<DailyTotalResult> stepsResult;
            stepsResult = Fitness.HistoryApi.readDailyTotal(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA);
            stepsResult.setResultCallback(this);
        }

        private void subscribeToSteps() {
            Fitness.RecordingApi.subscribe(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                    Log.i("Existing subscription for activity detected.");
                                } else {
                                    Log.i("Successfully subscribed!");
                                }
                            } else {
                                Log.i("There was a problem subscribing.");
                            }
                        }
                    });
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            mStepsRequested = false;
            subscribeToSteps();
            getTotalSteps();
        }

        @Override
        public void onConnectionSuspended(int i) {
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        }

        @Override
        public void onResult(@NonNull DailyTotalResult dailyTotalResult) {
            mStepsRequested = false;
            if (dailyTotalResult.getStatus().isSuccess()) {
                List<DataPoint> points = dailyTotalResult.getTotal().getDataPoints();
                if (!points.isEmpty()) {
                    mStepsTotal = points.get(0).getValue(Field.FIELD_STEPS).asInt();
                    Log.d("steps updated: %d", mStepsTotal);
                }
            }
        }
    }

    public static int getSteps() {
        if (sEngine != null) {
            return sEngine.mStepsTotal;
        }
        return 0;
    }


}
