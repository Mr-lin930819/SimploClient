package com.localhost.lin.simploc.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.localhost.lin.simploc.R;
import com.localhost.lin.simploc.Utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lin on 2015/12/7.
 */
public class GradeListTab extends android.support.v4.app.Fragment {

    private ListView gradeList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tab_list,container,false);
        gradeList = (ListView)view.findViewById(R.id.grade_listview);
        ArrayList<Map<String,String>> data = new ArrayList<Map<String, String>>();
        for(Map.Entry<String,String> item: JsonUtils.convJson2Map(getArguments().getString("jsonResult"), "GRADE").entrySet()){
            Map<String,String> map  = new HashMap<String,String>();
            map.put("item",item.getKey());
            map.put("value",item.getValue());
            data.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(view.getContext(),data, R.layout.grade_list_item,
                new String[]{"item","value"},new int[]{R.id.grade_item,R.id.grade_value});
        gradeList.setAdapter(adapter);
        return view;
    }
}
