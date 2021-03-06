package com.localhost.lin.simploc.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.localhost.lin.simploc.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GPAFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GPAFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GPAFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public GPAFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GPAFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GPAFragment newInstance(String param1, String param2) {
        GPAFragment fragment = new GPAFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gpa, container, false);
        TextView gpaTotal = (TextView)view.findViewById(R.id.tv_gpa_total);
        TextView creditTotal = (TextView)view.findViewById(R.id.tv_credit_total);
        ArrayList<String> displayData = new ArrayList<>();
        StringBuffer displayString = new StringBuffer();
        try {
            JSONObject gpa = new JSONObject(mParam1),
                    credit = new JSONObject(mParam2);
            displayData.add(gpa.getString("students"));
            displayData.add("\n");
            displayData.add(gpa.getString("totalGPA"));
            displayData.add(gpa.getString("averageGPA"));
            displayData.add("");
            displayData.add("所选学分：" + credit.getString("select"));
            displayData.add("获得学分：" + credit.getString("get"));
            displayData.add("重修学分：" + credit.getString("revamp"));
            displayData.add("正考未通过学分：" + credit.getString("nopass"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (String s:displayData) {
            displayString.append(s);
            displayString.append("\n");
        }
        gpaTotal.setText(displayString.toString());
//        creditTotal.setText(mParam2);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
