package com.localhost.lin.simploc;

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
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.localhost.lin.simploc.Entity.UserEntity;
import com.localhost.lin.simploc.Entity.UserInfo;
import com.localhost.lin.simploc.SQLite.SQLiteOperation;
import com.localhost.lin.simploc.Utils.ImageUtils;
import com.localhost.lin.simploc.Utils.JsonUtils;
import com.localhost.lin.simploc.Utils.NetworkUtils;
import com.localhost.lin.simploc.customview.MaskImage;
import com.localhost.lin.simploc.customview.NoneScrollGridView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtilsHC4;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.Inflater;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.HttpClient;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SETTING_REQUEST_CODE = 1;
    private static final int CUSTOM_QUERY_REQUEST_CODE = 2;
    NetworkThreads threads = null;
    private String resultJson;
    WebView resultWebview;
    MaskImage toxiang;
    TextView nameText;
    ListView gradeList,examList;
    SQLiteOperation sqLiteOperation;
    UserEntity userInfo = null;
    NoneScrollGridView courseTable,courseTableColumn,courseTableRow;
    LinearLayout tableView,mainInfoLayout;
    TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqLiteOperation = new SQLiteOperation(this);
        //载入已登录的用户信息，此处学号获取采用静态全局变量的方式，更好的方式是采用意图Intent
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

        tabHost = (TabHost)findViewById(R.id.tabHost);
        resultWebview = (WebView) findViewById(R.id.result_web_view);

        gradeList = (ListView)findViewById(R.id.grade_listview);
        tableView = (LinearLayout)findViewById(R.id.table_view);
        mainInfoLayout = (LinearLayout)findViewById(R.id.main_info_layout);

        initContentView();

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

    }

    void initContentView(){
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("柱状图显示").setContent(R.id.chartLayout));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("表格显示").setContent(R.id.tabelLayout));
        resultWebview.setWebChromeClient(new MyWebChromeClient());
        resultWebview.getSettings().setJavaScriptEnabled(true);
        resultWebview.getSettings().setSupportZoom(true);
        //扩大比例的缩放
        resultWebview.getSettings().setUseWideViewPort(true);
        resultWebview.addJavascriptInterface(new JsObject(), "jsObject");
        resultWebview.loadUrl("file:///android_asset/welcome_page.html");
    }

    /**
     * 联网获得专业名称
     */
    private void loadMajorName(){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(NetworkUtils.XN_OPTIONS_URL, new RequestParams(new HashMap<String, String>() {
            {
                put("number", userInfo.getNumber());
                put("xm", userInfo.getName());
                put("cookie", userInfo.getCookie());
            }
        }), new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                setCharset("gb2312");
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                String retData = null;
                retData = JsonUtils.getNodeString(s, "ZY");//获取专业信息
                nameText.setText(NetworkThreads.loginInfo.getXm() + "\t\t" + retData);
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
        if (id == R.id.action_settings) {
            return true;
        }else if (id == R.id.action_exit) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
            loadOption(QUERY_CTRL.QUERY_EXAM);
        } else if (id == R.id.nav_gallery) {
            resultWebview.loadUrl("file:///android_asset/wait_page.html");
            new Thread(threads.new QueryGradeThread("2013-2014", "",sqLiteOperation)).start();
        } else if (id == R.id.nav_slideshow) {
            resultWebview.loadUrl("file:///android_asset/wait_page.html");
            new Thread(threads.new QueryGradeThread("2014-2015", "",sqLiteOperation)).start();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this,SelectTimeActivity.class);
            startActivityForResult(intent, CUSTOM_QUERY_REQUEST_CODE);

        } else if (id == R.id.query_lesson) {
            loadOption(QUERY_CTRL.QUERY_LESSON);       //弹出时间选择对话框,启动课程表查询
        } else if (id == R.id.nav_share) {
            //resultWebview.loadUrl("file:///android_asset/wait_page.html");
            startActivityForResult(new Intent().setClass(MainActivity.this, SettingActivity.class), SETTING_REQUEST_CODE);
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.fade_out);
        } else if (id == R.id.nav_send) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this,LoginActivity.class);
            //设置数据库中登录状态为登出
            sqLiteOperation.updateLoginStatus(NetworkThreads.loginInfo.getNumber(),"0");
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    enum QUERY_CTRL{
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
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams(new HashMap<String,String>(){
            {
                put("number",userInfo.getNumber());
                put("xm",userInfo.getName());
                put("cookie",userInfo.getCookie());
            }
        });


        httpClient.get(NetworkUtils.XN_OPTIONS_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, String s, Throwable throwable) {
                Toast.makeText(MainActivity.this, "网络出错", Toast.LENGTH_LONG);
            }

            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, String s) {
                ArrayList<String> retData = JsonUtils.convJson2List(s, "CXTJ");
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
                xnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                xnSpinner.setAdapter(xnAdapter);
                xnSpinner.setSelection(1);

                xqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                xqSpinner.setAdapter(xqAdapter);
                xqSpinner.setSelection(1, true);
                new AlertDialog.Builder(MainActivity.this).setTitle("选择要查询的学年或学期").setView(layout)
                        .setPositiveButton("查询", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (func) {
                                    case QUERY_LESSON:
                                        queryLesson(xnSpinner.getSelectedItem().toString(),
                                                String.valueOf(xqSpinner.getSelectedItemPosition()));
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
     */
    private void queryLesson(final String xn, final String xq) {
        AsyncHttpClient networkManager = new AsyncHttpClient();
        RequestParams params = new RequestParams(new HashMap<String,String>(){
            {
                put("number",userInfo.getNumber());
                put("name",userInfo.getName());
                put("cookie",userInfo.getCookie());
                put("xn",xn);
                put("xq",xq);
            }
        });
        networkManager.get(NetworkUtils.LESSON_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                setCharset("gb2312");
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Log.d("Fail~~", NetworkUtils.LESSON_URL);
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                Log.d("Success!!", userInfo.getNumber());
                showCourseTable(s);
            }
        });
    }

    /**
     * 启动考试时间表查询的网络请求
     * @param xn    查询的学年
     * @param xq    查询的学期
     */
    private void queryExam(final String xn,final String xq){
        AsyncHttpClient networkManager = new AsyncHttpClient();
        RequestParams params = new RequestParams(new HashMap<String,String>(){
            {
                put("number",userInfo.getNumber());
                put("name",userInfo.getName());
                put("cookie",userInfo.getCookie());
                put("xn",xn);
                put("xq",xq);
            }
        });
        networkManager.get(NetworkUtils.EXAM_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                setCharset("gb2312");
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Log.d("Fail~~", NetworkUtils.EXAM_URL);
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                showExamTimeTable(s);
            }
        });
    }

    /**
     * 根据Json内容显示课程表信息到课程表视图中
     * @param jsonContent 传入的Json数据
     */
    private void showCourseTable(String jsonContent){
        courseTable = (NoneScrollGridView)findViewById(R.id.lesson_table);
        courseTableColumn = (NoneScrollGridView)findViewById(R.id.table_column);
        courseTableRow = (NoneScrollGridView)findViewById(R.id.table_row);

        String[] weekString = new String[]{"一","二","三","四","五","六","日"};
        //设置列表头
        ArrayList<Map<String,String>> colmunData = new ArrayList<Map<String, String>>();
        for(int i = 0 ;i < 7;i++){
            Map<String,String> map  = new HashMap<String,String>();
            map.put("item","星期"+weekString[i]);
            colmunData.add(map);
        }
        SimpleAdapter columnAdapter = new SimpleAdapter(MainActivity.this, colmunData, R.layout.course_table_column_item,
                new String[]{"item"}, new int[]{R.id.column_item});
        courseTableColumn.setAdapter(columnAdapter);

        //设置行表头
        ArrayList<Map<String,String>> rowData = new ArrayList<Map<String, String>>();
        for(int i = 0 ;i < 6;i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("item", "第" + String.valueOf(i*2 + 1) + "-" + String.valueOf(i*2 + 2) + "节");
            rowData.add(map);
        }
        SimpleAdapter rowAdapter = new SimpleAdapter(MainActivity.this, rowData, R.layout.course_table_row_item,
                new String[]{"item"}, new int[]{R.id.row_item});
        courseTableRow.setAdapter(rowAdapter);

        //设置表数据
        ArrayList<Map<String,Object>> tableData = new ArrayList<Map<String, Object>>();
//        for(Map.Entry<String,String> item:JsonUtils.convJson2Map(jsonContent,"GRADE").entrySet()){
//            Map<String,Object> map  = new HashMap<String,Object>();
//            map.put("item",item.getKey());        //在表格中添加一个Item，设置Item的文字
//            map.put("back",R.color.colorPrimary);     //设置Item的背景
//            tableData.add(map);
//        }
        ArrayList<String> rawData = new ArrayList<String>();
        String[] lessonNumber = new String[]{"第1节","第3节","第5节","第7节","第9节","第11节"};
        int maxlesson = JsonUtils.numOfNode(jsonContent);
        for(int i =0;i<maxlesson;i++){
            Log.d("Converting...",lessonNumber[i]);
            rawData.addAll(JsonUtils.convJson2List(jsonContent, lessonNumber[i]));   //从json数据中获取节数相关的一周所有课程
        }
        for (String s:rawData){
            Map<String,Object> map = new HashMap<String,Object>();
            if(s.equals("?")){
                map.put("item","");
                map.put("back",R.color.colorTransparent);
            }else {
                map.put("item", s);                          //在表格中添加一个Item，设置Item的文字
                map.put("back", R.color.colorPrimary);    //设置Item的背景
            }
            tableData.add(map);
        }
        SimpleAdapter tableAdapter = new SimpleAdapter(MainActivity.this,tableData,R.layout.course_table_item,
                new String[]{"item","back"},new int[]{R.id.course_name,R.id.course_icon});
        courseTable.setAdapter(tableAdapter);
        courseTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Clicking!!! --> ", String.valueOf(position));//课程表点击事件处理
            }
        });

        setViewVisable(VIEWS.LESSON_TABLE);
        //courseTable.setVisibility(View.VISIBLE);
    }

    /**
     * 根据Json内容显示考试时间表到List中
     * @param jsonContent 传入的Json数据
     */
    private void showExamTimeTable(String jsonContent){
        examList = (ListView)findViewById(R.id.main_info_list);
        ArrayList<ArrayList<String>> rawData = JsonUtils.convJson2StringLists(jsonContent);
        ArrayList<Map<String,String>> listData = new ArrayList<Map<String, String>>();//List表格数据

        for(ArrayList<String> itemData:rawData){
            Map<String,String> map = new HashMap<>();
            for (String s: itemData){
                map.put("item" + String.valueOf(itemData.indexOf(s) + 1), s);
            }
            listData.add(map);
        }
        SimpleAdapter examAdapter = new SimpleAdapter(MainActivity.this,listData,R.layout.exam_list_item,
                new String[]{"item1","item2","item3","item4","item5"},
                new int[]{R.id.exam_name,R.id.exam_time,R.id.exam_addr,R.id.exam_site,R.id.exam_zone});
        examList.setAdapter(examAdapter);
        setViewVisable(VIEWS.MAIN_INFO);
    }

    private enum VIEWS{
        LESSON_TABLE,
        GRADE_TAB,
        MAIN_INFO
    }
    private void setViewVisable(VIEWS view){
        switch (view){
            case LESSON_TABLE:
                tabHost.setVisibility(View.GONE);
                mainInfoLayout.setVisibility(View.GONE);
                tableView.setVisibility(View.VISIBLE);
                break;
            case GRADE_TAB:
                tableView.setVisibility(View.GONE);
                mainInfoLayout.setVisibility(View.GONE);
                tabHost.setVisibility(View.VISIBLE);
                break;
            case MAIN_INFO:
                tabHost.setVisibility(View.GONE);
                tableView.setVisibility(View.GONE);
                mainInfoLayout.setVisibility(View.VISIBLE);
                break;
            default:break;
        }
    }

    /**
     * 用于与JS交互的接口对象
     */
    public class JsObject {
        @JavascriptInterface
        public String getGradeJson() {
            return resultJson;
        }

        @JavascriptInterface
        public String getJsonTest(){
            String ret = "";
//            ret += "'";
//            ret += Test.jsMethodTest();
//            ret += "'";
            return ret;
        }
    }

    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d("Web Console------>", message);
            result.confirm();
            return true;
        }
    }

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
//            HttpGetHC4 gradeQueryGetRequest = new HttpGetHC4(NetworkUtils.AVATOR_URL + "?number=" + mNumber +
//                    "&cookie=" + mCookie);
            /*
             *   2015-1-16 从服务端获取头像改为直接从网站获取头像
             */
            HttpGetHC4 gradeQueryGetRequest = new HttpGetHC4("http://jwgl.fjnu.edu.cn/readimagexs.aspx?xh=" + mNumber);
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
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
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
                setViewVisable(VIEWS.GRADE_TAB);
                resultWebview.loadUrl("file:///android_asset/wait_page.html");
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
//            //载入验证码图片
//            if (((String) msg.obj).equalsIgnoreCase("loginPageLoaded")) {
//                Bundle bundle = msg.getData();
//                ImageView iv = (ImageView) findViewById(R.id.code_img);
//                byte[] imgRes = bundle.getByteArray("checkImg");
//                if (imgRes != null) {
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgRes, 0, imgRes.length);
//                    iv.setImageBitmap(bitmap);
//                }
//            } else if (((String) msg.obj).equalsIgnoreCase("tryLoginEnd")) {
//                Button btn = (Button) findViewById(R.id.btn_login);
//                btn.setClickable(true);
//
//                Bundle bundle = msg.getData();
//                btn.setText(bundle.getString("canLogin"));
//
//            } else if (((String) msg.obj).equalsIgnoreCase("queryGradeFinished")) {
            if (((String) msg.obj).equalsIgnoreCase("queryGradeFinished")) {
//                Bundle bundle = msg.getData();
//                for(String key:bundle.keySet()){
//
//                }
//                RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_page);
//                layout.removeAllViews();
//                View view = getLayoutInflater().inflate(R.layout.activity_result_page, null);

//                RelativeLayout resultView = (RelativeLayout) view.findViewById(R.id.result_view);
                WebView resultWebview = (WebView) findViewById(R.id.result_web_view);

                resultJson = msg.getData().getString("json");
                Log.d("LLAALLAA", resultJson);
                resultWebview.setWebChromeClient(new MyWebChromeClient());
                resultWebview.getSettings().setJavaScriptEnabled(true);
                resultWebview.addJavascriptInterface(new JsObject(), "jsObject");

                resultWebview.loadUrl("file:///android_asset/result_page.html");

                //添加列表数据
                ArrayList<Map<String,String>> data = new ArrayList<Map<String, String>>();
                for(Map.Entry<String,String> item:JsonUtils.convJson2Map(resultJson,"GRADE").entrySet()){
                    Map<String,String> map  = new HashMap<String,String>();
                    Log.d("MainActivity 253:",item.getKey() +"," + item.getValue());
                    map.put("item",item.getKey());
                    map.put("value",item.getValue());
                    data.add(map);
                }
                SimpleAdapter adapter = new SimpleAdapter(MainActivity.this,data,R.layout.grade_list_item,
                        new String[]{"item","value"},new int[]{R.id.grade_item,R.id.grade_value});
                gradeList.setAdapter(adapter);
            }
        }
    };


}
