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
import java.util.List;
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
        final WatchfaceGridTemBinding binding;

        public ViewHolder(WatchfaceGridTemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class Payload {
        private final Set<Long> mOldSelection;
        private final Set<Long> mNewSelection;

        public Payload(Set<Long> oldSelection, Set<Long> newSelection) {
            mOldSelection = oldSelection;
            mNewSelection = newSelection;
        }

        public boolean hasBeenSelected(long id) {
            return mNewSelection.contains(id) && !mOldSelection.contains(id);
        }

        public boolean hasBeenDeselected(long id) {
            return !mNewSelection.contains(id) && mOldSelection.contains(id);
        }
    }

    private final Context mContext;
    private final WatchfaceCallbacks mCallbacks;
    private final LayoutInflater mLayoutInflater;
    @Nullable
    private WatchfaceCursor mCursor;
    private boolean mSelectionMode;
    private Set<Long> mSelection = new HashSet<>(10);
    @Nullable
    private String mSendingPublicId;
    private final float mScaleSelected;
    private final float mScaleNormal;

    public WatchfaceGridAdapter(Context context, WatchfaceCallbacks callbacks) {
        mContext = context;
        mCallbacks = callbacks;
        mLayoutInflater = LayoutInflater.from(mContext);
        mScaleSelected = mContext.getResources().getFraction(R.fraction.watchface_grid_item_scale_selected, 1, 1);
        mScaleNormal = mContext.getResources().getFraction(R.fraction.watchface_grid_item_scale_normal, 1, 1);
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        WatchfaceGridTemBinding viewDataBinding = DataBindingUtil.inflate(mLayoutInflater, R.layout.watchface_grid_tem, parent, false);
        return new ViewHolder(viewDataBinding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {}

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
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

        if (!payloads.isEmpty()) {
            Payload payload = (Payload) payloads.get(0);
            if (payload.hasBeenSelected(holder.getItemId())) {
                holder.binding.conCard.setScaleX(mScaleNormal);
                holder.binding.conCard.setScaleY(mScaleNormal);
                holder.binding.conCard.animate().scaleX(mScaleSelected).scaleY(mScaleSelected).setDuration(150);
            } else if (payload.hasBeenDeselected(holder.getItemId())) {
                holder.binding.conCard.setScaleX(mScaleSelected);
                holder.binding.conCard.setScaleY(mScaleSelected);
                holder.binding.conCard.animate().scaleX(mScaleNormal).scaleY(mScaleNormal).setDuration(150);
            }
        }
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

    public void setCursor(@Nullable Cursor cursor) {
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
            long id = mCursor.getId();
            if (mSelectionMode) {
                // Disallow selecting bundled watchfaces
                if (mCursor.getIsBundled()) return;

                // Disallow selecting selected watchface
                if (mCursor.getIsSelected()) return;

                // Toggle selected state for this item
                Set<Long> oldSelection = new HashSet<>(mSelection);
                if (mSelection.contains(id)) {
                    mSelection.remove(id);
                } else {
                    mSelection.add(id);
                }

                // If no items selected, stop selection mode
                if (mSelection.isEmpty()) {
                    mSelectionMode = false;
                }

                // Notify callbacks
                mCallbacks.onWatchfacesSelected(mSelection);

                notifyItemChanged(position, new Payload(oldSelection, mSelection));
            } else {
                if (mCursor.getIsSelected()) {
                    // Already selected: do nothing
                    return;
                }
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
                int position = (int) v.getTag();
                assert mCursor != null;
                mCursor.moveToPosition(position);

                // Disallow selecting bundled watchfaces
                if (mCursor.getIsBundled()) return true;

                // Disallow selecting selected watchface
                if (mCursor.getIsSelected()) return true;

                // Switch to selection mode
                mSelectionMode = true;

                // Add the clicked item to the selection
                long id = mCursor.getId();
                Set<Long> oldSelection = new HashSet<>(mSelection);
                mSelection.add(id);

                // Notify callbacks
                mCallbacks.onWatchfacesSelected(mSelection);

                notifyItemChanged(position, new Payload(oldSelection, mSelection));
            }
            return true;
        }
    };

    // -------------------------------------------------------------------------
    // region Selection.
    // -------------------------------------------------------------------------

    public boolean isSelectionMode() {
        return mSelectionMode;
    }

    public boolean isSelected(long id) {
        return mSelection.contains(id);
    }

    public Set<Long> getSelection() {
        return Collections.unmodifiableSet(mSelection);
    }

    public void setSelection(Set<Long> selection) {
        mSelection.clear();
        mSelection.addAll(selection);
        mSelectionMode = !mSelection.isEmpty();
        notifyDataSetChanged();
        // Notify callbacks
        mCallbacks.onWatchfacesSelected(mSelection);
    }

    public void stopSelectionMode() {
        mSelectionMode = false;
        Set<Long> oldSelection = new HashSet<>(mSelection);
        mSelection.clear();
        notifyItemRangeChanged(0, getItemCount(), new Payload(oldSelection, mSelection));
    }

    // endregion


    // -------------------------------------------------------------------------
    // region Sending.
    // -------------------------------------------------------------------------

    public boolean isSending(String publicId) {
        return publicId.equals(mSendingPublicId);
    }

    public boolean hasSendingPublicId() {
        return mSendingPublicId != null;
    }

    public void setSendingPublicId(@Nullable String sendingPublicId) {
        mSendingPublicId = sendingPublicId;
        notifyDataSetChanged();
    }

    // endregion

}
