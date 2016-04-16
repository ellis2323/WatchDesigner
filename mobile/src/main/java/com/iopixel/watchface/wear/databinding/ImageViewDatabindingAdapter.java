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
package com.iopixel.watchface.wear.databinding;

import java.io.File;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.iopixel.library.Storage;
import com.squareup.picasso.Picasso;

public class ImageViewDatabindingAdapter {
    @BindingAdapter("bind:iconFile")
    public static void loadImage(ImageView view, String publicId) {
        File previewFile = Storage.getPreviewImage(view.getContext(), publicId);
        Picasso.with(view.getContext()).load(previewFile).into(view);
    }
}
