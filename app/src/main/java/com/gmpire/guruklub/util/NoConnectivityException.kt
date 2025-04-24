package com.gmpire.guruklub.util

import java.io.IOException


/**
 * Created by Tahsin Rahman on 29/8/20.
 */


class NoConnectivityException() : IOException() {
    override fun getLocalizedMessage(): String? {
        return "No internet available"
    }
}