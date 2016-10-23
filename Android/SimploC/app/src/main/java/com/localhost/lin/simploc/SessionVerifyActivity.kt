package com.localhost.lin.simploc

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

import com.localhost.lin.simploc.Entity.UserEntity
import com.localhost.lin.simploc.SQLite.SQLiteOperation
import com.localhost.lin.simploc.Utils.JsonUtils
import com.localhost.lin.simploc.Utils.NetworkUrlUtils
import com.localhost.lin.simploc.query_interface.ThesisApi
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import com.loopj.android.http.TextHttpResponseHandler

import org.apache.http.client.methods.HttpGetHC4
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtilsHC4
import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.HashMap
import java.util.Locale

import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.content_session_verify.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SessionVerifyActivity : AppCompatActivity() {

    internal var sqLiteOperation: SQLiteOperation? = null
    internal var userInfo: UserEntity? = null
    private var mViewState = ""
    private var mCookie = ""
    internal var checkInput: EditText? = null
    val thesisService = CustomApplication.getInstance().retrofit.create(ThesisApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //先进行验证，身份过期了再进行接下去的操作，否则跳转到主界面
        sqLiteOperation = SQLiteOperation(this)
        userInfo = sqLiteOperation!!.findUser(sqLiteOperation!!.findLoginUser())
        setContentView(R.layout.activity_session_verify)

        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        checkInput = findViewById(R.id.re_login_edittext) as EditText?
        val confirmBtn = findViewById(R.id.re_login_btn) as Button?
        confirmBtn!!.setOnClickListener {
            val procBox = AlertDialog.Builder(this@SessionVerifyActivity).setTitle("重新登录").setMessage("正在重新登录...").show()
            val reloginClient = AsyncHttpClient()
            reloginClient.get(NetworkUrlUtils.RE_LOGIN, RequestParams(object : HashMap<String, String>() {
                init {
                    put(NetworkUrlUtils.RQ_K_OPENID, userInfo!!.openAppId)
                    put("viewState", mViewState)
                    put("cookie", mCookie)
                    put("checkCode", checkInput!!.text.toString())
                }
            }), object : TextHttpResponseHandler() {
                override fun onFailure(i: Int, headers: Array<Header>, s: String, throwable: Throwable) {
                    procBox.dismiss()
                    processNetErr()
                }

                override fun onSuccess(i: Int, headers: Array<Header>, s: String) {
                    val rsJson: JSONObject
                    var rstText = ""
                    try {
                        rsJson = JSONObject(s).getJSONObject("reLoginRst")
                        rstText = rsJson.get("result").toString()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    procBox.dismiss()
                    Log.d("SessionVerify", rstText)
                    if (rstText == "SUCCE") {          //身份信息有效
                        processSuccess()
                    } else if (rstText == "ERRVR") {   //验证码错误
                        processVerifyError()
                    } else if (rstText == "ERRSV") {   //服务器内部错误
                        processNetErr()
                    }
                }
            })
        }

        verifySession(userInfo!!.openAppId)
    }

    private fun verifySession(openID: String) {
        val verifyDialog = ProgressDialog.show(this@SessionVerifyActivity,
                "登录验证", "正在验证用户信息")
        val client = AsyncHttpClient()
        client.get(NetworkUrlUtils.SESSION_VERIFY, RequestParams(object : HashMap<String, String>() {
            init {
                put(NetworkUrlUtils.RQ_K_OPENID, openID)
            }
        }), object : TextHttpResponseHandler() {
            override fun onStart() {
                charset = "gb2312"
                super.onStart()
            }

            override fun onFailure(i: Int, headers: Array<Header>, s: String, throwable: Throwable) {
                processNetErr()
                verifyDialog.dismiss()
            }

            override fun onSuccess(i: Int, headers: Array<Header>, s: String) {
                val rsJson: JSONObject
                var rstText = ""
                try {
                    rsJson = JSONObject(s).getJSONObject("verifyRst")
                    rstText = rsJson.get("result").toString()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                if (rstText == "SUCCE") {          //身份信息有效
                    processSuccess()
                } else if (rstText == "ERREP") {   //身份信息过期
                    Log.d("SessionVerify", "验证身份")
                    processExpire()
                }
                verifyDialog.dismiss()
            }
        })
    }

    private fun processSuccess() {
        sqLiteOperation!!.updateLoginInfo(userInfo!!.number,
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date()), mCookie)
        val intent = Intent()
        intent.setClass(this@SessionVerifyActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun processNetErr() {
        Toast.makeText(this@SessionVerifyActivity, "服务器错误", Toast.LENGTH_LONG).show()
    }

    private fun processVerifyError() {
        processExpire()
        checkInput!!.error = "验证码输入错误！"
        checkInput!!.requestFocus()
    }

    private fun processExpire() {
        loadCheckImage()    //载入验证码
    }

    //后台载入验证码
    private fun loadCheckImage() {
        val call = thesisService.loadLoginPage()
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val tmpData = JsonUtils.convJson2Map(response.body(), "loginPage")
                Log.d("SessionVerify", "获得登录信息")
                if (tmpData == null) {
                    return
                }

                val checkImgCall = thesisService.getCheckImg(tmpData["cookie"].toString())
                checkImgCall.enqueue(object : Callback<String> {
                    override fun onFailure(call: Call<String>?, t: Throwable?) {
                        Toast.makeText(this@SessionVerifyActivity, "获取登录信息失败!", Toast.LENGTH_LONG)
                    }

                    override fun onResponse(call: Call<String>?, response: Response<String>?) {
                        val checkImg = response?.body()?.toByteArray()
                        Log.d("SessionVerify", "获得验证码图片" + tmpData["viewState"] + "   " + tmpData["cookie"])
                        /*先获取Cookie和ViewState*/
                        mViewState = tmpData["viewState"].toString()
                        mCookie = tmpData["cookie"].toString()

                        val bitmap = BitmapFactory.decodeByteArray(checkImg, 0, checkImg?.size!!)
                        re_login_check_img.setImageBitmap(bitmap)
                    }

                })
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@SessionVerifyActivity, "验证码获取失败!", Toast.LENGTH_LONG)
            }
        })
    }
}
