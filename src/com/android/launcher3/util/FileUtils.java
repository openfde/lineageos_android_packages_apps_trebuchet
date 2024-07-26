package com.android.launcher3.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import android.util.Log;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import com.android.launcher3.model.data.WorkspaceItemInfo;
import com.android.launcher3.LauncherSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;


public class FileUtils {
    public static final String PATH_ID_DESKTOP = "/mnt/sdcard/Desktop/";
    protected static final String TAG = "FileUtils";


private static String getUniqueFileName(String documentId,String fileName ) {
    String name = fileName ;
    String extension = "" ;
    if(fileName.contains(".") && fileName.length() > 0){
         name = fileName.substring(0, fileName.lastIndexOf('.'));
         extension = fileName.substring(fileName.lastIndexOf('.'));
    }else{

    }
  
    String newName = name;
    int count = 0;
    File newFile;
    do {
        count++;
        newName = name + "_" + count + extension;
        newFile = new File(documentId,newName);
    } while (newFile.exists());

    return newName;
}

public static Bitmap drawableToBitmap(Drawable drawable) {
    int width = drawable.getIntrinsicWidth();
    int height = drawable.getIntrinsicHeight();
    Bitmap bitmap = Bitmap.createBitmap(
            width,
            height,
            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565
    );
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, width, height);
    drawable.draw(canvas);
    return bitmap;
}
    
public static Drawable getAppIcon(Context context, String packageName) {
    PackageManager pm = context.getPackageManager();
    try {
        ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
        return pm.getApplicationIcon(appInfo);
    } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
        return null;
    }
}

public static void createShortcut(Context mContext, String packageName ,String name ) {
    Icon icon = Icon.createWithBitmap(drawableToBitmap(getAppIcon(mContext,packageName)));
    ShortcutManager shortcutManager = (ShortcutManager) mContext.getSystemService(ShortcutManager.class);
    if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
        Intent launchIntentForPackage = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntentForPackage != null) {
            launchIntentForPackage.setAction(Intent.ACTION_MAIN);
            ShortcutInfo pinShortcutInfo = new ShortcutInfo.Builder(mContext, name)
                    .setLongLabel(name)
                    .setShortLabel(name)
                    .setIcon(icon)
                    .setIntent(launchIntentForPackage)
                    .build();
            Intent pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo);
            PendingIntent successCallback = PendingIntent.getBroadcast(
                    mContext, 0,
                    pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE
            );
            shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.getIntentSender());
        }
    }
}


/**
 * get desktop count
 */
public static int getDesktopFileCount (){
    String documentId = FileUtils.PATH_ID_DESKTOP; 
    File parent = new File(documentId);
    File[] files = parent.listFiles();
    if(files !=null){
        return files.length;
    }else{
        return 0 ;
    }
}

public static List<WorkspaceItemInfo> getDesktop(int count){
    String documentId = FileUtils.PATH_ID_DESKTOP; 
    File parent = new File(documentId);
    File[] files = parent.listFiles();
    if(files !=null){
        Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
        Log.d(TAG, "Launcher_bindItems: files size  "+files.length + ",count "+count );
        int otherCount = count; //- files.length + 1;
    
        List<WorkspaceItemInfo> list = new ArrayList();
        int scale = 9 ;
        int index = 0;
        int xindex = otherCount / scale;
        int yindex = otherCount % scale; 
        for(File f : files){
            WorkspaceItemInfo info = new WorkspaceItemInfo();
            ComponentName mComponentName = new ComponentName("com.android.documentsui","com.android.documentsui.LauncherActivity");
            info.mComponentName = mComponentName;
            info.title = f.getName();
            info.container = -100;
            info.screenId = 0;
            int y = yindex + index ;
            info.cellY = y%scale ;
            info.cellX = xindex + y/scale;
            info.id =  300 + (info.cellX *1000) + (info.cellY *10) ;

            Log.d(TAG, "Launcher_bindItems: files info.cellX  "+info.cellX + " ,info.cellY: "+info.cellY + " ,info.title: "+info.title +",index "+ index +",xindex  "+xindex +", yindex "+yindex);

            if(f.isDirectory()){
                info.itemType = LauncherSettings.Favorites.ITEM_TYPE_DIRECTORY;
            }else{
                info.itemType = LauncherSettings.Favorites.ITEM_TYPE_DOCUMENT;
            }
            list.add(info);
            index++;
        }
        return list;
    }else{
        Log.d(TAG, "bindItems: files is null  " );
    }
    return null ;    
}

/**
     * get file type
     * @param filePath
     * @return
     */
    public static String getFileTyle (String filePath){
        try {
            File file = new File(filePath);
            String fileName = file.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                String extension = fileName.substring(dotIndex + 1);
                return  extension;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
