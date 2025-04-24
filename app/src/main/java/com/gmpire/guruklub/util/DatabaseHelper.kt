package com.gmpire.guruklub.util

import android.content.Context
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class DatabaseHelper {
    private var subscribe: Disposable? = null

    fun executeDBOperation(
        operation: Any,
        operationName: String,
        iDatabaseCallBack: IDatabaseCallBack?
    ) {
        subscribe = Observable.fromCallable { operation }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ iDatabaseCallBack?.onSuccessDB(it, operationName) },
                { iDatabaseCallBack?.onSuccessDB(it, operationName) })
    }


}