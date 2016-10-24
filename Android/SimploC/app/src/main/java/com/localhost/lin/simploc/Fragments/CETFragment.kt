package com.localhost.lin.simploc.Fragments


import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import com.localhost.lin.simploc.R
import com.localhost.lin.simploc.Utils.JsonUtils
import kotlinx.android.synthetic.main.cet_list_detail_item.view.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CETFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CETFragment : Fragment() {

    private var mGradeData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mGradeData = arguments.getString(ARG_GRADE_DATA)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_cet, container, false)
        val cetList = rootView.findViewById(R.id.cet_info_list) as ListView
        val rawData = JsonUtils.convJson2StringLists(mGradeData)
        val listData = ArrayList<Map<String, String>>()//List表格数据

        for (itemData in rawData!!) {
            val map = HashMap<String, String>()
            for (s in itemData) {
                map.put("item" + (itemData.indexOf(s) + 1).toString(), s)
            }
            listData.add(map)
        }
        val cetAdapter = SimpleAdapter(activity, listData, R.layout.cet_list_item,
                arrayOf("item2", "item3", "item4", "item7"),
                intArrayOf(R.id.xn_cet, R.id.xq_cet, R.id.name_cet, R.id.grade_cet))
        cetList.adapter = cetAdapter
        cetList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val v = activity.layoutInflater.inflate(R.layout.cet_list_detail_item, null)
            val map = listData[position]

            v.cet_list_detail_xn.text = "学年：\t\t\t\t" + map["item2"]
            v.cet_list_detail_xq.text = "学期：\t\t\t\t" + map["item3"]
            v.cet_list_detail_date.text = "考试时间：\t\t" + map["item6"]
            v.cet_list_detail_name.text = "考试名称：\t\t" + map["item4"]
            v.cet_list_detail_number.text = "准考证号：\t\t" + map["item5"]
            v.cet_list_detail_grade.text = "成绩：\t\t\t\t" + map["item7"]
            AlertDialog.Builder(activity).setTitle("等级考试详情").setView(v).show()
        }
        return rootView
    }

    companion object {
        private val ARG_GRADE_DATA = "param1"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param gradeData Parameter 1.
         * *
         * @return A new instance of fragment CETFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(gradeData: String): CETFragment {
            val fragment = CETFragment()
            val args = Bundle()
            args.putString(ARG_GRADE_DATA, gradeData)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
