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

import com.localhost.lin.simploc.R;
import com.localhost.lin.simploc.Utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CETFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CETFragment extends Fragment {
    private static final String ARG_GRADE_DATA = "param1";

    private String mGradeData;


    public CETFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param gradeData Parameter 1.
     * @return A new instance of fragment CETFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CETFragment newInstance(String gradeData) {
        CETFragment fragment = new CETFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GRADE_DATA, gradeData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGradeData = getArguments().getString(ARG_GRADE_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cet, container, false);
        ListView cetList = (ListView)rootView.findViewById(R.id.cet_info_list);
        ArrayList<ArrayList<String>> rawData = JsonUtils.convJson2StringLists(mGradeData);
        final ArrayList<Map<String,String>> listData = new ArrayList<Map<String, String>>();//List表格数据

        for(ArrayList<String> itemData:rawData){
            Map<String,String> map = new HashMap<>();
            for (String s: itemData){
                map.put("item" + String.valueOf(itemData.indexOf(s) + 1), s);
            }
            listData.add(map);
        }
        SimpleAdapter cetAdapter = new SimpleAdapter(getActivity(),listData,R.layout.cet_list_item,
                new String[]{"item2","item3","item4","item7"},
                new int[]{R.id.xn_cet,R.id.xq_cet,R.id.name_cet,R.id.grade_cet});
        cetList.setAdapter(cetAdapter);
        cetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View v = getActivity().getLayoutInflater()
                        .inflate(R.layout.cet_list_detail_item,null);
                Map map = listData.get(position);
                ((TextView)v.findViewById(R.id.cet_list_detail_xn)).setText("学年：\t\t\t\t"+map.get("item2"));
                ((TextView)v.findViewById(R.id.cet_list_detail_xq)).setText("学期：\t\t\t\t"+map.get("item3"));
                ((TextView)v.findViewById(R.id.cet_list_detail_date)).setText("考试时间：\t\t"+map.get("item6"));
                ((TextView)v.findViewById(R.id.cet_list_detail_name)).setText("考试名称：\t\t"+map.get("item4"));
                ((TextView)v.findViewById(R.id.cet_list_detail_number)).setText("准考证号：\t\t"+map.get("item5"));
                ((TextView)v.findViewById(R.id.cet_list_detail_grade)).setText("成绩：\t\t\t\t"+map.get("item7"));
                new AlertDialog.Builder(getActivity()).setTitle("等级考试详情").setView(v).show();
            }
        });
        return rootView;
    }

}
