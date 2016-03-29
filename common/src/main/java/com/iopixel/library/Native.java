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
package com.iopixel.library;

/**
 * Created by ellis on 29/03/16.
 */
public class Native {

    // JNI
    static {
        System.loadLibrary("watchface");
    }

    // JNI methods

    // common

    // mobile

    // wear
    public static native void Destroy();
    public static native void OnSurfaceCreated(String apkPath, String pwdPath, int width, int height);
    public static native void Shape(int shape);
    public static native void OnDraw();
    public static native void SendEvent(int x, int y);
    public static native void AmbientMode(boolean mode);
    public static native void VisibilityChanged(boolean visibility);

}
