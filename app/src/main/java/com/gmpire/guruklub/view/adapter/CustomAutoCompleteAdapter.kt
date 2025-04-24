package com.gmpire.guruklub.view.adapter

import android.R
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.util.IObserverCallBack
import java.util.ArrayList

class CustomAutoCompleteAdapter(
    private val mContext: Context,
    private val itemLayout: Int,
    private var dataList: MutableList<String>,
    private var dataManager: DataManager
) : ArrayAdapter<String>(mContext, itemLayout, dataList) {
    private var resourceId: Int = 0
    private val stringFilter = CustomFilter()
    private var items: List<String>? = null
    private var tempItems: ArrayList<String>? = null
    private var suggestions: ArrayList<String>? = null
    private lateinit var callBack: IObserverCallBack

    init {
        this.items = dataList
        this.resourceId = resourceId;
        tempItems = ArrayList<String>(dataList)
        suggestions = arrayListOf()
        try {
            callBack = mContext as IObserverCallBack
        } catch (e: Exception) {
            print(e.toString() + "Implement IObserverCallback")
        }
    }

    override fun getCount(): Int {
        return items?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): String? {
        return items?.get(position)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view

        if (view == null) {
            view = LayoutInflater.from(parent.context)
                .inflate(itemLayout, parent, false)
        }
        val strName = view?.findViewById<View>(R.id.text1) as TextView
        strName.setTextColor(Color.RED)
        strName.text = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            Html.fromHtml(getItem(position), Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(getItem(position))
        }
        return view
    }

    override fun getFilter(): Filter {
        return stringFilter
    }


    inner class CustomFilter : Filter() {
        override fun convertResultToString(resultValue: Any): CharSequence {
            return resultValue as String
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return if (constraint != null) {
                val filterResults = FilterResults()
                // synchronous api call
                val dataBody =
                    dataManager.apiHelper.apiService.getQuestionByKeyword(constraint.toString())
                        ?.execute()?.body()
                dataBody.let {
                    val questionList = it?.data?.data
                    Log.d("QuesSize", questionList?.size.toString())
                    if (!questionList.isNullOrEmpty()) {
                        var questionTitleList = mutableListOf<String>()
                        questionList.forEach {
                            if (!it.title.isNullOrEmpty()) {
                                questionTitleList.add(it.title)
                            }
                        }
                        suggestions?.clear()
                        suggestions?.addAll(questionTitleList)
                        filterResults.values = questionList;
                        filterResults.count = questionList.size
                    } else {
                        return FilterResults()
                    }
                }
                filterResults
            } else {
                FilterResults()
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null && results.count > 0) {
                clear()
                addAll(suggestions?.toList() ?: arrayListOf())
                notifyDataSetChanged()
            } else {
                clear()
                notifyDataSetChanged()
            }
        }

    }
}




