/*
 * Copyright (C) 2016 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package com.iopixel.watchface.wear.app.testing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.Log;
import org.jraf.android.util.log.LogUtil;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableStatusCodes;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.connectivity.WearFileTransfer;
import com.iopixel.watchface.wear.R;

public class TestingActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing);
        View btnSendAFile = findViewById(R.id.btnSendAFile);
        assert btnSendAFile != null;
        btnSendAFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAFile("text1.gwd");
            }
        });
    }

    private void sendAFile(String fileName) {
        File file = getFile(fileName);
        assert file != null;
        Set<Node> connectedNodes = WearManager.getInstance().getConnectedNodes();
        Log.d("connectedNodes=%s", connectedNodes);
        Node firstNode = connectedNodes.iterator().next();
        WearFileTransfer wearFileTransfer = new WearFileTransfer.Builder(firstNode).setTargetName(fileName).setFile(file).setOnFileTransferResultListener(
                new WearFileTransfer.OnFileTransferRequestListener() {
                    @Override
                    public void onFileTransferStatusResult(int statusCode) {
                        Log.d("statusCode=%s", LogUtil.getConstantName(WearableStatusCodes.class, statusCode));
                    }
                }).build();

        wearFileTransfer.startTransfer();
    }

    @Nullable
    private File getFile(String fileName) {
        File gwdStorage = getExternalFilesDir("gwd");
        File file = new File(gwdStorage, fileName);
        if (file.exists()) return file;

        // File does not exist, create it by copying it from the assets folder
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getAssets().open(fileName);
            out = new FileOutputStream(file);
            IoUtil.copy(in, out);
        } catch (IOException e) {
            Log.w(e, "Could not copy file");
            return null;
        } finally {
            IoUtil.closeSilently(in, out);
        }
        return file;
    }
}
