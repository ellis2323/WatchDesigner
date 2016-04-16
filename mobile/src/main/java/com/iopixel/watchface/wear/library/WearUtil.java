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
package com.iopixel.watchface.wear.library;

import java.io.File;
import java.util.Set;

import org.jraf.android.util.log.Log;
import org.jraf.android.util.log.LogUtil;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableStatusCodes;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.connectivity.WearFileTransfer;

public class WearUtil {
    public static void sendAFile(File file) {
        Set<Node> connectedNodes = WearManager.getInstance().getConnectedNodes();
        Log.d("connectedNodes=%s", connectedNodes);
        Node firstNode = null;
        for (Node n : connectedNodes) {
            if (n.isNearby()) {
                firstNode = n;
                break;
            }
        }
        if (firstNode == null) {
            Log.d("Could not find any nearby nodes: give up");
            return;
        }
        WearFileTransfer wearFileTransfer = new WearFileTransfer.Builder(firstNode).setTargetName(file.getName()).setFile(file).setOnFileTransferResultListener(
                new WearFileTransfer.OnFileTransferRequestListener() {
                    @Override
                    public void onFileTransferStatusResult(int statusCode) {
                        Log.d("statusCode=%s", LogUtil.getConstantName(WearableStatusCodes.class, statusCode));
                    }
                }).build();

        wearFileTransfer.startTransfer();
    }
}
