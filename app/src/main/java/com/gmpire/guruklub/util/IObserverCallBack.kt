package com.gmpire.guruklub.util

import okhttp3.ResponseBody
import retrofit2.Response

interface IObserverCallBack {
    fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String)
    fun onLoading(isLoader: Boolean, key: String)
    fun onError(err: Throwable, key: String)
    fun onErrorCode(code: Int, message: String, result: LiveDataResult<Response<ResponseBody>>)
}