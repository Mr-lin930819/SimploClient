package com.localhost.lin.simploc.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.localhost.lin.simploc.R

/**
 * Created by Lin on 2015/12/7.
 */
class GradeChartTab : android.support.v4.app.Fragment() {
    private var resultWebview: WebView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragement_tab_chart, container, false)
        resultWebview = view.findViewById(R.id.result_web_view) as WebView
        resultWebview!!.setWebChromeClient(MyWebChromeClient())
        resultWebview!!.settings.javaScriptEnabled = true
        resultWebview!!.settings.setSupportZoom(true)
        //扩大比例的缩放
        resultWebview!!.settings.useWideViewPort = true
        resultWebview!!.addJavascriptInterface(JsObject(), "jsObject")
        resultWebview!!.loadUrl("file:///android_asset/result_page.html")
        return view
    }

    internal inner class MyWebChromeClient : WebChromeClient() {
        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
            Log.d("Web Console------>", message)
            result.confirm()
            return true
        }
    }

    /**
     * 用于与JS交互的接口对象
     */
    inner class JsObject {
        @JavascriptInterface
        fun getGradeJson() = arguments.getString("jsonResult")

        //        @JavascriptInterface
        //        public String getJsonTest(){
        //            String ret = "";
        ////            ret += "'";
        ////            ret += Test.jsMethodTest();
        ////            ret += "'";
        //            return ret;
        //        }
    }

    companion object {
        fun newInstance(index: Int): GradeChartTab {
            val f = GradeChartTab()
            val args = Bundle()
            args.putInt("index", index)
            f.arguments = args
            return f
        }
    }

}
