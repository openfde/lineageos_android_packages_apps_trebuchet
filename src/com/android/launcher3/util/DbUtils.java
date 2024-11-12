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


public class DbUtils {
    protected static final String TAG = "DbUtils";


    public static List<Map<String,Object>> queryFilesByPointFromDatabase(Context context,int x, int y){
        String selection = "cellX = ? and cellY = ? ";
        String[] selectionArgs = {String.valueOf(x),String.valueOf(y)};
    
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
            Log.i(TAG, "queryFilesByPointFromDatabase is null "+ ",cellX: "+x + ", cellY: "+y);
        }else{
        }
    
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
    
        return list ;
    }
    
    public  List<Map<String,Object>> queryAllDesktopFilesFromDatabase(Context context){
        String[] selectionArgs = {"8","9","10"};
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
    
        return list ;
    }


    public static List<Map<String,Object>> queryDesktopTextFilesFromDatabase(Context context){
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
            Log.i(TAG, "queryDesktopTextFilesFromDatabase is null");
        }
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
    
}
