package com.localhost.lin.simploc.query_interface

import com.localhost.lin.simploc.Utils.NetworkUrlUtils
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 后台接口
 * Created by mrlin on 2016/10/23.
 */
interface ThesisApi {
    @GET(NetworkUrlUtils.LOGIN_URL)
    fun loadLoginPage():Call<String>

    @GET(NetworkUrlUtils.C_IMG_URL)
    fun getCheckImg(@Query("cookie") cookie:String):Call<String>

    @GET(NetworkUrlUtils.XN_OPTIONS_URL)
    fun loadMajorName(@Query("openUserId") openUserId:String):Call<String>

    @GET(NetworkUrlUtils.XN_OPTIONS_URL)
    fun loadXNIOption(@Query("openUserId") openUserId:String):Call<String>

    @GET(NetworkUrlUtils.LESSON_URL)
    fun queryLesson(@Query(NetworkUrlUtils.RQ_K_OPENID) openUserId: String, @Query("xn") xn:String,
                    @Query("xq") xq:String, @Query("week") week:String):Call<String>

    @GET(NetworkUrlUtils.EXAM_URL)
    fun queryExamInfo(@Query(NetworkUrlUtils.RQ_K_OPENID) openid:String,
                      @Query("xn") xn:String, @Query("xq") xq:String):Call<String>

    @GET(NetworkUrlUtils.CET_URL)
    fun queryCETInfo(@Query(NetworkUrlUtils.RQ_K_OPENID) openid: String):Call<String>

    @GET(NetworkUrlUtils.ONE_KEY_COMMENT)
    fun processOnekeyComment(@Query(NetworkUrlUtils.RQ_K_OPENID) openid: String)
            :Call<String>

    @GET(NetworkUrlUtils.LOGOUT)
    fun doLogout(@Query(NetworkUrlUtils.RQ_K_OPENID) openid: String):Call<String>

    @GET(NetworkUrlUtils.SESSION_VERIFY)
    fun verifySession(@Query(NetworkUrlUtils.RQ_K_OPENID) openid: String):Call<String>

    @GET(NetworkUrlUtils.RE_LOGIN)
    fun doReLogin(@Query(NetworkUrlUtils.RQ_K_OPENID) openid: String,
                  @Query("viewState") viewState:String, @Query("cookie") cookie: String,
                  @Query("checkCode") checkCode:String) : Call<String>
}