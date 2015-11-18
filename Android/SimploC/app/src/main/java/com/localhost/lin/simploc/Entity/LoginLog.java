package com.localhost.lin.simploc.Entity;

import org.litepal.crud.DataSupport;

/**
 * Created by Lin on 2015/11/18.
 */
public class LoginLog extends DataSupport{
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

    public boolean isShowAvator() {
        return ShowAvator;
    }

    public void setShowAvator(boolean showAvator) {
        ShowAvator = showAvator;
    }

    private String lastLogin;
    private String hadLogin;
    private boolean ShowAvator;
}
