package com.localhost.lin.simploc.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.localhost.lin.simploc.R;
import com.localhost.lin.simploc.Utils.JsonUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExamTimeTableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExamTimeTableFragment extends Fragment {
    private static final String ARG_EXAM_TIME_DATA = "param1";

    private String mTimeTableData;

    public ExamTimeTableFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param timeTableData Parameter 1.
     * @return A new instance of fragment ExamTimeTableFragment.
     */
    public static ExamTimeTableFragment newInstance(String timeTableData) {
        ExamTimeTableFragment fragment = new ExamTimeTableFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EXAM_TIME_DATA, timeTableData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTimeTableData = getArguments().getString(ARG_EXAM_TIME_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exam_time_table, container, false);
        ListView examList = (ListView)rootView.findViewById(R.id.main_info_list);
        ArrayList<ArrayList<String>> rawData;
        try {
            rawData = JsonUtils.convJson2StringLists(mTimeTableData);
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), "接收数据有误", Toast.LENGTH_SHORT).show();
            return rootView;
        }

        final ArrayList<Map<String,String>> listData = new ArrayList<Map<String, String>>();//List表格数据
        for(ArrayList<String> itemData:rawData){
            Map<String,String> map = new HashMap<>();
            for (String s: itemData){
                map.put("item" + String.valueOf(itemData.indexOf(s) + 1), s);
            }
            listData.add(map);
        }
        SimpleAdapter examAdapter = new SimpleAdapter(getActivity(),listData,R.layout.exam_list_item,
                new String[]{"item1","item2"},
                new int[]{R.id.exam_name,R.id.exam_time});
        examList.setAdapter(examAdapter);
        examList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,String> map = listData.get(position);
                View v = getActivity().getLayoutInflater()
                        .inflate(R.layout.exam_list_detail_item,null);
                ((TextView)v.findViewById(R.id.exam_list_detail_name)).setText("考试名称：" + map.get("item1"));
                ((TextView)v.findViewById(R.id.exam_list_detail_time)).setText("考试时间：" + map.get("item2"));
                ((TextView)v.findViewById(R.id.exam_list_detail_addr)).setText("考试教室：" + map.get("item3"));
                ((TextView)v.findViewById(R.id.exam_list_detail_site)).setText("考试座位：" + map.get("item4"));
                ((TextView)v.findViewById(R.id.exam_list_detail_zone)).setText("考试校区：" + map.get("item5"));
                new AlertDialog.Builder(getActivity()).setTitle("考试信息详情").setView(v).show();
            }
        });
        return rootView;
    }

}
