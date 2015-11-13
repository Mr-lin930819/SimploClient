package com.localhost.lin.simploc.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lin on 2015/11/11.
 */
public class DatabaseOperator extends SQLiteOpenHelper{

    public DatabaseOperator(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table userInfo(id INTEGER PRIMARY KEY AUTOINCREMENT,number varchar(12),password varchar(20),cookie varchar(50),name varchar(20))");
        db.execSQL("create table loginLog(id INTEGER ,lastLogin date, hadLogin smallint)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
