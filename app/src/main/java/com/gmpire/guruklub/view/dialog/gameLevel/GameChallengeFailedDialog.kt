package com.gmpire.guruklub.view.dialog.gameLevel

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.gameheartpackages.GuruKlubGameHeartChallengeCriteria
import com.gmpire.guruklub.databinding.FragmentGameChallengeFailedDialogBinding


class GameChallengeFailedDialog : DialogFragment() {

    private lateinit var binding: FragmentGameChallengeFailedDialogBinding
    private lateinit var listener: IChallengeFailedDialog
    private var criteria: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as IChallengeFailedDialog
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle1)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_game_challenge_failed_dialog,
            container,
            false
        )
        criteria = arguments?.getString("criteria")
        if (criteria == GuruKlubGameHeartChallengeCriteria.CLOCK_SHOOT.name)
            binding.tvTryAgain.text = "Exit"
        binding.tvTryAgain.setOnClickListener {
            dismiss()
            listener.onTryChallengeAgain()
        }
        binding.ivCloseFailedDialog.setOnClickListener {
            dismiss()
            listener.onChallengeFailedDialogDismiss()
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(gameCriteria: String?) =
            GameChallengeFailedDialog().apply {
                arguments = Bundle().apply {
                    this.putString("criteria", gameCriteria)
                }
            }
    }


    interface IChallengeFailedDialog {
        fun onTryChallengeAgain()
        fun onChallengeFailedDialogDismiss()
    }


}