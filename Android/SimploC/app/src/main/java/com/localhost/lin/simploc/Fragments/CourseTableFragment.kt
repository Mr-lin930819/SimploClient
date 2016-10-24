package com.localhost.lin.simploc.Fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.TextView
import com.localhost.lin.simploc.R
import com.localhost.lin.simploc.Utils.JsonUtils
import com.localhost.lin.simploc.customview.NoneScrollGridView
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * A simple [Fragment] subclass.
 * Use the [CourseTableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CourseTableFragment : Fragment() {

    private var mCourseData: String? = null
    private var mWeek: String? = null

    private var mNowWeekIndex = 0
    private val mTextData = ArrayList<Array<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mCourseData = arguments.getString(ARG_COURSE_DATA)
            mWeek = arguments.getString(ARG_WEEK)
        }
        val nowWeek = SimpleDateFormat("EEEE", Locale.CHINA).format(java.util.Date())
        for (i in 0..6) {
            if (nowWeek == weekString[i])
                mNowWeekIndex = i
        }
        Log.i("Fragment", "create!")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val retView = inflater!!.inflate(R.layout.fragment_course_table, container, false)
        val courseTable = retView.findViewById(R.id.lesson_table) as NoneScrollGridView
        val courseTableColumn = retView.findViewById(R.id.table_column) as NoneScrollGridView
        val courseTableRow = retView.findViewById(R.id.table_row) as NoneScrollGridView
        val simpleAdapters = getAdaptData(activity,
                mCourseData.toString(), Integer.valueOf(mWeek)!!)

        courseTableColumn.adapter = simpleAdapters[0]
        courseTableRow.adapter = simpleAdapters[1]
        courseTable.adapter = simpleAdapters[2]
        courseTable.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val showData = mTextData[position]
            if (showData.size == 0)
                return@OnItemClickListener
            val v = activity.layoutInflater.inflate(R.layout.course_table_detail, null)
            (v.findViewById(R.id.course_table_detail_name) as TextView).text = "课程：\t" + showData[1]
            (v.findViewById(R.id.course_table_detail_teacher) as TextView).text = "教师：\t" + showData[2]
            if (showData.size >= 4)
                (v.findViewById(R.id.course_table_detail_addr) as TextView).text = "教室：\t" + showData[3]
            AlertDialog.Builder(activity).setTitle("课程详情").setView(v).show()
        }
        Log.i("Fragment", "create view!")
        return retView
    }

    private fun getAdaptData(context: Context, jsonContent: String, week: Int): ArrayList<SimpleAdapter> {
        val adapters = ArrayList<SimpleAdapter>()
        /****************** 设置列表头  */
        val colmunData = ArrayList<Map<String, Any>>()
        for (i in 0..6) {
            val map = HashMap<String, Any>()
            map.put("item", weekString[i])
            if (i == mNowWeekIndex)
                map.put("icon", R.drawable.course_header_backicon)
            else
                map.put("icon", R.color.colorTransparent)
            colmunData.add(map)
        }
        val columnAdapter = SimpleAdapter(context, colmunData, R.layout.course_table_column_item,
                arrayOf("item", "icon"), intArrayOf(R.id.column_item, R.id.column_item_icon))
        adapters.add(columnAdapter)
        //设置行表头
        val rowData = ArrayList<Map<String, String>>()
        for (i in 0..5) {
            val map = HashMap<String, String>()
            map.put("item1", (i * 2 + 1).toString())
            map.put("item2", (i * 2 + 2).toString())
            rowData.add(map)
        }
        val rowAdapter = SimpleAdapter(context, rowData, R.layout.course_table_row_item,
                arrayOf("item1", "item2"), intArrayOf(R.id.row_item_top, R.id.row_item_bottom))
        adapters.add(rowAdapter)

        /********************** 设置表数据  */
        val tableData = ArrayList<Map<String, Any>>()
        val rawData = ArrayList<String>()
        val lessonNumber = arrayOf("第1节", "第3节", "第5节", "第7节", "第9节", "第11节")
        val maxlesson = JsonUtils.numOfNode(jsonContent)
        for (i in 0..maxlesson - 1) {
            rawData.addAll(JsonUtils.convJson2List(jsonContent, lessonNumber[i]))   //从json数据中获取节数相关的一周所有课程
        }

        print(rawData.toString())
        for (s in rawData) {
            val map = HashMap<String, Any>()
            //如果这个时间没课
            if (s == "") {
                map.put("item", "")
                map.put("back", R.color.colorTransparent)
                mTextData.add(arrayOf<String>())
            } else {     //有课则进行解析，格式为【 周数1;课程名1;教师1;教室1$周数2;课程名2;教师2;教室2$... ... 】,周数格式为"单/双周"或"n-m周".
                val ss: Array<String>
                var detailText = arrayOf<String>()
                //TODO 标记，此处split使用$符号做划分，但是$又为正则表达式元符号，所以需要转义
                ss = s.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (i in ss.indices) {
                    Log.d("Raw..", ss[i])
                }
                val showText = StringBuffer()
                val pattern = Pattern.compile("([0-9]{1,2})-([0-9]{1,2})周")
                for (item in ss) {
                    val conts = item.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    //                    Log.d("Content",conts[0]+"," +conts[1]+","+conts[2]+","+conts[3]);
                    var matcher = pattern.matcher(conts[0])
                    if (matcher.find()) {
                        val min: Int
                        val max: Int
                        min = Integer.parseInt(matcher.group(1))
                        max = Integer.parseInt(matcher.group(2))
                        //如果选择的周数在课程周数范围内（非单双周课程)
                        if (min <= week && week <= max) {
                            showText.append(conts[1] + "\n")
                            showText.append(conts[2] + "\n")
                            if (conts.size < 4)
                                showText.append("\n")
                            else
                                showText.append(conts[3] + "\n")
                            detailText = conts
                            break
                        } else {
                            continue
                        }
                    } else {
                        //单双周的情况
                        matcher = Pattern.compile("(.)周").matcher(conts[0])
                        if (matcher.find()) {
                            if (matcher.group(1) == "单" && week % 2 == 1 || matcher.group(1) == "双" && week % 2 == 0) {
                                showText.append(conts[1] + "\n")
                                showText.append(conts[2] + "\n")
                                showText.append(conts[3] + "\n")
                                detailText = conts
                                break
                            }
                        }
                    }
                }
                if (showText.toString() != "") {
                    //在表格中添加一个Item，设置Item的文字
                    Log.d("Adding...", "")
                    //map.put("back", R.color.colorPrimary);    //设置Item的背景
                    map.put("item", showText)
                    mTextData.add(detailText)
                    map.put("back", R.color.colorTableCell)
                } else {
                    map.put("item", "")
                    map.put("back", R.color.colorTransparent)
                    mTextData.add(arrayOf<String>())
                }
            }
            tableData.add(map)
        }
        val tableAdapter = SimpleAdapter(context, tableData, R.layout.course_table_item,
                arrayOf("item", "back"), intArrayOf(R.id.course_name, R.id.course_icon))
        adapters.add(tableAdapter)

        return adapters
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_COURSE_DATA = "param1"
        private val ARG_WEEK = "param2"
        private val weekString = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param courseData 课程表json数据.
         * *
         * @return A new instance of fragment CourseTableFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(courseData: String, week: String): CourseTableFragment {
            val fragment = CourseTableFragment()
            val args = Bundle()
            args.putString(ARG_COURSE_DATA, courseData)
            args.putString(ARG_WEEK, week)
            fragment.arguments = args
            Log.i("Fragment", "new!")
            return fragment
        }
    }

}// Required empty public constructor
