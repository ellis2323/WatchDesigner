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
package com.iopixel.watchface.wear.app.main.grid;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.util.app.base.BaseFragment;

import com.iopixel.watchface.wear.R;
import com.iopixel.watchface.wear.app.main.WatchfaceCallbacks;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceColumns;
import com.iopixel.watchface.wear.databinding.WatchfaceGridBinding;

public class WatchfaceGridFragment extends BaseFragment<WatchfaceCallbacks> implements LoaderManager.LoaderCallbacks<Cursor> {
    private WatchfaceGridBinding mBinding;
    private WatchfaceGridAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.watchface_grid, container, false);
        mBinding.rclGrid.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getContext(), 3);
        mBinding.rclGrid.setLayoutManager(mLayoutManager);
        return mBinding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    /*
     * Loader.
     */
    //region

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), WatchfaceColumns.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter == null) {
            mAdapter = new WatchfaceGridAdapter(getActivity(), getCallbacks());
            mBinding.rclGrid.setAdapter(mAdapter);
        }
        boolean empty = data.getCount() == 0;
        if (empty) {
            mBinding.conEmpty.setVisibility(View.VISIBLE);
            mBinding.rclGrid.setVisibility(View.GONE);
        } else {
            mBinding.conEmpty.setVisibility(View.GONE);
            mBinding.rclGrid.setVisibility(View.VISIBLE);
            mAdapter.setCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setCursor(null);
    }

    //endregion
}