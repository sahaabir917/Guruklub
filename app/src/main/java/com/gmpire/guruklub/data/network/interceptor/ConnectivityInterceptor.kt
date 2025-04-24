package com.gmpire.guruklub.data.network.interceptor

import android.content.Context
import android.net.ConnectivityManager
import com.gmpire.guruklub.util.NoConnectivityException
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

import java.io.IOException
import java.net.UnknownHostException


/**
 * Created by Tahsin Rahman on 29/8/20.
 */


class ConnectivityInterceptor(context: Context) : Interceptor {
    private val mContext: Context = context

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isConnected()) {
            throw NoConnectivityException()
            // Throwing our custom exception 'NoConnectivityException'
        }
        val builder: Request.Builder = chain.request().newBuilder()
        return chain.proceed(builder.build())
    }

    fun isConnected(): Boolean {
        val connectivityManager =
            mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }

}