package com.localhost.lin.simploc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.localhost.lin.simploc.Entity.LoginInfo;
import com.localhost.lin.simploc.SQLite.SQLiteOperation;
import com.localhost.lin.simploc.Utils.JsonUtils;
import com.localhost.lin.simploc.Utils.NetworkUrlUtils;

import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtilsHC4;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Lin on 2015/11/5.
 * 该类最初用于处理所有的后台网络请求，现在大部分请求过程在Activity中使用异步网络请求库进行
 * 因此这个类只处理获取登陆验证码、获取成绩这两个功能的网络请求
 */
public class NetworkThreads {
    public static final String HOST_URL           = NetworkUrlUtils.HOST_URL;
    //public static final String HOST_URL           = "http://192.168.1.102:8080/SimploServer";
    //public static final String HOST_URL           = "http://www.pockitcampus.com/SimploServer";
    public static final String LOGIN_URL          = HOST_URL + "/LoginPageServlet";
    public static final String TRY_LOGIN_URL     = HOST_URL + "/TryLoginServlet";
    public static final String C_IMG_URL          = HOST_URL + "/CheckImgServlet";
    public static final String QUERY_URL          = HOST_URL + "/QueryGradeServlet";


//    final class LoginInfo{
//        public String number;
//        public String password;
//        public String checkCode;
//        public String viewState;
//        public String cookie;
//        public String xm;
//    };
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

    public interface RecvLoginPageListener{
        void onNetworkError(String reason);
    }

    public class RecvLoginPageThread implements Runnable{

        private RecvLoginPageListener mListener;

        public RecvLoginPageThread(RecvLoginPageListener listener) {
            mListener = listener;
        }

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
                tmpData = JsonUtils.convJson2Map(loginPage, "loginPage");
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
//                e.printStackTrace();
                if(mListener != null) {
                    mListener.onNetworkError("网络连接错误");
                    return;
                }
            }finally {
                try {
                    netManager.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            loginInfo.setViewState(tmpData.get("viewState"));
            loginInfo.setCookie(tmpData.get("cookie"));
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

//    public class TryLoginThread implements Runnable{
//        private String num = null,passwd = null,checkC;
//        public TryLoginThread(String number,String password,String checkCode){
//            num = number;
//            passwd = password;
//            checkC = checkCode;
//        }
//        @Override
//        public void run() {
//            CloseableHttpClient  netManager = HttpClients.createDefault();
//            HttpGetHC4 tryLoginGetRequest = new HttpGetHC4(TRY_LOGIN_URL + "?number=" + num +
//                                                        "&password=" + passwd + "&checkCode="
//                                                        + checkC + "&viewState=" + loginInfo.getViewState()
//                                                        + "&cookie=" + loginInfo.getCookie());
//            String result = null;
//            loginInfo.setCheckCode(checkC);
//            loginInfo.setNumber(num);
//            loginInfo.setPassword(passwd);
//            try {
//                result = EntityUtilsHC4.toString(netManager.execute(tryLoginGetRequest).getEntity());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            Bundle bundle = new Bundle();
//            bundle.putString("canLogin",result);
//
//            Message msg = mHandler.obtainMessage();
//            msg.obj = "tryLoginEnd";
//            msg.setData(bundle);
//            mHandler.sendMessage(msg);
//        }
//    }

    public class QueryGradeThread implements Runnable{
        private String xnStr = null,xqStr = null;
        private SQLiteOperation mSqLiteOperation;
        public QueryGradeThread(String xnString,String xqString,SQLiteOperation sqLiteOperation){
            xnStr = xnString;
            xqStr = xqString;
            mSqLiteOperation = sqLiteOperation;
        }
        @Override
        public void run() {
            CloseableHttpClient  netManager = HttpClients.createDefault();
            Log.d("Network:cookie--",NetworkThreads.loginInfo.getCookie());
            String[] loginMsg = mSqLiteOperation.find(loginInfo.getNumber());
            HttpGetHC4 gradeQueryGetRequest = new HttpGetHC4(QUERY_URL + "?number=" + loginMsg[1] +
                                            "&cookie=" + loginMsg[3] + "&xn=" + xnStr +"&xq=" + xqStr
                                            + "&xm=" + loginMsg[4] + "&openUserId=" + loginMsg[8]);
            String result = null;
            //HashMap<String,String> gradeList = new HashMap<String,String>();
            try {
                result = EntityUtilsHC4.toString(netManager.execute(gradeQueryGetRequest).getEntity(), "gb2312");
                //gradeList = convJson2Map(result,"GRADE");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bundle bundle = new Bundle();
            bundle.putString("json",result);
            System.out.print(result);
            Message msg = mHandler.obtainMessage();
            msg.obj = "queryGradeFinished";
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

}
