package com.localhost.lin.simploc

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.localhost.lin.simploc.Entity.LoginLog
import com.localhost.lin.simploc.Entity.UserInfo
import com.localhost.lin.simploc.SQLite.SQLiteOperation
import kotlinx.android.synthetic.main.activity_login.*
import org.apache.http.client.methods.HttpGetHC4
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtilsHC4
import org.json.JSONException
import org.json.JSONObject
import org.litepal.crud.DataSupport
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {
    //implements LoaderCallbacks<Cursor> {

    private var mAuthTask: UserLoginTask? = null

    private var loginViewState: String? = null
    private var loginCookie: String? = null
    internal var threads: NetworkThreads? = null
    internal var sqLiteOperation: SQLiteOperation? = null
    private var retryCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //SQLiteDatabase sqLiteDatabase = Connector.getDatabase();
        //数据库操作
        sqLiteOperation = SQLiteOperation(this)
        /**启动后台线程获取登录验证码 */
        threads = NetworkThreads(handler)

        // Set up the login form.
        populateAutoComplete()
        act_email.addTextChangedListener(NumberInputWatcher())
        //        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        //            @Override
        //            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        //                String [] info = null;
        //                if (v.getText().length() == 12){
        //                    info = sqLiteOperation.find(v.getText().toString());
        //                    if(info != null){
        //                        mPasswordView.setText(info[2]);
        //                    }
        //                }
        //                return false;
        //            }
        //        });

        et_password.setOnEditorActionListener(TextView.OnEditorActionListener { textView, id, keyEvent ->
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        val mEmailSignInButton = findViewById(R.id.email_sign_in_button) as Button?
        mEmailSignInButton!!.setOnClickListener { attemptLogin() }

        val loginThread = Thread(threads!!.RecvLoginPageThread())
        loginThread.start()
    }

    //监听学号输入，满12位且存有当前学号，则补全密码
    private inner class NumberInputWatcher : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val info: Array<String?>?
            if (s.length == 12) {
                info = sqLiteOperation?.find(s.toString())
                if (info != null) {
                    et_password.setText(info[2])
                }
            }
        }

        override fun afterTextChanged(s: Editable) {

        }
    }

    private fun populateAutoComplete() {
        //String nowDate = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        val number = sqLiteOperation?.findLoginUser()
        //        if (!mayRequestContacts()) {
        //            return;
        //        }
        //如果当天已登录，则自动登陆

        //替换为向服务器请求验证，不必局限于当天登录，只验证已登录用户
        if (number != null) {
            val data = sqLiteOperation?.find(number)
            NetworkThreads.loginInfo!!.number = data?.get(1)
            NetworkThreads.loginInfo!!.cookie = data?.get(3)
            NetworkThreads.loginInfo!!.xm = data?.get(4)
            startActivity(Intent().setClass(this@LoginActivity, SessionVerifyActivity::class.java))
            finish()
        } else {
            val list = sqLiteOperation?.allNumber
            addEmailsToAutoComplete(list!!)
        }
        //getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        act_email.error = null
        et_password.error = null

        // Store values at the time of the login attempt.
        val userNumber = act_email.text.toString()
        val password = et_password.text.toString()
        val checkCode = check_code.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            et_password.error = getString(R.string.error_invalid_password)
            focusView = et_password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(userNumber)) {
            act_email.error = getString(R.string.error_field_required)
            focusView = act_email
            cancel = true
        } else if (!isNumberValid(userNumber)) {
            act_email.error = getString(R.string.error_invalid_email)
            focusView = act_email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthTask = UserLoginTask(userNumber, password, checkCode)
            mAuthTask!!.execute(null)
        }
    }

    private fun isNumberValid(number: String): Boolean {
        //TODO: Replace this with your own logic
        return number.length == 12 && Pattern.compile("[0-9]*").matcher(number).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    login_form.visibility = if (show) View.GONE else View.VISIBLE
                }
            })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    login_progress.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }


    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        act_email.setAdapter(adapter)
    }

    private val handler = object : Handler(Looper.myLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if ((msg.obj as String).equals("loginPageLoaded", ignoreCase = true)) {
                val bundle = msg.data
                val iv = findViewById(R.id.check_code_img) as ImageView?
                loginViewState = bundle.getString("viewState")
                loginCookie = bundle.getString("cookie")
                val imgRes = bundle.getByteArray("checkImg")
                if (imgRes != null) {
                    val bitmap = BitmapFactory.decodeByteArray(imgRes, 0, imgRes.size)
                    iv!!.setImageBitmap(bitmap)
                }
            } else if ((msg.obj as String).equals("runError", ignoreCase = true)) {
                val info = msg.data.getString("info")
                //登录界面获取错误，重试
                if (info == "ThesisApi") {
                    Thread(threads?.RecvLoginPageThread()).start()
                    //重试次数大于5，退出程序
                    if (++retryCount > 5) {
                        Toast.makeText(this@LoginActivity, "网络连接错误", Toast.LENGTH_LONG).show()
                        this@LoginActivity.finish()
                    }
                }
            }
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mNumber: String, private val mPassword: String, private val mCheckCode: String) : AsyncTask<Void, Void, Int>() {
        private var mXmStr: String? = null
        private var mOpenId: String? = null

        override fun doInBackground(vararg params: Void): Int? {
            // TODO: attempt authentication against a network service.

            var result: String? = null
            var canLogin = ""
            val netManager = HttpClients.createDefault()
            val tryLoginGetRequest = HttpGetHC4(NetworkThreads.TRY_LOGIN_URL + "?number=" + mNumber +
                    "&password=" + mPassword + "&checkCode="
                    + mCheckCode + "&viewState=" + loginViewState
                    + "&cookie=" + loginCookie)
            NetworkThreads.loginInfo!!.checkCode = mCheckCode
            NetworkThreads.loginInfo!!.number = mNumber
            NetworkThreads.loginInfo!!.password = mPassword
            try {
                result = EntityUtilsHC4.toString(netManager.execute(tryLoginGetRequest).entity, "utf-8")
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                val rsJson = JSONObject(result).getJSONObject("TRY")
                canLogin = rsJson.get("lgRstCode").toString()
                mXmStr = rsJson.get("xm").toString()
                mOpenId = rsJson.get("openAppId").toString()
            } catch (e: JSONException) {
                e.printStackTrace()
                return 3
            }

            NetworkThreads.loginInfo!!.xm = mXmStr
            if (canLogin == "0") {
                return 0
            } else if (canLogin == "2") {
                return 2
            }

            //LitePal测试代码
            if (DataSupport.where("number=?", mNumber).find(UserInfo::class.java).size == 0) {
                val userInfo = UserInfo()
                val loginLog = LoginLog()

                loginLog.lastLogin = SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
                loginLog.hadLogin = "1"
                loginLog.isShowAvator = false
                userInfo.number = mNumber
                userInfo.password = mPassword
                userInfo.cookie = loginCookie
                userInfo.name = mXmStr
                userInfo.loginLog = loginLog
                loginLog.save()
                userInfo.save()
            } else {
                val userInfo = UserInfo()
                val loginLog = LoginLog()
                loginLog.lastLogin = SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
                userInfo.cookie = loginCookie
                userInfo.loginLog = loginLog
                userInfo.updateAll("number=?", mNumber)
            }

            //将用户登录信息存入数据库
            if (sqLiteOperation?.find(mNumber) == null) {
                sqLiteOperation?.insertUser(mNumber, mPassword, loginCookie!!, mXmStr!!)
                sqLiteOperation?.insertLog(mNumber, SimpleDateFormat("yyyy-MM-dd").format(java.util.Date()), "1")
            } else {
                Log.d("SQLITE Update:", "cookie--" + loginCookie!!)
                sqLiteOperation?.updateLoginInfo(mNumber, SimpleDateFormat("yyyy-MM-dd").format(java.util.Date()), loginCookie!!)
            }

            return 1
        }

        override fun onPostExecute(success: Int?) {
            mAuthTask = null

            if (success === 1) {     //1: 登录成功
                sqLiteOperation?.updateOpenId(mNumber, mOpenId!!)
                val intent = Intent()
                intent.setClass(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else if (success === 2) {    //2: 验证码错误
                showProgress(false)
                Thread(threads?.RecvLoginPageThread()).start()
                check_code.error = getString(R.string.error_maybe_invalid_checkcode)
                check_code.requestFocus()
            } else if (success === 0) {                      //0: 密码错误
                showProgress(false)
                Thread(threads?.RecvLoginPageThread()).start()
                et_password.error = getString(R.string.error_incorrect_password)
                et_password.requestFocus()
            } else if (success === 3) {         //连接服务器错误
                showProgress(false)
                Toast.makeText(this@LoginActivity, "连接服务器错误", Toast.LENGTH_LONG).show()
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }
}

