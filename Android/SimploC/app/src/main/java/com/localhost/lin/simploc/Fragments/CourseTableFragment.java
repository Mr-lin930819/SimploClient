package com.localhost.lin.simploc.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.localhost.lin.simploc.R;
import com.localhost.lin.simploc.Utils.JsonUtils;
import com.localhost.lin.simploc.customview.NoneScrollGridView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseTableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseTableFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_COURSE_DATA = "param1";
    private static final String ARG_WEEK = "param2";

    private String mCourseData;
    private String mWeek;

    private int mNowWeekIndex = 0;
    private static String[] weekString = new String[]{"星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
    private final ArrayList<String[]> mTextData = new ArrayList<String[]>();

    public CourseTableFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param courseData 课程表json数据.
     * @return A new instance of fragment CourseTableFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CourseTableFragment newInstance(String courseData, String week) {
        CourseTableFragment fragment = new CourseTableFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COURSE_DATA, courseData);
        args.putString(ARG_WEEK, week);
        fragment.setArguments(args);
        Log.i("Fragment", "new!");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCourseData = getArguments().getString(ARG_COURSE_DATA);
            mWeek = getArguments().getString(ARG_WEEK);
        }
        String nowWeek = new SimpleDateFormat("EEEE", Locale.CHINA).format(new java.util.Date());
        for(int i=0; i<7; i++){
            if(nowWeek.equals(weekString[i]))
                mNowWeekIndex = i;
        }
        Log.i("Fragment", "create!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retView = inflater.inflate(R.layout.fragment_course_table, container,false);
        NoneScrollGridView courseTable = (NoneScrollGridView)retView.findViewById(R.id.lesson_table),
                courseTableColumn = (NoneScrollGridView)retView.findViewById(R.id.table_column),
                courseTableRow = (NoneScrollGridView)retView.findViewById(R.id.table_row);
        ArrayList<SimpleAdapter> simpleAdapters = getAdaptData(getActivity(),
                mCourseData, Integer.valueOf(mWeek));

        courseTableColumn.setAdapter(simpleAdapters.get(0));
        courseTableRow.setAdapter(simpleAdapters.get(1));
        courseTable.setAdapter(simpleAdapters.get(2));
        courseTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] showData = mTextData.get(position);
                if(showData.length == 0)
                    return;
                View v = getActivity().getLayoutInflater().
                        inflate(R.layout.course_table_detail,null);
                ((TextView)v.findViewById(R.id.course_table_detail_name)).setText("课程：\t" + showData[1]);
                ((TextView)v.findViewById(R.id.course_table_detail_teacher)).setText("教师：\t" + showData[2]);
                if(showData.length >= 4)
                    ((TextView)v.findViewById(R.id.course_table_detail_addr)).setText("教室：\t" + showData[3]);
                new AlertDialog.Builder(getActivity()).setTitle("课程详情").setView(v).show();
            }
        });
        Log.i("Fragment", "create view!");
        return retView;
    }

    private ArrayList<SimpleAdapter> getAdaptData(Context context, String jsonContent, int week) {
        ArrayList<SimpleAdapter> adapters = new ArrayList<>();
        /****************** 设置列表头 ********************/
        ArrayList<Map<String,Object>> colmunData = new ArrayList<Map<String, Object>>();
        for(int i = 0 ;i < 7;i++){
            Map<String,Object> map  = new HashMap<String,Object>();
            map.put("item", weekString[i]);
            if( i == mNowWeekIndex )
                map.put("icon",R.drawable.course_header_backicon);
            else
                map.put("icon",R.color.colorTransparent);
            colmunData.add(map);
        }
        SimpleAdapter columnAdapter = new SimpleAdapter(context, colmunData, R.layout.course_table_column_item,
                new String[]{"item","icon"}, new int[]{R.id.column_item,R.id.column_item_icon});
        adapters.add(columnAdapter);
        //设置行表头
        ArrayList<Map<String,String>> rowData = new ArrayList<Map<String, String>>();
        for(int i = 0 ;i < 6;i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("item1", String.valueOf(i*2 + 1));
            map.put("item2", String.valueOf(i*2 + 2));
            rowData.add(map);
        }
        SimpleAdapter rowAdapter = new SimpleAdapter(context, rowData, R.layout.course_table_row_item,
                new String[]{"item1","item2"}, new int[]{R.id.row_item_top,R.id.row_item_bottom});
        adapters.add(rowAdapter);

        /********************** 设置表数据 ******************/
        ArrayList<Map<String,Object>> tableData = new ArrayList<Map<String, Object>>();
        ArrayList<String> rawData = new ArrayList<String>();
        String[] lessonNumber = new String[]{"第1节","第3节","第5节","第7节","第9节","第11节"};
        int maxlesson = JsonUtils.numOfNode(jsonContent);
        for(int i =0;i<maxlesson;i++){
            rawData.addAll(JsonUtils.convJson2List(jsonContent, lessonNumber[i]));   //从json数据中获取节数相关的一周所有课程
        }

        System.out.print(rawData.toString());
        for (String s:rawData){
            Map<String,Object> map = new HashMap<String,Object>();
            //如果这个时间没课
            if(s.equals("")){
                map.put("item","");
                map.put("back",R.color.colorTransparent);
                mTextData.add(new String[]{});
            }else {     //有课则进行解析，格式为【 周数1;课程名1;教师1;教室1$周数2;课程名2;教师2;教室2$... ... 】,周数格式为"单/双周"或"n-m周".
                String[] ss,detailText = new String[]{};
                //TODO 标记，此处split使用$符号做划分，但是$又为正则表达式元符号，所以需要转义
                ss = s.split("\\$");
                for(int i = 0; i < ss.length; i++) {
                    Log.d("Raw..",ss[i]);
                }
                StringBuffer showText = new StringBuffer();
                Pattern pattern = Pattern.compile("([0-9]{1,2})-([0-9]{1,2})周");
                for(String item:ss){
                    String []conts = item.split(";");
//                    Log.d("Content",conts[0]+"," +conts[1]+","+conts[2]+","+conts[3]);
                    Matcher matcher = pattern.matcher(conts[0]);
                    if(matcher.find()){
                        int min, max;
                        min = Integer.parseInt(matcher.group(1));
                        max = Integer.parseInt(matcher.group(2));
                        //如果选择的周数在课程周数范围内（非单双周课程)
                        if(min <= week && week <=max){
                            showText.append(conts[1] + "\n");
                            showText.append(conts[2] + "\n");
                            if(conts.length < 4)
                                showText.append("\n");
                            else
                                showText.append(conts[3] + "\n");
                            detailText = conts;
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        //单双周的情况
                        matcher = Pattern.compile("(.)周").matcher(conts[0]);
                        if(matcher.find()) {
                            if( (matcher.group(1).equals("单") && week % 2 == 1) ||
                                    (matcher.group(1).equals("双") && week % 2 == 0)) {
                                showText.append(conts[1] + "\n");
                                showText.append(conts[2] + "\n");
                                showText.append(conts[3] + "\n");
                                detailText = conts;
                                break;
                            }
                        }
                    }
                }
                if(!showText.toString().equals("")) {
                    //在表格中添加一个Item，设置Item的文字
                    Log.d("Adding...","");
                    //map.put("back", R.color.colorPrimary);    //设置Item的背景
                    map.put("item", showText);
                    mTextData.add(detailText);
                    map.put("back", R.color.colorTableCell);
                }else{
                    map.put("item","");
                    map.put("back",R.color.colorTransparent);
                    mTextData.add(new String[]{});
                }
            }
            tableData.add(map);
        }
        SimpleAdapter tableAdapter = new SimpleAdapter(context,tableData,R.layout.course_table_item,
                new String[]{"item","back"},new int[]{R.id.course_name,R.id.course_icon});
        adapters.add(tableAdapter);

        return adapters;
    }

}
