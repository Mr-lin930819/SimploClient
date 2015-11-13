package com.localhost.lin.simploc.SQLite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

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
        db.execSQL("insert into userInfo (id,number,password,cookie,name) values(NULL,?,?,?,?)",
                new Object[] {number,password,cookie,name });
        db.close();// 记得关闭数据库操作
    }

    public void insertLog(String number,String lastLogin,String hadLogin){
        SQLiteDatabase db = databaseOperator.getWritableDatabase();// 取得数据库操作
        String id = getUserId(number);
        db.execSQL("insert into loginLog (id,lastLogin,hadLogin) values(?,?,?)",
                new Object[]{id, lastLogin, hadLogin});
        db.close();// 记得关闭数据库操作
    }

    /**
     * 获取当天已登录用户信息
     * @param date  当前时间
     * @return      已登录用户学号
     */
    public String findLoginUser(String date){
        SQLiteDatabase database = databaseOperator.getReadableDatabase();
        String retNumber;
        Cursor cursor = database.rawQuery("select userInfo.number from userInfo,loginLog " +
                "where userInfo.id=loginLog.id and loginLog.hadLogin=1 and loginLog.lastLogin=?",new String[]{date});
        if(cursor.moveToFirst()){
            retNumber = cursor.getString(0);
            return retNumber;
        }
        return null;
    }

    public void delete(String number) {// 删除纪录
        SQLiteDatabase db = databaseOperator.getWritableDatabase();
        String id = find(number)[0];
        db.execSQL("delete from userInfo where id=?", new Object[] {id });
        db.execSQL("delete from loginLog where id=?", new Object[] {id });
        db.close();
    }

    public void updateLoginStatus(String number,String isLogin) {// 修改纪录
        SQLiteDatabase db = databaseOperator.getWritableDatabase();
        db.execSQL("update loginLog set hadLogin=? " +
                "where id=(select id from userInfo where number=?)", new Object[] { isLogin, number });
        db.close();
    }

    public void updateLoginInfo(String number,String loginDate,String cookie){
        SQLiteDatabase db = databaseOperator.getWritableDatabase();
        db.execSQL("update userInfo set cookie=? where number=?", new Object[]{cookie, number});
        db.execSQL("update loginLog set lastLogin=?,hadLogin=1 " +
                "where id=(select id from userInfo where number=?)", new Object[] { loginDate, number });
        db.close();
    }

    public String[] find(String number) {// 根据学号查找纪录
        String[] tusers = new String[7];
        SQLiteDatabase db = databaseOperator.getReadableDatabase();
        // 用游标Cursor接收从数据库检索到的数据
        Cursor cursor = db.rawQuery("select * from userInfo where number=?", new String[]{number});
        if (cursor.moveToFirst()) {// 依次取出数据
            tusers[0] = (cursor.getString(cursor.getColumnIndex("id")));
            tusers[1] = (cursor.getString(cursor.getColumnIndex("number")));
            tusers[2] = (cursor.getString(cursor.getColumnIndex("password")));
            tusers[3] = (cursor.getString(cursor.getColumnIndex("cookie")));
            tusers[4] = (cursor.getString(cursor.getColumnIndex("name")));
            Cursor innerCursor = db.rawQuery("select * from loginLog where id=?", new String[]{tusers[0]});
            if (innerCursor.moveToFirst()) {
                tusers[5] = (innerCursor.getString(innerCursor.getColumnIndex("lastLogin")));
                tusers[6] = (innerCursor.getString(innerCursor.getColumnIndex("hadLogin")));
            }
            return tusers;
        }
        db.close();
        return null;
    }

    public ArrayList<String> getAllNumber(){
        SQLiteDatabase db = databaseOperator.getReadableDatabase();
        ArrayList<String> result = new ArrayList<String>();
        Cursor cursor = db.rawQuery("select number from userInfo",new String[]{});
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                result.add(cursor.getString(cursor.getColumnIndex("number")));
                cursor.moveToNext();
            }
        }
        System.out.print(result.toString());
        return result;
    }

    private String getUserId(String number){
        SQLiteDatabase db = databaseOperator.getReadableDatabase();
        String id = null;
        Cursor cursor = db.rawQuery("select id from userInfo where number=?", new String[]{number});
        if (cursor.moveToFirst()) {// 依次取出数据
            id = (cursor.getString(cursor.getColumnIndex("id")));
        }
        return id;
    }
}
