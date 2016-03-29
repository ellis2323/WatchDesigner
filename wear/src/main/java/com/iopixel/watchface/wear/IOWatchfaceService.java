package com.iopixel.watchface.wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.opengl.GLES20;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.Gles2WatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

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

import com.iopixel.library.I18NEngine;
import com.iopixel.library.Native;

import java.util.List;
import java.util.TimeZone;

/**
 * Created by ellis on 13/02/16.
 */
public class IOWatchfaceService extends Gles2WatchFaceService {

    public static final String TAG = "iopixel";
    public static final boolean DEBUG = true;
    public static Engine sEngine;

    public void logInfo(String message) {
        if (DEBUG) {
            Log.i(TAG, message);
        }
    }

    public String getAPKFileName() {
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), 0);
            return appInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error when locating the apk filename");
        }
        return "";
    }

    public String getInternalStoragePath() {
        Context ctx = getApplicationContext();
        if (ctx.getFilesDir() == null) {
            Log.e(TAG, "error, getFilesDir is null");
        }

        if (!ctx.getFilesDir().exists()) {
            ctx.getFilesDir().mkdir();
        }
        return ctx.getFilesDir().getAbsolutePath();
    }

    @Override
    public Engine onCreateEngine() {
        sEngine = new Engine();
        return sEngine;
    }

    private class Engine extends Gles2WatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            ResultCallback<DailyTotalResult> {

        private GoogleApiClient mGoogleApiClient;
        private boolean mStepsRequested;
        private boolean mRegisteredReceiver = false;
        public int mStepsTotal = 0;

        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                invalidate();
            }
        };

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            logInfo("onCreate");
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
            logInfo("onDestroy");
            Native.Destroy();
        }

        @Override
        public void onGlContextCreated() {
            super.onGlContextCreated();
        }

        @Override
        public void onGlSurfaceCreated(int width, int height) {
            super.onGlSurfaceCreated(width, height);
            Native.OnSurfaceCreated(getAPKFileName(), getInternalStoragePath(), width, height);
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
            // burnin protec†ion
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
            if (mRegisteredReceiver) {
                return;
            }
            mRegisteredReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            IOWatchfaceService.this.registerReceiver(mReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredReceiver) {
                return;
            }
            mRegisteredReceiver = false;
            IOWatchfaceService.this.unregisterReceiver(mReceiver);
        }


        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            if(insets.isRound()){
                Native.Shape(1);
            }
            else{
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
                invalidate();
            }
        }

        @Override
        public void onTapCommand(@TapType int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case WatchFaceService.TAP_TYPE_TAP:
                    logInfo("tap " + x + " " + y);
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
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "getTotalSteps()");
            }

            if ((mGoogleApiClient != null)
                    && (mGoogleApiClient.isConnected())
                    && (!mStepsRequested)) {

                mStepsRequested = true;

                PendingResult<DailyTotalResult> stepsResult =
                        Fitness.HistoryApi.readDailyTotal(
                                mGoogleApiClient,
                                DataType.TYPE_STEP_COUNT_DELTA);

                stepsResult.setResultCallback(this);
            }
        }

        private void subscribeToSteps() {
            Fitness.RecordingApi.subscribe(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                    Log.i(TAG, "Existing subscription for activity detected.");
                                } else {
                                    Log.i(TAG, "Successfully subscribed!");
                                }
                            } else {
                                Log.i(TAG, "There was a problem subscribing.");
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
                List<DataPoint> points = dailyTotalResult.getTotal().getDataPoints();;
                if (!points.isEmpty()) {
                    mStepsTotal = points.get(0).getValue(Field.FIELD_STEPS).asInt();
                    Log.d(TAG, "steps updated: " + mStepsTotal);
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
