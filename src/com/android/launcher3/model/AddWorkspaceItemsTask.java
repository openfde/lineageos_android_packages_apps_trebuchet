/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.launcher3.model;

import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInstaller.SessionInfo;
import android.os.UserHandle;
import android.util.LongSparseArray;
import android.util.Pair;

import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel.CallbackTask;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.model.BgDataModel.Callbacks;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.FolderInfo;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.model.data.LauncherAppWidgetInfo;
import com.android.launcher3.model.data.WorkspaceItemInfo;
import com.android.launcher3.pm.InstallSessionHelper;
import com.android.launcher3.util.GridOccupancy;
import com.android.launcher3.util.IntArray;
import com.android.launcher3.util.PackageManagerHelper;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import com.android.launcher3.Launcher;
import com.android.launcher3.util.FileUtils;
import org.greenrobot.eventbus.EventBus;
import com.android.launcher3.model.data.MessageEvent;
/**
 * Task to add auto-created workspace items.
 */
public class AddWorkspaceItemsTask extends BaseModelUpdateTask {
    public static final String TAG = "LauncherAddWorkspaceItemsTask";

    private final List<Pair<ItemInfo, Object>> mItemList;

    /**
     * @param itemList items to add on the workspace
     */
    public AddWorkspaceItemsTask(List<Pair<ItemInfo, Object>> itemList) {
        mItemList = itemList;
    }

