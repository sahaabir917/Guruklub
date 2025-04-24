package com.gmpire.guruklub.view.base

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gmpire.guruklub.MyApp
import com.gmpire.guruklub.data.prefence.PreferencesHelper
import com.gmpire.guruklub.util.ConstantField
import com.gmpire.guruklub.view.adapter.IAdapterListener
import java.lang.Exception


const val DATA_VIEW_TYPE = 0
const val NATIVE_EXPRESS_AD_VIEW_TYPE = 1

class BaseRecyclerAdapter<T>(
    mContext: Context?,
    mListener: IAdapterListener,
    datas: ArrayList<T>
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    var context: Context? = mContext
    var listener: IAdapterListener = mListener
    var datas = datas
    var isAdsFree = PreferencesHelper(MyApp.getInstance()).prefGetIsAdFree() ?:false

    var itemCountBeforeAd = ConstantField.NATIVE_AD_INTERVAL


    lateinit var holder: BaseViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return listener.getViewHolder(parent, viewType)
    }

    override fun getItemCount(): Int {
        return if (datas.size > 0) datas.size else 1
    }

    override fun getItemViewType(position: Int): Int {
        if (datas.size == 0)
            return -1
        return if (position == 5 && !isAdsFree) NATIVE_EXPRESS_AD_VIEW_TYPE else DATA_VIEW_TYPE
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (getItemViewType(position) == DATA_VIEW_TYPE) {
            if (datas.size > 0) {
                this.holder = holder
                this.holder.onBind(position, datas[position], listener)
            } else {
                this.holder = holder
                this.holder.onBind(position, listener)
            }
        } else {
            try {
                this.holder = holder
                this.holder.onBind(position, datas[position], listener)
            } catch (ex: ArrayIndexOutOfBoundsException) {
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            //this.holder.onBind(position, listener)
        }

        if (position == datas.size - 1) {
            listener.loadMoreItem()
            this.holder.onLastPosition()
        }
    }

//    fun addData(datas: ArrayList<T>) {
//        this.datas.addAll(datas)
//        notifyDataSetChanged()
//    }

    /*fun removeData(datas: ArrayList<T>) {
        this.datas.clear()
        notifyDataSetChanged()
    }

    fun addData(data: T) {
        this.datas.add(data)
        notifyDataSetChanged()
    }

    fun removeData(position: Int) {
        this.datas.removeAt(position)
        notifyDataSetChanged()
    }*/


}