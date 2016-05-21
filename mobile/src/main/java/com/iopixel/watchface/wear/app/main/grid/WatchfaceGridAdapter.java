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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iopixel.watchface.wear.R;
import com.iopixel.watchface.wear.app.main.WatchfaceCallbacks;
import com.iopixel.watchface.wear.backend.provider.watchface.WatchfaceCursor;
import com.iopixel.watchface.wear.databinding.WatchfaceGridTemBinding;

public class WatchfaceGridAdapter extends RecyclerView.Adapter<WatchfaceGridAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final WatchfaceGridTemBinding binding;

        public ViewHolder(WatchfaceGridTemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private final Context mContext;
    private final WatchfaceCallbacks mCallbacks;
    private final LayoutInflater mLayoutInflater;
    @Nullable
    private WatchfaceCursor mCursor;
    private boolean mSelectionMode;
    private Set<String> mSelection = new HashSet<>(10);

    public WatchfaceGridAdapter(Context context, WatchfaceCallbacks callbacks) {
        mContext = context;
        mCallbacks = callbacks;
        mLayoutInflater = LayoutInflater.from(mContext);
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        WatchfaceGridTemBinding viewDataBinding = DataBindingUtil.inflate(mLayoutInflater, R.layout.watchface_grid_tem, parent, false);
        return new ViewHolder(viewDataBinding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        assert mCursor != null;
        mCursor.moveToPosition(position);
        holder.binding.setCursor(mCursor);
        holder.binding.setAdapter(this);
        // We must execute the bindings now, otherwise they will be deferred to later,
        // and the cursor position will have changed.
        holder.binding.executePendingBindings();
        holder.binding.getRoot().setTag(position);
        holder.binding.getRoot().setOnClickListener(mOnClickListener);
        holder.binding.getRoot().setOnLongClickListener(mOnLongClickListener);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        assert mCursor != null;
        mCursor.moveToPosition(position);
        return mCursor.getId();
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor == null ? null : new WatchfaceCursor(cursor);
        notifyDataSetChanged();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            assert mCursor != null;
            mCursor.moveToPosition(position);
            String publicId = mCursor.getPublicId();
            if (mSelectionMode) {
                // Toggle selected state for this item
                if (mSelection.contains(publicId)) {
                    mSelection.remove(publicId);
                } else {
                    mSelection.add(publicId);
                }

                // If no items selected, stop selection mode
                if (mSelection.isEmpty()) {
                    mSelectionMode = false;
                }

                // Notify callbacks
                mCallbacks.onWatchfacesSelected(mSelection);

                notifyDataSetChanged();
            } else {
                mCallbacks.onWatchfaceClick(publicId);
            }
        }
    };

    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mSelectionMode) {
                // Treat long click as normal click when in selection mode
                mOnClickListener.onClick(v);
            } else {
                // Switch to selection mode
                mSelectionMode = true;
                int position = (int) v.getTag();
                assert mCursor != null;
                mCursor.moveToPosition(position);

                // Add the clicked item to the selection
                String publicId = mCursor.getPublicId();
                mSelection.add(publicId);

                // Notify callbacks
                mCallbacks.onWatchfacesSelected(mSelection);
            }
            notifyDataSetChanged();
            return true;
        }
    };

    public boolean isSelectionMode() {
        return mSelectionMode;
    }

    public boolean isSelected(String publicId) {
        return mSelection.contains(publicId);
    }

    public Set<String> getSelection() {
        return Collections.unmodifiableSet(mSelection);
    }

    public void stopSelectionMode() {
        mSelectionMode = false;
        mSelection.clear();
        notifyDataSetChanged();
    }
}
