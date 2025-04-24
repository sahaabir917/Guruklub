package com.gmpire.guruklub.view.dialog

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.FragmentGameRuleDialogBinding
import com.gmpire.guruklub.databinding.FragmentNewsDetailsGestureDialogBinding


class NewsDetailsGestureDialog : DialogFragment() {

    private lateinit var mContext: Context
    private lateinit var binding: FragmentNewsDetailsGestureDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle1)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_news_details_gesture_dialog, container, false)
        binding.animationLeftRight.visibility = View.VISIBLE
        Glide.with(this).asGif().load(R.drawable.leftrightarrow).into(binding.animationLeftRight)
        val handler = Handler()
        handler.postDelayed(Runnable { //Write whatever to want to do after delay specified (1 sec)
            dialog?.dismiss()
        }, 5000)

        binding.animationLeftRight.setOnClickListener {
            dialog?.dismiss()
        }

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            NewsDetailsGestureDialog().apply {
                arguments = Bundle().apply {
                }
            }
    }
}