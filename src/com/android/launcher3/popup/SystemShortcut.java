package com.android.launcher3.popup;

import static com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_SYSTEM_SHORTCUT_APP_INFO_TAP;
import static com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_SYSTEM_SHORTCUT_WIDGETS_TAP;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView.OnEditorActionListener;

import androidx.annotation.Nullable;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.model.WidgetItem;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.model.data.WorkspaceItemInfo;
import com.android.launcher3.touch.ItemClickHandler;
import com.android.launcher3.userevent.nano.LauncherLogProto.Action;
import com.android.launcher3.userevent.nano.LauncherLogProto.ControlType;
import com.android.launcher3.util.InstantAppResolver;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.widget.WidgetsBottomSheet;

import java.util.List;
import android.util.Log;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.util.FileUtils;
import java.io.File;
import android.app.AlertDialog;
import android.content.DialogInterface;



/**
 * Represents a system shortcut for a given app. The shortcut should have a label and icon, and an
 * onClickListener that depends on the item that the shortcut services.
 *
 * Example system shortcuts, defined as inner classes, include Widgets and AppInfo.
 * @param <T>
 */
public abstract class SystemShortcut<T extends BaseDraggingActivity> extends ItemInfo
        implements View.OnClickListener {

    private final int mIconResId;
    private final int mLabelResId;
    private final int mAccessibilityActionId;
    private static final String TAG = "AppOpen";
    protected final T mTarget;
    protected final ItemInfo mItemInfo;
    protected View icon;

    public SystemShortcut(int iconResId, int labelResId, T target, ItemInfo itemInfo, View bubbleTextView) {
        mIconResId = iconResId;
        mLabelResId = labelResId;
        mAccessibilityActionId = labelResId;
        mTarget = target;
        mItemInfo = itemInfo;
        icon = bubbleTextView;
    }

    public SystemShortcut(int iconResId, int labelResId, T target, ItemInfo itemInfo) {
        mIconResId = iconResId;
        mLabelResId = labelResId;
        mAccessibilityActionId = labelResId;
        mTarget = target;
        mItemInfo = itemInfo;
    }

    public SystemShortcut(SystemShortcut<T> other) {
        mIconResId = other.mIconResId;
        mLabelResId = other.mLabelResId;
        mAccessibilityActionId = other.mAccessibilityActionId;
        mTarget = other.mTarget;
        mItemInfo = other.mItemInfo;
        icon = other.icon;
    }

    /**
     * Should be in the left group of icons in app's context menu header.
     */
    public boolean isLeftGroup() {
        return false;
    }

    public void setIconAndLabelFor(View iconView, TextView labelView) {
        iconView.setBackgroundResource(mIconResId);
        labelView.setText(mLabelResId);
    }

    public void setIconAndContentDescriptionFor(ImageView view) {
        view.setImageResource(mIconResId);
        view.setContentDescription(view.getContext().getText(mLabelResId));
    }

    public AccessibilityNodeInfo.AccessibilityAction createAccessibilityAction(Context context) {
        return new AccessibilityNodeInfo.AccessibilityAction(
                mAccessibilityActionId, context.getText(mLabelResId));
    }

    public boolean hasHandlerForAction(int action) {
        return mAccessibilityActionId == action;
    }

    public interface Factory<T extends BaseDraggingActivity> {

//        @Nullable SystemShortcut<T> getShortcut(T activity, ItemInfo itemInfo);

        @Nullable SystemShortcut<T> getShortcut(T activity, ItemInfo itemInfo, View icon);
    }

    public static final Factory<Launcher> WIDGETS = (launcher, itemInfo, bubbleTextView) -> {
        if (itemInfo.getTargetComponent() == null) return null;
        final List<WidgetItem> widgets =
                launcher.getPopupDataProvider().getWidgetsForPackageUser(new PackageUserKey(
                        itemInfo.getTargetComponent().getPackageName(), itemInfo.user));
        if (widgets == null) {
            return null;
        }
        return new Widgets(launcher, itemInfo, null);
    };

    public static class Widgets extends SystemShortcut<Launcher> {
        public Widgets(Launcher target, ItemInfo itemInfo, BubbleTextView icon) {
            super(R.drawable.ic_widget, R.string.widget_button_text, target, itemInfo, null);
        }

        @Override
        public void onClick(View view) {
            if (!Utilities.isWorkspaceEditAllowed(mTarget.getApplicationContext())) return;
            AbstractFloatingView.closeAllOpenViews(mTarget);
            WidgetsBottomSheet widgetsBottomSheet =
                    (WidgetsBottomSheet) mTarget.getLayoutInflater().inflate(
                            R.layout.widgets_bottom_sheet, mTarget.getDragLayer(), false);
            widgetsBottomSheet.populateAndShow(mItemInfo);
            mTarget.getUserEventDispatcher().logActionOnControl(Action.Touch.TAP,
                    ControlType.WIDGETS_BUTTON, view);
            mTarget.getStatsLogManager().logger().withItemInfo(mItemInfo)
                    .log(LAUNCHER_SYSTEM_SHORTCUT_WIDGETS_TAP);
        }
    }

    public static final Factory<BaseDraggingActivity> APP_INFO = AppInfo::new;
    public static final Factory<BaseDraggingActivity> APP_OPEN = AppOpen::new;
    public static final Factory<BaseDraggingActivity> APP_OPEN_TYPE = AppOpenType::new;
    public static final Factory<BaseDraggingActivity> APP_REMOVE = AppRemove::new;
    public static final Factory<BaseDraggingActivity> APP_COPY = AppCopy::new ;
    public static final Factory<BaseDraggingActivity> APP_CUT = AppCut::new ;
    public static final Factory<BaseDraggingActivity> APP_RENAME = AppRename::new ;

    public static class AppInfo extends SystemShortcut {

        public AppInfo(BaseDraggingActivity target, ItemInfo itemInfo, View icon) {
            super(R.drawable.ic_info_no_shadow, R.string.app_info_drop_target_label, target,
                    itemInfo, icon);
        }

        public AppInfo(BaseDraggingActivity target, ItemInfo itemInfo) {
            super(R.drawable.ic_info_no_shadow, R.string.app_info_drop_target_label, target,
                    itemInfo);
        }

        @Override
        public void onClick(View view) {
            dismissTaskMenuView(mTarget);
            Rect sourceBounds = mTarget.getViewBounds(view);
            new PackageManagerHelper(mTarget).startDetailsActivityForInfo(
                    mItemInfo, sourceBounds, ActivityOptions.makeBasic().toBundle());
            mTarget.getUserEventDispatcher().logActionOnControl(Action.Touch.TAP,
                    ControlType.APPINFO_TARGET, view);
            mTarget.getStatsLogManager().logger().withItemInfo(mItemInfo)
                    .log(LAUNCHER_SYSTEM_SHORTCUT_APP_INFO_TAP);
        }
    }

    public static class AppOpen extends SystemShortcut {

        public AppOpen(BaseDraggingActivity target, ItemInfo itemInfo, View bubbleTextView) {
            super(R.drawable.ic_open_app, R.string.app_open_drop_target_label, target,
                    itemInfo, bubbleTextView);
        }

        @Override
        public void onClick(View view) {
           dismissTaskMenuView(mTarget);
           ItemClickHandler.startAppShortcutOrInfoActivity(view, mItemInfo, Launcher.getLauncher(view.getContext()), null);
        }
    }

    public static class AppOpenType extends SystemShortcut {

        public AppOpenType(BaseDraggingActivity target, ItemInfo itemInfo, View bubbleTextView) {
            super(R.drawable.ic_open_type, R.string.app_open_type, target,
                    itemInfo, bubbleTextView);
        }

        @Override
        public void onClick(View view) {
           dismissTaskMenuView(mTarget);
           ItemClickHandler.appOpenLinuxType( Launcher.getLauncher(view.getContext()),mItemInfo,"type");
        }
    }

    public static class AppCopy extends SystemShortcut {

        public AppCopy(BaseDraggingActivity target, ItemInfo itemInfo, View bubbleTextView) {
            super(R.drawable.ic_copy_no_shadow, R.string.copy_drop_target, target,
                    itemInfo, bubbleTextView);
        }

        @Override
        public void onClick(View view) {
           dismissTaskMenuView(mTarget);
           ItemClickHandler.copyFiletoClipboard( Launcher.getLauncher(view.getContext()),mItemInfo);
        //    ItemClickHandler.startAppShortcutOrInfoActivity(view, mItemInfo, Launcher.getLauncher(view.getContext()), null);
        }
    }

    public static class AppCut extends SystemShortcut {

        public AppCut(BaseDraggingActivity target, ItemInfo itemInfo, View bubbleTextView) {
            super(R.drawable.ic_cut_no_shadow, R.string.cut_drop_target, target,
                    itemInfo, bubbleTextView);
        }

        @Override
        public void onClick(View view) {
           dismissTaskMenuView(mTarget);
           ItemClickHandler.cutFiletoClipboard( Launcher.getLauncher(view.getContext()),mItemInfo);
        }
    }

    public static class AppRename extends SystemShortcut {

        public AppRename(BaseDraggingActivity target, ItemInfo itemInfo, View bubbleTextView) {
            super(R.drawable.ic_rename_no_shadow, R.string.rename_drop_target, target,
                    itemInfo, bubbleTextView);
        }

        @Override
        public void onClick(View view) {
            dismissTaskMenuView(mTarget);
            ItemClickHandler.renameFiletoClipboard( Launcher.getLauncher(view.getContext()),mItemInfo);
            // Launcher launcher = Launcher.getLauncher(view.getContext());
           
        }
    }


    public static class AppRemove extends SystemShortcut {

        public AppRemove(BaseDraggingActivity target, ItemInfo itemInfo, View bubbleTextView) {
            super(R.drawable.ic_remove_no_shadow, R.string.delete_drop_target, target,
                    itemInfo, bubbleTextView);
        }

        @Override
        public void onClick(View view) {
            dismissTaskMenuView(mTarget);
            AlertDialog alertDialog = new AlertDialog.Builder(view.getContext())
            .setTitle(R.string.desktop_tips)
            .setMessage(R.string.desktop_delete_tips)
            .setNegativeButton(R.string.desktop_cancel, null)
            .setPositiveButton(R.string.desktop_delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    Launcher launcher = Launcher.getLauncher(view.getContext());
                    if(mItemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_DIRECTORY ){
                        launcher.gotoDocApp(FileUtils.DELETE_FILE,FileUtils.PATH_ID_DESKTOP+""+mItemInfo.title);
                    }else if(mItemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_DOCUMENT){
                        launcher.gotoDocApp(FileUtils.DELETE_FILE,FileUtils.PATH_ID_DESKTOP+""+mItemInfo.title);
                    }
                    dismissTaskMenuView(mTarget);
                    launcher.removeItem(icon, mItemInfo,true);
                    // launcher.deleteFavorites(mItemInfo);
                }
            }).create();
            alertDialog.show();
        }
    }

    public static final Factory<BaseDraggingActivity> INSTALL = (activity, itemInfo, bubbleTextView) -> {
        boolean supportsWebUI = (itemInfo instanceof WorkspaceItemInfo)
                && ((WorkspaceItemInfo) itemInfo).hasStatusFlag(
                        WorkspaceItemInfo.FLAG_SUPPORTS_WEB_UI);
        boolean isInstantApp = false;
        if (itemInfo instanceof com.android.launcher3.model.data.AppInfo) {
            com.android.launcher3.model.data.AppInfo
                    appInfo = (com.android.launcher3.model.data.AppInfo) itemInfo;
            isInstantApp = InstantAppResolver.newInstance(activity).isInstantApp(appInfo);
        }
        boolean enabled = supportsWebUI || isInstantApp;
        if (!enabled) {
            return null;
        }
        return new Install(activity, itemInfo, null);
    };

    public static class Install extends SystemShortcut {

        public Install(BaseDraggingActivity target, ItemInfo itemInfo, View bubbleTextView) {
            super(R.drawable.ic_install_no_shadow, R.string.install_drop_target_label,
                    target, itemInfo, bubbleTextView);
        }

        public Install(BaseDraggingActivity target, ItemInfo itemInfo) {
            super(R.drawable.ic_install_no_shadow, R.string.install_drop_target_label,
                    target, itemInfo);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new PackageManagerHelper(view.getContext()).getMarketIntent(
                    mItemInfo.getTargetComponent().getPackageName());
            mTarget.startActivitySafely(view, intent, mItemInfo, null);
            AbstractFloatingView.closeAllOpenViews(mTarget);
        }
    }

    public static void dismissTaskMenuView(BaseDraggingActivity activity) {
        AbstractFloatingView.closeOpenViews(activity, true,
            AbstractFloatingView.TYPE_ALL & ~AbstractFloatingView.TYPE_REBIND_SAFE);
    }
}
