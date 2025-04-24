package com.gmpire.guruklub.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.categoryAndSubject.HeaderFilter
import com.gmpire.guruklub.data.model.categoryAndSubject.Subject
import com.gmpire.guruklub.databinding.DialogMultiselectionBinding
import com.gmpire.guruklub.view.activity.categoryAndSubjectSelection.MultiSelectHeaderViewHolder
import com.gmpire.guruklub.view.activity.categoryAndSubjectSelection.MultiselectionItemViewHolder
import com.gmpire.guruklub.view.adapter.HeaderRecyclerViewAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.adapter.TYPE_HEADER
import com.gmpire.guruklub.view.base.BaseViewHolder


class DialogMultiselection constructor(
    val mContext: Context,
    val title: String,
    val items: ArrayList<BaseItem>,
    val onDoneBtnClickListener: OnDoneBtnClickListener,
    val titleBarTitle : String
) : Dialog(mContext) {

    lateinit var binding: DialogMultiselectionBinding
    private val alertDialog: Dialog
    private val isCancelable = false

    init {
        alertDialog = Dialog(context, R.style.DialogStyleDialogMultiSelect)
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun show() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_multiselection,
            null,
            false
        )
        alertDialog.setContentView(binding.root)
        alertDialog.setCancelable(isCancelable)
        alertDialog.setCanceledOnTouchOutside(isCancelable)

        if (title.contains("Subjects")) {
            binding.llSubjectAll.visibility = View.VISIBLE
        }


        initRecView()

        checkForSubjectSelectAll()

        binding.itemAllSubject.setOnClickListener {
            val setValue = binding.itemAllSubject.isChecked
            items.forEach {
                it.isSelected = setValue
            }
            binding.selectionItemsRecview.adapter?.notifyDataSetChanged()
        }
    }

    private fun initRecView() {
        binding.titleTv.text = title
        binding.tvFilterTitle.text = titleBarTitle

        val handler = Handler()
        handler.postDelayed(
            {
                val customAdapter = HeaderRecyclerViewAdapter(object : IAdapterListener {
                    override fun <T> callBack(position: Int, model: T, view: View) {
                        // Check all checkboxes until another header shows up
                        if (model is HeaderFilter) {
                            (items[position] as HeaderFilter).isAllSelected =
                                (view as CheckBox).isChecked
                            for (i in position + 1 until items.size) {
                                if (items[i] is HeaderFilter) {
                                    break
                                }
                                items[i].isSelected = (view as CheckBox).isChecked
                            }
                            binding.selectionItemsRecview.adapter?.notifyDataSetChanged()
                        } else {
                            items[position].isSelected = (view as CheckBox).isChecked
                            if (!items[position].isSelected) {
                                if (items[position] !is Subject) {
                                    uncheckHeader(position)
                                    binding.selectionItemsRecview.adapter?.notifyDataSetChanged()
                                } else {
                                    checkForSubjectSelectAll()
                                }
                            } else {
                                if (items[position] !is Subject) {
                                    items[position].parentPos?.let { checkHeader(it) }
                                    binding.selectionItemsRecview.adapter?.notifyDataSetChanged()
                                } else {
                                    checkForSubjectSelectAll()
                                }
                            }
                        }
                    }

                    override fun getViewHolder(
                        parent: ViewGroup,
                        viewType: Int
                    ): BaseViewHolder {
                        return if (viewType === TYPE_HEADER) {
                            val v: ViewDataBinding = DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context)
                                , R.layout.item_multiselect_header
                                , parent, false
                            )
                            MultiSelectHeaderViewHolder(v)
                        } else {
                            val v: ViewDataBinding = DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context)
                                , R.layout.item_multiselection_recview
                                , parent, false
                            )
                            MultiselectionItemViewHolder(v, parent.context, title)
                        }
                        throw IllegalArgumentException()
                    }

                    override fun loadMoreItem() {

                    }

                }, items)
                binding.selectionItemsRecview.adapter = customAdapter
            },
            100
        )

        binding.btnClear.setOnClickListener {
            items.forEach {
                it.isSelected = false
            }
            binding.selectionItemsRecview.adapter?.notifyDataSetChanged()
        }

        binding.btnCancel.setOnClickListener {
            dismissDialog()
        }
        binding.btnDone.setOnClickListener {
            onDoneBtnClickListener.onDoneBtnClick(items, title)
            dismissDialog()
        }

        alertDialog.show()
    }

    private fun checkForSubjectSelectAll() {
        var isSelectedAll = true
        for (i in 0 until items.size) {
            if (!items[i].isSelected) {
                isSelectedAll = false

            }
        }
        binding.itemAllSubject.post {
            binding.itemAllSubject.isChecked = isSelectedAll
            binding.itemAllSubject.jumpDrawablesToCurrentState() // This is most important
        }
    }

    private fun checkHeader(parentPos: Int) {
        var isAllChecked = true
        for (i in parentPos + 1 until items.size) {
            if (items[i] is HeaderFilter) {
                break
            }
            if (!items[i].isSelected) {
                isAllChecked = false
            }
        }
        if (isAllChecked) {
            (items[parentPos] as HeaderFilter).isAllSelected = true
        }
    }

    private fun uncheckHeader(position: Int) {
        for (n in position downTo 0) {
            if (items[n] is HeaderFilter) {
                (items[n] as HeaderFilter).isAllSelected = false
                break
            }
        }
    }

    fun dismissDialog() {
        alertDialog.dismiss()
    }

    interface Listener {
        fun onOkClicked()
    }

    interface OnDoneBtnClickListener {
        fun onDoneBtnClick(selectedItemIds: ArrayList<BaseItem>, title: String)
    }


}

