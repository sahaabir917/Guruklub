package com.gmpire.guruklub.util

class LiveDataResult<T>(
    val status: Status,
    val data: T? = null,
    val err: Throwable? = null,
    val message: String? = null,
    val code: Int? = null
) {
    companion object {
        fun <T> success(data: T?) = LiveDataResult(Status.SUCCESS, data)
        fun <T> loading() = LiveDataResult<T>(Status.LOADING)
        fun <T> error(err: Throwable?) = LiveDataResult<T>(Status.ERROR, null, err)
        fun <T> error(code: Int?, message: String?, data: T?) =
            LiveDataResult<T>(Status.CODE_ERROR, data, null, message, code)
    }

    enum class Status {
        SUCCESS, ERROR, LOADING, CODE_ERROR
    }

}
