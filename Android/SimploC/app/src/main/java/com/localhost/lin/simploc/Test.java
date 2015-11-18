package com.localhost.lin.simploc;

import android.util.Log;

import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtilsHC4;

import java.io.IOException;

/**
 * Created by mrlin on 2015/11/6.
 */
public class Test {
    static public String jsMethodTest(){

        String result = null;
        CloseableHttpClient client = HttpClients.createDefault();

        try {
            result = EntityUtilsHC4.toString(client.execute(
                    new HttpGetHC4("http://192.168.1.102:8080/SimploServer/JsMixTestServlet"))
                    .getEntity());

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("_____________________",result);
        return result;
    }
}
