package com.android.launcher3.util;

import android.database.Cursor;
import android.content.Context;
import android.util.Log;
import android.text.TextUtils;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.model.data.ItemInfo;


import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;


public class DbUtils {
    protected static final String TAG = "DbUtils";


    public static Map<String,Object> testFilesByPointFromDatabase(Context context,int x, int y){

        // SQLiteDatabase db = context.getReadableDatabase();
        // String selection = "PACKAGE_NAME = ? AND KEY_CODE = ? AND IS_DEL != 1";
        // String[] selectionArgs = {packageName, keycode};
        // Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        // Map<String, Object> mp = new HashMap<>();
        // if (cursor != null && cursor.moveToFirst()) {
        //     int _ID = cursor.getInt(cursor.getColumnIndex("_ID"));
        //     String PACKAGE_NAME = cursor.getString(cursor.getColumnIndex("PACKAGE_NAME"));
        //     String KEY_CODE = cursor.getString(cursor.getColumnIndex("KEY_CODE"));
        //     String VALUE = cursor.getString(cursor.getColumnIndex("VALUE"));
        //     String IS_DEL = cursor.getString(cursor.getColumnIndex("IS_DEL"));
        //     String CREATE_DATE = cursor.getString(cursor.getColumnIndex("CREATE_DATE"));
        //     String EDIT_DATE = cursor.getString(cursor.getColumnIndex("EDIT_DATE"));
        //     String FIELDS1 = cursor.getString(cursor.getColumnIndex("FIELDS1"));
        //     mp.put("_ID", _ID);
        //     mp.put("PACKAGE_NAME", PACKAGE_NAME);
        //     mp.put("KEY_CODE", KEY_CODE);
        //     mp.put("VALUE", VALUE);
        //     mp.put("IS_DEL", IS_DEL);
        //     mp.put("CREATE_DATE", CREATE_DATE);
        //     mp.put("EDIT_DATE", EDIT_DATE);
        //     mp.put("FIELDS1", FIELDS1);
        // }
        // cursor.close();
        // db.close();
        // return mp;

        // String selection = "cellX = ? and cellY = ? ";
        // String[] selectionArgs = {String.valueOf(x),String.valueOf(y)};
    
        // List<Map<String,Object>> list = null;
        

    
    
        // if(list == null ){
        //     Log.i(TAG, "queryFilesByPointFromDatabase is null "+ ",cellX: "+x + ", cellY: "+y);
        // }else{
        // }
        // return list ;
        return null ;
    }

