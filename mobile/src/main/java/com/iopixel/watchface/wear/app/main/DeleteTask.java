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
package com.iopixel.watchface.wear.app.main;

import java.io.File;

import android.support.design.widget.Snackbar;

import org.jraf.android.util.async.Task;
import org.jraf.android.util.collection.CollectionUtil;

import com.iopixel.library.Storage;
import com.iopixel.watchface.wear.R;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceCursor;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceSelection;

class DeleteTask extends Task<MainActivity> {
    public int mSize;

    @Override
    protected void doInBackground() throws Throwable {
        WatchfaceSelection selection = new WatchfaceSelection();
        long[] selectedIds = CollectionUtil.unwrapLong(getActivity().getWatchfaceGridFragment().getSelection());
        mSize = selectedIds.length;
        selection.id(selectedIds);

        // Delete all the gwd files
        WatchfaceCursor c = selection.query(getActivity());
        try {
            while (c.moveToNext()) {
                String publicId = c.getPublicId();
                File gwdFile = Storage.getGwdFile(getActivity(), publicId);
                gwdFile.delete();
            }
        } finally {
            c.close();
        }

        // Delete from the provider
        selection.delete(getActivity());
    }

    @Override
    protected void onPostExecuteOk() {
        if (getActivity().mActionMode != null) getActivity().mActionMode.finish();
        String message = getActivity().getResources().getQuantityString(R.plurals.main_delete_success, mSize, mSize);
        Snackbar.make(getActivity().mBinding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }
}
