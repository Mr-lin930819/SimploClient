package com.localhost.lin.simploc.com.localhost.lin.simploc.SQLite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Lin on 2015/11/11.
 */
public class SQLiteOperation {
    private DatabaseOperator databaseOperator = null;
    public SQLiteOperation(Context context){
        databaseOperator = new DatabaseOperator(context,"users.db",null,1);
    }

    public void insertUser(String number,String password,String cookie,String name){
        SQLiteDatabase db = databaseOperator.getWritableDatabase();// 取得数据库操作
        db.execSQL("insert into userInfo (number,password,cookie,name) values(?,?,?,?)",
                new Object[] {number,password,cookie,name });
        db.close();// 记得关闭数据库操作
    }

    public void insertLog(String lastLogin,String hadLogin){
        SQLiteDatabase db = databaseOperator.getWritableDatabase();// 取得数据库操作
        db.execSQL("insert into loginLog (lastLogin,hadLogin) values(?,?)",
                new Object[] {lastLogin,hadLogin });
        db.close();// 记得关闭数据库操作
    }

    public void delete(String number) {// 删除纪录
        SQLiteDatabase db = databaseOperator.getWritableDatabase();
        db.execSQL("delete from userInfo where number=?", new Object[] {number });
        db.close();
    }


    public String[] find(String number) {// 根据学号查找纪录
        String[] tusers = null;
        SQLiteDatabase db = databaseOperator.getReadableDatabase();
        // 用游标Cursor接收从数据库检索到的数据
        Cursor cursor = db.rawQuery("select * from userInfo where number=?", new String[]{number});
        if (cursor.moveToFirst()) {// 依次取出数据
            tusers[0] = (cursor.getString(cursor.getColumnIndex("number")));
            tusers[1] = (cursor.getString(cursor.getColumnIndex("password")));
            tusers[2] = (cursor.getString(cursor.getColumnIndex("cookie")));
            tusers[3] = (cursor.getString(cursor.getColumnIndex("name")));
        }
        db.close();
        return tusers;
    }
}
