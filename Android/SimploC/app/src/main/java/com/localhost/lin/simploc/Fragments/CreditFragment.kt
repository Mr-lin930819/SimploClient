package com.localhost.lin.simploc.Fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import com.localhost.lin.simploc.R
import org.json.JSONArray
import org.json.JSONException
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CreditFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CreditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreditFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam = arguments.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_credit2, container, false)
        val listView = view.findViewById(R.id.lv_credit) as ListView
        val creditData = getJsonData(mParam.toString())
        val adapter = SimpleAdapter(view.context, creditData,
                R.layout.list_item_credit, mColumns,
                intArrayOf(R.id.credit_name, R.id.credit_need, R.id.credit_get, R.id.credit_onpass, R.id.credit_rest))
        listView.adapter = adapter
        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context as OnFragmentInteractionListener?
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun getJsonData(data: String): List<HashMap<String, String>> {
        val result = ArrayList<HashMap<String, String>>()
        try {
            val credits = JSONArray(data)
            for (i in 0..credits.length() - 1) {
                val jsonItem = credits.getJSONObject(i)
                val item = HashMap<String, String>()
                val keys = jsonItem.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    item.put(key, jsonItem.getString(key))
                }
                result.add(item)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return result
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param"
        private val mColumns = arrayOf("class", "need", "get", "nopass", "rest")

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param param Parameter 1.
         * *
         * @return A new instance of fragment CreditFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param: String): CreditFragment {
            val fragment = CreditFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