    public static List<Map<String,Object>> queryFilesByPointFromDatabase(Context context,int x, int y){
        String selection = "cellX = ? and cellY = ? ";
        String[] selectionArgs = {String.valueOf(x),String.valueOf(y)};
    
        List<Map<String,Object>> list = null;
    
        Cursor cursor  =   context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                int itemType = cursor.getInt(cursor.getColumnIndex("itemType"));
                int cellX = cursor.getInt(cursor.getColumnIndex("cellX"));
                int cellY = cursor.getInt(cursor.getColumnIndex("cellY"));
                Map<String,Object> mp = new HashMap<>();
                mp.put("_id",_id);
                mp.put("title",title);
                mp.put("itemType",itemType);
                mp.put("cellX",cellX);
                mp.put("cellY",cellY);
                list.add(mp);
            } while (cursor.moveToNext());
        }
    
        if(list == null ){
            Log.i(TAG, "queryFilesByPointFromDatabase is null "+ ",cellX: "+x + ", cellY: "+y);
        }else{
        }
    
        return list ;
    }

    public static synchronized List<Point> queryFilesByPointFromDatabase(Context context){
        // String selection = "cellX = ? and cellY = ? ";
        // String[] selectionArgs = {String.valueOf(x),String.valueOf(y)};
        String selection = null;
        String[] selectionArgs = null;
    
        List<Point> list = null;
    
        Cursor cursor  = context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                // int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                // String title = cursor.getString(cursor.getColumnIndex("title"));
                // int itemType = cursor.getInt(cursor.getColumnIndex("itemType"));
                int cellX = cursor.getInt(cursor.getColumnIndex("cellX"));
                int cellY = cursor.getInt(cursor.getColumnIndex("cellY"));
                Point p = new Point();
                // mp.put("_id",_id);
                // mp.put("title",title);
                // mp.put("itemType",itemType);
                // mp.put("cellX",cellX);
                // mp.put("cellY",cellY);
                p.x = cellX ;
                p.y = cellY ;
                list.add(p);
            } while (cursor.moveToNext());
        }
    
        if(list == null ){
            Log.i(TAG, "queryFilesByPointFromDatabase is null ");
        }else{
            Log.i(TAG, "queryFilesByPointFromDatabase list =  "+list.size());
            list.sort((p1, p2) -> {
                int xCompare = Integer.compare(p1.x, p2.x);
                if (xCompare != 0) {
                    return xCompare;
                }
                return Integer.compare(p1.y, p2.y);
            });
        }
        cursor.close();
        return list ;
    }
    
    
    public static List<Map<String,Object>> queryAllFilesFromDatabase(Context context){
        String[] selectionArgs = null;
        String selection = null;
    
        List<Map<String,Object>> list = null;
    
        Cursor cursor  = context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                int itemType = cursor.getInt(cursor.getColumnIndex("itemType"));
                int cellX = cursor.getInt(cursor.getColumnIndex("cellX"));
                int cellY = cursor.getInt(cursor.getColumnIndex("cellY"));
                Map<String,Object> mp = new HashMap<>();
                mp.put("_id",_id);
                mp.put("title",title);
                mp.put("itemType",itemType);
                mp.put("cellX",cellX);
                mp.put("cellY",cellY);
                list.add(mp);
            } while (cursor.moveToNext());
        }
    
        if(list == null ){
            Log.i(TAG, "queryAllFilesFromDatabase is null");
        }else{
        }
        cursor.close();
        return list ;
    }
    
    public  List<Map<String,Object>> queryAllDesktopFilesFromDatabase(Context context){
        String[] selectionArgs = {"8","9"};
        String selection = "itemType" + " IN (" + TextUtils.join(",", Collections.nCopies(selectionArgs.length, "?")) + ")";
    
        List<Map<String,Object>> list = null;
    
        Cursor cursor  = context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                int itemType = cursor.getInt(cursor.getColumnIndex("itemType"));
                int cellX = cursor.getInt(cursor.getColumnIndex("cellX"));
                int cellY = cursor.getInt(cursor.getColumnIndex("cellY"));
                Map<String,Object> mp = new HashMap<>();
                mp.put("_id",_id);
                mp.put("title",title);
                mp.put("itemType",itemType);
                mp.put("cellX",cellX);
                mp.put("cellY",cellY);
                list.add(mp);
            } while (cursor.moveToNext());
        }
    
        if(list == null ){
            Log.i(TAG, "queryAllDesktopFilesFromDatabase is null");
        }else{
        }
        cursor.close();
        return list ;
    }
    


    
    public static List<Map<String,Object>> queryAllIconFromDatabase(Context context){

        List<Map<String,Object>> list = null;
    
        Cursor cursor  = context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                int itemType = cursor.getInt(cursor.getColumnIndex("itemType"));
                int cellX = cursor.getInt(cursor.getColumnIndex("cellX"));
                int cellY = cursor.getInt(cursor.getColumnIndex("cellY"));
                Map<String,Object> mp = new HashMap<>();
                mp.put("_id",_id);
                mp.put("title",title);
                mp.put("itemType",itemType);
                mp.put("cellX",cellX);
                mp.put("cellY",cellY);
                list.add(mp);
            } while (cursor.moveToNext());
        }
    
        if(list == null ){
            Log.i(TAG, "queryAllNotDesktopFilesFromDatabase is null");
        }else{
        }
        cursor.close();
        return list ;
    }
    
    public static List<Map<String,Object>> queryAllNotDesktopFilesFromDatabase(Context context){
        String[] selectionArgs = {"0","1","2","3","4","5","6","7"};
        String selection = "itemType" + " IN (" + TextUtils.join(",", Collections.nCopies(selectionArgs.length, "?")) + ")";
    
        List<Map<String,Object>> list = null;
    
        Cursor cursor  = context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                int itemType = cursor.getInt(cursor.getColumnIndex("itemType"));
                int cellX = cursor.getInt(cursor.getColumnIndex("cellX"));
                int cellY = cursor.getInt(cursor.getColumnIndex("cellY"));
                Map<String,Object> mp = new HashMap<>();
                mp.put("_id",_id);
                mp.put("title",title);
                mp.put("itemType",itemType);
                mp.put("cellX",cellX);
                mp.put("cellY",cellY);
                list.add(mp);
            } while (cursor.moveToNext());
        }
    
        if(list == null ){
            Log.i(TAG, "queryAllNotDesktopFilesFromDatabase is null");
        }else{
        }
        cursor.close();
        return list ;
    }

    public static List<Map<String,Object>> queryDesktopLinuxAppInDatabase(Context context){
        // String[] displayNames  = {String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_LINUX_APP)};
        // String[] selectionArgs = new String[displayNames.length + 1];
        // String selection = "title = ? and itemType" + " IN (" + TextUtils.join(",", Collections.nCopies(selectionArgs.length, "?")) + ")";
        // selectionArgs[0] = fileName; // MIME_TYPE 的值
        // System.arraycopy(displayNames, 0, selectionArgs, 1, displayNames.length);
        // // String selection = "title = ?";
        // // String[] selectionArgs = {fileName};
        String[] selectionArgs = {String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_LINUX_APP)};
        String selection = "itemType" + " IN (" + TextUtils.join(",", Collections.nCopies(selectionArgs.length, "?")) + ")";
      
        List<Map<String,Object>> list = null;
       
        Cursor cursor  = context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, null, selection, selectionArgs, null);

        try{
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String title = cursor.getString(cursor.getColumnIndex("title"));
                    int itemType = cursor.getInt(cursor.getColumnIndex("itemType"));
                    int cellX = cursor.getInt(cursor.getColumnIndex("cellX"));
                    int cellY = cursor.getInt(cursor.getColumnIndex("cellY"));
                    Map<String,Object> mp = new HashMap<>();
                    mp.put("_id",_id);
                    mp.put("title",title);
                    mp.put("itemType",itemType);
                    mp.put("cellX",cellX);
                    mp.put("cellY",cellY);
                    list.add(mp);
                } while (cursor.moveToNext());
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
           cursor.close();
        }

        if(list == null ){
            Log.i(TAG, "queryDesktopFileInDatabase is null" );
        }else{
            Log.i(TAG, "queryDesktopFileInDatabase  size is  list "+list.size() );
        }

        return list ;
    }


    public static List<Map<String,Object>> queryDesktopTextFilesFromDatabase(Context context){
        String[] selectionArgs = {String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_DIRECTORY),String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_DOCUMENT)};
        String selection = "itemType" + " IN (" + TextUtils.join(",", Collections.nCopies(selectionArgs.length, "?")) + ")";
    
        List<Map<String,Object>> list = null;
    
        Cursor cursor  = context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                int itemType = cursor.getInt(cursor.getColumnIndex("itemType"));
                int cellX = cursor.getInt(cursor.getColumnIndex("cellX"));
                int cellY = cursor.getInt(cursor.getColumnIndex("cellY"));
                Map<String,Object> mp = new HashMap<>();
                mp.put("_id",_id);
                mp.put("title",title);
                mp.put("itemType",itemType);
                mp.put("cellX",cellX);
                mp.put("cellY",cellY);
                list.add(mp);
            } while (cursor.moveToNext());
        }
    
        if(list == null ){
            Log.i(TAG, "queryDesktopTextFilesFromDatabase is null");
        }
        cursor.close();
        return list ;
    }


    public static List<Map<String,Object>> queryItemsFromDatabase(Context context,String fileName){
        String selection = "title = ?";
        String[] selectionArgs = {fileName};
        List<Map<String,Object>> list = null;

        Cursor cursor  = context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                int itemType = cursor.getInt(cursor.getColumnIndex("itemType"));
                int cellX = cursor.getInt(cursor.getColumnIndex("cellX"));
                int cellY = cursor.getInt(cursor.getColumnIndex("cellY"));
                Map<String,Object> mp = new HashMap<>();
                mp.put("_id",_id);
                mp.put("title",title);
                mp.put("itemType",itemType);
                mp.put("cellX",cellX);
                mp.put("cellY",cellY);
                list.add(mp);
            } while (cursor.moveToNext());
        }

        if(list == null ){
            Log.i(TAG, "queryItemsFromDatabase is null");
        }else{
            Log.i(TAG, "queryItemsFromDatabase  size is  list "+list.size());
        }
        cursor.close();
        return list ;
    }

    public static List<Map<String,Object>> queryItemsFromDatabase(Context context,ItemInfo item){
        String selection = "title = ?";
        String[] selectionArgs = {item.title.toString()};
        List<Map<String,Object>> list = null;

        Cursor cursor  = context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                int itemType = cursor.getInt(cursor.getColumnIndex("itemType"));
                int cellX = cursor.getInt(cursor.getColumnIndex("cellX"));
                int cellY = cursor.getInt(cursor.getColumnIndex("cellY"));
                Map<String,Object> mp = new HashMap<>();
                mp.put("_id",_id);
                mp.put("title",title);
                mp.put("itemType",itemType);
                mp.put("cellX",cellX);
                mp.put("cellY",cellY);
                list.add(mp);
            } while (cursor.moveToNext());
        }

        if(list == null ){
            Log.i(TAG, "queryItemsFromDatabase is null");
        }else{
            Log.i(TAG, "queryItemsFromDatabase  size is  list "+list.size());
        }
        cursor.close();
        return list ;
    }


    public static void updateTitleFromDatabase(Context context,String titleOld,String titleNew){
        Log.i(TAG, "updateTitleFromDatabase is titleOld: "+titleOld + " ,titleNew:  "+titleNew);
        String selection = "title = ?";
        String[] selectionArgs = {titleOld};

        ContentValues values = new ContentValues();
        values.put("title",titleNew);
        int res = context.getContentResolver().update(LauncherSettings.Favorites.CONTENT_URI, values,
        selection, selectionArgs);

        Log.i(TAG, "updateTitleFromDatabase is res: "+res);
    }


    public static void deleteTitleFromDatabase(Context context,String title){
        Log.i(TAG, "deleteTitleFromDatabase is title: "+title );
        String selection = "title = ?";
        String[] selectionArgs = {title};
        int res = context.getContentResolver().delete(LauncherSettings.Favorites.CONTENT_URI,selection, selectionArgs);
        Log.i(TAG, "deleteTitleFromDatabase is res: "+res);
    }
    
    public static synchronized int queryMaxIdFromDatabase(Context context){
        String[] projection = {"MAX(_id) AS max_id"};
        int maxId = 0;
    
        // Cursor cursor  =  dbController.query(LauncherSettings.Favorites.TABLE_NAME, projection, null, null, null);
        Cursor cursor  = context.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            maxId = cursor.getInt(cursor.getColumnIndex("max_id"));
        }
        return maxId ;
    }
}
