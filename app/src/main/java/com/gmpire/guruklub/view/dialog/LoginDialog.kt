package com.gmpire.guruklub.view.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.DialogLoginOptionBinding


class LoginDialog : DialogFragment() {

    public fun LoginDialog() {
        // doesn't do anything special
    }

    private lateinit var binding: DialogLoginOptionBinding
    private lateinit var loginListenr: LoginDialogButtonListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle1)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LoginDialogButtonListener) {
            loginListenr = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_login_option, container, false)

        binding.cardViewGoogleLogin.setOnClickListener {
            loginListenr.onGoogleLogin()
        }

        binding.cardViewFbLogin.setOnClickListener {
            loginListenr.onFacebookLogin()
        }

        binding.cardViewRegularLogin.setOnClickListener {
            loginListenr.onRegularLogin()
        }

        binding.relativeLayoutCloseDialog.setOnClickListener {
            dismiss()
        }

        return binding.root
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            LoginDialog().apply {
                arguments = Bundle().apply {
                }
            }
    }


    interface LoginDialogButtonListener {
        fun onGoogleLogin()
        fun onFacebookLogin()
        fun onRegularLogin()
    }
}