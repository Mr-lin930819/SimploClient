package com.localhost.lin.simploc;

import android.content.Entity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.localhost.lin.simploc.SQLite.SQLiteOperation;
import com.localhost.lin.simploc.Utils.JsonUtils;
import com.localhost.lin.simploc.Utils.NetworkUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtilsHC4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SelectTimeActivity extends AppCompatActivity {

    private Spinner xnSpinner = null;
    SQLiteOperation mSqLiteOperation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_time);
        mSqLiteOperation = new SQLiteOperation(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        xnSpinner = (Spinner)findViewById(R.id.xn_spinner);
        final Spinner xqSpinner = (Spinner)findViewById(R.id.xq_spinner);
        ArrayAdapter xqAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,
                new ArrayList<String>(){
                    {
                        add("");
                        add("第一学期");
                        add("第二学期");
                        add("第三学期");
                    }
                });
        xqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        xqSpinner.setAdapter(xqAdapter);
        xqSpinner.setSelection(1, true);
        Button button = (Button)findViewById(R.id.confire_query);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("xn",(String)xnSpinner.getSelectedItem());
                intent.putExtra("xq",String.valueOf(xqSpinner.getSelectedItemPosition()));
                setResult(RESULT_OK, intent);
                finish();
            }
        });

//        GetOptionTask getOptionTask = new GetOptionTask();
//        getOptionTask.execute();
        loadOption();

    }

    private void loadOption(){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        final String[] lgMsg = mSqLiteOperation.find(NetworkThreads.loginInfo.getNumber());
        RequestParams params = new RequestParams(new HashMap<String,String>(){
            {
//                put("number",NetworkThreads.loginInfo.getNumber());
//                put("xm",lgMsg[4]);
//                put("cookie",lgMsg[3]);
                put("openUserId", lgMsg[8]);
            }
        });
        httpClient.get(NetworkUtils.XN_OPTIONS_URL, params, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                setCharset("gb2312");
                super.onStart();
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, String s, Throwable throwable) {
                Toast.makeText(SelectTimeActivity.this,"网络出错",Toast.LENGTH_LONG);
                finish();
            }

            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, String s) {
                ArrayList<String> retData = JsonUtils.convJson2List(s, "CXTJ");
                Log.d("convert",retData.get(0));
                ArrayAdapter xnAdapter = new ArrayAdapter<>(SelectTimeActivity.this,
                        android.R.layout.simple_spinner_item,retData);
                xnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                xnSpinner.setAdapter(xnAdapter);
                xnSpinner.setSelection(1);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return false;
    }

//    class GetOptionTask extends AsyncTask<Void,Void,ArrayList<String>>{
//        @Override
//        protected ArrayList<String> doInBackground(Void... params) {
//            ArrayList<String> retData;
//            String result = null;
//
//            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
//            String[] loginMsg = mSqLiteOperation.find(NetworkThreads.loginInfo.getNumber());
//            HttpGetHC4 request = new HttpGetHC4(NetworkUtils.XN_OPTIONS_URL + "?number=" + NetworkThreads.loginInfo.getNumber()
//                    + "&xm=" + loginMsg[4] + "&cookie=" + loginMsg[3]);
//            try {
//                result = EntityUtilsHC4.toString(closeableHttpClient.execute(request).getEntity(), "gb2312");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            retData = JsonUtils.convJson2List(result, "CXTJ");
//            return retData;
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<String> strings) {
//            super.onPostExecute(strings);
//            ArrayAdapter xnAdapter = new ArrayAdapter<String>(SelectTimeActivity.this,
//                    android.R.layout.simple_spinner_item,strings);
//            xnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            xnSpinner.setAdapter(xnAdapter);
//            xnSpinner.setSelection(1);
//        }
//    }
}
