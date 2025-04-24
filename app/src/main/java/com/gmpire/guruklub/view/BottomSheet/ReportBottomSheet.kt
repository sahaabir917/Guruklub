package com.gmpire.guruklub.view.BottomSheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.gmpire.guruklub.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_all_videos.view.*
import kotlinx.android.synthetic.main.report_bottomsheet.view.*
import kotlinx.android.synthetic.main.report_bottomsheet.view.back_btn_iv
import kotlinx.android.synthetic.main.report_bottomsheet.view.btnSubmit
import kotlinx.android.synthetic.main.report_bottomsheet.view.issue_title
import kotlinx.android.synthetic.main.report_bottomsheet.view.report_description_et
import kotlinx.android.synthetic.main.report_bottomsheet.view.report_details_cl
import kotlinx.android.synthetic.main.report_bottomsheet.view.report_topic_cl

class ReportBottomSheet(val question_id: String) : BottomSheetDialogFragment() {

    private var isVideo: Boolean = false
    private var isGame: Boolean = false
    var listener: IBottomSheetDialogClicked? = null
    private var type: String = "1"
    lateinit var details: String

    fun ReportBottomSheet() {
        // doesn't do anything special
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.report_bottomsheet, null)
        dialog.setContentView(contentView)

        if (isGame) {
            contentView.btnSubmit.setBackgroundResource(R.drawable.yellow_box_with_colorfull_bg)
            contentView.re_content.setBackgroundResource(R.drawable.bg_list_background_top_rounded_game)
            contentView.report_topic_cl.setBackgroundColor(Color.parseColor("#3F0E5F"))
            contentView.issue_title.setTextColor(Color.WHITE)
            contentView.textView6.setTextColor(Color.WHITE)
            contentView.other_error_tv.setTextColor(Color.WHITE)
            contentView.choice_error_tv.setTextColor(Color.WHITE)
            contentView.question_error_tv.setTextColor(Color.WHITE)
            contentView.textView13.setTextColor(Color.WHITE)
            contentView.report_description_et.setBackgroundResource(R.drawable.bg_textarea_game)
            contentView.report_description_et.setHintTextColor(Color.GRAY)
            contentView.report_description_et.setTextColor(Color.WHITE)
            contentView.textView6.setTextColor(Color.parseColor("#f1d40c"))
        }


        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }

        contentView.setOnClickListener {
            val inputManager = context?.getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            val focusedView = dialog.currentFocus

            if (focusedView != null) {
                inputManager.hideSoftInputFromWindow(
                    focusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }

        contentView.question_error_tv.setOnClickListener {
            playAnimation(contentView.report_details_cl, R.anim.layout_animation_from_right)
            playAnimation(contentView.report_topic_cl, R.anim.layout_animation_to_left)
            contentView.report_topic_cl.visibility = View.GONE
            contentView.report_details_cl.visibility = View.VISIBLE
            contentView.report_details_cl.issue_title.text = contentView.question_error_tv.text
            type = "1"
            contentView.back_btn_iv.setImageDrawable(
                context?.let { it1 ->
                    ContextCompat.getDrawable(
                        it1,
                        R.drawable.ic_back
                    )
                }
            )
        }

        contentView.choice_error_tv.setOnClickListener {
            playAnimation(contentView.report_details_cl, R.anim.layout_animation_from_right)
            playAnimation(contentView.report_topic_cl, R.anim.layout_animation_to_left)
            contentView.report_topic_cl.visibility = View.GONE
            contentView.report_details_cl.visibility = View.VISIBLE
            contentView.report_details_cl.issue_title.text = contentView.choice_error_tv.text
            type = "2"
            contentView.back_btn_iv.setImageDrawable(
                context?.let { it1 ->
                    ContextCompat.getDrawable(
                        it1,
                        R.drawable.ic_back
                    )
                }
            )
        }

        contentView.other_error_tv.setOnClickListener {
            playAnimation(contentView.report_details_cl, R.anim.layout_animation_from_right)
            playAnimation(contentView.report_topic_cl, R.anim.layout_animation_to_left)
            contentView.report_topic_cl.visibility = View.GONE
            contentView.report_details_cl.visibility = View.VISIBLE
            contentView.report_details_cl.issue_title.text = contentView.other_error_tv.text
            type = "3"
            contentView.back_btn_iv.setImageDrawable(
                context?.let { it1 ->
                    ContextCompat.getDrawable(
                        it1,
                        R.drawable.ic_back
                    )
                }
            )
        }

        contentView.back_btn_iv.setOnClickListener {
            when {
                contentView.report_topic_cl.isVisible -> dismiss()
                contentView.report_details_cl.isVisible -> {

                    playAnimation(contentView.report_topic_cl, R.anim.layout_animation_from_left)
                    playAnimation(contentView.report_details_cl, R.anim.layout_animation_to_right)

                    contentView.report_topic_cl.visibility = View.VISIBLE
                    contentView.report_details_cl.visibility = View.GONE
                    contentView.back_btn_iv.setImageDrawable(
                        context?.let { it1 ->
                            ContextCompat.getDrawable(
                                it1,
                                R.drawable.ic_close_icon
                            )
                        }
                    )
                }
                else -> {
                    dismiss()
                }
            }
        }

        contentView.btnSubmit.setOnClickListener {
            if (contentView.report_description_et.text.toString().isEmpty()) {
                contentView.report_description_et.error = "Please specify the problem"
                return@setOnClickListener
            }

            if(isVideo){
                listener?.onVideoReportSubmitted(
                    question_id,
                    type,
                    contentView.report_description_et.text.toString()
                )
            }
            else{
                listener?.onReportSubmitted(
                    question_id,
                    type,
                    contentView.report_description_et.text.toString()
                )
            }

            dismiss()
        }


    }


    fun setBottomDialogListener(listener: IBottomSheetDialogClicked, gameActivity: Boolean,videoActivity:Boolean) {
        this.listener = listener
        this.isGame = gameActivity
        this.isVideo = videoActivity
    }

    interface IBottomSheetDialogClicked {
        fun onReportSubmitted(question_id: String, type: String, details: String)
        fun onVideoReportSubmitted(question_id: String, type: String, details: String)
        fun onReportDialogDismiss()
    }

    private fun playAnimation(report_details_cl: View, resId: Int) {
        val animation = AnimationUtils.loadLayoutAnimation(context, resId)
        report_details_cl.startAnimation(animation.animation)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        this.listener?.onReportDialogDismiss()

    }

}