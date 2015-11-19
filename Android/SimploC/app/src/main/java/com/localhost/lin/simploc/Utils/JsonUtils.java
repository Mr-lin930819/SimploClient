package com.localhost.lin.simploc.Utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

    public static ArrayList<String> convJson2List(String json,String node){
        ArrayList<String> data = new ArrayList<String>();
        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        try{
            jsonObject = new JSONObject(json);
            if(node != null)
                jsonArray = jsonObject.getJSONArray(node);
            else
                jsonArray = new JSONArray(json);
            for(int i=0;i<jsonArray.length();i++){
                data.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            return null;
        }
        return data;
    }

    public static String getNodeString(String json,String node){
        String retData = null;
        JSONObject jsonObject = null;
        try{
            jsonObject = new JSONObject(json);
            if(node != null)
                retData = jsonObject.getString(node);
        } catch (JSONException e) {
            return null;
        }
        return retData;
    }

    public static int numOfNode(String json){
        JSONObject jsonObject = null;
        try{
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            return 0;
        }
        return jsonObject==null?0:jsonObject.length();
    }
}
