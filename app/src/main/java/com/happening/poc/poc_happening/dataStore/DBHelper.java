package com.happening.poc.poc_happening.dataStore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.happening.poc.poc_happening.fragment.ChatFragment;

import java.util.ArrayList;

/**
 * Created by daired on 03/01/17.
 */

public class DBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "happening.db";

    // Query Strings
    private static final String SQL_CREATE_PROFILE_ENTRIES =
            "CREATE TABLE " + DBContract.DBEntry.PROFILE_TABLE_NAME + " (" +
                    DBContract.DBEntry._ID + " INTEGER PRIMARY KEY," +
                    DBContract.DBEntry.PROFILE_COLUMN_USERNAME + "," +
                    DBContract.DBEntry.PROFILE_COLUMN_FIRSTNAME + "," +
                    DBContract.DBEntry.PROFILE_COLUMN_LASTNAME + ")";

    private static final String SQL_CREATE_DEVICE_ENTRIES =
            "CREATE TABLE " + DBContract.DBEntry.DEVICES_TABLE_NAME + " (" +
                    DBContract.DBEntry._ID + " INTEGER PRIMARY KEY," +
                    DBContract.DBEntry.DEVICES_COLUMN_NAME + "," +
                    DBContract.DBEntry.DEVICES_COLUMN_ADDRESS + "," +
                    DBContract.DBEntry.DEVICES_COLUMN_LAST_SEEN + ")";

    private static final String SQL_CREATE_PRIVATE_MESSAGES_ENTRIES =
            "CREATE TABLE " + DBContract.DBEntry.PRIVATE_MESSAGES_TABLE_NAME + " (" +
                    DBContract.DBEntry._ID + " INTEGER PRIMARY KEY," +
                    DBContract.DBEntry.PRIVATE_MESSAGES_COLUMN_FROM_DEVICE_ID + "," +
                    DBContract.DBEntry.PRIVATE_MESSAGES_COLUMN_CREATION_TIME + "," +
                    DBContract.DBEntry.PRIVATE_MESSAGES_COLUMN_TYPE + "," +
                    DBContract.DBEntry.PRIVATE_MESSAGES_COLUMN_CONTENT + ")";

    private static final String SQL_CREATE_GLOBAL_MESSAGES_ENTRIES =
            "CREATE TABLE " + DBContract.DBEntry.GLOBAL_MESSAGES_TABLE_NAME + " (" +
                    DBContract.DBEntry._ID + " INTEGER PRIMARY KEY," +
                    DBContract.DBEntry.GLOBAL_MESSAGES_COLUMN_FROM_DEVICE_ID + "," +
                    DBContract.DBEntry.GLOBAL_MESSAGES_COLUMN_CREATION_TIME + "," +
                    DBContract.DBEntry.GLOBAL_MESSAGES_COLUMN_TYPE + "," +
                    DBContract.DBEntry.GLOBAL_MESSAGES_COLUMN_CONTENT + ")";


    private static final String SQL_DELETE_DEVICE_ENTRIES =
            "DROP TABLE IF EXISTS " +  DBContract.DBEntry.DEVICES_TABLE_NAME;


    // Singelton Pattern
    private static DBHelper instance = null;

    public static DBHelper getInstance(Context context) {
        instance = new DBHelper(context);
        return instance;
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PROFILE_ENTRIES);
        db.execSQL(SQL_CREATE_DEVICE_ENTRIES);
        db.execSQL(SQL_CREATE_PRIVATE_MESSAGES_ENTRIES);
        db.execSQL(SQL_CREATE_GLOBAL_MESSAGES_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // db.execSQL(SQL_CREATE_PROFILE_ENTRIES);
        // db.execSQL(SQL_DELETE_ENTRIES);
        // db.execSQL(SQL_CREATE_PRIVATE_MESSAGES_ENTRIES);
        // db.execSQL(SQL_CREATE_GLOBAL_MESSAGES_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }



    // Get Data

    public Cursor getDevice(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res =  db.rawQuery( "select * from " + DBContract.DBEntry.DEVICES_TABLE_NAME + " where " + DBContract.DBEntry._ID + " = " + id + "", null );
        return res;
    }

    public ArrayList<String> getAllDeviceNames() {
        ArrayList<String> list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + DBContract.DBEntry.DEVICES_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            list.add(res.getString(res.getColumnIndex(DBContract.DBEntry.DEVICES_COLUMN_NAME)));
            res.moveToNext();
        }
        return list;
    }


    // Insert Methods

    public boolean insertDevice (String name, String address, String lastSeen) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.DBEntry.DEVICES_COLUMN_NAME, name);
        contentValues.put(DBContract.DBEntry.DEVICES_COLUMN_ADDRESS, address);
        contentValues.put(DBContract.DBEntry.DEVICES_COLUMN_LAST_SEEN, lastSeen);

        db.insert(DBContract.DBEntry.DEVICES_TABLE_NAME, null, contentValues);
        return true;
    }



    // Update Methods

    public boolean updateDevice (Integer id, String name, String address, String lastSeen) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.DBEntry.DEVICES_COLUMN_NAME, name);
        contentValues.put(DBContract.DBEntry.DEVICES_COLUMN_ADDRESS, address);
        contentValues.put(DBContract.DBEntry.DEVICES_COLUMN_LAST_SEEN, lastSeen);

        db.update(DBContract.DBEntry.DEVICES_TABLE_NAME, contentValues, DBContract.DBEntry._ID + " = ? ", new String[] { Integer.toString(id) } );
        return true;
    }



    // Delete Methods

    public Integer deleteDevice (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(DBContract.DBEntry.DEVICES_TABLE_NAME,
                DBContract.DBEntry._ID + " = ? ",
                new String[] { Integer.toString(id) });
    }



}