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

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;


public class WatchfaceItemAnimator extends RecyclerView.ItemAnimator implements Animator.AnimatorListener {
    private final WatchfaceGridAdapter mAdapter;
    private boolean mRunning;

    public WatchfaceItemAnimator(WatchfaceGridAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo,
                                        @Nullable ItemHolderInfo postLayoutInfo) {
        return false;
    }

    @Override
    public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo,
                                     @NonNull ItemHolderInfo postLayoutInfo) {
        return false;
    }

    @Override
    public boolean animatePersistence(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo,
                                      @NonNull ItemHolderInfo postLayoutInfo) {
        return false;
    }

    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder,
                                 @NonNull ItemHolderInfo preLayoutInfo,
                                 @NonNull ItemHolderInfo postLayoutInfo) {

        WatchfaceGridAdapter.ViewHolder oldH = (WatchfaceGridAdapter.ViewHolder) oldHolder;
        WatchfaceGridAdapter.ViewHolder newH = (WatchfaceGridAdapter.ViewHolder) newHolder;

        if (mAdapter.isSelected(newH.getItemId())) {
            newH.binding.getRoot().animate().scaleX(.85f).scaleY(.85f).setDuration(150).setListener(this);
        } else {
            newH.binding.getRoot().animate().scaleX(1f).scaleY(1f).setDuration(150).setListener(this);
        }

        return false;
    }

    @Override
    public void runPendingAnimations() {

    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {

    }

    @Override
    public void endAnimations() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }


    //region Animator.AnimatorListener.

    @Override
    public void onAnimationStart(Animator animation) {
        mRunning = true;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mRunning = false;
    }

    @Override
    public void onAnimationCancel(Animator animation) {}

    @Override
    public void onAnimationRepeat(Animator animation) {}

    //endregion
}
