package com.localhost.lin.simploc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.localhost.lin.simploc.Entity.LoginLog;
import com.localhost.lin.simploc.Entity.UserInfo;
import com.localhost.lin.simploc.SQLite.SQLiteOperation;

import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtilsHC4;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{
        //implements LoaderCallbacks<Cursor> {

    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private EditText mCheckCodeView;

    private String loginViewState,loginCookie;
    NetworkThreads threads;
    SQLiteOperation sqLiteOperation;
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //SQLiteDatabase sqLiteDatabase = Connector.getDatabase();
        //数据库操作
        sqLiteOperation = new SQLiteOperation(this);
        /**启动后台线程获取登录验证码*/
        threads = new NetworkThreads(handler);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();
        mEmailView.addTextChangedListener(new NumberInputWatcher());
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

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mCheckCodeView = (EditText)findViewById(R.id.check_code);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Thread loginThread = new Thread(threads.new RecvLoginPageThread());
        loginThread.start();

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    //监听学号输入，满12位且存有当前学号，则补全密码
    private class NumberInputWatcher implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String [] info = null;
            if (s.length() == 12){
                info = sqLiteOperation.find(s.toString());
                if(info != null){
                    mPasswordView.setText(info[2]);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private void populateAutoComplete() {
        String nowDate = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String number = sqLiteOperation.findLoginUser(nowDate);
//        if (!mayRequestContacts()) {
//            return;
//        }
        //如果当天已登录，则自动登陆

        //替换为向服务器请求验证，不必局限于当天登录，只验证已登录用户
        if( number != null){
            String[] data = sqLiteOperation.find(number);
            NetworkThreads.loginInfo.setNumber(data[1]);
            NetworkThreads.loginInfo.setCookie(data[3]);
            NetworkThreads.loginInfo.setXm(data[4]);
            startActivity(new Intent().setClass(LoginActivity.this,SessionVerifyActivity.class));
            finish();
        }else{
            List<String> list = sqLiteOperation.getAllNumber();
            addEmailsToAutoComplete(list);
        }
        //getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String checkCode = mCheckCodeView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isNumberValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password,checkCode);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isNumberValid(String number) {
        //TODO: Replace this with your own logic
        return (number.length() == 12 && Pattern.compile("[0-9]*").matcher(number).matches());
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (((String) msg.obj).equalsIgnoreCase("loginPageLoaded")) {
                Bundle bundle = msg.getData();
                ImageView iv = (ImageView) findViewById(R.id.check_code_img);
                loginViewState = bundle.getString("viewState");
                loginCookie = bundle.getString("cookie");
                byte[] imgRes = bundle.getByteArray("checkImg");
                if (imgRes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgRes, 0, imgRes.length);
                    iv.setImageBitmap(bitmap);
                }
            }else if(((String)msg.obj).equalsIgnoreCase("runError")){
                String info = msg.getData().getString("info");
                //登录界面获取错误，重试
                if(info.equals("LoginPage")){
                    new Thread(threads.new RecvLoginPageThread()).start();
                    //重试次数大于5，退出程序
                    if( ++retryCount > 5){
                        Toast.makeText(LoginActivity.this,"网络连接错误",Toast.LENGTH_LONG);
                        LoginActivity.this.finish();
                    }
                }
            }
        }
    };

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String mNumber;
        private final String mPassword;
        private final String mCheckCode;
        private String mXmStr, mOpenId;

        UserLoginTask(String email, String password,String checkCode) {
            mNumber = email;
            mPassword = password;
            mCheckCode = checkCode;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            String result = null;
            String canLogin="";
            CloseableHttpClient netManager = HttpClients.createDefault();
            HttpGetHC4 tryLoginGetRequest = new HttpGetHC4(NetworkThreads.TRY_LOGIN_URL + "?number=" + mNumber +
                    "&password=" + mPassword + "&checkCode="
                    + mCheckCode + "&viewState=" + loginViewState
                    + "&cookie=" + loginCookie);
            NetworkThreads.loginInfo.setCheckCode(mCheckCode);
            NetworkThreads.loginInfo.setNumber(mNumber);
            NetworkThreads.loginInfo.setPassword(mPassword);
            try {
                result = EntityUtilsHC4.toString(netManager.execute(tryLoginGetRequest).getEntity(),"gb2312");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                JSONObject rsJson = new JSONObject(result).getJSONObject("TRY");
                canLogin = rsJson.get("lgRstCode").toString();
                mXmStr = rsJson.get("xm").toString();
                mOpenId = rsJson.get("openAppId").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            NetworkThreads.loginInfo.setXm(mXmStr);
            if(canLogin.equals("0")){
                return 0;
            }else if(canLogin.equals("2")){
                return 2;
            }

            //LitePal测试代码
            if(DataSupport.where("number=?", mNumber).find(UserInfo.class).size() == 0){
                UserInfo userInfo = new UserInfo();
                LoginLog loginLog = new LoginLog();

                loginLog.setLastLogin(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
                loginLog.setHadLogin("1");
                loginLog.setShowAvator(false);
                userInfo.setNumber(mNumber);
                userInfo.setPassword(mPassword);
                userInfo.setCookie(loginCookie);
                userInfo.setName(mXmStr);
                userInfo.setLoginLog(loginLog);
                loginLog.save();
                userInfo.save();
            } else {
                UserInfo userInfo = new UserInfo();
                LoginLog loginLog = new LoginLog();
                loginLog.setLastLogin(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
                userInfo.setCookie(loginCookie);
                userInfo.setLoginLog(loginLog);
                userInfo.updateAll("number=?", mNumber);
            }

            //将用户登录信息存入数据库
            if(sqLiteOperation.find(mNumber) == null){
                sqLiteOperation.insertUser(mNumber,mPassword,loginCookie,mXmStr);
                sqLiteOperation.insertLog(mNumber,new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()),"1");
            }else {
                Log.d("SQLITE Update:","cookie--"+loginCookie);
                sqLiteOperation.updateLoginInfo(mNumber, new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()), loginCookie);
            }

            return 1;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mAuthTask = null;

            if (success == 1) {     //1: 登录成功
                sqLiteOperation.updateOpenId(mNumber, mOpenId);
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }else if(success == 2) {    //2: 验证码错误
                showProgress(false);
                new Thread(threads.new RecvLoginPageThread()).start();
                mCheckCodeView.setError(getString(R.string.error_maybe_invalid_checkcode));
                mCheckCodeView.requestFocus();
            }
            else {                      //0: 密码错误
                showProgress(false);
                new Thread(threads.new RecvLoginPageThread()).start();
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

