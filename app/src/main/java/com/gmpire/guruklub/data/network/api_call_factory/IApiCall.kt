package com.gmpire.guruklub.data.network.api_call_factory

import io.reactivex.Maybe
import com.gmpire.guruklub.data.network.IApiService
import okhttp3.ResponseBody
import retrofit2.Response

interface IApiCall {
    fun<T> getMaybeObserVable(apiService: IApiService, path: String, hashMap: HashMap<String,T>):Maybe<Response<ResponseBody>>
}