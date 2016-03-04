package com.localhost.lin.simploc.SQLite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.localhost.lin.simploc.Entity.UserEntity;
import com.localhost.lin.simploc.Entity.UserInfo;

import java.util.ArrayList;

/**
 * Created by Lin on 2015/11/11.
 */
public class SQLiteOperation {
    private DatabaseOperator databaseOperator = null;
    public SQLiteOperation(Context context){
        databaseOperator = new DatabaseOperator(context,"users.db",null,3);
    }

    /*----------------添加操作------------------*/

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

    public void insertCourseTb(String number, String tabledata){
        SQLiteDatabase db = databaseOperator.getWritableDatabase();// 取得数据库操作
        db.execSQL("insert into courseTable(id,number,tabledata) values(NULL,?,?)",
                new Object[] {number,tabledata});
        db.close();
    }

    /*----------------查询操作-----------------*/

    /**
     * 获取当天已登录用户信息
     * @return      已登录用户学号
     */
    public String findLoginUser(){
        SQLiteDatabase database = databaseOperator.getReadableDatabase();
        String retNumber;
//        Cursor cursor = database.rawQuery("select userInfo.number from userInfo,loginLog " +
//                "where userInfo.id=loginLog.id and loginLog.hadLogin=1 and loginLog.lastLogin=?",new String[]{date});
        Cursor cursor = database.rawQuery("select userInfo.number from userInfo,loginLog " +
                "where userInfo.id=loginLog.id and loginLog.hadLogin=1",new String[]{ });
        if(cursor.moveToFirst()){
            retNumber = cursor.getString(0);
            return retNumber;
        }
        return null;
    }

    /**
     * 通过学号获取用户设置数据
     * @param number 学号
     * @return 字符数组，0-id，1-学号，2-密码，3-cookie，4-姓名，5-上次登录时间，6-是否已经登录，7-是否显示头像
     *                  8 - openAppId
     */
    public String[] find(String number) {// 根据学号查找纪录
        String[] tusers = new String[9];
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
                tusers[7] = (String.valueOf(innerCursor.getInt(innerCursor.getColumnIndex("showAvator"))));
            }
            tusers[8] = cursor.getString(cursor.getColumnIndex("open_app_id"));
            return tusers;
        }
        db.close();
        return null;
    }

    public UserEntity findUser(String number) {// 根据学号查找纪录
        UserEntity tusers = new UserEntity();
        SQLiteDatabase db = databaseOperator.getReadableDatabase();
        // 用游标Cursor接收从数据库检索到的数据
        Cursor cursor = db.rawQuery("select * from userInfo where number=?", new String[]{number});
        if (cursor.moveToFirst()) {// 依次取出数据
            tusers.setId(cursor.getString(cursor.getColumnIndex("id")));
            tusers.setNumber(cursor.getString(cursor.getColumnIndex("number")));
            tusers.setPassword(cursor.getString(cursor.getColumnIndex("password")));
            tusers.setCookie(cursor.getString(cursor.getColumnIndex("cookie")));
            tusers.setName(cursor.getString(cursor.getColumnIndex("name")));
            tusers.setOpenAppId(cursor.getString(cursor.getColumnIndex("open_app_id")));
            Cursor innerCursor = db.rawQuery("select * from loginLog where id=?", new String[]{tusers.getId()});
            if (innerCursor.moveToFirst()) {
                tusers.setLastLogin(innerCursor.getString(innerCursor.getColumnIndex("lastLogin")));
                tusers.setHadLogin(innerCursor.getString(innerCursor.getColumnIndex("hadLogin")));
                tusers.setIsShowAvator(innerCursor.getInt(innerCursor.getColumnIndex("showAvator")) == 1);
            }
            return tusers;
        }
        db.close();
        return null;
    }

    public boolean queryIsShowAvator(String number){
        SQLiteDatabase db = databaseOperator.getReadableDatabase();
        Cursor cursor = db.rawQuery("select showAvator from LoginLog where" +
                " id=(select id from userInfo where number=?)", new String[]{number});
        if(cursor.moveToFirst()){
            return cursor.getInt(0) == 1;
        }
        return false;
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

    /**
     * 查询数据库中存储的指定学号的课程表数据
     * @param number    学号
     * @return          json格式课程表数据
     */
    public String getSavedCsTb(String number){
        String data = null;
        SQLiteDatabase db = databaseOperator.getReadableDatabase();
        // 用游标Cursor接收从数据库检索到的数据
        Cursor cursor = db.rawQuery("select tabledata from courseTable where number=?", new String[]{number});
        if (cursor.moveToFirst()) {// 依次取出数据
            data = (cursor.getString(cursor.getColumnIndex("tabledata")));
        }else{
            db.close();
            return "";
        }
        db.close();
        return data;
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


    /*--------------删除操作-------------------*/

    public void delete(String number) {// 删除纪录
        SQLiteDatabase db = databaseOperator.getWritableDatabase();
        String id = find(number)[0];
        db.execSQL("delete from userInfo where id=?", new Object[] {id });
        db.execSQL("delete from loginLog where id=?", new Object[] {id });
        db.execSQL("delete from courseTable where number=?", new Object[]{number});
        db.close();
    }

    /*----------------更新操作---------------------*/

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

    public void updateIsShowAvator(String number,boolean isShow){
        SQLiteDatabase db = databaseOperator.getWritableDatabase();
        db.execSQL("update loginLog set showAvator=" + (isShow ? "1" : "0") +
                " where id=(select id from userInfo where number=?)", new Object[] { number });
        db.close();
    }

    public void updateCsTb(String number, String data){
        SQLiteDatabase db = databaseOperator.getWritableDatabase();
        db.execSQL("update courseTable set tabledata=?" +
                " where number=?", new Object[] { data, number });
        db.close();
    }

    public void updateOpenId(String number, String uuid){
        SQLiteDatabase database = databaseOperator.getWritableDatabase();
        database.execSQL("update userInfo set open_app_id=? where number=?",
                new Object[]{uuid, number});
        database.close();
    }
}
