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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import com.iopixel.library.Storage;
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
                sendAFile(getFirstFile());
            }
        });
/*
        Native.InitWithNullDriver(Storage.getAPKFileName(this), Storage.getInternalStoragePath(this));

        try {
            UnzipAssets("nyan.gwd", "toto");
            File outputDir = getOutputDir("toto");
            Native.CreateWD(outputDir.getAbsolutePath());
            deletePngInDirectory(outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }

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

    @Nullable
    private File getFirstFile() {
        File gwdStorage = Storage.getGwdStorage(this);
        return gwdStorage.listFiles()[0];
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

    /*
    private boolean UnzipAssets(String filepath, String dirName) throws IOException {
        File outputDir = getOutputDir(dirName);
        FileUtils.deleteDirectory(outputDir);
        outputDir.mkdir();
        // create spritesheets
        File sps = new File(outputDir.getAbsolutePath() + File.separator + "spritesheets");
        sps.mkdir();
        //FileInputStream is = new FileInputStream(filepath);
        InputStream is = getAssets().open(filepath);
        unpackZip(is, outputDir.getAbsolutePath());
        //ZipUtil.unpack(is, outputDir);
        return true;
    }
    */

    private File getOutputDir(String dirName) {
        String basepath = getFilesDir().getAbsolutePath();
        String dirpath = basepath + File.separator + dirName;
        return new File(dirpath);
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }

    /*
    private void deletePngInDirectory(File dir) {
        String[] exts = {"png"};
        Iterator it = FileUtils.iterateFiles(dir, exts, true);
        while (it.hasNext()) {
            File file = (File) it.next();
            if (file != null) {
                file.delete();
            }
        }
    }
    */

    private boolean unpackZip(InputStream is, String path) {
        ZipInputStream zis;
        try {
            String filename;
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
