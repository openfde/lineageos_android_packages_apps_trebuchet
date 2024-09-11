package com.android.launcher3.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import android.util.Log;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;


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
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.LauncherAppState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import android.content.ContentValues;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.SystemProperties;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.io.FileFilter;
import java.util.Locale;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import android.graphics.Point;
import android.text.TextUtils;
import java.io.FileReader;
import java.io.IOException;


public class FileUtils {
    public static final String PATH_ID_DESKTOP = "/mnt/sdcard/Desktop/";
    protected static final String TAG = "FileUtils";

    public static final String OPEN_DIR = "OPEN_DIR";

    public static final String OPEN_FILE = "OPEN_FILE";

    public static final String OPEN_LINUX_APP = "OPEN_LINUX_APP";

    public static final String DELETE_DIR = "DELETE_DIR";

    public static final String DELETE_FILE = "DELETE_FILE";

    public static final String NEW_DIR = "NEW_DIR";

    public static final String NEW_FILE = "NEW_FILE";

    public static final String COPY_DIR = "COPY_DIR";

    public static final String COPY_FILE = "COPY_FILE";

    public static final String CUT_DIR = "CUT_DIR";

    public static final String CUT_FILE = "CUT_FILE";

    public static final String PASTE_DIR = "PASTE_DIR";

    public static final String PASTE_FILE = "PASTE_FILE";

    public static final String RENAME_DIR = "RENAME_DIR";

    public static final String RENAME_FILE = "RENAME_FILE";

    public static final String DIR_INFO = "DIR_INFO";

    public static final String FILE_INFO = "FILE_INFO";

    public static final String OP_CREATE_LINUX_ICON = "OP_CREATE_LINUX_ICON";

    public static final String OP_CREATE_ANDROID_ICON = "OP_CREATE_ANDROID_ICON";


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

/**
 * rows count  --- 9 
 */
public static int getScreenRows(Context context){
    InvariantDeviceProfile idp = LauncherAppState.getIDP(context);
    int numRows = idp.numRows;
    return numRows;
}

/**
 * Columns count  --- 17 
 */
public static int getScreenColumns(Context context){
    InvariantDeviceProfile idp = LauncherAppState.getIDP(context);
    int numColumns = idp.numColumns ;
    return numColumns;
}

/**
 * find next free point
 */
public static Point findNextFreePoint(Context context){
    int numRows  =  getScreenRows(context);
    int numColumns  =  getScreenColumns(context);

    Point point = new Point(-1,-1);
    outer: 
    for(int i = 0 ; i < numColumns ; i++ ){
        for(int j = 0 ; j < numRows ; j++){
            if(DbUtils.queryFilesByPointFromDatabase(context,i,j) == null){
                point.x = i ;
                point.y = j ;
                break outer;
            }
        }
    }
    Log.i(TAG, "queryAllFilesFromDatabase: x:  "+point.x + " , y: "+point.y);
    return point ;
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

    public static  String readLinuxConfigFile() {
        String filePath = "/volumes/.fde_path_key";
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }

    public static int findNthSlashIndex(String str, int n) {
        int index = -1;
        int count = 0;

        // 从头开始查找斜杠
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '/') {
                count++;
                if (count == n) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public static String getLinuxHomeDir(){
        try {
             String propertyValue = SystemProperties.get("waydroid.host_data_path");
             int len = findNthSlashIndex(propertyValue,3);
             return  propertyValue.substring(0,len);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return "/";
     }

    public static String getLinuxUUID(){
        String result = null;
        try {
            String jsonString = readLinuxConfigFile();
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String uuid = jsonObject.getString("UUID");
                String path = jsonObject.getString("Path");
                if ("/".equals(path)) {
                    result = uuid;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static void createLinuxDesktopFile(ContentValues initialValues){
        if(initialValues !=null){
            Log.i(TAG,"bella...insert....3......... "+initialValues.toString());

            String title  = initialValues.get("title").toString();
            int itemType  = Integer.valueOf(initialValues.get("itemType").toString());

            if(title.contains(".desktop") || itemType == LauncherSettings.Favorites.ITEM_TYPE_DIRECTORY || itemType == LauncherSettings.Favorites.ITEM_TYPE_DOCUMENT){
                return ;
            }

            String pathDesktop = PATH_ID_DESKTOP+title+"_fde.desktop";
            File file = new File(pathDesktop);
            if(file.exists()){
                Log.i(TAG,"bella...pathDesktop is exists :  "+pathDesktop);
                return ;
            }
            Path desktopFilePath = Paths.get(pathDesktop);
            String picPath = "/volumes"+"/"+getLinuxUUID()+"/tmp/"+title+".png";
            file = new File(picPath);
            if(!file.exists()){
                Log.i(TAG,"bella...insert.............picPath: "+picPath);
            }else{
                //if pic exists ,return 
            }    


            List<String> lines = List.of(
                "[Desktop Entry]",
                "Type=Application",
                "Name="+title,
                "Name[zh_CN]="+title,
                "Categories="+itemType,
                "Exec=/usr/bin/fde_utils start",
                "Icon="+picPath
            );
     
            // 写入.desktop文件
            try {
                Files.write(desktopFilePath, lines, StandardOpenOption.CREATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

      // 读取文件内容到字符串
    public static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public static Map<String,Object> getLinuxContentString(String fileName){
        String filePath = "/volumes/da9e61df-57e9-4f6c-9550-fcd12b06f0e9/home/xudingqiang/桌面/"+fileName;
        String startChar = "[Desktop";  // 查找以字母 'A' 开头的段落
        Map<String,Object> map = new HashMap<>();
        try {
            // 读取文件内容
            String content = readFile(filePath);
            // 查找以指定字开头的段落

            int firstIndex  = content.indexOf("[Desktop");
            int secondIndex = content.indexOf("[Desktop", firstIndex + 1);
            // Log.i(TAG,"bella...firstIndex: "+firstIndex + ", secondIndex: "+secondIndex);
            if(secondIndex != -1){
                content = content.substring(firstIndex,secondIndex);
            }
        
            String []paragraphs = content.split("\n");
            for (String paragraph : paragraphs) {
                int equalIndex = paragraph.indexOf('=');
                if (equalIndex != -1) {
                    String key = paragraph.substring(0, equalIndex).trim();
                    String value = paragraph.substring(equalIndex + 1).trim();
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  map;
    }

    public  static Map<String,Object> getLinuxDesktopFileContent(String fileName ){
        Map<String,Object> mp = getLinuxContentString(fileName);
        Map<String,Object> map = new HashMap<>();
        try{
            String name = mp.get("Name").toString();
            String exec = mp.get("Exec").toString();
            String icon = mp.get("Icon").toString();
            map.put("name",name);
            map.put("exec",exec);
            map.put("icon",icon);
         
            String nameZh = mp.get("Name[zh_CN]").toString();
            if(nameZh !=null ){
                map.put("nameZh",nameZh);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return map;
    }

    public static File[]  findFilesByName(File directory, final String fileName) {
        if (directory == null || !directory.isDirectory()) {
            return null;
        }
 
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().equals(fileName);
            }
        });
 
        return files;
    }

    public static boolean isChineseLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.equals("zh");
    }

}
