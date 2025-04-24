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
import kotlinx.android.synthetic.main.contact_us_bottomsheet.view.btnOk
import kotlinx.android.synthetic.main.contact_us_bottomsheet.view.btnSend
import kotlinx.android.synthetic.main.contact_us_bottomsheet.view.success_layout
import kotlinx.android.synthetic.main.filter_bottomsheet.view.close_btn_iv
import kotlinx.android.synthetic.main.invite_friends_bottomsheet.view.*


class InviteFriendsBottomSheet(val listener: Listener) : BottomSheetDialogFragment(){

    private lateinit var contentView: View

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        contentView = View.inflate(context, R.layout.invite_friends_bottomsheet, null)
        dialog.setContentView(contentView)

        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }


        contentView.btnSend.setOnClickListener {
            val emails = contentView.emails_et.text.toString().split(",")

            emails.forEach {
                if(!LocalValidator.isEmailValid(it)){
                    contentView.emails_et.error = "Enter Valid Email addresses"
                    return@setOnClickListener
                }
            }

            listener.onInviteBtnClicked(contentView.emails_et.text.toString())
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

        contentView.close_btn_iv.setOnClickListener {
            dismiss()
        }

        contentView.btnOk.setOnClickListener {
            dismiss()
        }

    }

    fun updateView(){
        contentView.invitation_layout.visibility = View.GONE
        contentView.success_layout.visibility = View.VISIBLE
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        return super.onCreateDialog(savedInstanceState)
    }

    interface Listener{
        fun onInviteBtnClicked(emails: String)
    }


}