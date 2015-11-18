package com.localhost.lin.simploc.Entity;

import android.util.Log;

import org.litepal.crud.DataSupport;

/**
 * Created by Lin on 2015/11/12.
 */
public class UserInfo extends DataSupport{
    private long id;
    private String number;
    private String password;
    private String cookie;
    private String name;
    private LoginLog loginLog;

    public LoginLog getLoginLog() {
        return loginLog;
    }

    public void setLoginLog(LoginLog loginLog) {
        this.loginLog = loginLog;
    }


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
