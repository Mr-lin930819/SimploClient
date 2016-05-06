package com.localhost.lin.simploc;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.localhost.lin.simploc.Entity.UserEntity;
import com.localhost.lin.simploc.Fragments.CETFragment;
import com.localhost.lin.simploc.Fragments.CourseTableFragment;
import com.localhost.lin.simploc.Fragments.ExamTimeTableFragment;
import com.localhost.lin.simploc.Fragments.GradeFragment;
import com.localhost.lin.simploc.SQLite.SQLiteOperation;
import com.localhost.lin.simploc.Utils.ImageUtils;
import com.localhost.lin.simploc.Utils.JsonUtils;
import com.localhost.lin.simploc.Utils.NetworkUrlUtils;
import com.localhost.lin.simploc.customview.MaskImage;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtilsHC4;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SETTING_REQUEST_CODE = 1;
    private static final int CUSTOM_QUERY_REQUEST_CODE = 2;
    NetworkThreads threads = null;
    //WebView resultWebview;
    MaskImage toxiang;
    TextView nameText;
    SQLiteOperation sqLiteOperation;
    UserEntity userInfo = null;
    //TabHost tabHost;
    LinearLayout tabHost;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sqLiteOperation = new SQLiteOperation(this);
        setContentView(R.layout.activity_main);
        //载入已登录的用户信息，此处学号获取采用静态全局变量的方式，更好的方式是采用意图Intent
        // UserInfo为已登录用户信息，将会存在于整个MainActivity生命周期内，完成各项查询
        userInfo = sqLiteOperation.findUser(NetworkThreads.loginInfo.getNumber());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        threads = new NetworkThreads(handler);

        /**
         * 生成左边抽屉框的展示数据
         * 包括学号、姓名、专业以及头像照片
         */
        View navHeader = navigationView.inflateHeaderView(R.layout.nav_header_main);
        nameText = (TextView)navHeader.findViewById(R.id.navbar_name_text);
        TextView numberText = (TextView)navHeader.findViewById(R.id.nav_number_text);
        toxiang = (MaskImage)navHeader.findViewById(R.id.imageView);
        numberText.setText(userInfo.getNumber());

        //获取专业名称
        loadMajorName();
        //获取头像图片
        if (sqLiteOperation.queryIsShowAvator(userInfo.getNumber()))
            new AvatarGetTask(userInfo.getNumber(), userInfo.getCookie()).execute((Void)null);

        //默认主界面显示课程表    2016.01.27 Add
        String cstbData = sqLiteOperation.getSavedCsTb(userInfo.getNumber());
        if(!cstbData.equals("")){
            showCourseTable(cstbData, 10);
        }

    }

    /**
     * 联网获得专业名称
     */
    private void loadMajorName(){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(NetworkUrlUtils.XN_OPTIONS_URL, new RequestParams(new HashMap<String, String>() {
            {
//                put("number", userInfo.getNumber());
//                put("xm", userInfo.getName());
//                put("cookie", userInfo.getCookie());
                put("openUserId", userInfo.getOpenAppId());
            }
        }), new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                setCharset("gb2312");
                super.onStart();
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                String majorText = null;
                majorText = JsonUtils.getNodeString(s, "ZY");//获取专业信息
                //nameText.setText(NetworkThreads.loginInfo.getXm() + "\t\t" + retData);
                nameText.setText(userInfo.getName() + "\t\t" + majorText);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("退出应用").setMessage("确定退出吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }else if(id == R.id.action_about) {
            new AlertDialog.Builder(MainActivity.this).setTitle("关于")
                    .setMessage("SimploC    1.0.4 \n\nAuthor: Lin \n" +
                            "Technical Support: Tom Zhang \n\n " +
                            //"\n 应用使用的开源框架/库: \n - AsyncHttpClient (异步网络请求库)" +
                            //"\n - ViewPagerIndicator (ViewPager指示器)" +
                            "\n 应用使用的开源框架/库: \n - AsyncHttpClient " +
                            "\n - ViewPagerIndicator " +
                            "\n - Chart.js (基于Html5 Canvas的图表绘制库)" +
                            "\n - LitePay (SQLite数据库ORM)" +
                            "\n - org.json (JSON解析工具)" +
                            "\n\n 2016.01.27").show();
        }
//        }else if (id == R.id.action_exit) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_exam) {
            // Handle the camera action
            //查询考试信息
            loadOption(QUERY_CTRL.QUERY_EXAM);
        } else if (id == R.id.nav_cet) {
            loadOption(QUERY_CTRL.QUERY_CET);
//        } else if (id == R.id.nav_slideshow) {
//            resultWebview.loadUrl("file:///android_asset/wait_page.html");
//            new Thread(threads.new QueryGradeThread("2014-2015", "",sqLiteOperation)).start();
        } else if (id == R.id.nav_grade) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SelectTimeActivity.class);
            startActivityForResult(intent, CUSTOM_QUERY_REQUEST_CODE);
        } else if (id == R.id.query_lesson) {
            loadOption(QUERY_CTRL.QUERY_LESSON);       //弹出时间选择对话框,启动课程表查询
        } else if (id == R.id.nav_gpa_info) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, CreditStatActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            //resultWebview.loadUrl("file:///android_asset/wait_page.html");
            startActivityForResult(new Intent().setClass(MainActivity.this, SettingActivity.class), SETTING_REQUEST_CODE);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
        } else if (id == R.id.nav_send) {
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("登出").setMessage("确定登出吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logout();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private enum QUERY_CTRL{
        QUERY_LESSON,
        QUERY_EXAM,
        QUERY_CET
    }

    /**
     * 载入查询的学期学年选择对话框
     * @param func  要查询功能（课程表、考试信息等）
     */
    private void loadOption(final QUERY_CTRL func){
        final LinearLayout layout = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_select, null);
        final Spinner xnSpinner = (Spinner)layout.findViewById(R.id.dialog_xn_spinner);
        final Spinner xqSpinner = (Spinner)layout.findViewById(R.id.dialog_xq_spinner);
        final Spinner weekSpinner = (Spinner)layout.findViewById(R.id.dialog_week_spinner);
        String optUrl;

        if(func.equals(QUERY_CTRL.QUERY_CET)){
            queryCET();
            return;
        }

        if(func.equals(QUERY_CTRL.QUERY_LESSON)){
            optUrl = NetworkUrlUtils.TB_XN_OP_URL;
        }else{
            optUrl = NetworkUrlUtils.XN_OPTIONS_URL;
        }

        final AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams(new HashMap<String,String>(){
            {
//                put("number",userInfo.getNumber());
//                put("xm",userInfo.getName());
//                put("cookie",userInfo.getCookie());
                put(NetworkUrlUtils.RQ_K_OPENID, userInfo.getOpenAppId());
            }
        });

        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "获取选项",
                "正在获取选项信息，请稍后...", true, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        httpClient.cancelAllRequests(true);
                    }
                });
        httpClient.get(optUrl, params, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                setCharset("gb2312");
                super.onStart();
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, String s, Throwable throwable) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "网络请求出错", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, String s) {

                dialog.dismiss();
                ArrayList<String> retData = JsonUtils.convJson2List(s, "CXTJ");
                if(retData == null) {
                    Toast.makeText(MainActivity.this, "获取学年失败(服务器错误或网络错误", Toast.LENGTH_LONG).show();
                    return;
                }
                ArrayAdapter xnAdapter = new ArrayAdapter<>(layout.getContext(),
                        android.R.layout.simple_spinner_item, retData);
                ArrayAdapter xqAdapter = new ArrayAdapter<String>(layout.getContext(), android.R.layout.simple_spinner_item,
                        new ArrayList<String>() {
                            {
                                add("");
                                add("第一学期");
                                add("第二学期");
                                add("第三学期");
                            }
                        });
                ArrayAdapter weekAdapter;

                xnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                xnSpinner.setAdapter(xnAdapter);
                xnSpinner.setSelection(1);

                xqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                xqSpinner.setAdapter(xqAdapter);
                xqSpinner.setSelection(1, true);

                //如果是课程表查询，则需要加载周数选择
                if(func.equals(QUERY_CTRL.QUERY_LESSON)){
                    ArrayList<String> weekData = new ArrayList<>();
                    for(int j =0;j<18;j++){
                        weekData.add("第" + String.valueOf(j + 1) + "周");
                    }
                    weekAdapter = new ArrayAdapter<>(layout.getContext(),
                            android.R.layout.simple_spinner_item, weekData);
                    weekSpinner.setAdapter(weekAdapter);
                    weekSpinner.setVisibility(View.VISIBLE);
                }

                new AlertDialog.Builder(MainActivity.this).setTitle("选择要查询的学年或学期").setView(layout)
                        .setPositiveButton("查询", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (func) {
                                    case QUERY_LESSON:
                                        queryLesson(xnSpinner.getSelectedItem().toString(),
                                                String.valueOf(xqSpinner.getSelectedItemPosition()),
                                                String.valueOf(weekSpinner.getSelectedItemPosition() + 1));
                                        break;
                                    case QUERY_EXAM:
                                        queryExam(xnSpinner.getSelectedItem().toString(),
                                                String.valueOf(xqSpinner.getSelectedItemPosition()));
                                        break;
                                    default:
                                        break;
                                }

                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
    }


    /**
     * 启动课程表查询网络请求
     * @param xn    要查询的学年
     * @param xq    要查询的学期
     * @param week  要查询的周数[附加功能]
     */
    private void queryLesson(final String xn, final String xq,final String week) {
        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "课程表", "查询中... ...");

//        //测试
//        if(false) {
//            TestNetwork testNetwork = new TestNetwork(NetworkUrlUtils.TEST_LESSON_URL,"?number=" + userInfo.getNumber() + "&name=" + userInfo.getName() + "&cookie=" +
//                    userInfo.getCookie() + "&xn=" + xn + "&xq=" + xq);
//            new Thread(testNetwork).start();
//            dialog.dismiss();
//            return;
//        }

        AsyncHttpClient networkManager = new AsyncHttpClient();
        RequestParams params = new RequestParams(new HashMap<String,String>(){
            {
//                put("number",userInfo.getNumber());
//                put("name",userInfo.getName());
//                put("cookie",userInfo.getCookie());
                put(NetworkUrlUtils.RQ_K_OPENID, userInfo.getOpenAppId());
                put("xn",xn);
                put("xq",xq);
                put("week",week);
            }
        });
        networkManager.get(NetworkUrlUtils.LESSON_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                setCharset("gb2312");
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "成绩查询失败", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                Log.d("Success!!", userInfo.getNumber());
                dialog.dismiss();
                if(sqLiteOperation.getSavedCsTb(userInfo.getNumber()).equals("")){
                    sqLiteOperation.insertCourseTb(userInfo.getNumber(), s);
                }else{
                    sqLiteOperation.updateCsTb(userInfo.getNumber(), s);
                }
                showCourseTable(s, Integer.parseInt(week));
            }
        });
    }

    /**
     * 启动考试时间表查询的网络请求
     * @param xn    查询的学年
     * @param xq    查询的学期
     */
    private void queryExam(final String xn,final String xq){
        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this,"考试信息","查询中... ...");
        AsyncHttpClient networkManager = new AsyncHttpClient();
        RequestParams params = new RequestParams(new HashMap<String,String>(){
            {
//                put("number",userInfo.getNumber());
//                put("name",userInfo.getName());
//                put("cookie",userInfo.getCookie());
                put(NetworkUrlUtils.RQ_K_OPENID, userInfo.getOpenAppId());
                put("xn",xn);
                put("xq",xq);
            }
        });
        networkManager.get(NetworkUrlUtils.EXAM_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                setCharset("gb2312");
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Log.e("Fail~~", NetworkUrlUtils.EXAM_URL);
                dialog.dismiss();
                Toast.makeText(MainActivity.this,"请求数据出错，重新登陆或稍后重试.,",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                showExamTimeTable(s);
                dialog.dismiss();
            }
        });
    }

    /**
     * 查询等级信息的网络请求
     */
    private void queryCET(){
        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this,"等级考试信息","查询中... ...");
        AsyncHttpClient networkManager = new AsyncHttpClient();
        RequestParams params = new RequestParams(new HashMap<String,String>(){
            {
//                put("number",userInfo.getNumber());
//                put("name",userInfo.getName());
//                put("cookie",userInfo.getCookie());
                put(NetworkUrlUtils.RQ_K_OPENID, userInfo.getOpenAppId());
            }
        });
        networkManager.get(NetworkUrlUtils.CET_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                setCharset("gb2312");
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Log.d("Fail~~", NetworkUrlUtils.EXAM_URL);
                dialog.dismiss();
                Toast.makeText(MainActivity.this,"请求数据出错，重新登陆或稍后重试.,",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int i, Header[] headers, final String s) {
                dialog.dismiss();
                if(s.equals("CODE2")){//需要进行一键评价
                    new AlertDialog.Builder(MainActivity.this).setTitle("一键评价")
                            .setMessage("需要进行一键评价才能继续，确定进行一键评价吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    processOneKeyComment(s,QUERY_CTRL.QUERY_CET,0);
                                }
                            })
                            .setNegativeButton("取消", null).show();
                    return;
                }else if(s.equals("CODE1")) {
                    Toast.makeText(MainActivity.this,"身份信息超时，需重新登陆",Toast.LENGTH_LONG).show();
                    return;
                }
                showCETTable(s);
            }
        });
    }

    private void processOneKeyComment(String s, final QUERY_CTRL func,final int week){
        AsyncHttpClient networkManager = new AsyncHttpClient();
        RequestParams params = new RequestParams(new HashMap<String,String>(){
            {
//                put("number",userInfo.getNumber());
//                put("name",userInfo.getName());
//                put("cookie",userInfo.getCookie());
                put(NetworkUrlUtils.RQ_K_OPENID, userInfo.getOpenAppId());
            }
        });
        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this,"一键评价","一键评价进行中...");
        networkManager.get(NetworkUrlUtils.ONE_KEY_COMMENT, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this,"一键评价成功!",Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 根据Json内容显示课程表信息到课程表视图中
     * @param jsonContent 传入的Json数据
     */
    private void showCourseTable(String jsonContent,int week){
        getSupportActionBar().setTitle("课程表");
        getSupportActionBar().setSubtitle(new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(new java.util.Date()));
        Fragment courseTableView = CourseTableFragment.
                newInstance(jsonContent, String.valueOf(week));
        switchToView(courseTableView);
    }

    /**
     * 根据Json内容显示考试时间表到List中
     * @param jsonContent 传入的Json数据
     */
    private void showExamTimeTable(String jsonContent){
        if(jsonContent == null || jsonContent.equals("")) {
            Toast.makeText(MainActivity.this, "从服务器收到的数据有误", Toast.LENGTH_SHORT).show();
            return;
        }
        getSupportActionBar().setTitle("考试时间表");
        getSupportActionBar().setSubtitle(new SimpleDateFormat("yyyy年MM月dd日",Locale.CHINA).format(new java.util.Date()));
        Fragment timeTableView = ExamTimeTableFragment.newInstance(jsonContent);
        switchToView(timeTableView);
    }

    private void showCETTable(String jsonContent){
        getSupportActionBar().setTitle("等级考试成绩单");
        getSupportActionBar().setSubtitle("");
        Fragment cetFragment = CETFragment.newInstance(jsonContent);
        switchToView(cetFragment);
    }

    private void switchToView(Fragment view) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_page, view);
        transaction.commit();
    }

    private void logout(){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams(){
            {
                put(NetworkUrlUtils.RQ_K_OPENID, userInfo.getOpenAppId());
            }
        };
        httpClient.get(NetworkUrlUtils.LOGOUT, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,LoginActivity.class);
                //设置数据库中登录状态为登出
                sqLiteOperation.updateLoginStatus(NetworkThreads.loginInfo.getNumber(),"0");
                startActivity(intent);
                finish();
            }
        });
    }

    //后台获取头像照片的任务
    private class AvatarGetTask extends AsyncTask<Void,Void,Bitmap>{
        private String mNumber;
        private String mCookie;
        public AvatarGetTask(String number,String cookie) {
            mCookie = cookie;
            mNumber = number;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            CloseableHttpClient netManager = HttpClients.createDefault();
            Bitmap retImage = null;
//            HttpGetHC4 gradeQueryGetRequest = new HttpGetHC4(NetworkUrlUtils.AVATOR_URL + "?number=" + mNumber +
//                    "&cookie=" + mCookie);
            /*
             *   2015-10-16 从服务端获取头像改为直接从网站获取头像
             */
            HttpGetHC4 gradeQueryGetRequest = new HttpGetHC4("http://jwgl.fjnu.edu.cn/readimagexs.aspx?xh=" + userInfo.getNumber());
            gradeQueryGetRequest.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            gradeQueryGetRequest.setHeader("Connection", "Keep-Alive");
            gradeQueryGetRequest.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
            gradeQueryGetRequest.setHeader("Cookie", mCookie);
            gradeQueryGetRequest.setHeader("Upgrade-Insecure-Requests", "1");
            gradeQueryGetRequest.setHeader("Accept-Encoding", "gzip, deflate, sdch");

            byte[] result = null;
            //HashMap<String,String> gradeList = new HashMap<String,String>();
            try {
                result = EntityUtilsHC4.toByteArray(netManager.execute(gradeQueryGetRequest).getEntity());
                //gradeList = convJson2Map(result,"GRADE");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("MainActivity 315:",String.valueOf(result.length));

            retImage = BitmapFactory.decodeByteArray(result,0,result.length);
            retImage = ImageUtils.ImageCrop(retImage);
            retImage = ImageUtils.scaleImage(retImage,72,72);
            return retImage;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap == null){
                return;
            }
            //获取到头像后设置头像到界面
            toxiang.setMaskBitmap(bitmap, BitmapFactory.decodeResource(getResources(), R.drawable.mask));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SETTING_REQUEST_CODE){//设置
            //如果要求（删除用户后）登出：
            if(resultCode == RESULT_OK && data.getStringExtra("action").equals("logout")){
//                Intent intent = new Intent();
//                intent.setClass(MainActivity.this, LoginActivity.class);
//                startActivity(intent);
//                finish();
                logout();
            }else if(resultCode == RESULT_OK && data.getStringExtra("action").equals("refreshAvator")){
                //获取头像图片
                if (sqLiteOperation.queryIsShowAvator(userInfo.getNumber()))
                    new AvatarGetTask(userInfo.getNumber(),userInfo.getCookie()).execute((Void)null);
                else
                    toxiang.setMaskBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.avatar),
                            BitmapFactory.decodeResource(getResources(),R.drawable.mask));
            }
        }else if(requestCode == CUSTOM_QUERY_REQUEST_CODE){//自定义查询成绩
            if(resultCode == RESULT_OK){
                getSupportActionBar().setTitle("成绩单");
                Log.i("Grade", "学年：" + data.getStringExtra("xn") + "  " + data.getStringExtra("xq") );
                //resultWebview.loadUrl("file:///android_asset/wait_page.html");
                progressDialog = ProgressDialog.show(MainActivity.this, "成绩单", "查询中... ...");
                new Thread(threads.new QueryGradeThread(data.getStringExtra("xn"),data.getStringExtra("xq"),sqLiteOperation)).start();
            }
        }
    }

    /**
     * 用于处理消息
     */
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (((String) msg.obj).equalsIgnoreCase("queryGradeFinished")) {
//                Bundle bundle = msg.getData();
//                for(String key:bundle.keySet()){
//
//                }
//                RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_page);
//                layout.removeAllViews();
//                View view = getLayoutInflater().inflate(R.layout.activity_result_page, null);

//                RelativeLayout resultView = (RelativeLayout) view.findViewById(R.id.result_view);
                //WebView resultWebview = (WebView) findViewById(R.id.result_web_view);

                String resultJson = msg.getData().getString("json");
                Fragment gradeView = GradeFragment.newInstance(resultJson);
                progressDialog.dismiss();
                switchToView(gradeView);
            }
        }
    };
}
