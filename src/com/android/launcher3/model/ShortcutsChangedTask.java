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

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.os.UserHandle;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.model.data.WorkspaceItemInfo;
import com.android.launcher3.shortcuts.ShortcutKey;
import com.android.launcher3.shortcuts.ShortcutRequest;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.MultiHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import android.util.Log;

/**
 * Handles changes due to shortcut manager updates (deep shortcut changes)
 */
public class ShortcutsChangedTask extends BaseModelUpdateTask {

    private final String mPackageName;
    private final List<ShortcutInfo> mShortcuts;
    private final UserHandle mUser;
    private final boolean mUpdateIdMap;

    static final String TAG = "ShortcutsChangedTask";


    public ShortcutsChangedTask(String packageName, List<ShortcutInfo> shortcuts,
            UserHandle user, boolean updateIdMap) {
        mPackageName = packageName;
        mShortcuts = shortcuts;
        mUser = user;
        mUpdateIdMap = updateIdMap;
    }

    @Override
    public void execute(LauncherAppState app, BgDataModel dataModel, AllAppsList apps) {
        final Context context = app.getContext();
        // Find WorkspaceItemInfo's that have changed on the workspace.
        HashSet<ShortcutKey> removedKeys = new HashSet<>();
        MultiHashMap<ShortcutKey, WorkspaceItemInfo> keyToShortcutInfo = new MultiHashMap<>();
        HashSet<String> allIds = new HashSet<>();

        // Launcher launcher = Launcher.getLauncher(context);
        Log.i(TAG, "bella ShortcutsChangedTask..................mPackageName "+mPackageName);


        for (ItemInfo itemInfo : dataModel.itemsIdMap) {
            if (itemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT) {
                WorkspaceItemInfo si = (WorkspaceItemInfo) itemInfo;
                if (mPackageName.equals(si.getIntent().getPackage()) && si.user.equals(mUser)) {
                    keyToShortcutInfo.addToList(ShortcutKey.fromItemInfo(si), si);
                    allIds.add(si.getDeepShortcutId());
                }
            }
        }

        final ArrayList<WorkspaceItemInfo> updatedWorkspaceItemInfos = new ArrayList<>();
        if (!keyToShortcutInfo.isEmpty()) {
            // Update the workspace to reflect the changes to updated shortcuts residing on it.
            List<ShortcutInfo> shortcuts = new ShortcutRequest(context, mUser)
                    .forPackage(mPackageName, new ArrayList<>(allIds))
                    .query(ShortcutRequest.ALL);
            for (ShortcutInfo fullDetails : shortcuts) {
                ShortcutKey key = ShortcutKey.fromInfo(fullDetails);
                List<WorkspaceItemInfo> workspaceItemInfos = keyToShortcutInfo.remove(key);
                if (!fullDetails.isPinned()) {
                    // The shortcut was previously pinned but is no longer, so remove it from
                    // the workspace and our pinned shortcut counts.
                    // Note that we put this check here, after querying for full details,
                    // because there's a possible race condition between pinning and
                    // receiving this callback.
                    removedKeys.add(key);
                    continue;
                }
                for (final WorkspaceItemInfo workspaceItemInfo : workspaceItemInfos) {
                    workspaceItemInfo.updateFromDeepShortcutInfo(fullDetails, context);
                    app.getIconCache().getShortcutIcon(workspaceItemInfo, fullDetails);
                    updatedWorkspaceItemInfos.add(workspaceItemInfo);
                }
            }
        }

        // If there are still entries in keyToShortcutInfo, that means that
        // the corresponding shortcuts weren't passed in onShortcutsChanged(). This
        // means they were cleared, so we remove and unpin them now.
        removedKeys.addAll(keyToShortcutInfo.keySet());

        bindUpdatedWorkspaceItems(updatedWorkspaceItemInfos);
        if (!keyToShortcutInfo.isEmpty()) {
            deleteAndBindComponentsRemoved(ItemInfoMatcher.ofShortcutKeys(removedKeys));
        }

        if (mUpdateIdMap) {
            // Update the deep shortcut map if the list of ids has changed for an activity.
            dataModel.updateDeepShortcutCounts(mPackageName, mUser, mShortcuts);
            bindDeepShortcuts(dataModel);
        }
    }
}
