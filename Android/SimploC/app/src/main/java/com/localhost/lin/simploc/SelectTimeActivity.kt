package com.localhost.lin.simploc

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.localhost.lin.simploc.SQLite.SQLiteOperation
import com.localhost.lin.simploc.Utils.JsonUtils
import com.localhost.lin.simploc.query_interface.ThesisApi
import kotlinx.android.synthetic.main.activity_select_time.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SelectTimeActivity : AppCompatActivity() {

    private var xnSpinner: Spinner? = null
    internal var mSqLiteOperation: SQLiteOperation? = null
    private val thesisService = CustomApplication.getInstance().retrofit.create(ThesisApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_time)
        mSqLiteOperation = SQLiteOperation(this)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        xnSpinner = findViewById(R.id.xn_spinner) as Spinner?
        val xqSpinner = findViewById(R.id.xq_spinner) as Spinner?
        val xqAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
                object : ArrayList<String>() {
                    init {
                        add("")
                        add("第一学期")
                        add("第二学期")
                        add("第三学期")
                    }
                })
        xqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        xqSpinner!!.adapter = xqAdapter
        xqSpinner.setSelection(1, true)
        val button = findViewById(R.id.confire_query) as Button?
        button!!.setOnClickListener {
            val intent = Intent()
            intent.putExtra("xn", xnSpinner!!.selectedItem as String)
            intent.putExtra("xq", xqSpinner.selectedItemPosition.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        //        GetOptionTask getOptionTask = new GetOptionTask();
        //        getOptionTask.execute();
        loadOption()

    }

    private fun loadOption() {
//        val httpClient = AsyncHttpClient()
        val dialog = ProgressDialog.show(this, "获取选项", "获取学年信息中...")
        val lgMsg = mSqLiteOperation!!.find(NetworkThreads.loginInfo?.number.toString())

        val optionLoader = thesisService.loadXNIOption(lgMsg?.get(8).toString())
        optionLoader.enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                val retData = JsonUtils.convJson2List(response?.body().toString(), "CXTJ")
                Log.d("convert", retData!![0])
                val xnAdapter = ArrayAdapter(this@SelectTimeActivity,
                        android.R.layout.simple_spinner_item, retData)
                xnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                xn_spinner.adapter = xnAdapter
                xn_spinner.setSelection(1)
                dialog.dismiss()
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                Toast.makeText(this@SelectTimeActivity, "网络出错", Toast.LENGTH_LONG).show()
                dialog.dismiss()
                finish()
            }

        })

//        val params = RequestParams(object : HashMap<String, String>() {
//            init {
//                //                put("number",NetworkThreads.loginInfo.getNumber());
//                //                put("xm",lgMsg[4]);
//                //                put("cookie",lgMsg[3]);
//                put("openUserId", lgMsg[8])
//            }
//        })
//        httpClient.get(NetworkUrlUtils.XN_OPTIONS_URL, params, object : TextHttpResponseHandler() {
//
//            override fun onStart() {
//                charset = "gb2312"
//                super.onStart()
//            }
//
//            override fun onFailure(i: Int, headers: Array<cz.msebera.android.httpclient.Header>, s: String, throwable: Throwable) {
//                Toast.makeText(this@SelectTimeActivity, "网络出错", Toast.LENGTH_LONG).show()
//                dialog.dismiss()
//                finish()
//            }
//
//            override fun onSuccess(i: Int, headers: Array<cz.msebera.android.httpclient.Header>, s: String) {
//                val retData = JsonUtils.convJson2List(s, "CXTJ")
//                Log.d("convert", retData!![0])
//                val xnAdapter = ArrayAdapter(this@SelectTimeActivity,
//                        android.R.layout.simple_spinner_item, retData)
//                xnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                xnSpinner!!.adapter = xnAdapter
//                xnSpinner!!.setSelection(1)
//                dialog.dismiss()
//            }
//        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }

    //    class GetOptionTask extends AsyncTask<Void,Void,ArrayList<String>>{
    //        @Override
    //        protected ArrayList<String> doInBackground(Void... params) {
    //            ArrayList<String> retData;
    //            String result = null;
    //
    //            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
    //            String[] loginMsg = mSqLiteOperation.find(NetworkThreads.loginInfo.getNumber());
    //            HttpGetHC4 request = new HttpGetHC4(NetworkUrlUtils.XN_OPTIONS_URL + "?number=" + NetworkThreads.loginInfo.getNumber()
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
