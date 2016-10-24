package com.localhost.lin.simploc

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log

import com.localhost.lin.simploc.Entity.LoginInfo
import com.localhost.lin.simploc.SQLite.SQLiteOperation
import com.localhost.lin.simploc.Utils.JsonUtils
import com.localhost.lin.simploc.Utils.NetworkUrlUtils

import org.apache.http.client.methods.HttpGetHC4
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtilsHC4

import java.io.IOException
import java.util.HashMap

/**
 * Created by Lin on 2015/11/5.
 * 该类最初用于处理所有的后台网络请求，现在大部分请求过程在Activity中使用异步网络请求库进行
 * 因此这个类只处理获取登陆验证码、获取成绩这两个功能的网络请求
 */
class NetworkThreads internal constructor(private var mHandler: Handler?) {

    init {
        if (loginInfo == null) {
            loginInfo = LoginInfo()
        }
    }

    fun setHandler(mHandler: Handler) {
        this.mHandler = mHandler
    }

    inner class RecvLoginPageThread : Runnable {
        override fun run() {
            val netManager = HttpClients.createDefault()
            val loginGetRequest = HttpGetHC4(LOGIN_URL)

            var loginPage = ""
            var checkImg: ByteArray? = null
            var tmpData: HashMap<String, String>? = null

            val loginBundle = Bundle()
            val msg = mHandler!!.obtainMessage()

            /*先获取Cookie和ViewState*/
            try {
                loginPage = EntityUtilsHC4.toString(netManager.execute(loginGetRequest).entity)
                //获得单次查询会话的Cookie和ViewState,保存。
                tmpData = JsonUtils.convJson2Map(loginPage, "loginPage")
                if (tmpData == null) {
                    msg.obj = "runError"
                    loginBundle.putString("info", "ThesisApi")
                    msg.data = loginBundle
                    try {
                        Thread.sleep(2500)
                        //Thread.currentThread().sleep(2500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    mHandler!!.sendMessage(msg)
                    return
                }

                //根据之前的Cookie获取验证码图片
                val checkImgGetRequest = HttpGetHC4(C_IMG_URL + "?cookie=" + tmpData["cookie"])
                checkImg = EntityUtilsHC4.toByteArray(netManager.execute(checkImgGetRequest).entity)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    netManager.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            loginInfo!!.viewState = tmpData!!["viewState"]
            loginInfo!!.cookie = tmpData["cookie"]
            //            loginBundle.putString("viewState",tmpData.get("viewState"));
            //            loginBundle.putString("cookie", tmpData.get("cookie"));
            loginBundle.putString("viewState", tmpData["viewState"])
            loginBundle.putString("cookie", tmpData["cookie"])
            loginBundle.putByteArray("checkImg", checkImg)

            msg.data = loginBundle
            msg.obj = "loginPageLoaded"
            mHandler!!.sendMessage(msg)
        }
    }

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

    inner class QueryGradeThread(xnString: String, xqString: String, private val mSqLiteOperation: SQLiteOperation) : Runnable {
        private var xnStr: String? = null
        private var xqStr: String? = null

        init {
            xnStr = xnString
            xqStr = xqString
        }

        override fun run() {
            val netManager = HttpClients.createDefault()
            Log.d("Network:cookie--", NetworkThreads.loginInfo!!.cookie)
            val loginMsg = mSqLiteOperation.find(loginInfo!!.number)
            val gradeQueryGetRequest = HttpGetHC4(QUERY_URL + "?number=" + loginMsg[1] +
                    "&cookie=" + loginMsg[3] + "&xn=" + xnStr + "&xq=" + xqStr
                    + "&xm=" + loginMsg[4] + "&openUserId=" + loginMsg[8])
            var result: String? = null
            //HashMap<String,String> gradeList = new HashMap<String,String>();
            try {
                result = EntityUtilsHC4.toString(netManager.execute(gradeQueryGetRequest).entity, "gb2312")
                //gradeList = convJson2Map(result,"GRADE");
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val bundle = Bundle()
            bundle.putString("json", result)
            print(result)
            val msg = mHandler!!.obtainMessage()
            msg.obj = "queryGradeFinished"
            msg.data = bundle
            mHandler!!.sendMessage(msg)
        }
    }

    companion object {
        val HOST_URL = NetworkUrlUtils.HOST_URL
        //public static final String HOST_URL           = "http://192.168.1.102:8080/SimploServer";
        //public static final String HOST_URL           = "http://www.pockitcampus.com/SimploServer";
        val LOGIN_URL = HOST_URL + "/LoginPageServlet"
        val TRY_LOGIN_URL = HOST_URL + "/TryLoginServlet"
        val C_IMG_URL = HOST_URL + "/CheckImgServlet"
        val QUERY_URL = HOST_URL + "/QueryGradeServlet"


        //    final class LoginInfo{
        //        public String number;
        //        public String password;
        //        public String checkCode;
        //        public String viewState;
        //        public String cookie;
        //        public String xm;
        //    };
        var loginInfo: LoginInfo? = null
    }

}
