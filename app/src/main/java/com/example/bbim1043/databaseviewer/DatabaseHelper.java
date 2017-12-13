package com.example.bbim1043.databaseviewer;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by BBIM1043 on 08/09/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static String DB_PATH = Environment.getExternalStorageDirectory()+
            File.separator+".AGA/ModularDatabase.sqlite";
    public static String DB_NAME = "/ModularDatabase.sqlite";

    public SQLiteDatabase database;

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public void createDatabase() throws IOException {
        boolean doExist = checkDatabase();

        if (doExist) {

        } else {
            this.getReadableDatabase();

            try {
                copyDatabase();
            } catch (Exception e) {
                throw new Error("Excemption" + e);
            }
        }
    }

    public boolean checkDatabase() {
        SQLiteDatabase checkDB = null;

        try {
            String mypath = DB_PATH;

            checkDB = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {

        }

        if (checkDB != null) {
            checkDB.close();
        }


        return checkDB != null;
    }


    public void copyDatabase() throws IOException {
        InputStream myInput = context.getAssets().open(DB_NAME);

        String outFileName = DB_PATH ;

        OutputStream outputStream = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];

        int length;

        while ((length = myInput.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        outputStream.close();
        myInput.close();
    }


    public void openDatabase() throws SQLException

    {
        String mypath = DB_PATH;

        database = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READWRITE);

    }


}