package com.localhost.lin.simploc.SQLite

import android.content.Context
import com.localhost.lin.simploc.Entity.UserEntity
import java.util.*

/**
 * Created by Lin on 2015/11/11.
 */
class SQLiteOperation(context: Context) {
    private var databaseOperator: DatabaseOperator? = null

    init {
        databaseOperator = DatabaseOperator(context, "users.db", null, 3)
    }

    /*----------------添加操作------------------*/

    fun insertUser(number: String, password: String, cookie: String, name: String) {
        val db = databaseOperator!!.writableDatabase// 取得数据库操作
        db.execSQL("insert into userInfo (id,number,password,cookie,name) values(NULL,?,?,?,?)",
                arrayOf<Any>(number, password, cookie, name))
        db.close()// 记得关闭数据库操作
    }

    fun insertLog(number: String, lastLogin: String, hadLogin: String) {
        val db = databaseOperator!!.writableDatabase// 取得数据库操作
        val id = getUserId(number)
        db.execSQL("insert into loginLog (id,lastLogin,hadLogin) values(?,?,?)",
                arrayOf<Any?>(id, lastLogin, hadLogin))
        db.close()// 记得关闭数据库操作
    }

    fun insertCourseTb(number: String, tabledata: String) {
        val db = databaseOperator!!.writableDatabase// 取得数据库操作
        db.execSQL("insert into courseTable(id,number,tabledata) values(NULL,?,?)",
                arrayOf<Any>(number, tabledata))
        db.close()
    }

    /*----------------查询操作-----------------*/

    /**
     * 获取当天已登录用户信息
     * @return      已登录用户学号
     */
    fun findLoginUser(): String? {
        val database = databaseOperator!!.readableDatabase
        val retNumber: String
        //        Cursor cursor = database.rawQuery("select userInfo.number from userInfo,loginLog " +
        //                "where userInfo.id=loginLog.id and loginLog.hadLogin=1 and loginLog.lastLogin=?",new String[]{date});
        val cursor = database.rawQuery("select userInfo.number from userInfo,loginLog " + "where userInfo.id=loginLog.id and loginLog.hadLogin=1", arrayOf<String>())
        if (cursor.moveToFirst()) {
            retNumber = cursor.getString(0)
            return retNumber
        }
        return null
    }

    /**
     * 通过学号获取用户设置数据
     * @param number 学号
     * *
     * @return 字符数组，0-id，1-学号，2-密码，3-cookie，4-姓名，5-上次登录时间，6-是否已经登录，7-是否显示头像
     * *                  8 - openAppId
     */
    fun find(number: String): Array<String?>? {// 根据学号查找纪录
        val tusers = arrayOfNulls<String>(9)
        val db = databaseOperator!!.readableDatabase
        // 用游标Cursor接收从数据库检索到的数据
        val cursor = db.rawQuery("select * from userInfo where number=?", arrayOf(number))
        if (cursor.moveToFirst()) {// 依次取出数据
            tusers[0] = cursor.getString(cursor.getColumnIndex("id"))
            tusers[1] = cursor.getString(cursor.getColumnIndex("number"))
            tusers[2] = cursor.getString(cursor.getColumnIndex("password"))
            tusers[3] = cursor.getString(cursor.getColumnIndex("cookie"))
            tusers[4] = cursor.getString(cursor.getColumnIndex("name"))
            val innerCursor = db.rawQuery("select * from loginLog where id=?", arrayOf(tusers[0].toString()))
            if (innerCursor.moveToFirst()) {
                tusers[5] = innerCursor.getString(innerCursor.getColumnIndex("lastLogin"))
                tusers[6] = innerCursor.getString(innerCursor.getColumnIndex("hadLogin"))
                tusers[7] = innerCursor.getInt(innerCursor.getColumnIndex("showAvator")).toString()
            }
            tusers[8] = cursor.getString(cursor.getColumnIndex("open_app_id"))
            return tusers
        }
        db.close()
        return null
    }

