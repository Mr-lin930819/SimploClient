package com.localhost.lin.simploc.Entity

import org.litepal.crud.DataSupport

/**
 * Created by Lin on 2015/11/18.
 */
class LoginLog : DataSupport() {

    var lastLogin: String? = null
    var hadLogin: String? = null
    var isShowAvator: Boolean = false
}
