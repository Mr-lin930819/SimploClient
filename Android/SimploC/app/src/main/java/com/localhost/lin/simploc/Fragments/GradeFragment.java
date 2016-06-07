package com.localhost.lin.simploc.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.localhost.lin.simploc.R;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GradeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GradeFragment extends Fragment {

    private static final String ARG_GRADE_DATA = "param1";

    private String mGradeJsonData;
    //采用ViewPager替代TabHost
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    FragmentStatePagerAdapter mPagerAdapter;

    public GradeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param gradeJsonData Parameter 1.
     * @return A new instance of fragment GradeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GradeFragment newInstance(String gradeJsonData) {
        GradeFragment fragment = new GradeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GRADE_DATA, gradeJsonData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGradeJsonData = getArguments().getString(ARG_GRADE_DATA);
        }
        mPagerAdapter = new FragmentStatePagerAdapter(getActivity().getSupportFragmentManager()) {
            final private String[] titleText = new String[]{"柱状图显示","列表显示"};
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titleText[position % 2];
            }

        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_grade, container, false);
        GradeChartTab gradeChartTab =GradeChartTab.newInstance(0);
        GradeListTab gradeListTab = new GradeListTab();
        ViewPager viewPager = (ViewPager)rootView.findViewById(R.id.grade_pager);
        TabPageIndicator tabPageIndicator = (TabPageIndicator)rootView
                .findViewById(R.id.tab_indicator);
        Bundle bundle = new Bundle();
        bundle.putString("jsonResult", mGradeJsonData);
        gradeChartTab.setArguments(bundle);
        gradeListTab.setArguments(bundle);
        mFragments.clear();
        mFragments.add(gradeChartTab);
        mFragments.add(gradeListTab);
        viewPager.setAdapter(mPagerAdapter);
//        tabPageIndicator.setVisibility(View.VISIBLE);
        tabPageIndicator.setViewPager(viewPager, 0);
        return rootView;
    }

}
