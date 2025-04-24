package com.gmpire.guruklub.view.dialog

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gmpire.guruklub.R
import com.gmpire.guruklub.util.ConnectivityUtil
import kotlinx.android.synthetic.main.dialog_no_internet.*


class NoInternetDialog(val title: String, val mcontext: Context, val listener: Listener) :
    DialogFragment() {

    companion object {
        val TAG = NoInternetDialog::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogTheme_Dark)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_no_internet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        title_tv.text = title
        btnOk.setOnClickListener {
            if (ConnectivityUtil.isOnline(mcontext)) {
                dismiss()
                listener.gotOnline()
            } else {
                Toast.makeText(
                    mcontext,
                    "Still not connected to internet, Connect to internet and try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    interface Listener {
        fun gotOnline()
    }
}
