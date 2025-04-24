package com.gmpire.guruklub.view.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.Dropdown

class CustomSpinnerAdapter(context: Context, dropdown: Dropdown) {

    var mContext = context
    var mDropdown = dropdown
    var mPosition: Int = -1
    lateinit var spinnerAdapter: ArrayAdapter<String>


    fun getFragmentAdapter() :ArrayAdapter<String>{

        spinnerAdapter = object : ArrayAdapter<String>(
            mContext, R.layout.spinner_item, mDropdown.items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                tv.setPadding(18, 0, 0, 0)
                tv.setTextSize(13f)
                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(mContext, R.color.newwhite))
                } else {
                    tv.setTextColor(ContextCompat.getColor(mContext, R.color.newwhite))
                }
                return view
            }

            override fun getDropDownView(
                position: Int, convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                setItemBackground(position, tv)
                return view
            }
        }
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        return spinnerAdapter
    }


    fun getAdapter(isBlur:Boolean = false): ArrayAdapter<String> {
        spinnerAdapter = object : ArrayAdapter<String>(
            mContext, R.layout.spinner_item, mDropdown.items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                tv.setPadding(30, 0, 0, 0)
                tv.setTextSize(13f)
                if (position == 0) {
                    if(isBlur){
                        tv.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                    }
                    else {
                        tv.setTextColor(ContextCompat.getColor(mContext, R.color.text_color_hint))
                    }
                } else {
                    tv.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                }
                return view
            }

            override fun getDropDownView(
                position: Int, convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                setItemBackground(position, tv)
                return view
            }
        }
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        return spinnerAdapter
    }

    fun setDropDown(dropdown: Dropdown) {
        mDropdown = dropdown
        spinnerAdapter.clear()
        spinnerAdapter.addAll(mDropdown.items)
        spinnerAdapter.notifyDataSetChanged()
    }


    fun getSelectedItem(): String {
        return mDropdown.items.get(mPosition)
    }

    fun getSelectedId(): String {
        if (mPosition != -1) {
            return mDropdown.id[mPosition]
        } else {
            return "0"
        }
    }


    fun setPosition(position: Int) {
        mPosition = position
    }

    fun getPositionById(id: String): Int {
        for (item: String in mDropdown.id) {
            if (item == id) {
                return mDropdown.id.indexOf(id.toString())
            }
        }
        return 0
    }

    fun getPositionByItem(filterItem: String): Int {
        for (item: String in mDropdown.items) {
            if (item.equals(filterItem)) {
                return mDropdown.items.indexOf(filterItem)

            }
        }

        return 0
    }


    fun setItemBackground(position: Int, tv: TextView) {
        if (position == 0) {
            tv.setBackgroundColor(ContextCompat.getColor(mContext, R.color.semi_transparent));
        } else if (position % 2 == 1) {
            // Set the items background color
            tv.setBackgroundColor(ContextCompat.getColor(mContext, R.color.super_light_grey));
        } else {
            // Set the alternate items background color
            tv.setBackgroundColor(ContextCompat.getColor(mContext, R.color.super_light_grey));
        }
    }


}
