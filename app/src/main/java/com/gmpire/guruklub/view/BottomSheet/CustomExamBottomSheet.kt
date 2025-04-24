package com.gmpire.guruklub.view.BottomSheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.categoryAndSubject.Subject
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.dialog.DialogMultiselection
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.custom_exam_bottom_sheet.view.*


/**
 * Created by Tahsin Rahman on 14/9/20.
 */


class CustomExamBottomSheet() : DialogFragment(),
    DialogMultiselection.OnDoneBtnClickListener {

    fun CustomExamBottomSheet() {
        // doesn't do anything special
    }

    private lateinit var contentView: View
    var listener: IBottomSheetDialogClicked? = null
    var allSubject: ArrayList<Subject> = arrayListOf()
    private var selectedSubjectIds = ""
    private var commaSeperatedSubjects = ""


    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        contentView = View.inflate(context, R.layout.custom_exam_bottom_sheet, null)
        dialog.setContentView(contentView)


        dialog.setOnShowListener { dialog ->
//            val d = dialog as DialogFragment
//            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
//            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
//            val scrollView = d.findViewById(R.id.filterScrollView) as NestedScrollView?
//            val bsb = BottomSheetBehavior.from(bottomSheet)
//            bsb.state = BottomSheetBehavior.STATE_EXPANDED
//
//            scrollView?.viewTreeObserver
//                ?.addOnScrollChangedListener {
//                    bsb.isHideable = scrollView.scrollY == 0
//                }
        }

        contentView.close_btn_iv_right.setOnClickListener {
            dismiss()
        }

        contentView.seekbar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {
                contentView.textViewNumOfQues.text =
                    ((seekParams.thumbPosition + 1) * 40).toString()
                when (seekParams.thumbPosition) {
                    0 -> {
                        contentView.textViewTime.text = "24"
                    }
                    1 -> {
                        contentView.textViewTime.text = "48"
                    }
                    2 -> {
                        contentView.textViewTime.text = "72"
                    }
                    3 -> {
                        contentView.textViewTime.text = "96"
                    }
                    4 -> {
                        contentView.textViewTime.text = "120"
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
            }
        }

        contentView.select_subject_layout_custom_exam.setOnClickListener {
            listener?.onSubjectLayoutClicked()
        }

        contentView.submit_btn_custom_exam.setOnClickListener {
            if (contentView.subject_name_tv_custom_exam.text != "Select Subject") {
                listener?.onSubmitBtnClicked(
                    selectedSubjectIds,
                    contentView.textViewTime.text.toString(),
                    contentView.textViewNumOfQues.text.toString()
                )
            } else {
                Toast.makeText(activity, "Please select at least one subject", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val ss = SpannableString("Want to take part in topic-wise exam? Click here.")
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                dismiss()
                if (activity is MainActivity)
                    (activity as MainActivity).changeFragmentLibrary()
            }
        }
        ss.setSpan(clickableSpan, 38, 49, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        //where 22 and 27 are the starting and ending index of the String. Now word stack is clickable
        // onClicking stack it will open NextActiivty

        //where 22 and 27 are the starting and ending index of the String. Now word stack is clickable
        // onClicking stack it will open NextActiivty
        contentView.textView_test_exam_link.text = ss
        contentView.textView_test_exam_link.movementMethod = LinkMovementMethod.getInstance()
    }

    fun populateSubjectList(subjectList: ArrayList<Subject>) {
        allSubject.clear()
        allSubject.addAll(subjectList)
        getSubjectList(allSubject)
    }

    fun showSelectSubjectDialog(selectedItems: ArrayList<Subject>, shouldShow: Boolean) {
        if (commaSeperatedSubjects.isNotEmpty()) {
            val selected = commaSeperatedSubjects.split(",")
            selectedItems.forEach { sub ->
                selected.forEach {
                    if (sub.name?.contains(it) == true) {
                        sub.isSelected = true
                    }
                }
            }
        }

        val dialogMultiselection =
            context?.let {
                DialogMultiselection(
                    it,
                    "Select Subjects",
                    selectedItems as ArrayList<BaseItem>,
                    this,
                    "Choose subjects"
                )
            }
        dialogMultiselection?.show()
    }

    private fun getSubjectList(selectedItems: ArrayList<Subject>) {
        var subjectNameString = ""
        selectedItems.forEach {
            subjectNameString += it.name + ","
        }
        if (subjectNameString.isNotEmpty()) {
            subjectNameString.dropLast(1)
            contentView.subject_name_tv_custom_exam.text = subjectNameString
        }

        commaSeperatedSubjects = subjectNameString
        if (selectedItems.size == allSubject.size) {
            contentView.subject_name_tv_custom_exam.text = "All"
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle1)
        isCancelable = false
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onDoneBtnClick(items: ArrayList<BaseItem>, title: String) {
        when (title) {
            "Select Subjects" -> {
                var atleastOneSelected = false
                selectedSubjectIds = ""
                contentView.subject_name_tv_custom_exam.text = ""
                var selectedCount = 0
                items.forEach {
                    if (it.isSelected) {
                        selectedCount++
                        atleastOneSelected = true
                        contentView.subject_name_tv_custom_exam.append(it.name + ", ")
                        selectedSubjectIds = if (selectedSubjectIds == "") {
                            it.id.toString()
                        } else {
                            selectedSubjectIds + "," + it.id
                        }
                    }
                }
                contentView.subject_name_tv_custom_exam.text =
                    contentView.subject_name_tv_custom_exam.text.toString().removeSuffix(",")
                commaSeperatedSubjects = contentView.subject_name_tv_custom_exam.text.toString()
                if (!atleastOneSelected) {
                    contentView.subject_name_tv_custom_exam.text = "Select Subject"
                    commaSeperatedSubjects = ""
                } else {
                    if (selectedCount == allSubject.size)
                        contentView.subject_name_tv_custom_exam.text = "All"
                }
            }
        }
    }

    fun setBottomDialogListener(listener: IBottomSheetDialogClicked) {
        this.listener = listener
    }

    interface IBottomSheetDialogClicked {
        fun onSubjectLayoutClicked()
        fun onSubmitBtnClicked(
            subjectsIds: String,
            time: String,
            questionNum: String
        )
    }
}