    @Override
    public void execute(LauncherAppState app, BgDataModel dataModel, AllAppsList apps) {
        if (mItemList.isEmpty()) {
            return;
        }

        Log.i(TAG,"AddWorkspaceItemsTask AllAppsList size " +  " , data "+apps.data.size() + ", mItemList size "+mItemList.size());
        Log.i(TAG,"AddWorkspaceItemsTask mItemList : "+mItemList);

        final ArrayList<ItemInfo> addedItemsFinal = new ArrayList<>();
        final IntArray addedWorkspaceScreensFinal = new IntArray();

        synchronized(dataModel) {
            IntArray workspaceScreens = dataModel.collectWorkspaceScreens();

            List<ItemInfo> filteredItems = new ArrayList<>();
            for (Pair<ItemInfo, Object> entry : mItemList) {
                ItemInfo item = entry.first;
                if (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                        item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
                    // Short-circuit this logic if the icon exists somewhere on the workspace
                    if (shortcutExists(dataModel, item.getIntent(), item.user)) {
                        continue;
                    }

                    // b/139663018 Short-circuit this logic if the icon is a system app
                    if (PackageManagerHelper.isSystemApp(app.getContext(), item.getIntent())) {
                        continue;
                    }
                }

                if (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                    if (item instanceof AppInfo) {
                        item = ((AppInfo) item).makeWorkspaceItem();
                    }
                }
                if (item != null) {
                    filteredItems.add(item);
                }
            }

            InstallSessionHelper packageInstaller =
                    InstallSessionHelper.INSTANCE.get(app.getContext());
            LauncherApps launcherApps = app.getContext().getSystemService(LauncherApps.class);

            for (ItemInfo item : filteredItems) {
                // Find appropriate space for the item.
                int[] coords = findSpaceForItem(app, dataModel, workspaceScreens,
                        addedWorkspaceScreensFinal, item.spanX, item.spanY);
                int screenId = coords[0];

                ItemInfo itemInfo;
                if (item instanceof WorkspaceItemInfo || item instanceof FolderInfo ||
                        item instanceof LauncherAppWidgetInfo) {
                    itemInfo = item;
                } else if (item instanceof AppInfo) {
                    itemInfo = ((AppInfo) item).makeWorkspaceItem();
                } else {
                    throw new RuntimeException("Unexpected info type");
                }

                if (item instanceof WorkspaceItemInfo && ((WorkspaceItemInfo) item).isPromise()) {
                    WorkspaceItemInfo workspaceInfo = (WorkspaceItemInfo) item;
                    String packageName = item.getTargetComponent() != null
                            ? item.getTargetComponent().getPackageName() : null;
                    if (packageName == null) {
                        continue;
                    }
                    SessionInfo sessionInfo = packageInstaller.getActiveSessionInfo(item.user,
                            packageName);
                    List<LauncherActivityInfo> activities = launcherApps
                            .getActivityList(packageName, item.user);
                    boolean hasActivity = activities != null && !activities.isEmpty();

                    if (sessionInfo == null) {
                        if (!hasActivity) {
                            // Session was cancelled, do not add.
                            continue;
                        }
                    } else {
                        workspaceInfo.setInstallProgress((int) sessionInfo.getProgress());
                    }

                    if (hasActivity) {
                        // App was installed while launcher was in the background,
                        // or app was already installed for another user.
                        itemInfo = new AppInfo(app.getContext(), activities.get(0), item.user)
                                .makeWorkspaceItem();

                        if (shortcutExists(dataModel, itemInfo.getIntent(), itemInfo.user)) {
                            // We need this additional check here since we treat all auto added
                            // workspace items as promise icons. At this point we now have the
                            // correct intent to compare against existing workspace icons.
                            // Icon already exists on the workspace and should not be auto-added.
                            continue;
                        }

                        WorkspaceItemInfo wii = (WorkspaceItemInfo) itemInfo;
                        wii.title = "";
                        wii.bitmap = app.getIconCache().getDefaultIcon(item.user);
                        app.getIconCache().getTitleAndIcon(wii,
                                ((WorkspaceItemInfo) itemInfo).usingLowResIcon());
                    }
                }

                if(itemInfo.getTargetComponent() != null  && itemInfo.getTargetComponent().getPackageName() !=null){
                    EventBus.getDefault().post(new MessageEvent(FileUtils.OP_CREATE_ANDROID_ICON,itemInfo.getTargetComponent().getPackageName()));
                }else{
                    Log.i(TAG,"AddWorkspaceItemsTask itemInfo "+itemInfo);
                }
                

                // Add the shortcut to the db
                getModelWriter().addItemToDatabase(itemInfo,
                        LauncherSettings.Favorites.CONTAINER_DESKTOP, screenId,
                        coords[1], coords[2]);

                // Save the WorkspaceItemInfo for binding in the workspace
                addedItemsFinal.add(itemInfo);
            }
        }

        if (!addedItemsFinal.isEmpty()) {
            scheduleCallbackTask(new CallbackTask() {
                @Override
                public void execute(Callbacks callbacks) {
                    final ArrayList<ItemInfo> addAnimated = new ArrayList<>();
                    final ArrayList<ItemInfo> addNotAnimated = new ArrayList<>();
                    if (!addedItemsFinal.isEmpty()) {
                        ItemInfo info = addedItemsFinal.get(addedItemsFinal.size() - 1);
                        int lastScreenId = info.screenId;
                        for (ItemInfo i : addedItemsFinal) {
                            if (i.screenId == lastScreenId) {
                                addAnimated.add(i);
                            } else {
                                addNotAnimated.add(i);
                            }
                        }
                    }
                    Log.i(TAG, "bindAppsAdded  execute........ ");
                    callbacks.bindAppsAdded(addedWorkspaceScreensFinal,
                            addNotAnimated, addAnimated);
                }
            });
        }
    }

