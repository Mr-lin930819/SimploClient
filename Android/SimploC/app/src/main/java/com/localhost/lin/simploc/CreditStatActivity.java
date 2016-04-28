package com.localhost.lin.simploc;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.localhost.lin.simploc.Entity.UserEntity;
import com.localhost.lin.simploc.Fragments.CreditFragment;
import com.localhost.lin.simploc.Fragments.GPAFragment;
import com.localhost.lin.simploc.SQLite.SQLiteOperation;
import com.localhost.lin.simploc.Utils.NetworkUrlUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.viewpagerindicator.TabPageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * 这个Activity用于显示查询的学分信息
 */
public class CreditStatActivity extends AppCompatActivity
        implements GPAFragment.OnFragmentInteractionListener, CreditFragment.OnFragmentInteractionListener{

    private TabPageIndicator mViewIndicator;
    private ViewPager mViewPager;
    private List<Fragment> mPagerViews = new ArrayList<>();
    private SQLiteOperation mSqLiteOperation;
    private PagerAdapter mPagerAdapter;

    private static final String TAG = "[-CreditStatActivity-]";
    private static final String FN_CREDIT_QUERY = "11";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_stat);
        mSqLiteOperation = new SQLiteOperation(this);
        initContentView();
        loadData();
    }

    private void initContentView() {
        mViewIndicator = (TabPageIndicator) findViewById(R.id.indicator_credit);
        mViewPager = (ViewPager) findViewById(R.id.view_pager_credit);
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private String[] titleList = new String[]{"绩点统计", "总学分信息", "选修课学分"};
            @Override
            public Fragment getItem(int position) {
                return mPagerViews.get(position);
            }

            @Override
            public int getCount() {
                return mPagerViews.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titleList[ position % 3 ];
            }
        };
        //mViewPager.setAdapter(pagerAdapter);
    }

    void loadData() {
        final UserEntity user = mSqLiteOperation.findUser(NetworkThreads.loginInfo.getNumber());
        AsyncHttpClient creditClient = new AsyncHttpClient();
        final Dialog processDialog = ProgressDialog.show(CreditStatActivity.this, "学分绩点统计", "查询中...");
        creditClient.get(NetworkThreads.QUERY_URL, new RequestParams(new HashMap<String, String>() {
            {
                put(NetworkUrlUtils.RQ_K_OPENID, user.getOpenAppId());
                put("grade_function", FN_CREDIT_QUERY);
            }
        }), new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                setCharset("gb2312");
                super.onStart();
            }
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                processDialog.dismiss();
                Toast.makeText(CreditStatActivity.this, "网络连接错误，稍后重试", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                try {
                    //处理返回的json数据
                    processRecvJsonData(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                processDialog.dismiss();
            }
        });
    }

    /**
     * Json数据结构：
     * 		GRADE:{
     * 		 	AllCredit:{		(所有课程学分)
     *				[
     *					{
     *						class:"课程性质"
     *						need:"学分要求",
     *						get:"获得学分",
     *						nopass:"未通过学分",
     *						rest:"还需学分"
     *					},... ...
     *				]
     * 		 	},
     * 		 	OptionCredit:{		(选修课学分)
     *					{
     *						class:"课程性质"
     *						need:"学分要求",
     *						get:"获得学分",
     *						nopass:"未通过学分",
     *						rest:"还需学分"
     *					},... ...
     * 		 	},
     * 		 	GPAInfo:{
     * 		 	  	students:"专业学生数量",
     * 		 	  	averageGPA:"平均学分绩点",
     * 		 	  	totalGPA:"学分绩点总和"
     * 		 	},
     * 		 	Total:{		(统计)
     * 		 	    select:"所选学分",
     * 		 	    get:"获得学分",
     * 		 	    revamp:"重修学分",
     * 		 	    nopass:"正考未通过学分"
     * 		 	}
     * 		}
     */
    private void processRecvJsonData(String jsonData) throws JSONException {
        JSONObject receiveJson = new JSONObject(jsonData);
        Log.d(TAG, "Json: " + jsonData);
        JSONArray allCredits, optionCredits;
        JSONObject infoGPA, infoTotal;
        allCredits = receiveJson.getJSONObject("GRADE").getJSONArray("AllCredit");
        optionCredits = receiveJson.getJSONObject("GRADE").getJSONArray("OptionCredit");
        infoGPA = receiveJson.getJSONObject("GRADE").getJSONObject("GPAInfo");
        infoTotal = receiveJson.getJSONObject("GRADE").getJSONObject("Total");
        GPAFragment gpaFragment = GPAFragment.newInstance(infoGPA.toString(), infoTotal.toString());
        CreditFragment creditFragment = CreditFragment.newInstance(allCredits.toString()),
                optCreditFragment = CreditFragment.newInstance(optionCredits.toString());
        mPagerViews.add(gpaFragment);
        mPagerViews.add(creditFragment);
        mPagerViews.add(optCreditFragment);
        mViewPager.setAdapter(mPagerAdapter);
        mViewIndicator.setVisibility(View.VISIBLE);
        mViewIndicator.setViewPager(mViewPager);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
