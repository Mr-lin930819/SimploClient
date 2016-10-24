package com.localhost.lin.simploc.Fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import com.localhost.lin.simploc.R
import com.localhost.lin.simploc.Utils.JsonUtils
import kotlinx.android.synthetic.main.exam_list_detail_item.view.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [ExamTimeTableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExamTimeTableFragment : Fragment() {

    private var mTimeTableData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mTimeTableData = arguments.getString(ARG_EXAM_TIME_DATA)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_exam_time_table, container, false)
        val examList = rootView.findViewById(R.id.main_info_list) as ListView
        val rawData: ArrayList<ArrayList<String>>
        try {
            rawData = JsonUtils.convJson2StringLists(mTimeTableData)
        } catch (e: NullPointerException) {
            Toast.makeText(activity, "接收数据有误", Toast.LENGTH_SHORT).show()
            return rootView
        }

        val listData = ArrayList<Map<String, String>>()//List表格数据
        for (itemData in rawData) {
            val map = HashMap<String, String>()
            for (s in itemData) {
                map.put("item" + (itemData.indexOf(s) + 1).toString(), s)
            }
            listData.add(map)
        }
        val examAdapter = SimpleAdapter(activity, listData, R.layout.exam_list_item,
                arrayOf("item1", "item2"),
                intArrayOf(R.id.exam_name, R.id.exam_time))
        examList.adapter = examAdapter
        examList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val map = listData[position]
            val v = activity.layoutInflater.inflate(R.layout.exam_list_detail_item, null)
            v.exam_list_detail_name.text = "考试名称：" + map["item1"]
            v.exam_list_detail_time.text = "考试时间：" + map["item2"]
            v.exam_list_detail_addr.text = "考试教室：" + map["item3"]
            v.exam_list_detail_site.text = "考试座位：" + map["item4"]
            v.exam_list_detail_zone.text = "考试校区：" + map["item5"]
            AlertDialog.Builder(activity).setTitle("考试信息详情").setView(v).show()
        }
        return rootView
    }

    companion object {
        private val ARG_EXAM_TIME_DATA = "param1"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param timeTableData Parameter 1.
         * *
         * @return A new instance of fragment ExamTimeTableFragment.
         */
        fun newInstance(timeTableData: String): ExamTimeTableFragment {
            val fragment = ExamTimeTableFragment()
            val args = Bundle()
            args.putString(ARG_EXAM_TIME_DATA, timeTableData)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
