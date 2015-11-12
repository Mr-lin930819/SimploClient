package com.localhost.lin.simploc.com.localhost.lin.simploc.Entity;

/**
 * Created by Lin on 2015/11/12.
 */
public class UserInfo {
    private String id;
    private String number;
    private String password;
    private String cookie;
    private String name;
    private String lastLogin;
    private String hadLogin;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getHadLogin() {
        return hadLogin;
    }

    public void setHadLogin(String hadLogin) {
        this.hadLogin = hadLogin;
    }
}
