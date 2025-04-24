package com.gmpire.guruklub.view.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.Notice
import com.gmpire.guruklub.databinding.DialogNoticeBoardBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class NoticeBoardDialog : DialogFragment() {

    private lateinit var binding: DialogNoticeBoardBinding
    private lateinit var boardListener: NoticeBoardDialogButtonListener
    private var noticeList: List<Notice> = listOf()
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialog)
    }

    private fun bindNotices() {
        binding.rcvNotice.layoutManager = LinearLayoutManager(mContext)
        binding.rcvNotice.adapter =
            BaseRecyclerAdapter<Notice>(activity, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {

                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return NoticeAdapter(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_notice_board,
                            parent,
                            false
                        ), mContext
                    )
                }

                override fun loadMoreItem() {

                }

            }, ArrayList(noticeList))

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NoticeBoardDialogButtonListener) {
            boardListener = context
            mContext = context
        }
        arguments?.getString("noticeList")?.let {
            noticeList = Gson().fromJson(it, object : TypeToken<List<Notice>>() {}.type)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_notice_board, container, false)

        binding.btnGotIt.setOnClickListener {
            dismiss()
        }

        if (noticeList.isNotEmpty()) {
            bindNotices()
        }

        return binding.root
    }


    companion object {
        @JvmStatic
        fun newInstance(noticeList: List<Notice>) =
            NoticeBoardDialog().apply {
                arguments = Bundle().apply {
                    putString("noticeList", Gson().toJson(noticeList))
                }
            }
    }


    interface NoticeBoardDialogButtonListener {
        fun onNoticeDismiss()
    }
}