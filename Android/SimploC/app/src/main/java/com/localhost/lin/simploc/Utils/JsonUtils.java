package com.localhost.lin.simploc.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Lin on 2015/11/13.
 */
public class JsonUtils {
    public static HashMap<String,String> convJson2Map(String json,String node){
        HashMap<String,String> data = new HashMap<String,String>();

        JSONObject jsonObj = null,jsonArr = null;
        try {
            jsonObj = new JSONObject(json);
            if(node != null)
                jsonArr = jsonObj.getJSONObject(node);
            else
                jsonArr = jsonObj;
            Iterator<String> iterator = jsonArr.keys();
            while (iterator.hasNext()){
                String key = iterator.next();
                data.put(key,jsonArr.getString(key));
            }
        } catch (JSONException e) {
            return null;
//            e.printStackTrace();
        }
        return data;
    }
}
