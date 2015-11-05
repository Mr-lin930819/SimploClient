package com.localhost.lin.simploc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Lin on 2015/11/5.
 */
public class NetworkThreads {
    final String HOST_URL           = "http://172.20.27.41:8080/SimploServer";
    final String LOGIN_URL          = HOST_URL + "/LoginPageServlet";
    final String TRY_LOGIN_URL     = HOST_URL + "/TwoServlet";
    final String CATCH_GRADE_URL   = HOST_URL + "/ThreeServlet";
    final String C_IMG_URL          = HOST_URL + "/FourServlet";

    final class LoginInfo{
        public String number;
        public String password;
        public String checkCode;
        public String viewState;
        public String cookie;
    };
    private LoginInfo loginInfo;
    private Handler mHandler;

    NetworkThreads(Handler handler){
        mHandler = handler;
    }

    HashMap<String,String> convJson2Map(String json,String node){
        HashMap<String,String> data = new HashMap<String,String>();

        JSONObject jsonObj = null,jsonArr = null;
        try {
            jsonObj = new JSONObject(json);
            jsonArr = jsonObj.getJSONObject(node);
            Iterator<String> iterator = jsonArr.keys();
            while (iterator.hasNext()){
                String key = iterator.next();
                data.put(key,jsonArr.getString(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public class RecvLoginPageThread implements Runnable{
        @Override
        public void run() {
            CloseableHttpClient  netManager = HttpClients.createDefault();
            HttpGet loginGetRequest = new HttpGet(LOGIN_URL);
            String content = "";
            try {
                HttpResponse response   = netManager.execute(loginGetRequest);
                HttpEntity entity       = response.getEntity();
                content = EntityUtils.toString(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    netManager.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //获得单次查询会话的Cookie和ViewState,保存。
            HashMap<String,String> tmpData = convJson2Map(content, "loginPage");
//            loginInfo.viewState = tmpData.get("viewState");
//            loginInfo.cookie    = tmpData.get("cookie");
            Bundle loginBundle = new Bundle();
            loginBundle.putString("viewState",tmpData.get("viewState"));
            loginBundle.putString("cookie", tmpData.get("cookie"));

            Message msg = mHandler.obtainMessage();
            msg.setData(loginBundle);
            msg.obj = "loginPageLoaded";
            mHandler.sendMessage(msg);
        }

    };
}
