package com.gmpire.guruklub.view.BottomSheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.categoryAndSubject.HeaderFilter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.dialog.DialogMultiselection
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.filter_bottomsheet.view.*

class FilterBottomSheet(val viewmodel: ViewModel) : BottomSheetDialogFragment(),
    DialogMultiselection.OnDoneBtnClickListener {

    private lateinit var contentView: View
    private var selectedSubjectIds: String = ""
    private var selectedSectionIds: String = ""
    private var selectedTopicIds: String = ""

    private var savedSubjectNames: String = ""
    private var savedSectionNames: String = ""
    private var savedTopicNames: String = ""

    var listener: IBottomSheetDialogClicked? = null

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        contentView = View.inflate(context, R.layout.filter_bottomsheet, null)
        dialog.setContentView(contentView)

        if (savedSubjectNames.isNotBlank()) {
            contentView.subject_name_tv.text = savedSubjectNames
            contentView.subject_name_tv.visibility = View.GONE
            contentView.recyclerViewSubjects.visibility = View.VISIBLE
            val list = if (savedSubjectNames.contains(",")) {
                savedSubjectNames.split(",") as ArrayList<String>
            } else {
                arrayListOf(savedSubjectNames)
            }
            initRecyclerView(
                contentView.recyclerViewSubjects,
                list
            )
        }

        if (savedSectionNames.isNotBlank()) {
            contentView.section_name_tv.text = savedSectionNames
            contentView.section_name_tv.visibility = View.GONE
            contentView.recyclerViewSections.visibility = View.VISIBLE
            val list = if (savedSectionNames.contains(",")) {
                savedSectionNames.split(",") as ArrayList<String>
            } else {
                arrayListOf(savedSectionNames)
            }
            initRecyclerView(
                contentView.recyclerViewSections,
                list
            )
        }

        if (savedTopicNames.isNotBlank()) {
            contentView.topic_name_tv.text = savedTopicNames
            contentView.topic_name_tv.visibility = View.GONE
            contentView.recyclerViewTopics.visibility = View.VISIBLE
            val list = if (savedTopicNames.contains(",")) {
                savedTopicNames.split(",") as ArrayList<String>
            } else {
                arrayListOf(savedTopicNames)
            }
            initRecyclerView(
                contentView.recyclerViewTopics,
                list
            )
        }

        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            val scrollView = d.findViewById(R.id.filterScrollView) as NestedScrollView?
            val bsb = BottomSheetBehavior.from(bottomSheet)
            bsb.state = BottomSheetBehavior.STATE_EXPANDED

            scrollView?.viewTreeObserver
                ?.addOnScrollChangedListener {
                    bsb.isHideable = scrollView.scrollY == 0
                }
        }

        contentView.select_subject_layout.setOnClickListener {
            listener?.onSubjectLayoutClicked()
        }

        contentView.select_section_layout.setOnClickListener {
            if (selectedSubjectIds != "") {
                listener?.onSectionLayoutClicked(selectedSubjectIds)
            }
        }

        contentView.select_topic_layout.setOnClickListener {
            if (selectedSectionIds != "") {
                listener?.onTopicLayoutClicked(selectedSectionIds)
            }
        }

        contentView.submit_btn.setOnClickListener {
            listener?.onSubmitBtnClicked(
                selectedSubjectIds,
                selectedSectionIds,
                selectedTopicIds,
                contentView.subject_name_tv.text.toString(),
                contentView.section_name_tv.text.toString(),
                contentView.topic_name_tv.text.toString()
            )
        }

        contentView.btnClear.setOnClickListener {
            selectedSubjectIds = ""
            selectedSectionIds = ""
            selectedTopicIds = ""
            contentView.subject_name_tv.text = "Select Subject"
            contentView.section_name_tv.text = ""
            contentView.topic_name_tv.text = ""
            contentView.section_name_tv.hint = "Select Section"
            contentView.topic_name_tv.hint = "Select Topic"

            contentView.section_icon.setImageResource(R.drawable.blur_down_arrow)
            contentView.topic_icon.setImageResource(R.drawable.blur_down_arrow)

            contentView.subject_name_tv.visibility = View.VISIBLE
            contentView.recyclerViewSubjects.visibility = View.GONE
            contentView.section_name_tv.visibility = View.VISIBLE
            contentView.recyclerViewSections.visibility = View.GONE
            contentView.topic_name_tv.visibility = View.VISIBLE
            contentView.recyclerViewTopics.visibility = View.GONE
        }

        contentView.close_btn_iv.setOnClickListener {
            dismiss()
        }
    }

    fun setBottomDialogListener(listener: IBottomSheetDialogClicked) {
        this.listener = listener
    }

    interface IBottomSheetDialogClicked {
        fun onSubjectLayoutClicked()
        fun onSectionLayoutClicked(subjectIds: String)
        fun onTopicLayoutClicked(sectionIds: String)
        fun onSubmitBtnClicked(
            subjectsIds: String, sectionIds: String, topicIds: String,
            subjectNames: String, sectionNames: String, topicNames: String
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onDoneBtnClick(items: ArrayList<BaseItem>, title: String) {
        when (title) {
            "Select Subjects" -> {
                var atleastOneSelected = false
                selectedSubjectIds = ""
                contentView.subject_name_tv.text = ""
                selectedSectionIds = ""

                selectedTopicIds = ""
                contentView.topic_name_tv.text = ""
                contentView.topic_name_tv.hint = "Select Topic"
                contentView.topic_icon.setImageResource(R.drawable.blur_down_arrow)


                contentView.section_name_tv.visibility = View.VISIBLE
                contentView.recyclerViewSections.visibility = View.GONE
                contentView.topic_name_tv.visibility = View.VISIBLE
                contentView.recyclerViewTopics.visibility = View.GONE

                val selectedItems = arrayListOf<String>()
                items.forEach {
                    if (it.isSelected) {
                        atleastOneSelected = true
                        selectedItems.add(it.name ?: "")
                        contentView.subject_name_tv.append(it.name + ",")
                        if (selectedSubjectIds == "") {
                            selectedSubjectIds = it.id.toString()
                        } else {
                            selectedSubjectIds = selectedSubjectIds + "," + it.id
                        }
                    }
                }
                contentView.subject_name_tv.text =
                    contentView.subject_name_tv.text.toString().removeSuffix(",")

                if (!atleastOneSelected) {
                    contentView.subject_name_tv.text = "Select Subject"
                    contentView.subject_name_tv.visibility = View.VISIBLE
                    contentView.recyclerViewSubjects.visibility = View.GONE
                    contentView.section_icon.setImageResource(R.drawable.blur_down_arrow)
                } else {
                    contentView.subject_name_tv.visibility = View.GONE
                    contentView.recyclerViewSubjects.visibility = View.VISIBLE
                    initRecyclerView(contentView.recyclerViewSubjects, selectedItems)
                    contentView.section_icon.setImageResource(R.drawable.ic_down_arrow_black_png)
                    blurHintSection()
                }
            }

            "Select Section" -> {
                var atleastOneSelected = false
                selectedSectionIds = ""
                contentView.section_name_tv.text = ""
                selectedTopicIds = ""
                contentView.topic_name_tv.text = "Select Topic"

                contentView.topic_name_tv.visibility = View.VISIBLE
                contentView.recyclerViewTopics.visibility = View.GONE

                val selectedItems = arrayListOf<String>()
                items.forEach {
                    if (it !is HeaderFilter) {
                        if (it.isSelected) {
                            atleastOneSelected = true
                            selectedItems.add(it.name ?: "")
                            contentView.section_name_tv.append(it.name + ",")
                            selectedSectionIds = if (selectedSectionIds == "") {
                                it.id.toString()
                            } else {
                                selectedSectionIds + "," + it.id
                            }
                        }
                    }
                }
                contentView.section_name_tv.text =
                    contentView.section_name_tv.text.toString().removeSuffix(",")

                if (!atleastOneSelected) {
                    contentView.section_name_tv.text = "Select Section"
                    contentView.section_name_tv.visibility = View.VISIBLE
                    contentView.recyclerViewSections.visibility = View.GONE
                    contentView.topic_icon.setImageResource(R.drawable.blur_down_arrow)
                } else {
                    contentView.section_name_tv.visibility = View.GONE
                    contentView.recyclerViewSections.visibility = View.VISIBLE
                    contentView.topic_icon.setImageResource(R.drawable.ic_down_arrow_black_png)
                    initRecyclerView(contentView.recyclerViewSections, selectedItems)
                    blurHintTopic()
                }
            }

            "Select Topic" -> {
                var atleastOneSelected = false
                selectedTopicIds = ""
                contentView.topic_name_tv.text = ""

                val selectedItems = arrayListOf<String>()
                items.forEach {
                    if (it !is HeaderFilter) {
                        if (it.isSelected) {
                            atleastOneSelected = true
                            selectedItems.add(it.name ?: "")
                            contentView.topic_name_tv.append(it.name + ",")
                            selectedTopicIds = if (selectedTopicIds == "") {
                                it.id.toString()
                            } else {
                                selectedTopicIds + "," + it.id
                            }
                        }
                    }
                }
                contentView.topic_name_tv.text =
                    contentView.topic_name_tv.text.toString().removeSuffix(",")

                if (!atleastOneSelected) {
                    contentView.topic_name_tv.text = "Select Topic"
                    contentView.topic_name_tv.visibility = View.VISIBLE
                    contentView.recyclerViewTopics.visibility = View.GONE
                } else {
                    contentView.topic_name_tv.visibility = View.GONE
                    contentView.recyclerViewTopics.visibility = View.VISIBLE
                    initRecyclerView(contentView.recyclerViewTopics, selectedItems)
                }
            }
        }
    }

    private fun blurHintTopic() {
        contentView.topic_name_tv.text = "Select Topic"
    }

    private fun initRecyclerView(recyclerView: RecyclerView?, items: ArrayList<String>) {
        recyclerView?.adapter =
            BaseRecyclerAdapter(this.context, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return FilterValueViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context)
                            , R.layout.item_filter_selected
                            , parent, false
                        ), requireContext())
                }

                override fun loadMoreItem() {
                }
            }, items)
    }


    fun showSelectSubjectDialog(selectedItems: ArrayList<BaseItem>, shouldShow: Boolean) {
        val selected = contentView.subject_name_tv.text.toString().split(",")
        selected.forEach { sub ->
            selectedItems.forEach {
                if (sub == it.name) {
                    it.isSelected = true
                }
            }
        }

        val dialogMultiselection =
            context?.let {
                DialogMultiselection(
                    it,
                    "Select Subjects",
                    selectedItems,
                    this,
                    "Choose Subjects"
                )
            }
        dialogMultiselection?.show()
    }

    fun showSelectSectionDialog(selectedItems: ArrayList<BaseItem>, shouldShow: Boolean) {
        val selected = contentView.section_name_tv.text.toString().split(",")
        selected.forEach { sub ->
            selectedItems.forEach {
                if (sub == it.name) {
                    it.isSelected = true
                }
            }
        }

        // Check select all
        selectedItems.forEach {
            if (it is HeaderFilter) {
                it.isAllSelected = true
            } else {
                if (!it.isSelected) {
                    try {
                        (selectedItems[it.parentPos!!] as HeaderFilter).isAllSelected = false
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        val dialogMultiselection =
            context?.let {
                DialogMultiselection(
                    it,
                    "Select Section",
                    selectedItems,
                    this,
                    "Choose Sections"
                )
            }
        dialogMultiselection?.show()
    }

    fun showSelectTopicDialog(selectedItems: ArrayList<BaseItem>, shouldShow: Boolean) {
        val selected = contentView.topic_name_tv.text.toString().split(",")
        selected.forEach { sub ->
            selectedItems.forEach {
                if (sub == it.name) {
                    it.isSelected = true
                }
            }
        }

        // Check select all
        selectedItems.forEach {
            if (it is HeaderFilter) {
                it.isAllSelected = true
            } else {
                if (!it.isSelected) {
                    try {
                        (selectedItems[it.parentPos!!] as HeaderFilter).isAllSelected = false
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        val dialogMultiselection =
            context?.let {
                DialogMultiselection(
                    it,
                    "Select Topic",
                    selectedItems,
                    this,
                    "Choose Topics"
                )
            }
        dialogMultiselection?.show()
    }

    fun blurHintSection() {
        contentView.section_name_tv.text = "Select Section"
    }

    fun setSavedFilterValues(ids: String, names: String, type: String) {
        when (type) {
            "Subject" -> {
                selectedSubjectIds = ids
                savedSubjectNames = names
            }
            "Section" -> {
                selectedSectionIds = ids
                savedSectionNames = names
            }
            "Topic" -> {
                selectedTopicIds = ids
                savedTopicNames = names
            }
        }
    }

}