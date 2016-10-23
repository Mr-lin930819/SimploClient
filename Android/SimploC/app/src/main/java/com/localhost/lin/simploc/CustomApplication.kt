package com.localhost.lin.simploc

import com.localhost.lin.simploc.Utils.NetworkUrlUtils
import org.litepal.LitePalApplication
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Created by Lin on 2015/11/18.
 */
class CustomApplication : LitePalApplication() {
    var retrofit = Retrofit.Builder().baseUrl(NetworkUrlUtils.HOST_URL)
            .addConverterFactory(ScalarsConverterFactory.create())//增加返回值为String的支持
            .addConverterFactory(GsonConverterFactory.create())//增加返回值为Gson的支持(以实体类返回)
            .build()

    override fun onCreate() {
        super.onCreate()
        Factory.application = this
    }

    companion object Factory {
        private var application: CustomApplication? = null
        fun getInstance():CustomApplication = application!!
    }
}
