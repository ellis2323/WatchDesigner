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
package com.iopixel.library;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Set;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jraf.android.util.log.Log;
import org.jraf.android.util.log.LogUtil;
import org.jraf.android.util.serializable.SerializableUtil;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableStatusCodes;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.connectivity.WearFileTransfer;

public class Wear {
    private static final String PATH_MESSAGE_PREFIX = "message/";
    private static final String PATH_MESSAGE_SUFFIX_REQUEST = "/req";
    private static final String PATH_MESSAGE_SUFFIX_REPLY = "/repl";

    private static final String PATH_MESSAGE_SET_GWD = "setGwd";
    public static final String PATH_MESSAGE_SET_GWD_REQUEST = PATH_MESSAGE_PREFIX + PATH_MESSAGE_SET_GWD + PATH_MESSAGE_SUFFIX_REQUEST;
    public static final String PATH_MESSAGE_SET_GWD_REPLY = PATH_MESSAGE_PREFIX + PATH_MESSAGE_SET_GWD + PATH_MESSAGE_SUFFIX_REPLY;

    private static final String PATH_MESSAGE_DELETE_GWDS = "deleteGwds";
    public static final String PATH_MESSAGE_DELETE_GWDS_REQUEST = PATH_MESSAGE_PREFIX + PATH_MESSAGE_DELETE_GWDS + PATH_MESSAGE_SUFFIX_REQUEST;
    public static final String PATH_MESSAGE_DELETE_GWDS_REPLY = PATH_MESSAGE_PREFIX + PATH_MESSAGE_DELETE_GWDS + PATH_MESSAGE_SUFFIX_REPLY;

    private static final String PATH_MESSAGE_CHECK_WF_SET = "checkWatchfaceSet";
    public static final String PATH_MESSAGE_CHECK_WF_SET_REQUEST = PATH_MESSAGE_PREFIX + PATH_MESSAGE_CHECK_WF_SET + PATH_MESSAGE_SUFFIX_REQUEST;
    public static final String PATH_MESSAGE_CHECK_WF_SET_REPLY = PATH_MESSAGE_PREFIX + PATH_MESSAGE_CHECK_WF_SET + PATH_MESSAGE_SUFFIX_REPLY;

    public static final byte[] DATA_OK = {0};
    public static final byte[] DATA_KO = {1};

    public static boolean isOk(MessageEvent messageEvent) {
        return Arrays.equals(DATA_OK, messageEvent.getData());
    }

    @Nullable
    private static Node getFirstNode() {
        Set<Node> connectedNodes = WearManager.getInstance().getConnectedNodes();
        Log.d("connectedNodes=%s", connectedNodes);
        Node firstNode = null;
        for (Node n : connectedNodes) {
            if (n.isNearby()) {
                firstNode = n;
                break;
            }
        }
        return firstNode;
    }

    public static void sendAFile(File file) {
        Node firstNode = getFirstNode();
        if (firstNode == null) {
            Log.d("Could not find any nearby nodes: give up");
            return;
        }
        if (!file.exists()) {
            Log.w("Trying to send a non existing file: give up - %s", file);
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

    public static void sendMessage(String path, @Nullable byte[] data) {
        Node firstNode = getFirstNode();
        if (firstNode == null) {
            Log.d("Could not find any nearby nodes: give up");
            return;
        }
        WearManager.getInstance().sendMessage(firstNode.getId(), path, data);
    }

    public static void sendMessage(String path) {
        sendMessage(path, (byte[]) null);
    }

    public static void sendMessage(String path, @NonNull String data) {
        byte[] dataBytes = null;
        try {
            dataBytes = data.getBytes("utf-8");
        } catch (UnsupportedEncodingException ignored) {}
        sendMessage(path, dataBytes);
    }

    public static void sendMessage(String path, @NonNull Serializable data) {
        byte[] dataBytes = null;
        dataBytes = SerializableUtil.serialize(data);
        sendMessage(path, dataBytes);
    }
}
