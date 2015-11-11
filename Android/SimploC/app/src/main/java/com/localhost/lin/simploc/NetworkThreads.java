package com.localhost.lin.simploc;

import android.content.Entity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.EntityUtilsHC4;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Lin on 2015/11/5.
 */
public class NetworkThreads {
    //public static final String HOST_URL           = "http://172.20.27.41:8080/SimploServer";
    //public static final String HOST_URL           = "http://192.168.1.102:8080/SimploServer";
    public static final String HOST_URL           = "http://www.pockitcampus.com/SimploServer/";
    public static final String LOGIN_URL          = HOST_URL + "/LoginPageServlet";
    public static final String TRY_LOGIN_URL     = HOST_URL + "/TryLoginServlet";
    public static final String C_IMG_URL          = HOST_URL + "/CheckImgServlet";
    public static final String QUERY_URL          = HOST_URL + "/QueryGradeServlet";

    final class LoginInfo{
        public String number;
        public String password;
        public String checkCode;
        public String viewState;
        public String cookie;
        public String xm;
    };
    public static LoginInfo loginInfo = null;
    private Handler mHandler;
    NetworkThreads(Handler handler){
        mHandler = handler;
        if (loginInfo == null) {
            loginInfo = new LoginInfo();
        }
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
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
            return null;
//            e.printStackTrace();
        }
        return data;
    }

    public class RecvLoginPageThread implements Runnable{
        @Override
        public void run() {
            CloseableHttpClient  netManager = HttpClients.createDefault();
            HttpGetHC4 loginGetRequest = new HttpGetHC4(LOGIN_URL);

            String loginPage = "";
            byte[] checkImg = null;
            HashMap<String,String> tmpData = null;

            Bundle loginBundle = new Bundle();
            Message msg = mHandler.obtainMessage();

            /*先获取Cookie和ViewState*/
            try {
                loginPage   = EntityUtilsHC4.toString(netManager.execute(loginGetRequest).getEntity());
                //获得单次查询会话的Cookie和ViewState,保存。
                tmpData = convJson2Map(loginPage, "loginPage");
                if(tmpData == null){
                    msg.obj = "runError";
                    loginBundle.putString("info", "LoginPage");
                    msg.setData(loginBundle);
                    try {
                        Thread.currentThread().sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendMessage(msg);
                    return;
                }

                //根据之前的Cookie获取验证码图片
                HttpGetHC4 checkImgGetRequest = new HttpGetHC4(C_IMG_URL + "?cookie=" + tmpData.get("cookie"));
                checkImg   = EntityUtilsHC4.toByteArray(netManager.execute(checkImgGetRequest).getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    netManager.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            loginInfo.viewState = tmpData.get("viewState");
            loginInfo.cookie    = tmpData.get("cookie");
//            loginBundle.putString("viewState",tmpData.get("viewState"));
//            loginBundle.putString("cookie", tmpData.get("cookie"));
            loginBundle.putString("viewState",tmpData.get("viewState"));
            loginBundle.putString("cookie",tmpData.get("cookie"));
            loginBundle.putByteArray("checkImg",checkImg);

            msg.setData(loginBundle);
            msg.obj = "loginPageLoaded";
            mHandler.sendMessage(msg);
        }
    };

    public class TryLoginThread implements Runnable{
        private String num = null,passwd = null,checkC;
        public TryLoginThread(String number,String password,String checkCode){
            num = number;
            passwd = password;
            checkC = checkCode;
        }
        @Override
        public void run() {
            CloseableHttpClient  netManager = HttpClients.createDefault();
            HttpGetHC4 tryLoginGetRequest = new HttpGetHC4(TRY_LOGIN_URL + "?number=" + num +
                                                        "&password=" + passwd + "&checkCode="
                                                        + checkC + "&viewState=" + loginInfo.viewState
                                                        + "&cookie=" + loginInfo.cookie);
            String result = null;
            loginInfo.checkCode = checkC;
            loginInfo.number = num;
            loginInfo.password = passwd;
            try {
                result = EntityUtilsHC4.toString(netManager.execute(tryLoginGetRequest).getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bundle bundle = new Bundle();
            bundle.putString("canLogin",result);

            Message msg = mHandler.obtainMessage();
            msg.obj = "tryLoginEnd";
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    public class QueryGradeThread implements Runnable{
        private String xnStr = null,xqStr = null;
        public QueryGradeThread(String xnString,String xqString){
            xnStr = xnString;
            xqStr = xqString;
        }
        @Override
        public void run() {
            CloseableHttpClient  netManager = HttpClients.createDefault();
            HttpGetHC4 gradeQueryGetRequest = new HttpGetHC4(QUERY_URL + "?number=" + loginInfo.number +
                                            "&cookie=" + loginInfo.cookie + "&xn=" + xnStr +"&xq=" + xqStr
                                            + "&xm=" + loginInfo.xm);
            String result = null;
            //HashMap<String,String> gradeList = new HashMap<String,String>();
            try {
                result = EntityUtilsHC4.toString(netManager.execute(gradeQueryGetRequest).getEntity(), "gb2312");
                //gradeList = convJson2Map(result,"GRADE");
            } catch (IOException e) {
                e.printStackTrace();
            }

//            File file = new File("file:///android_asset/result.json");
//            if (!file.exists()) {
//                try {
//                    //在指定的文件夹中创建文件
//                    file.createNewFile();
//                } catch (Exception e) {
//                }
//            }
//            try {
//                FileWriter fileWriter = new FileWriter(file,false);
//                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//                bufferedWriter.write(result);
//                bufferedWriter.flush();
//                bufferedWriter.close();
//                fileWriter.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            Bundle bundle = new Bundle();
//            for(Map.Entry<String,String> entry:gradeList.entrySet()){
//                bundle.putString(entry.getKey(),entry.getValue());
//            }
            bundle.putString("json",result);
            System.out.print(result);
            Message msg = mHandler.obtainMessage();
            msg.obj = "queryGradeFinished";
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }
}
