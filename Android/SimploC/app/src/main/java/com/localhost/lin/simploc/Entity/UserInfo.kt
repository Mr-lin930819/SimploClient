package com.localhost.lin.simploc.Entity

import org.litepal.crud.DataSupport

/**
 * Created by Lin on 2015/11/12.
 */
class UserInfo : DataSupport() {
    private val id: Long = 0
    var number: String? = null
    var password: String? = null
    var cookie: String? = null
    var name: String? = null
    var loginLog: LoginLog? = null

    var openAppId: String? = null

}
