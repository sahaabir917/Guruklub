package com.gmpire.guruklub.util.appEvents

import android.content.Context
import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import java.math.BigDecimal
import java.util.*

class AppEvent private constructor() {
    private val appLogger = AppEventsLogger.newLogger(mContext)

    fun logLimitedHeartPurchaseEvent(userName: String, amount: Int) {
        val params = Bundle()
        params.putString("User", userName)
        params.putInt("Amount", amount)
        appLogger.logEvent(AppEventKEY.LIMITED_HEARTS_PURCHASE, params)
        appLogger.flush()
    }

    fun logUnLimitedHeartPurchaseEvent(userName: String, days: Int) {
        val params = Bundle()
        params.putString("User", userName)
        params.putInt("Days", days)
        appLogger.logEvent(AppEventKEY.UNLIMITED_HEARTS_PURCHASE, params)
        appLogger.flush()
    }

    fun logLiveExamParticipantEvent(userName: String, dateTime: String) {
        val params = Bundle()
        params.putString("User", userName)
        params.putString("Join Time", dateTime)
        appLogger.logEvent(AppEventKEY.LIVE_EXAM_PARTICIPANTS, params)
        appLogger.flush()
    }


    fun logTakeChallengeEvent(userName: String, challengeTitle: String) {
        val params = Bundle()
        params.putString("User", userName)
        params.putString("Challenge", challengeTitle)
        appLogger.logEvent(AppEventKEY.CHALLENGE_TAKER, params)
        appLogger.flush()
    }

    fun logAdsFreeEvent(userName: String, days: Int, amount: Float) {
        val params = Bundle()
        params.putString("User", userName)
        params.putInt("Days", days)
        params.putFloat("Amount", amount)
        appLogger.logEvent(AppEventKEY.ADS_FREE_PACKAGE_BUYER, params)
        appLogger.flush()
    }

    fun logModelTestStartEvent(userName: String, modelTest: String) {
        val params = Bundle()
        params.putString("User", userName)
        params.putString("Test Title", modelTest)
        appLogger.logEvent(AppEventKEY.MODEL_TEST_START, params)
        appLogger.flush()
    }


    companion object {
        private var mContext: Context? = null
        private var appEvent: AppEvent? = null

        fun getInstance(context: Context): AppEvent {
            mContext = context
            if (appEvent == null) {
                appEvent = AppEvent()
            }
            return appEvent as AppEvent
        }

    }

}