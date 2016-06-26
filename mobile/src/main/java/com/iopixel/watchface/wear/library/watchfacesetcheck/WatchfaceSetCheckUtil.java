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
package com.iopixel.watchface.wear.library.watchfacesetcheck;

import java.util.concurrent.TimeUnit;

import android.support.annotation.MainThread;

import org.greenrobot.eventbus.EventBus;
import org.jraf.android.util.handler.HandlerUtil;
import org.jraf.android.util.log.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;
import com.google.devrel.wcl.callbacks.WearConsumer;
import com.iopixel.library.Wear;

public class WatchfaceSetCheckUtil {
    private static final long QUERY_DELAY = TimeUnit.SECONDS.toMillis(1);
    private static final long WAIT_REPLY_DELAY = TimeUnit.SECONDS.toMillis(5);

    private static WearConsumer sWearConsumer = new AbstractWearConsumer() {
        @Override
        public void onWearableMessageReceived(MessageEvent messageEvent) {
            String path = messageEvent.getPath();
            Log.d("path=%s", path);
            switch (path) {
                case Wear.PATH_MESSAGE_CHECK_WF_SET_REPLY:
                    HandlerUtil.getMainHandler().removeCallbacks(sGiveUpRunnable);

                    boolean ok = Wear.isOk(messageEvent);
                    if (ok) {
                        EventBus.getDefault().postSticky(new WatchfaceSetOkEvent());
                    } else {
                        EventBus.getDefault().postSticky(new WatchfaceSetNokEvent());
                    }

                    break;
            }
        }
    };

    private static Runnable sQueryRunnable = new Runnable() {
        @Override
        public void run() {
            Wear.sendMessage(Wear.PATH_MESSAGE_CHECK_WF_SET_REQUEST);
            HandlerUtil.getMainHandler().postDelayed(sGiveUpRunnable, WAIT_REPLY_DELAY);
        }
    };

    private static Runnable sGiveUpRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("Waiting delay expired, give up: send a NOK event");
            EventBus.getDefault().post(new WatchfaceSetNokEvent());
        }
    };

    @MainThread
    public static void startWatchfaceSetCheck() {
        WearManager.getInstance().addWearConsumer(sWearConsumer);
        // Wait a bit before querying because it can take a moment before nodes are visible
        HandlerUtil.getMainHandler().postDelayed(sQueryRunnable, QUERY_DELAY);
    }

    public static void stopWatchfaceSetCheck() {
        WearManager.getInstance().removeWearConsumer(sWearConsumer);
        HandlerUtil.getMainHandler().removeCallbacks(sQueryRunnable);
        HandlerUtil.getMainHandler().removeCallbacks(sGiveUpRunnable);
    }
}
