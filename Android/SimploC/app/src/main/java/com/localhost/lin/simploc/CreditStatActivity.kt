package com.localhost.lin.simploc

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.localhost.lin.simploc.Fragments.CreditFragment
import com.localhost.lin.simploc.Fragments.GPAFragment
import com.localhost.lin.simploc.SQLite.SQLiteOperation
import com.localhost.lin.simploc.Utils.NetworkUrlUtils
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_credit_stat.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 这个Activity用于显示查询的学分信息
 */
class CreditStatActivity : AppCompatActivity(), GPAFragment.OnFragmentInteractionListener, CreditFragment.OnFragmentInteractionListener {

    private val mPagerViews = ArrayList<Fragment>()
    private var mSqLiteOperation: SQLiteOperation? = null
    private var mPagerAdapter: PagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_stat)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mSqLiteOperation = SQLiteOperation(this)
        initContentView()
        loadData()
    }

    private fun initContentView() {
        mPagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
            private val titleList = arrayOf("绩点统计", "总学分信息", "选修课学分")
            override fun getItem(position: Int): Fragment {
                return mPagerViews[position]
            }

            override fun getCount(): Int {
                return mPagerViews.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return titleList[position % 3]
            }
        }
        //mViewPager.setAdapter(pagerAdapter);
    }

    internal fun loadData() {
        val user = mSqLiteOperation!!.findUser(NetworkThreads.loginInfo?.number.toString())
        val creditClient = AsyncHttpClient()
        val processDialog = ProgressDialog.show(this@CreditStatActivity, "学分绩点统计", "查询中...")
        creditClient.get(NetworkThreads.QUERY_URL, RequestParams(object : HashMap<String, String>() {
            init {
                put(NetworkUrlUtils.RQ_K_OPENID, user?.openAppId.toString())
                put("grade_function", FN_CREDIT_QUERY)
            }
        }), object : TextHttpResponseHandler() {
            override fun onStart() {
                charset = "gb2312"
                super.onStart()
            }

            override fun onFailure(i: Int, headers: Array<Header>, s: String, throwable: Throwable) {
                processDialog.dismiss()
                Toast.makeText(this@CreditStatActivity, "网络连接错误，稍后重试", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onSuccess(i: Int, headers: Array<Header>, s: String) {
                try {
                    //处理返回的json数据
                    processRecvJsonData(s)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                processDialog.dismiss()
            }
        })
    }

    /**
     * Json数据结构：
     * GRADE:{
     * AllCredit:{		(所有课程学分)
     * [
     * {
     * class:"课程性质"
     * need:"学分要求",
     * get:"获得学分",
     * nopass:"未通过学分",
     * rest:"还需学分"
     * },... ...
     * ]
     * },
     * OptionCredit:{		(选修课学分)
     * {
     * class:"课程性质"
     * need:"学分要求",
     * get:"获得学分",
     * nopass:"未通过学分",
     * rest:"还需学分"
     * },... ...
     * },
     * GPAInfo:{
     * students:"专业学生数量",
     * averageGPA:"平均学分绩点",
     * totalGPA:"学分绩点总和"
     * },
     * Total:{		(统计)
     * select:"所选学分",
     * get:"获得学分",
     * revamp:"重修学分",
     * nopass:"正考未通过学分"
     * }
     * }
     */
    @Throws(JSONException::class)
    private fun processRecvJsonData(jsonData: String) {
        val receiveJson = JSONObject(jsonData)
        Log.d(TAG, "Json: " + jsonData)
        val allCredits: JSONArray
        val optionCredits: JSONArray
        val infoGPA: JSONObject
        val infoTotal: JSONObject
        allCredits = receiveJson.getJSONObject("GRADE").getJSONArray("AllCredit")
        optionCredits = receiveJson.getJSONObject("GRADE").getJSONArray("OptionCredit")
        infoGPA = receiveJson.getJSONObject("GRADE").getJSONObject("GPAInfo")
        infoTotal = receiveJson.getJSONObject("GRADE").getJSONObject("Total")
        val gpaFragment = GPAFragment.newInstance(infoGPA.toString(), infoTotal.toString())
        val creditFragment = CreditFragment.newInstance(allCredits.toString())
        val optCreditFragment = CreditFragment.newInstance(optionCredits.toString())
        mPagerViews.add(gpaFragment)
        mPagerViews.add(creditFragment)
        mPagerViews.add(optCreditFragment)
        view_pager_credit.adapter = mPagerAdapter
        //indicator_credit.visibility = View.VISIBLE
        //indicator_credit.setViewPager(view_pager_credit)
    }


    override fun onFragmentInteraction(uri: Uri) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        } else {
            return false
        }
    }

    companion object {

        private val TAG = "[-CreditStatActivity-]"
        private val FN_CREDIT_QUERY = "11"
    }
}
