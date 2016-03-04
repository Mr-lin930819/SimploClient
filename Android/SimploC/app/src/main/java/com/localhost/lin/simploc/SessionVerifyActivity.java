package com.localhost.lin.simploc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.localhost.lin.simploc.Entity.UserEntity;
import com.localhost.lin.simploc.SQLite.SQLiteOperation;
import com.localhost.lin.simploc.Utils.JsonUtils;
import com.localhost.lin.simploc.Utils.NetworkUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtilsHC4;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class SessionVerifyActivity extends AppCompatActivity {

    SQLiteOperation sqLiteOperation;
    UserEntity userInfo = null;
    private String mViewState = "";
    private String mCookie = "";
    EditText checkInput = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //先进行验证，身份过期了再进行接下去的操作，否则跳转到主界面
        sqLiteOperation = new SQLiteOperation(this);
        userInfo = sqLiteOperation.findUser(sqLiteOperation.findLoginUser());
        setContentView(R.layout.activity_session_verify);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkInput = (EditText)findViewById(R.id.re_login_edittext);
        Button confirmBtn = (Button)findViewById(R.id.re_login_btn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog procBox = new AlertDialog.Builder(SessionVerifyActivity.this).setTitle("重新登录").setMessage("正在重新登录...").show();
                AsyncHttpClient reloginClient = new AsyncHttpClient();
                reloginClient.get(NetworkUtils.RE_LOGIN, new RequestParams(new HashMap<String, String>() {
                    {
                        put(NetworkUtils.RQ_K_OPENID, userInfo.getOpenAppId());
                        put("viewState", mViewState);
                        put("cookie", mCookie);
                        put("checkCode", checkInput.getText().toString());
                    }
                }), new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                        procBox.dismiss();
                        processNetErr();
                    }

                    @Override
                    public void onSuccess(int i, Header[] headers, String s) {
                        JSONObject rsJson;
                        String rstText = "";
                        try {
                            rsJson = new JSONObject(s).getJSONObject("reLoginRst");
                            rstText = rsJson.get("result").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        procBox.dismiss();
                        Log.d("SessionVerify", rstText);
                        if (rstText.equals("SUCCE")) {          //身份信息有效
                            processSuccess();
                        } else if (rstText.equals("ERRVR")) {   //验证码错误
                            processVerifyError();
                        } else if (rstText.equals("ERRSV")) {   //服务器内部错误
                            processNetErr();
                        }
                    }
                });
            }
        });

        verifySession(userInfo.getOpenAppId());
    }

    private void verifySession(final String openID){
        final ProgressDialog verifyDialog = ProgressDialog.show(SessionVerifyActivity.this,
                "登录验证", "正在验证用户信息");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(NetworkUtils.SESSION_VERIFY, new RequestParams(new HashMap<String, String>() {
            {
                put(NetworkUtils.RQ_K_OPENID, openID);
            }
        }), new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                setCharset("gb2312");
                super.onStart();
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                processNetErr();
                verifyDialog.dismiss();
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                JSONObject rsJson;
                String rstText = "";
                try {
                    rsJson = new JSONObject(s).getJSONObject("verifyRst");
                    rstText = rsJson.get("result").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (rstText.equals("SUCCE")) {          //身份信息有效
                    processSuccess();
                } else if (rstText.equals("ERREP")) {   //身份信息过期
                    Log.d("SessionVerify", "验证身份");
                    processExpire();
                }
                verifyDialog.dismiss();
            }
        });
    }

    private void processSuccess(){
        sqLiteOperation.updateLoginInfo(userInfo.getNumber(),
                new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date()), mCookie);
        Intent intent = new Intent();
        intent.setClass(SessionVerifyActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void processNetErr(){
        Toast.makeText(SessionVerifyActivity.this, "服务器错误", Toast.LENGTH_LONG).show();
    }

    private void processVerifyError(){
        processExpire();
        checkInput.setError("验证码输入错误！");
        checkInput.requestFocus();
    }

    private void processExpire(){
        new LoadCheckCode().execute((Void) null);    //载入验证码
    }

    //后台载入验证码
    private class LoadCheckCode extends AsyncTask<Void, Void, byte[]>{

        @Override
        protected byte[] doInBackground(Void... params) {
            CloseableHttpClient netManager = HttpClients.createDefault();
            HttpGetHC4 loginGetRequest = new HttpGetHC4(NetworkUtils.LOGIN_URL);

            String loginPage = "";
            byte[] checkImg = null;
            HashMap<String,String> tmpData = null;

            /*先获取Cookie和ViewState*/
            try {
                loginPage   = EntityUtilsHC4.toString(netManager.execute(loginGetRequest).getEntity());
                //获得单次查询会话的Cookie和ViewState,保存。
                tmpData = JsonUtils.convJson2Map(loginPage, "loginPage");
                Log.d("SessionVerify", "获得登录信息");
                if(tmpData == null){
                    return null;
                }

                //根据之前的Cookie获取验证码图片
                HttpGetHC4 checkImgGetRequest = new HttpGetHC4(NetworkUtils.C_IMG_URL + "?cookie=" + tmpData.get("cookie"));
                checkImg   = EntityUtilsHC4.toByteArray(netManager.execute(checkImgGetRequest).getEntity());
                Log.d("SessionVerify", "获得验证码图片" + tmpData.get("viewState") + "   " +tmpData.get("cookie"));
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    netManager.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(tmpData != null) {
                mViewState = tmpData.get("viewState");
                mCookie = tmpData.get("cookie");
            }
            return checkImg;
        }

        @Override
        protected void onPostExecute(byte[] imgData) {
            super.onPostExecute(imgData);
            //设置验证码的显示
            ImageView iv = (ImageView) findViewById(R.id.re_login_check_img);
            if (imgData != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                iv.setImageBitmap(bitmap);
            }
        }
    }
}