    /**
     * Returns true if the shortcuts already exists on the workspace. This must be called after
     * the workspace has been loaded. We identify a shortcut by its intent.
     */
    protected boolean shortcutExists(BgDataModel dataModel, Intent intent, UserHandle user) {
        final String compPkgName, intentWithPkg, intentWithoutPkg;
        if (intent == null) {
            // Skip items with null intents
            return true;
        }
        if (intent.getComponent() != null) {
            // If component is not null, an intent with null package will produce
            // the same result and should also be a match.
            compPkgName = intent.getComponent().getPackageName();
            if (intent.getPackage() != null) {
                intentWithPkg = intent.toUri(0);
                intentWithoutPkg = new Intent(intent).setPackage(null).toUri(0);
            } else {
                intentWithPkg = new Intent(intent).setPackage(compPkgName).toUri(0);
                intentWithoutPkg = intent.toUri(0);
            }
        } else {
            compPkgName = null;
            intentWithPkg = intent.toUri(0);
            intentWithoutPkg = intent.toUri(0);
        }

        boolean isLauncherAppTarget = PackageManagerHelper.isLauncherAppTarget(intent);
        synchronized (dataModel) {
            for (ItemInfo item : dataModel.itemsIdMap) {
                if (item instanceof WorkspaceItemInfo) {
                    WorkspaceItemInfo info = (WorkspaceItemInfo) item;
                    if (item.getIntent() != null && info.user.equals(user)) {
                        Intent copyIntent = new Intent(item.getIntent());
                        copyIntent.setSourceBounds(intent.getSourceBounds());
                        String s = copyIntent.toUri(0);
                        if (intentWithPkg.equals(s) || intentWithoutPkg.equals(s)) {
                            return true;
                        }

                        // checking for existing promise icon with same package name
                        if (isLauncherAppTarget
                                && info.isPromise()
                                && info.hasStatusFlag(WorkspaceItemInfo.FLAG_AUTOINSTALL_ICON)
                                && info.getTargetComponent() != null
                                && compPkgName != null
                                && compPkgName.equals(info.getTargetComponent().getPackageName())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Find a position on the screen for the given size or adds a new screen.
     * @return screenId and the coordinates for the item in an int array of size 3.
     */
    protected int[] findSpaceForItem( LauncherAppState app, BgDataModel dataModel,
            IntArray workspaceScreens, IntArray addedWorkspaceScreensFinal, int spanX, int spanY) {
        LongSparseArray<ArrayList<ItemInfo>> screenItems = new LongSparseArray<>();

        // Use sBgItemsIdMap as all the items are already loaded.
        synchronized (dataModel) {
            for (ItemInfo info : dataModel.itemsIdMap) {
                if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    ArrayList<ItemInfo> items = screenItems.get(info.screenId);
                    if (items == null) {
                        items = new ArrayList<>();
                        screenItems.put(info.screenId, items);
                    }
                    items.add(info);
                }
            }
        }

        // Find appropriate space for the item.
        int screenId = 0;
        int[] cordinates = new int[2];
        boolean found = false;

        int screenCount = workspaceScreens.size();
        // First check the preferred screen.
        int preferredScreenIndex =  0 ;// workspaceScreens.isEmpty() ? 0 : 1;
        if (preferredScreenIndex < screenCount) {
            screenId = workspaceScreens.get(preferredScreenIndex);
            found = findNextAvailableIconSpaceInScreen(
                    app, screenItems.get(screenId), cordinates, spanX, spanY);
        }

        if (!found) {
            // Search on any of the screens starting from the first screen.
            for (int screen = 1; screen < screenCount; screen++) {
                screenId = workspaceScreens.get(screen);
                if (findNextAvailableIconSpaceInScreen(
                        app, screenItems.get(screenId), cordinates, spanX, spanY)) {
                    // We found a space for it
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            // Still no position found. Add a new screen to the end.
            screenId = LauncherSettings.Settings.call(app.getContext().getContentResolver(),
                    LauncherSettings.Settings.METHOD_NEW_SCREEN_ID)
                    .getInt(LauncherSettings.Settings.EXTRA_VALUE);

            // Save the screen id for binding in the workspace
            workspaceScreens.add(screenId);
            addedWorkspaceScreensFinal.add(screenId);

            // If we still can't find an empty space, then God help us all!!!
            if (!findNextAvailableIconSpaceInScreen(
                    app, screenItems.get(screenId), cordinates, spanX, spanY)) {
                throw new RuntimeException("Can't find space to add the item");
            }
        }
        return new int[] {screenId, cordinates[0], cordinates[1]};
    }

    private boolean findNextAvailableIconSpaceInScreen(
            LauncherAppState app, ArrayList<ItemInfo> occupiedPos,
            int[] xy, int spanX, int spanY) {
        InvariantDeviceProfile profile = app.getInvariantDeviceProfile();

        GridOccupancy occupied = new GridOccupancy(profile.numColumns, profile.numRows);
        if (occupiedPos != null) {
            for (ItemInfo r : occupiedPos) {
                occupied.markCells(r, true);
            }
        }
        return occupied.findVacantCellVertical(xy, spanX, spanY);
    }

}
