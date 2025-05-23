package sslwireless.android.easy.loyal.merchant.viewmodel.util

import androidx.lifecycle.MutableLiveData
import io.reactivex.MaybeObserver
import io.reactivex.disposables.Disposable
import com.gmpire.guruklub.util.LiveDataResult
import okhttp3.ResponseBody
import retrofit2.Response

class ApiCallbackHelper(
    liveData: MutableLiveData<LiveDataResult<Response<ResponseBody>>>
) :
    MaybeObserver<Response<ResponseBody>> {

    val liveData = liveData
    override fun onSubscribe(d: Disposable) {
        liveData.postValue(LiveDataResult.loading())
    }

    override fun onError(e: Throwable) {
        liveData.postValue(LiveDataResult.error(e))
    }

    override fun onSuccess(t: Response<ResponseBody>) {
        if (t.code() == 401) {
            liveData.postValue(LiveDataResult.error(t.code(), t.message(), t))
        } else {
            liveData.postValue(LiveDataResult.success(t))
        }
    }

    override fun onComplete() {
    }

}