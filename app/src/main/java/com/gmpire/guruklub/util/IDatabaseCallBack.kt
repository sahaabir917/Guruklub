package com.gmpire.guruklub.util

interface IDatabaseCallBack {
    fun onSuccessDB(result : Any, optName : String)
    fun onFailedDB(result : Any, optName : String)
}