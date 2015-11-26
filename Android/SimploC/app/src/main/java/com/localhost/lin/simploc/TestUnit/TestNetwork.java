package com.localhost.lin.simploc.TestUnit;

import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtilsHC4;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Lin on 2015/11/26.
 */
public class TestNetwork implements Runnable{
    String mUrl,mParam;

    public TestNetwork(String url,String parm){
        mUrl = url;
        mParam = parm;
    }

    public static void TestGetUrl(String param,String url){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGetHC4 httpGetHC4 = new HttpGetHC4(url + param);
        try {
            String s = EntityUtilsHC4.toString(client.execute(httpGetHC4).getEntity(),"gb2312");
            System.out.print(s);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void run() {
        TestGetUrl(mParam,mUrl);
    }
}
