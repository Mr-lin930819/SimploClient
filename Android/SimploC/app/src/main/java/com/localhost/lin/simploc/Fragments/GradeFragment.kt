package com.localhost.lin.simploc.Fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.localhost.lin.simploc.R
import com.viewpagerindicator.TabPageIndicator
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [GradeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GradeFragment : Fragment() {

    private var mGradeJsonData: String? = null
    //采用ViewPager替代TabHost
    private val mFragments = ArrayList<Fragment>()
    internal var mPagerAdapter: FragmentStatePagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mGradeJsonData = arguments.getString(ARG_GRADE_DATA)
        }
        mPagerAdapter = object : FragmentStatePagerAdapter(activity.supportFragmentManager) {
            private val titleText = arrayOf("柱状图显示", "列表显示")
            override fun getItem(position: Int): Fragment {
                return mFragments[position]
            }

            override fun getCount(): Int {
                return mFragments.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return titleText[position % 2]
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_grade, container, false)
        val gradeChartTab = GradeChartTab.newInstance(0)
        val gradeListTab = GradeListTab()
        val viewPager = rootView.findViewById(R.id.grade_pager) as ViewPager
        val tabPageIndicator = rootView.findViewById(R.id.tab_indicator) as TabPageIndicator
        val bundle = Bundle()
        bundle.putString("jsonResult", mGradeJsonData)
        gradeChartTab.arguments = bundle
        gradeListTab.arguments = bundle
        mFragments.clear()
        mFragments.add(gradeChartTab)
        mFragments.add(gradeListTab)
        viewPager.adapter = mPagerAdapter
        //        tabPageIndicator.setVisibility(View.VISIBLE);
        tabPageIndicator.setViewPager(viewPager, 0)
        return rootView
    }

    companion object {

        private val ARG_GRADE_DATA = "param1"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param gradeJsonData Parameter 1.
         * *
         * @return A new instance of fragment GradeFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(gradeJsonData: String): GradeFragment {
            val fragment = GradeFragment()
            val args = Bundle()
            args.putString(ARG_GRADE_DATA, gradeJsonData)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
