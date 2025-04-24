package com.gmpire.guruklub.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.databinding.DialogImageviewBinding
import com.bumptech.glide.Glide


class ImageViewDialog constructor(
    val mContext: Context, val image: String?
) : Dialog(mContext) {

    lateinit var binding: DialogImageviewBinding
    private val alertDialog: Dialog
    private val isCancelable = false

    init {
        alertDialog = Dialog(context, R.style.DialogStyle)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun show() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_imageview,
            null,
            false
        )
        alertDialog.setContentView(binding.getRoot())
        alertDialog.setCancelable(isCancelable)
        alertDialog.setCanceledOnTouchOutside(isCancelable)


        if (image != null) {
            val handler = Handler()
            handler.postDelayed(
                {
                    Glide.with(mContext).load("http://demo.gmpire.com/" + image)
                        .into(binding.photoView)
                },
                100
            )
        }

        binding.btnCancel.setOnClickListener {
            dismissDialog()
        }

        alertDialog.show()

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

