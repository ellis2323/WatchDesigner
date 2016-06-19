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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class GWDReader {
    // buffer of 4096 bytes
    private static final int BUFFER_SIZE = 4096;

    /**
     * Extract the icon and returns the watchface name.
     *
     * @param gwdFile Gwd file to read.
     * @return The name of the watchface as declared in its manifest, or {@code null} if a problem occurred while extracting or parsing.
     */
    //@WorkerThread
    @Nullable
    public static String loadGWD(File gwdFile) {
        String gwdName = "unknown";
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(gwdFile));
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                // search watchface.xml
                if (ze.getName().equals("res/watchface.xml")) {
                    // parse XML
                    String content = IoUtil.readFully(zis);
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document dom = builder.parse(new InputSource(new StringReader(content)));
                    Element element = dom.getDocumentElement();
                    if (element != null) {
                        // search <watchface name="WHAT_I_WANT">
                        gwdName = element.getAttribute("name");
                        Log.i("name: %s", gwdName);
                    }
                } else if (ze.getName().startsWith("res/") && ze.getName().endsWith("_last.png")) {
                    String iconName = FileUtil.removeExtension(gwdFile) + ".png";
                    // icon name
                    Log.i("icon: ||%s||", iconName);
                    File iconPath = new File(gwdFile.getParent(), iconName);
                    // write into file with filename_of_gwd.png (we removed the .gwd)
                    extractFile(zis, iconPath);
                } else if (ze.getName().startsWith("shared/res/") && ze.getName().endsWith(".png")) {
                    String iconName = FileUtil.removeExtension(gwdFile) + ".png";
                    // icon name
                    Log.i("icon: ||%s||", iconName);
                    File iconPath = new File(gwdFile.getParent(), iconName);
                    // write into file with filename_of_gwd.png (we removed the .gwd)
                    extractFile(zis, iconPath);
                }
            }
        } catch (Exception e) {
            Log.e(e, "Could not extract icon, or read or parse watchface.xml");
            return null;
        } finally {
            IoUtil.closeSilently(zis);
        }
        return gwdName;
    }

    private static void extractFile(ZipInputStream zipIn, File file) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}
