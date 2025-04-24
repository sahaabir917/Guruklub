package com.gmpire.guruklub.data.network.api_call_factory

import io.reactivex.Maybe
import com.gmpire.guruklub.data.network.IApiService
import okhttp3.ResponseBody
import retrofit2.Response

class ApiGetCall() : IApiCall {

    override fun<T> getMaybeObserVable(apiService: IApiService, path: String, hashMap: HashMap<String,T>): Maybe<Response<ResponseBody>> {
        return apiService.getRequest(path,hashMap as HashMap<String,String>)
    }



}