    fun findUser(number: String): UserEntity? {// 根据学号查找纪录
        val tusers = UserEntity()
        val db = databaseOperator!!.readableDatabase
        // 用游标Cursor接收从数据库检索到的数据
        val cursor = db.rawQuery("select * from userInfo where number=?", arrayOf(number))
        if (cursor.moveToFirst()) {// 依次取出数据
            tusers.id = cursor.getString(cursor.getColumnIndex("id"))
            tusers.number = cursor.getString(cursor.getColumnIndex("number"))
            tusers.password = cursor.getString(cursor.getColumnIndex("password"))
            tusers.cookie = cursor.getString(cursor.getColumnIndex("cookie"))
            tusers.name = cursor.getString(cursor.getColumnIndex("name"))
            tusers.openAppId = cursor.getString(cursor.getColumnIndex("open_app_id"))
            val innerCursor = db.rawQuery("select * from loginLog where id=?", arrayOf(tusers.id))
            if (innerCursor.moveToFirst()) {
                tusers.lastLogin = innerCursor.getString(innerCursor.getColumnIndex("lastLogin"))
                tusers.hadLogin = innerCursor.getString(innerCursor.getColumnIndex("hadLogin"))
                tusers.isShowAvator = innerCursor.getInt(innerCursor.getColumnIndex("showAvator")) == 1
            }
            return tusers
        }
        db.close()
        return null
    }

    fun queryIsShowAvator(number: String): Boolean {
        val db = databaseOperator!!.readableDatabase
        val cursor = db.rawQuery("select showAvator from LoginLog where" + " id=(select id from userInfo where number=?)", arrayOf(number))
        if (cursor.moveToFirst()) {
            return cursor.getInt(0) == 1
        }
        return false
    }

    val allNumber: ArrayList<String>
        get() {
            val db = databaseOperator!!.readableDatabase
            val result = ArrayList<String>()
            val cursor = db.rawQuery("select number from userInfo", arrayOf<String>())
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    result.add(cursor.getString(cursor.getColumnIndex("number")))
                    cursor.moveToNext()
                }
            }
            print(result.toString())
            return result
        }

    /**
     * 查询数据库中存储的指定学号的课程表数据
     * @param number    学号
     * *
     * @return          json格式课程表数据
     */
    fun getSavedCsTb(number: String): String {
        var data: String? = null
        val db = databaseOperator!!.readableDatabase
        // 用游标Cursor接收从数据库检索到的数据
        val cursor = db.rawQuery("select tabledata from courseTable where number=?", arrayOf(number))
        if (cursor.moveToFirst()) {// 依次取出数据
            data = cursor.getString(cursor.getColumnIndex("tabledata"))
        } else {
            db.close()
            return ""
        }
        db.close()
        return data
    }

    private fun getUserId(number: String): String? {
        val db = databaseOperator!!.readableDatabase
        var id: String? = null
        val cursor = db.rawQuery("select id from userInfo where number=?", arrayOf(number))
        if (cursor.moveToFirst()) {// 依次取出数据
            id = cursor.getString(cursor.getColumnIndex("id"))
        }
        return id
    }


    /*--------------删除操作-------------------*/

    fun delete(number: String) {// 删除纪录
        val db = databaseOperator!!.writableDatabase
        val id = find(number)!![0]
        db.execSQL("delete from userInfo where id=?", arrayOf<Any?>(id))
        db.execSQL("delete from loginLog where id=?", arrayOf<Any?>(id))
        db.execSQL("delete from courseTable where number=?", arrayOf<Any>(number))
        db.close()
    }

    /*----------------更新操作---------------------*/

    fun updateLoginStatus(number: String, isLogin: String) {// 修改纪录
        val db = databaseOperator!!.writableDatabase
        db.execSQL("update loginLog set hadLogin=? " + "where id=(select id from userInfo where number=?)", arrayOf<Any>(isLogin, number))
        db.close()
    }

    fun updateLoginInfo(number: String, loginDate: String, cookie: String) {
        val db = databaseOperator!!.writableDatabase
        db.execSQL("update userInfo set cookie=? where number=?", arrayOf<Any>(cookie, number))
        db.execSQL("update loginLog set lastLogin=?,hadLogin=1 " + "where id=(select id from userInfo where number=?)", arrayOf<Any>(loginDate, number))
        db.close()
    }

    fun updateIsShowAvator(number: String, isShow: Boolean) {
        val db = databaseOperator!!.writableDatabase
        db.execSQL("update loginLog set showAvator=" + (if (isShow) "1" else "0") +
                " where id=(select id from userInfo where number=?)", arrayOf<Any>(number))
        db.close()
    }

    fun updateCsTb(number: String, data: String) {
        val db = databaseOperator!!.writableDatabase
        db.execSQL("update courseTable set tabledata=?" + " where number=?", arrayOf<Any>(data, number))
        db.close()
    }

    fun updateOpenId(number: String, uuid: String) {
        val database = databaseOperator!!.writableDatabase
        database.execSQL("update userInfo set open_app_id=? where number=?",
                arrayOf<Any>(uuid, number))
        database.close()
    }
}
