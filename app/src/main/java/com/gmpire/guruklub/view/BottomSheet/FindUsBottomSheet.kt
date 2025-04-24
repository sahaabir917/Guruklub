package com.gmpire.guruklub.view.BottomSheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.findus.FindUsItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.filter_bottomsheet.view.close_btn_iv
import kotlinx.android.synthetic.main.find_us_bottomsheet.view.*


class FindUsBottomSheet(val findusItem: FindUsItem) : BottomSheetDialogFragment() {

    private lateinit var contentView: View

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        contentView = View.inflate(context, R.layout.find_us_bottomsheet, null)
        dialog.setContentView(contentView)

        dialog.setOnShowListener {
            val d = it as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }


//        if (!findusItem.facebook.startsWith("http://") && !findusItem.facebook.startsWith("https://")){
//            findusItem.facebook = "https://" + findusItem.facebook
//        }
//
//        if (!findusItem.linked_in.startsWith("http://") && !findusItem.linked_in.startsWith("https://")){
//            findusItem.linked_in = "https://" + findusItem.linked_in
//        }
//
//        if (!findusItem.twitter.startsWith("http://") && !findusItem.twitter.startsWith("https://")){
//            findusItem.twitter = "https://" + findusItem.twitter
//        }
//
//        if (!findusItem.youtube.startsWith("http://") && !findusItem.youtube.startsWith("https://")){
//            findusItem.youtube = "https://" + findusItem.youtube
//        }


        manageUrls(findusItem.facebook, contentView.facebook_layout)
        manageUrls(findusItem.linked_in, contentView.linked_in_layout)
        manageUrls(findusItem.twitter, contentView.twitter_layout)
        manageUrls(findusItem.youtube, contentView.youtube_layout)

        contentView.close_btn_iv.setOnClickListener {
            dismiss()
        }

    }


    private fun manageUrls(url: String, layout: View) {
        if (url.isNotEmpty()) {
            layout.setOnClickListener {
                try {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                } catch (ex: ActivityNotFoundException) {
                    // Notify the user?
                    Toast.makeText(activity, "No app found for this action.", Toast.LENGTH_SHORT)
                        .show()
                }

            }
        } else {
            layout.visibility = View.GONE
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        return super.onCreateDialog(savedInstanceState)
    }


}