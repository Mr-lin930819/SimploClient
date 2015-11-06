package com.localhost.lin.simploc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.net.Uri;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NetworkThreads threads = null;
    private String resultJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        final Button loginButton = (Button) findViewById(R.id.btn_login);
        threads = new NetworkThreads(handler);
        final EditText nameET = (EditText) findViewById(R.id.stu_num_edit);
        final EditText passET = (EditText) findViewById(R.id.stu_passwd_edit);
        final EditText checkET = (EditText) findViewById(R.id.code_edit);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(threads.new TryLoginThread(nameET.getText().toString(), passET.getText().toString()
                        , checkET.getText().toString())).start();
                loginButton.setClickable(false);
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
        } else if (id == R.id.nav_gallery) {
            Thread loginThread = new Thread(threads.new RecvLoginPageThread());
            loginThread.start();
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            new Thread(threads.new QueryGradeThread("2014-2015", "")).start();

        } else if (id == R.id.nav_share) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_page);
            layout.removeAllViews();
            RelativeLayout resultView = (RelativeLayout) getLayoutInflater().inflate(
                    R.layout.content_main, null).findViewById(R.id.main_content);
            layout.addView(resultView);

        } else if (id == R.id.nav_send) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_page);
            layout.removeAllViews();
            View view = getLayoutInflater().inflate(R.layout.activity_result_page, null);

            RelativeLayout resultView = (RelativeLayout) view.findViewById(R.id.result_view);
            WebView resultWebview = (WebView) view.findViewById(R.id.result_web_view);
            resultWebview.getSettings().setJavaScriptEnabled(true);
            resultWebview.loadUrl("file:///android_asset/result_page.html");
            layout.addView(resultView);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 用于处理消息
     */
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //载入验证码图片
            if (((String) msg.obj).equalsIgnoreCase("loginPageLoaded")) {
                Bundle bundle = msg.getData();
                ImageView iv = (ImageView) findViewById(R.id.code_img);
                byte[] imgRes = bundle.getByteArray("checkImg");
                if (imgRes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgRes, 0, imgRes.length);
                    iv.setImageBitmap(bitmap);
                }
            } else if (((String) msg.obj).equalsIgnoreCase("tryLoginEnd")) {
                Button btn = (Button) findViewById(R.id.btn_login);
                btn.setClickable(true);

                Bundle bundle = msg.getData();
                btn.setText(bundle.getString("canLogin"));

            } else if (((String) msg.obj).equalsIgnoreCase("queryGradeFinished")) {
//                Bundle bundle = msg.getData();
//                for(String key:bundle.keySet()){
//
//                }
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_page);
                layout.removeAllViews();
                View view = getLayoutInflater().inflate(R.layout.activity_result_page, null);

                RelativeLayout resultView = (RelativeLayout) view.findViewById(R.id.result_view);
                WebView resultWebview = (WebView) view.findViewById(R.id.result_web_view);

                resultJson = msg.getData().getString("json");
                resultWebview.setWebChromeClient(new MyWebChromeClient());
                resultWebview.getSettings().setJavaScriptEnabled(true);
                resultWebview.addJavascriptInterface(new JsObject(), "jsObject");

                resultWebview.loadUrl("file:///android_asset/result_page.html");
                //resultWebview.loadUrl("javascript:setData("+resultJson+")");
                layout.addView(resultView);
            }
        }
    };

    public class JsObject {
        @JavascriptInterface
        public String getGradeJson() {
            return "'"+resultJson+"'";
        }
    }


    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d("NNNNNN", message);
            result.confirm();
            return true;
        }
    }
}
