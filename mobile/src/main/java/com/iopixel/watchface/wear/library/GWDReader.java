/*
 * Copyright (C) 2016 ellis
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

import android.content.Context;

import org.jraf.android.util.file.FileUtil;
import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by ellis on 11/04/16.
 */
public class GWDReader {

    // buffer of 4096 bytes
    private static final int BUFFER_SIZE = 4096;

    public GWDReader(File gwdFile) {
        loadGWD(gwdFile);
    }

    protected static boolean loadGWD(File gwdFile) {
        try {
            InputStream is = new FileInputStream(gwdFile);
            ZipInputStream zis = new ZipInputStream(is);
            String gwdName = "unknown";
            try {
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    // search watchface.xml
                    if (ze != null) {
                        if (ze.getName().equals("res/watchface.xml")) {
                            // parse XML
                            String content = IoUtil.readFully(zis);
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder builder = factory.newDocumentBuilder();
                            Document dom = builder.parse(new InputSource(new StringReader(content)));
                            Element element = (Element)dom.getDocumentElement();
                            if (element != null) {
                                // search <watchface name="WHAT_I_WANT">
                                gwdName = element.getAttribute("name");
                                Log.i("name: %s", gwdName);
                            }
                        } else if (ze.getName().startsWith("shared/res/") && ze.getName().endsWith(".png")) {
                            String iconName = removeExtension(gwdFile.getName()) + ".png";
                            // icon name
                            Log.i("icon: ||%s||", iconName);
                            String iconPath = gwdFile.getParent() + File.separator + iconName;
                            // write into file with filename_of_gwd.png (we removed the .gwd)
                            extractFile(zis, iconPath);
                        } else {
                            //Log.i("other name: ||%s||", ze.getName());
                        }
                    }
                }
            } finally {
                zis.close();
            }
        } catch (ParserConfigurationException e) {
            Log.e("ParserConfigurationException: %s", e.getLocalizedMessage());
            return false;
        } catch (SAXException e) {
            Log.e("SAXException: %s", e.getLocalizedMessage());
            return false;
        } catch (IOException e) {
            Log.e("IOException: %s", e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    protected static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }


    protected static String removeExtension(String s) {
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(File.separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1)
            return filename;

        return filename.substring(0, extensionIndex);
    }

}
