package com.gmpire.guruklub.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseFragment

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if (!ConnectivityUtil.isOnline(context)) {
                if (context is BaseActivity) {
                   // context.showNoInternetDialog()
                } else if (context is BaseFragment) {
                   // context.showNoInternetDialog()
                }
            }
        }
    }
}