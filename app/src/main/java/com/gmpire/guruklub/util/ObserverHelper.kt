package com.gmpire.guruklub.util

import androidx.lifecycle.Observer
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.HttpException
import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.NullPointerException

class ObserverHelper(iObserverCallBack: IObserverCallBack, key: String) {

    var baseObserver = Observer<LiveDataResult<Response<ResponseBody>>> { result ->

        when (result?.status) {

            LiveDataResult.Status.LOADING -> {
                iObserverCallBack.onLoading(true, key)
            }
            LiveDataResult.Status.ERROR -> {
                iObserverCallBack.onLoading(false, key)
                try {

                    val r = result.err as HttpException
                    val code = r.code()
                    if (code == 401) {
                        iObserverCallBack.onError(result.err, key)
                    }
                } catch (e: Exception) {
                    iObserverCallBack.onError(result.err ?: e, key)
                }
            }

            LiveDataResult.Status.CODE_ERROR -> {
                iObserverCallBack.onLoading(false, key)
                iObserverCallBack.onErrorCode(result.code ?: 0, result.message.toString(), result)
            }

            LiveDataResult.Status.SUCCESS -> {
                iObserverCallBack.onLoading(false, key)
                try {
                    iObserverCallBack.onSuccess(result, key)
                } catch (ex: JSONException) {
                    ex.printStackTrace()
                } catch (ex: NullPointerException) {
                    ex.printStackTrace()
                } catch (ex: IllegalStateException) {
                    ex.printStackTrace()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }
}
