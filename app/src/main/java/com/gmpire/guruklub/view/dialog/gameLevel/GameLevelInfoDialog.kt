package com.gmpire.guruklub.view.dialog.gameLevel

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.FragmentGameRuleDialogBinding
import com.gmpire.guruklub.view.dialog.notice.GameWebViewClient

class GameLevelInfoDialog : DialogFragment() {

    private var infoTitle: String = ""
    private var infoText: String = ""
    private var hasOkBtn: Boolean? = null
    private var hasCenterAlign: Boolean? = null
    private lateinit var mContext: Context
    private lateinit var binding: FragmentGameRuleDialogBinding
    private lateinit var gameLevelInfoDialogListener: GameLevelInfoDialogListener


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        gameLevelInfoDialogListener = context as GameLevelInfoDialogListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_game_rule_dialog, container, false)

        try {
            infoTitle = arguments?.getString("infoTitle") as String
            infoText = arguments?.getString("infoText") as String
            hasOkBtn = arguments?.getBoolean("hasOkbtn") as Boolean
            hasCenterAlign = arguments?.getBoolean("hasCenterAlign") as Boolean
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }

        binding.ruleTitle.text = infoTitle

        var head =
            "<head><style>" +
                    "@font-face {font-family: 'Poppins';src: url('file:///android_asset/font/poppins_regular.ttf');}" +
                    "body {font-family: Poppins';}</style></head>"
        var text = "<html>$head<body>"
        text +=  "<div style='color:white'>$infoText</div>"
        text = text.replace("<a", "<a style=\"color:yellow\"")
        text += "</body></html>"
        val settings = binding.gameRule.settings
        settings.defaultTextEncodingName = "utf-8"
        binding.gameRule.setBackgroundColor(Color.TRANSPARENT);
        binding.gameRule.loadDataWithBaseURL(
            "file:///android_asset/",
            text,
            "text/html; charset=utf-8",
            "utf-8", null
        )

        if(hasCenterAlign ?: false){
           val params = binding.gameRule.layoutParams as ViewGroup.MarginLayoutParams
            params.marginStart = 15
//            params.rightMargin = 30
        }

        if (hasOkBtn ?: false) {
            binding.okBtnLayout.visibility = View.VISIBLE
        } else {
            binding.okBtnLayout.visibility = View.GONE
        }

        binding.closeBtnIv.setOnClickListener {
            if (infoTitle == "Challenge Details")
                gameLevelInfoDialogListener.onCloseChallengeDetailsClicked()
            else
                gameLevelInfoDialogListener.onCloseClicked()
        }

        binding.okBtnLayout.setOnClickListener {
            gameLevelInfoDialogListener.onOkBtnClicked(infoTitle)
        }
        isCancelable = false
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(infoTitle: String, infoText: String, hasOkBtn: Boolean,hasCenterAlign:Boolean) =
            GameLevelInfoDialog().apply {
                arguments = Bundle().apply {
                    this.putString("infoTitle", infoTitle)
                    this.putString("infoText", infoText)
                    this.putBoolean("hasOkbtn", hasOkBtn)
                    this.putBoolean("hasCenterAlign",hasCenterAlign)
                }
            }
    }

    interface GameLevelInfoDialogListener {
        fun onCloseClicked()
        fun onCloseChallengeDetailsClicked() {}
        fun onOkBtnClicked(title: String)
    }
}