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

import android.content.res.Resources;
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
        mLayoutManager = new GridLayoutManager(getContext(), 1);
        mBinding.rclGrid.setLayoutManager(mLayoutManager);

        // Calculate the optimal column count based on the width of the grid, and the width of one cell
        mBinding.rclGrid.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Resources resources = v.getContext().getResources();
                int cellWidth = resources.getDimensionPixelSize(R.dimen.watchface_grid_item_preview_width) +
                        resources.getDimensionPixelSize(R.dimen.watchface_grid_item_padding) * 2;
                // We also want some minimum padding between cells
                int interPadding = resources.getDimensionPixelSize(R.dimen.watchface_grid_item_inter_padding);
                cellWidth += interPadding;
                int gridWidth = right - left;
                gridWidth -= interPadding;
                int columnCount = gridWidth / cellWidth;
                mLayoutManager.setSpanCount(columnCount);

                // Relayout everything since we changed the number of columns
                if (mAdapter != null) mAdapter.notifyDataSetChanged();

                // We're done
                mBinding.rclGrid.removeOnLayoutChangeListener(this);
            }
        });

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
