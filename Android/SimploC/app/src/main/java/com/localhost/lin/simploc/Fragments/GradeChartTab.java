package com.localhost.lin.simploc.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.localhost.lin.simploc.R;

/**
 * Created by Lin on 2015/12/7.
 */
public class GradeChartTab extends android.support.v4.app.Fragment{
    @Nullable
    private WebView resultWebview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragement_tab_chart,container,false);
        resultWebview = (WebView)view.findViewById(R.id.result_web_view);
        resultWebview.setWebChromeClient(new MyWebChromeClient());
        resultWebview.getSettings().setJavaScriptEnabled(true);
        resultWebview.getSettings().setSupportZoom(true);
        //扩大比例的缩放
        resultWebview.getSettings().setUseWideViewPort(true);
        resultWebview.addJavascriptInterface(new JsObject(), "jsObject");
        resultWebview.loadUrl("file:///android_asset/result_page.html");
        return view;
    }

    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d("Web Console------>", message);
            result.confirm();
            return true;
        }
    }

    /**
     * 用于与JS交互的接口对象
     */
    public class JsObject {
        @JavascriptInterface
        public String getGradeJson() {
            return getArguments().getString("jsonResult");
        }

//        @JavascriptInterface
//        public String getJsonTest(){
//            String ret = "";
////            ret += "'";
////            ret += Test.jsMethodTest();
////            ret += "'";
//            return ret;
//        }
    }

}
