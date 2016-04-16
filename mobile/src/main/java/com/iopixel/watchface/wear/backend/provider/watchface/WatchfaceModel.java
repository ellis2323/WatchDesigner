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
package com.iopixel.watchface.wear.backend.provider.watchface;

import java.util.Date;

import android.support.annotation.NonNull;

import com.iopixel.watchface.wear.backend.provider.base.BaseModel;

/**
 * Watchface.
 */
public interface WatchfaceModel extends BaseModel {

    /**
     * Id of the watchface, which is used to build file names (e.g. "xyw_superwf_v2").
     * Cannot be {@code null}.
     */
    @NonNull
    String getId();

    /**
     * Display name (e.g. "Super Duper Watchface v2").
     * Cannot be {@code null}.
     */
    @NonNull
    String getDisplayName();

    /**
     * Is it the currently selected watchface?<br/>At any time, the value should be {@code true} for one and only one row.
     */
    boolean getIsSelected();

    /**
     * When was the watchface installed.
     * Cannot be {@code null}.
     */
    @NonNull
    Date getInstallDate();
}
