package com.localhost.lin.simploc.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import com.localhost.lin.simploc.R
import com.localhost.lin.simploc.Utils.JsonUtils
import java.util.*

/**
 * Created by Lin on 2015/12/7.
 */
class GradeListTab : android.support.v4.app.Fragment() {

    private var gradeList: ListView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_tab_list, container, false)
        gradeList = view.findViewById(R.id.grade_listview) as ListView
        val data = ArrayList<Map<String, String>>()
        try {
            for ((key, value) in JsonUtils.convJson2Map(arguments.getString("jsonResult"), "GRADE")!!) {
                val map = HashMap<String, String>()
                map.put("item", key)
                map.put("value", value)
                data.add(map)
            }
        } catch (e: NullPointerException) {    //空指针则为获取成绩数据出错,返回空盘
            val adapter = SimpleAdapter(view.context, data, R.layout.grade_list_item,
                    arrayOf("item", "value"), intArrayOf(R.id.grade_item, R.id.grade_value))
            gradeList!!.adapter = adapter
            return view
        }

        val adapter = SimpleAdapter(view.context, data, R.layout.grade_list_item,
                arrayOf("item", "value"), intArrayOf(R.id.grade_item, R.id.grade_value))
        gradeList!!.adapter = adapter
        return view
    }
}
