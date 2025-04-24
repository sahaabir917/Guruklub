package com.gmpire.guruklub.view.BottomSheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import com.gmpire.guruklub.R
import com.gmpire.guruklub.util.LocalValidator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.contact_us_bottomsheet.view.*
import kotlinx.android.synthetic.main.filter_bottomsheet.view.close_btn_iv


class ContactUsBottomSheet(val email:String,val listener: Listener) : BottomSheetDialogFragment(){

    private lateinit var contentView: View

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        contentView = View.inflate(context, R.layout.contact_us_bottomsheet, null)
        dialog.setContentView(contentView)

        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }


        contentView.email_et.setText(email)


        contentView.btnSend.setOnClickListener {
            if(!LocalValidator.isEmailValid(contentView.email_et.text.toString())){
                contentView.email_et.error = "Enter valid email"
                return@setOnClickListener
            }

            if(contentView.comments_et.text.toString().length==0){
                contentView.comments_et.error = "Enter message"
                return@setOnClickListener
            }
            listener.onContactUsSendBtnClicked(contentView.email_et.text.toString(),contentView.comments_et.text.toString())
        }


        contentView.setOnClickListener {
            val inputManager = context?.getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            val focusedView = dialog.currentFocus

            if (focusedView != null) {
                inputManager.hideSoftInputFromWindow(
                    focusedView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }

        contentView.close_btn_iv.setOnClickListener {
            dismiss()
        }

        contentView.btnOk.setOnClickListener {
            dismiss()
        }

    }

    fun updateView(){
        contentView.contact_us_layout.visibility = View.GONE
        contentView.success_layout.visibility = View.VISIBLE
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        return super.onCreateDialog(savedInstanceState)
    }

    interface Listener{
        fun onContactUsSendBtnClicked(email: String,message:String)
    }


}