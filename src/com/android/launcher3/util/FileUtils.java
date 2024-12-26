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
import java.lang.reflect.Method;

public class FileUtils {
    public static final String PATH_ID_DESKTOP = "/mnt/sdcard/Desktop/";
    protected static final String TAG = "FileUtils";

    public static final String OPEN_DIR = "OPEN_DIR";

    public static final String OPEN_FILE = "OPEN_FILE";

    public static final String OPEN_LINUX_APP = "OPEN_LINUX_APP";

    public static final String CLICK_BLANK = "CLICK_BLANK";

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

    public static final String OP_INIT = "OP_INIT";

    public static final String RENAME_DIR = "RENAME_DIR";

    public static final String RENAME_FILE = "RENAME_FILE";

    public static final String DIR_INFO = "DIR_INFO";

    public static final String FILE_INFO = "FILE_INFO";

    public static final String OP_CREATE_LINUX_ICON = "OP_CREATE_LINUX_ICON";

    public static final String OP_CREATE_ANDROID_ICON = "OP_CREATE_ANDROID_ICON";

    public static boolean isOpenLinuxApp = true ;

public static void createDesktopDir(String path){
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
}    

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

    public static String getPackageNameByAppName(Context context, String appName) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            String appLabel = (String) packageManager.getApplicationLabel(app);  // 获取应用的显示名称
            if (appLabel != null && appLabel.equalsIgnoreCase(appName)) {
                return app.packageName;  
            }
        }
        return null;  
    }

    public static void createLinuxDesktopFile(ContentValues initialValues){
        // desktop linux app temp delete 
        // if(!isOpenLinuxApp){
        //     return ;
        // }
        Log.i(TAG,"bella...insert....2......... "+initialValues.toString());
        createDesktopDir(PATH_ID_DESKTOP);
        if(initialValues !=null){
            try{
                String title  = initialValues.get("title").toString();
                if(initialValues.get("packageName") == null){
                    Log.e(TAG,"bella packageName is null  ");
                    return ;
                }
                String packageName  = initialValues.get("packageName").toString();
                int itemType  = Integer.valueOf(initialValues.get("itemType").toString());
    
                if(title.contains(".desktop") || itemType == LauncherSettings.Favorites.ITEM_TYPE_DIRECTORY || itemType == LauncherSettings.Favorites.ITEM_TYPE_DOCUMENT){
                    return ;
                }
    
                String documentId =  "/volumes"+"/"+getLinuxUUID()+getLinuxHomeDir()+"/桌面/";  
                File ff = new File(documentId);
                if(!ff.exists()){
                    documentId =  "/volumes"+"/"+getLinuxUUID()+getLinuxHomeDir()+"/Desktop/";  
                }
      
                String subPackageName = packageName;
                if(packageName.length() > 10){
                    subPackageName = packageName.replace("com.","");
                }
                String pathDesktop = documentId+""+ subPackageName+"_fde.desktop";
                File file = new File(pathDesktop);
                if(file.exists()){
                    Log.i(TAG,"bella...pathDesktop is exists :  "+pathDesktop);
                    return ;
                }
                Path desktopFilePath = Paths.get(pathDesktop);

                String picPath = "/volumes"+"/"+getLinuxUUID()+getLinuxHomeDir()+"/.local/share/icons/"+title+".png" ;
                File filePic = new File(picPath);
                String homeDir = getLinuxHomeDir();
                String linuxPath = homeDir+"/.local/share/icons/"+title+".png";
                Log.i(TAG,"bella...homeDir :  "+homeDir + ",linuxPath: "+linuxPath);
                File linuxPic = new File(linuxPath);
                if(!linuxPic.exists()){
                    Log.i(TAG,"bella...insert.............picPath: "+picPath +  ", linuxPath "+linuxPath + ",packageName:  "+packageName);
                }else{
                    //if pic exists ,return 
                }    
    
                List<String> lines = List.of(
                    "[Desktop Entry]",
                    "Type=Application",
                    "Name="+title,
                    "Name[zh_CN]="+title,
                    "Categories="+itemType,
                    "Exec=fde_launch "+packageName,
                    "Icon="+linuxPic
                );
         
                // 写入.desktop文件
                Files.write(desktopFilePath, lines, StandardOpenOption.CREATE);
                file.setExecutable(true);
 
            }catch(Exception e){
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
        String documentId =  "/volumes"+"/"+getLinuxUUID()+getLinuxHomeDir()+"/桌面/";  
        File ff = new File(documentId);
        if(!ff.exists()){
            documentId =  "/volumes"+"/"+getLinuxUUID()+getLinuxHomeDir()+"/Desktop/";  
        }
        String filePath = documentId +fileName;
        String startChar = "[Desktop";  // 
        Map<String,Object> map = null;
        try {
            // 读取文件内容
            map = new HashMap<>();
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
        Map<String,Object> map = null;
        try{
            map = new HashMap<>();
            if(mp.get("Name") !=null){
                map.put("name",mp.get("Name").toString());
            }
            
            if(mp.get("Exec") !=null){
                map.put("exec",mp.get("Exec").toString());
            }
            if(mp.get("Icon") !=null){
                map.put("icon",mp.get("Icon").toString());
            }
                     
            if(mp.get("Name[zh_CN]") != null ){
                map.put("nameZh",mp.get("Name[zh_CN]").toString());
            }else{
                map.put("nameZh",mp.get("Name").toString()); 
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

     // 递归查找文件
     public static String findFileInDirectory(File directory, String fileName) {
        File[] files = directory.listFiles();  
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 如果是目录，递归查找子目录
                    String found = findFileInDirectory(file, fileName);
                    if (found != null) {
                        return found;
                    }
                } else if (file.getName().equals(fileName)) {
                    // 找到文件
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }

    public static void setSystemProperty(String key, String value) {
        try {
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method setMethod = systemPropertiesClass.getDeclaredMethod("set", String.class, String.class);
            setMethod.invoke(null, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true; // app installed
        } catch (PackageManager.NameNotFoundException e) {
            return false; // app not install
        }
    }


    public static String findLinuxIconPath(String fileName){
        String absoluteIcon = "/volumes"+"/"+getLinuxUUID()+fileName ;
        File file = new File(absoluteIcon);
        if(file.exists()){
            return absoluteIcon;
        }else {
            absoluteIcon =  "/volumes"+"/"+getLinuxUUID()+"/usr/share/kylin-software-center/data/icons/"+fileName;
            file = new File(absoluteIcon);
            if(file.exists()){
                return absoluteIcon;
            }else{
                absoluteIcon =  "/volumes"+"/"+getLinuxUUID()+"/usr/share/icons/ukui-icon-theme-default/128x128/apps/"+fileName;
                file = new File(absoluteIcon);
                if(file.exists()){
                    return absoluteIcon;
                }else{
                    absoluteIcon =  "/volumes"+"/"+getLinuxUUID()+"/usr/share/icons/hicolor/scalable/apps/"+fileName;
                    file = new File(absoluteIcon);
                    if(file.exists()){
                        return absoluteIcon;
                    }else{
                        absoluteIcon =  "/volumes"+"/"+getLinuxUUID()+"/usr/share/icons/ukui-icon-theme-default/32x32/apps/"+fileName;                        file = new File(absoluteIcon);
                        file = new File(absoluteIcon);
                        if(file.exists()){
                            return absoluteIcon;
                        }else{
                            absoluteIcon =  "/volumes"+"/"+getLinuxUUID()+"/usr/share/icons/hicolor/256x256/apps/"+fileName;                        file = new File(absoluteIcon);
                            file = new File(absoluteIcon);
                            if(file.exists()){
                                return absoluteIcon;
                            }else{
                                absoluteIcon =  "/volumes"+"/"+getLinuxUUID()+"/usr/share/icons/Vintage/apps/32/"+fileName;                        file = new File(absoluteIcon);
                                file = new File(absoluteIcon);
                                if(file.exists()){
                                    return absoluteIcon;
                                }
                                return null ;
                            }
                        }
                    }
                }
            }
        }
    }

}
