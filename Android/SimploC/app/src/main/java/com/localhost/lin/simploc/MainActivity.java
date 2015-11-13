package com.localhost.lin.simploc;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

import com.localhost.lin.simploc.SQLite.SQLiteOperation;
import com.localhost.lin.simploc.Utils.ImageUtils;
import com.localhost.lin.simploc.Utils.JsonUtils;
import com.localhost.lin.simploc.Utils.NetworkUtils;
import com.localhost.lin.simploc.customview.MaskImage;

import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtilsHC4;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SETTING_REQUEST_CODE = 1;
    NetworkThreads threads = null;
    private String resultJson;
    WebView resultWebview;
    MaskImage toxiang;
    ListView gradeList;
    SQLiteOperation sqLiteOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqLiteOperation = new SQLiteOperation(this);
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

 //       final Button loginButton = (Button) findViewById(R.id.btn_login);
        threads = new NetworkThreads(handler);

        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("柱状图显示").setContent(R.id.chartLayout));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("表格显示").setContent(R.id.tabelLayout));

        resultWebview = (WebView) findViewById(R.id.result_web_view);
        resultWebview.setWebChromeClient(new MyWebChromeClient());
        resultWebview.getSettings().setJavaScriptEnabled(true);
        resultWebview.getSettings().setSupportZoom(true);
        //扩大比例的缩放
        resultWebview.getSettings().setUseWideViewPort(true);
        resultWebview.addJavascriptInterface(new JsObject(), "jsObject");
        resultWebview.loadUrl("file:///android_asset/welcome_page.html");

        gradeList = (ListView)findViewById(R.id.grade_listview);

        //View navHeader = View.inflate(navigationView.getContext(),R.layout.nav_header_main,null);
        View navHeader = navigationView.inflateHeaderView(R.layout.nav_header_main);
        TextView nameText = (TextView)navHeader.findViewById(R.id.navbar_name_text);
        TextView numberText = (TextView)navHeader.findViewById(R.id.nav_number_text);
        toxiang = (MaskImage)navHeader.findViewById(R.id.imageView);
        //TextView nameText = (TextView)(navigationView.inflateHeaderView(R.layout.nav_header_main)).findViewById(R.id.navbar_name_text);
        //TextView nameText = (TextView)navHeader.findViewById(R.id.navbar_name_text);
        nameText.setText(NetworkThreads.loginInfo.getXm());
        numberText.setText(NetworkThreads.loginInfo.getNumber());
        Log.d("COOOOOOOOOOOOK",NetworkThreads.loginInfo.getCookie());
        //获取头像图片
        new AvatarGetTask(NetworkThreads.loginInfo.getNumber(),NetworkThreads.loginInfo.getCookie()).execute((Void)null);
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
            resultWebview.loadUrl("file:///android_asset/wait_page.html");
            new Thread(threads.new QueryGradeThread("2012-2013", "",sqLiteOperation)).start();
        } else if (id == R.id.nav_gallery) {
            resultWebview.loadUrl("file:///android_asset/wait_page.html");
            new Thread(threads.new QueryGradeThread("2013-2014", "",sqLiteOperation)).start();
        } else if (id == R.id.nav_slideshow) {
            resultWebview.loadUrl("file:///android_asset/wait_page.html");
            new Thread(threads.new QueryGradeThread("2014-2015", "",sqLiteOperation)).start();
        } else if (id == R.id.nav_manage) {
            resultWebview.loadUrl("file:///android_asset/result_page.html");

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SETTING_REQUEST_CODE){
            //如果要求（删除用户后）登出：
            if(resultCode == RESULT_OK && data.getStringExtra("action").equals("logout")){
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
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
            ret += TestUnit.jsMethodTest();
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
            HttpGetHC4 gradeQueryGetRequest = new HttpGetHC4(NetworkUtils.AVATOR_URL + "?number=" + mNumber +
                    "&cookie=" + mCookie);
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
}
