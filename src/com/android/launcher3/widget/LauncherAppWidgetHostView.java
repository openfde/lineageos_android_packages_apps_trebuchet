/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.widget;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.SystemClock;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView;
import android.widget.Advanceable;
import android.widget.RemoteViews;

import com.android.launcher3.CheckLongPressHelper;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.model.data.LauncherAppWidgetInfo;
import com.android.launcher3.popup.PopupContainerWithArrow;
import com.android.launcher3.util.Executors;
import com.android.launcher3.util.Themes;
import com.android.launcher3.views.BaseDragLayer.TouchCompleteListener;
import com.android.launcher3.views.DoubleShadowBubbleTextView;

/**
 * {@inheritDoc}
 */
public class LauncherAppWidgetHostView extends NavigableAppWidgetHostView
        implements TouchCompleteListener, View.OnLongClickListener {

    // Related to the auto-advancing of widgets
    private static final long ADVANCE_INTERVAL = 20000;
    private static final long ADVANCE_STAGGER = 250;

    // Maintains a list of widget ids which are supposed to be auto advanced.
    private static final SparseBooleanArray sAutoAdvanceWidgetIds = new SparseBooleanArray();

    protected final LayoutInflater mInflater;

    private final CheckLongPressHelper mLongPressHelper;
    protected final Launcher mLauncher;

    @ViewDebug.ExportedProperty(category = "launcher")
    private boolean mReinflateOnConfigChange;

    private boolean mIsScrollable;
    private boolean mIsAttachedToWindow;
    private boolean mIsAutoAdvanceRegistered;
    private Runnable mAutoAdvanceRunnable;



    public LauncherAppWidgetHostView(Context context) {
        super(context);
        mLauncher = Launcher.getLauncher(context);
        mLongPressHelper = new CheckLongPressHelper(this, this);
        mInflater = LayoutInflater.from(context);
        setAccessibilityDelegate(mLauncher.getAccessibilityDelegate());
        setBackgroundResource(R.drawable.widget_internal_focus_bg);

        if (Utilities.ATLEAST_OREO) {
            setExecutor(Executors.THREAD_POOL_EXECUTOR);
        }
        if (Utilities.ATLEAST_Q && Themes.getAttrBoolean(mLauncher, R.attr.isWorkspaceDarkText)) {
            setOnLightBackground(true);
        }
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getButtonState() == MotionEvent.BUTTON_SECONDARY){
                    PopupContainerWithArrow.showForIcon(LauncherAppWidgetHostView.this);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onLongClick(View view) {
        // PopupContainerWithArrow.showForIcon(this);
        if (!Utilities.isWorkspaceEditAllowed(mLauncher.getApplicationContext())) return true;
        if (mIsScrollable) {
            DragLayer dragLayer = Launcher.getLauncher(getContext()).getDragLayer();
            dragLayer.requestDisallowInterceptTouchEvent(false);
        }
        view.performLongClick();
        return true;
    }

    @Override
    protected View getErrorView() {
        return mInflater.inflate(R.layout.appwidget_error, this, false);
    }

    @Override
    public void updateAppWidget(RemoteViews remoteViews) {
        super.updateAppWidget(remoteViews);

        // The provider info or the views might have changed.
        checkIfAutoAdvance();

        // It is possible that widgets can receive updates while launcher is not in the foreground.
        // Consequently, the widgets will be inflated for the orientation of the foreground activity
        // (framework issue). On resuming, we ensure that any widgets are inflated for the current
        // orientation.
        mReinflateOnConfigChange = !isSameOrientation();
    }

    private boolean isSameOrientation() {
        return mLauncher.getResources().getConfiguration().orientation ==
                mLauncher.getOrientation();
    }

    private boolean checkScrollableRecursively(ViewGroup viewGroup) {
        if (viewGroup instanceof AdapterView) {
            return true;
        } else {
            for (int i=0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof ViewGroup) {
                    if (checkScrollableRecursively((ViewGroup) child)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getButtonState() == MotionEvent.BUTTON_SECONDARY){
            PopupContainerWithArrow.showForIcon(this);
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            DragLayer dragLayer = Launcher.getLauncher(getContext()).getDragLayer();
            if (mIsScrollable) {
                dragLayer.requestDisallowInterceptTouchEvent(true);
            }
            dragLayer.setTouchCompleteListener(this);
        }
        mLongPressHelper.onTouchEvent(ev);
        return mLongPressHelper.hasPerformedLongPress();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        mLongPressHelper.onTouchEvent(ev);
        // We want to keep receiving though events to be able to cancel long press on ACTION_UP
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mIsAttachedToWindow = true;
        checkIfAutoAdvance();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // We can't directly use isAttachedToWindow() here, as this is called before the internal
        // state is updated. So isAttachedToWindow() will return true until next frame.
        mIsAttachedToWindow = false;
        checkIfAutoAdvance();
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mLongPressHelper.cancelLongPress();
    }

    @Override
    public AppWidgetProviderInfo getAppWidgetInfo() {
        AppWidgetProviderInfo info = super.getAppWidgetInfo();
        if (info != null && !(info instanceof LauncherAppWidgetProviderInfo)) {
            throw new IllegalStateException("Launcher widget must have"
                    + " LauncherAppWidgetProviderInfo");
        }
        return info;
    }

    @Override
    public void onTouchComplete() {
        if (!mLongPressHelper.hasPerformedLongPress()) {
            // If a long press has been performed, we don't want to clear the record of that since
            // we still may be receiving a touch up which we want to intercept
            mLongPressHelper.cancelLongPress();
        }
    }

    public void switchToErrorView() {
        // Update the widget with 0 Layout id, to reset the view to error view.
        updateAppWidget(new RemoteViews(getAppWidgetInfo().provider.getPackageName(), 0));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            super.onLayout(changed, left, top, right, bottom);
        } catch (final RuntimeException e) {
            post(new Runnable() {
                @Override
                public void run() {
                    switchToErrorView();
                }
            });
        }

        mIsScrollable = checkScrollableRecursively(this);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(getClass().getName());
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        maybeRegisterAutoAdvance();
    }

    private void checkIfAutoAdvance() {
        boolean isAutoAdvance = false;
        Advanceable target = getAdvanceable();
        if (target != null) {
            isAutoAdvance = true;
            target.fyiWillBeAdvancedByHostKThx();
        }

        boolean wasAutoAdvance = sAutoAdvanceWidgetIds.indexOfKey(getAppWidgetId()) >= 0;
        if (isAutoAdvance != wasAutoAdvance) {
            if (isAutoAdvance) {
                sAutoAdvanceWidgetIds.put(getAppWidgetId(), true);
            } else {
                sAutoAdvanceWidgetIds.delete(getAppWidgetId());
            }
            maybeRegisterAutoAdvance();
        }
    }

    private Advanceable getAdvanceable() {
        AppWidgetProviderInfo info = getAppWidgetInfo();
        if (info == null || info.autoAdvanceViewId == NO_ID || !mIsAttachedToWindow) {
            return null;
        }
        View v = findViewById(info.autoAdvanceViewId);
        return (v instanceof Advanceable) ? (Advanceable) v : null;
    }

    private void maybeRegisterAutoAdvance() {
        Handler handler = getHandler();
        boolean shouldRegisterAutoAdvance = getWindowVisibility() == VISIBLE && handler != null
                && (sAutoAdvanceWidgetIds.indexOfKey(getAppWidgetId()) >= 0);
        if (shouldRegisterAutoAdvance != mIsAutoAdvanceRegistered) {
            mIsAutoAdvanceRegistered = shouldRegisterAutoAdvance;
            if (mAutoAdvanceRunnable == null) {
                mAutoAdvanceRunnable = this::runAutoAdvance;
            }

            handler.removeCallbacks(mAutoAdvanceRunnable);
            scheduleNextAdvance();
        }
    }

    private void scheduleNextAdvance() {
        if (!mIsAutoAdvanceRegistered) {
            return;
        }
        long now = SystemClock.uptimeMillis();
        long advanceTime = now + (ADVANCE_INTERVAL - (now % ADVANCE_INTERVAL)) +
                ADVANCE_STAGGER * sAutoAdvanceWidgetIds.indexOfKey(getAppWidgetId());
        Handler handler = getHandler();
        if (handler != null) {
            handler.postAtTime(mAutoAdvanceRunnable, advanceTime);
        }
    }

    private void runAutoAdvance() {
        Advanceable target = getAdvanceable();
        if (target != null) {
            target.advance();
        }
        scheduleNextAdvance();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Only reinflate when the final configuration is same as the required configuration
        if (mReinflateOnConfigChange && isSameOrientation()) {
            mReinflateOnConfigChange = false;
            reInflate();
        }
    }

    public void reInflate() {
        if (!isAttachedToWindow()) {
            return;
        }
        LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) getTag();
        // Remove and rebind the current widget (which was inflated in the wrong
        // orientation), but don't delete it from the database
        mLauncher.removeItem(this, info, false  /* deleteFromDb */);
        mLauncher.bindAppWidget(info);
    }

    @Override
    protected boolean shouldAllowDirectClick() {
        if (getTag() instanceof ItemInfo) {
            ItemInfo item = (ItemInfo) getTag();
            return item.spanX == 1 && item.spanY == 1;
        }
        return false;
    }
}
