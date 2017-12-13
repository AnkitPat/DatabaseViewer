package com.example.bbim1043.databaseviewer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by BBIM1043 on 03/10/17.
 */

public class MyOwnDatabase extends SQLiteOpenHelper {

    private static String MY_DATABASE = "my_database";
    private static int DATBASE_VERSION = 2;
    private static String RECENT_TABLE = "recent_table";
    private static String RECENT_ID = "recent_id";
    private static String RECENT_PATH = "recent_path";

    public MyOwnDatabase(Context context)
    {
        super(context,MY_DATABASE,null,DATBASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table "+RECENT_TABLE+" ("+RECENT_ID+" text,"+RECENT_PATH+" text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        if(i!=i1)
        {
            sqLiteDatabase.rawQuery("drop table "+RECENT_TABLE,new String[]{});
            onCreate(sqLiteDatabase);

        }
    }


    public void insertRecentAccessData(String  recentDatabasePath)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(RECENT_PATH,recentDatabasePath);
        long inserted = sqLiteDatabase.insert(RECENT_TABLE,null,contentValues);

        sqLiteDatabase.close();

    }


    public ArrayList<String> getRecentDatabase()
    {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        ArrayList<String> recentArrayList = new ArrayList<>();

        Cursor cursor = sqLiteDatabase.rawQuery("select recent_path from "+RECENT_TABLE,new String[]{});

        if(cursor!=null)
        {
            if(cursor.moveToFirst())
            {
                cursor.moveToLast();
               for(int i=0;i<5;i++)
               {
                   if (!recentArrayList.contains(cursor.getString(cursor.getColumnIndex(RECENT_PATH)))) {
                       recentArrayList.add(cursor.getString(cursor.getColumnIndex(RECENT_PATH)));
                   }

                   if(!cursor.moveToPrevious())
                       break;
               }
            }
        }

        return recentArrayList;

    }



}
