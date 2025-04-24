package com.gmpire.guruklub.data.prefence

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.facebook.ads.internal.settings.AdSharedPreferences.getSharedPreferences
import com.gmpire.guruklub.data.model.GlobalSettings
import com.gmpire.guruklub.data.model.NoticeTracking
import com.gmpire.guruklub.data.model.game.GameResultSubmitRequestItem
import com.gmpire.guruklub.data.model.gameheartpackages.GameChallengeState
import com.gmpire.guruklub.data.model.gamelevel.GameUserState
import com.gmpire.guruklub.data.model.gamelevel.HeartGift
import com.gmpire.guruklub.data.model.gamelevel.HeartSubscription
import com.gmpire.guruklub.data.model.gamelevel.Level
import com.gmpire.guruklub.data.model.gameusersettings.UserSettings
import com.gmpire.guruklub.data.model.library.Library
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.data.model.modelTest.ModelTest
import com.gmpire.guruklub.data.model.modelTest.ModelTestRegistrationResponse
import com.gmpire.guruklub.data.model.modelTest.ModelTestResponseModel
import com.gmpire.guruklub.data.model.user.User
import com.gmpire.guruklub.view.activity.login.LoginActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson


class PreferencesHelper(context: Context) {
    private val PREF_KEY_REFERENCE_NAVIGATE_FROM_GAME: String =
        "PREF_KEY_REFERENCE_NAVIGATE_FROM_GAME"
    private val PREF_KEY_REFERENCE_NOTICE: String = "PREF_KEY_REFERENCE_NOTICE"
    private val PREF_KEY_REFERENCE_TOKEN: String = "PREF_KEY_REFERENCE_TOKEN"
    private val PREF_KEY_IS_LOGIN = "PREF_KEY_IS_LOGIN"
    private val PREF_KEY_TOKEN = "PREF_KEY_TOKEN"
    private val PREF_KEY_USER_INFO = "PREF_KEY_USER_INFO"
    private val PREF_KEY_RESUME_STUDY = "PREF_KEY_RESUME_STUDY"
    private val PREF_KEY_MODEL_TEST_REGISTRATION_RESPONSE =
        "PREF_KEY_MODEL_TEST_REGISTRATION_RESPONSE"
    private val PREF_KEY_MODEL_TEST_SUBMIT_REQUEST_ITEM = "PREF_KEY_MODEL_TEST_SUBMIT_REQUEST_ITEM"
    private val PREF_KEY_MODEL_TEST_RESPONSE_MODEL = "PREF_KEY_MODEL_TEST_RESPONSE_MODEL"
    private val PREF_KEY_RECOMMENDED_STUDY = "PREF_KEY_RECOMMENDED_STUDY"
    private val PREF_KEY_MODEL_TEST_ATTENDANCE = "PREF_KEY_MODEL_TEST_ATTENDANCE"
    private val PREF_KEY_MODEL_TEST_PARTICIPATION = "PREF_KEY_MODEL_TEST_PARTICIPATION"

    private val PREF_KEY_INVITATION_DATE = "invitation_date"
    private val PREF_KEY_RATING_DATE = "rating_date"
    private val PREF_KEY_MODEL_TEST_ID = "model_test_id"
    private val PREF_KEY_USER_SETTINGS = "user_settings"
    private val PREF_KEY_USER_GLOBAL_SETTINGS = "user_global_settings"
    private val PREF_KEY_GAME_USER_STATE = "gameUserState"
    private val PREF_KEY_GAME_USER_LEVEL = "gameUserLevel"
    private val PREF_KEY_GAME_USER_HEART = "userHeart"
    private val PREF_KEY_GAME_BG_MUSIC = "bg_music"
    private val PREF_KEY_GAME_USER_AD_LIMIT = "user_ad_limit"
    private val PREF_KEY_GAME_USER_AD_FREE = "user_ad_free"
    private val PREF_KEY_GAME_USER_HEART_SUBSCRIPTION = "userHeartSubscription"
    private val PREF_KEY_GAME_USER_HEART_GIFT = "userHeartGift"
    private val PREF_KEY_GAME_CHALLENGE_STATE = "gameChallengeState"
    private val PREF_KEY_GAME_HOLD_MSG_SHOW = "gameHoldMsgShow"
    private val PREF_KEY_REFERRAL_POP_UP = "referralPopup"
    private val PREF_KEY_IS_FIRST_DENIAL_PHONE = "isFirstDenialPhonePermission"
    private val PREF_KEY_IS_FIRST_GESTURE: String = "PREF_KEY_IS_FIRST_GESTURE"
    private val PREF_KEY_IS_FIRST_GESTURE_QUESTION_DET: String = "firstGestureQuestionDet"
    private val PREF_KEY_RATING_DIALOG_SHOW= "ratingDialogShow"


    private val mPrefs: SharedPreferences =
        context.getSharedPreferences("preference_name", Context.MODE_PRIVATE)

    fun prefGetCurrentUser(): User {
        return Gson().fromJson(mPrefs.getString(PREF_KEY_USER_INFO, null), User::class.java)
    }

    fun prefLogin() {
        mPrefs.edit().putBoolean(PREF_KEY_IS_LOGIN, true).apply()
        setAuthorized(true)
    }

    fun prefSetToken(token: String?) {
        if (token != null) {
            mPrefs.edit().putString(PREF_KEY_TOKEN, token).apply()
        }
    }

    fun prefGetToken(): String {
        return mPrefs.getString(PREF_KEY_TOKEN, "").toString()
    }

    fun prefSetUserInfo(user: UserInfo?) {
        if (user != null)
            mPrefs.edit().putString(PREF_KEY_USER_INFO, Gson().toJson(user)).apply()
    }

    fun prefGetResumeStudy(): Library? {
        return Gson().fromJson(mPrefs.getString(PREF_KEY_RESUME_STUDY, null), Library::class.java)
    }

    fun prefSetResumeStudy(library: Library) {
        mPrefs.edit().putString(PREF_KEY_RESUME_STUDY, Gson().toJson(library)).apply()
    }

    fun prefGetUserInfo(): UserInfo {
        val userInfoString = mPrefs.getString(PREF_KEY_USER_INFO, "null")
        if (userInfoString != null && userInfoString != "null") {
            return Gson().fromJson(userInfoString, UserInfo::class.java)
        }
        return UserInfo()
    }

    fun prefGetLoginMode(): Boolean {
        return mPrefs.getBoolean(PREF_KEY_IS_LOGIN, false)
    }

    fun prefLogout(context: Context) {
        mPrefs.edit().clear().apply()
        context.startActivity(Intent(context, LoginActivity::class.java))
        (context as BaseActivity).finishAffinity()
        setAuthorized(false)
    }

    fun prefGetModelTestRegistrationResponse(): ModelTestRegistrationResponse? {
        return Gson().fromJson(
            mPrefs.getString(PREF_KEY_MODEL_TEST_REGISTRATION_RESPONSE, null),
            ModelTestRegistrationResponse::class.java
        )
    }

    fun prefSetModelTestRegistrationResponse(modelTestRegistrationResponse: ModelTestRegistrationResponse?) {
        mPrefs.edit().putString(
            PREF_KEY_MODEL_TEST_REGISTRATION_RESPONSE,
            Gson().toJson(modelTestRegistrationResponse)
        ).apply()
    }


    fun prefGetModelTestRegistrationResponseById(id: String): ModelTestRegistrationResponse? {
        return Gson().fromJson(
            mPrefs.getString(PREF_KEY_MODEL_TEST_REGISTRATION_RESPONSE + "_" + id, null),
            ModelTestRegistrationResponse::class.java
        )
    }

    fun prefSetModelTestRegistrationResponseById(
        modelTestRegistrationResponse: ModelTestRegistrationResponse?,
        id: String
    ) {
        mPrefs.edit().putString(
            PREF_KEY_MODEL_TEST_REGISTRATION_RESPONSE + "_" + id,
            Gson().toJson(modelTestRegistrationResponse)
        ).apply()
    }

    fun prefGetModelTestResponseModel(): ModelTestResponseModel? {
        return Gson().fromJson(
            mPrefs.getString(PREF_KEY_MODEL_TEST_RESPONSE_MODEL, null),
            ModelTestResponseModel::class.java
        )
    }

    fun prefSetModelTestResponseModel(modelTestResponseModel: ModelTestResponseModel?) {
        mPrefs.edit()
            .putString(PREF_KEY_MODEL_TEST_RESPONSE_MODEL, Gson().toJson(modelTestResponseModel))
            .apply()
    }

    fun prefGetModelTestResponseModelById(id: String): ModelTest {
        return Gson().fromJson(
            mPrefs.getString(PREF_KEY_MODEL_TEST_RESPONSE_MODEL + "_" + id, null),
            ModelTest::class.java
        )
    }

    fun prefSetModelTestResponseModelById(modelTest: ModelTest?, id: String) {
        mPrefs.edit()
            .putString(
                PREF_KEY_MODEL_TEST_RESPONSE_MODEL + "_" + id,
                Gson().toJson(modelTest)
            )
            .apply()
    }

    fun prefGetModelTestResultSubmitRequestItem(): GameResultSubmitRequestItem? {
        return Gson().fromJson(
            mPrefs.getString(PREF_KEY_MODEL_TEST_SUBMIT_REQUEST_ITEM, null),
            GameResultSubmitRequestItem::class.java
        )
    }

    fun prefSetModelTestResultSubmitRequestItem(gameResultSubmitRequestItem: GameResultSubmitRequestItem?) {
        mPrefs.edit().putString(
            PREF_KEY_MODEL_TEST_SUBMIT_REQUEST_ITEM,
            Gson().toJson(gameResultSubmitRequestItem)
        ).apply()
    }

    fun isAuthorized(): Boolean {
        return mPrefs.getBoolean("is_authorized", false)
    }

    fun setAuthorized(authorized: Boolean) {
        mPrefs.edit().putBoolean("is_authorized", authorized).apply()
    }

    fun setModelTestAttendanceById(authorized: Boolean, id: String) {
        mPrefs.edit().putBoolean(PREF_KEY_MODEL_TEST_ATTENDANCE + "_" + id, authorized).apply()
    }

    fun getModelTestAttendanceById(id: String): Boolean {
        return mPrefs.getBoolean(PREF_KEY_MODEL_TEST_ATTENDANCE + "_" + id, false)
    }

    fun setModelTestParticipationById(authorized: Boolean, id: String) {
        mPrefs.edit().putBoolean(PREF_KEY_MODEL_TEST_PARTICIPATION + "_" + id, authorized).apply()
    }

    fun getModelTestParticipationById(id: String): Boolean {
        return mPrefs.getBoolean(PREF_KEY_MODEL_TEST_PARTICIPATION + "_" + id, false)
    }


    fun saveFcmToken(
        applicationContext: Context?,
        menuItem: String?
    ) {
        val preferences =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = preferences.edit()
        editor.putString("fcmToken", menuItem)
        editor.commit()
    }

    fun getFcmToken(context: Context?): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString("fcmToken", "")
    }

    fun saveDateOfInvite(date: String) {
        mPrefs.edit().putString(PREF_KEY_INVITATION_DATE, date).apply()
    }

    fun getDateOfInvite(): String? {
        return mPrefs.getString(PREF_KEY_INVITATION_DATE, "")
    }

    fun setModeltestid(modelTestId: String) {
        mPrefs.edit().putString(PREF_KEY_MODEL_TEST_ID, modelTestId).apply()
    }

    fun getModelTestId(): String? {
        return mPrefs.getString(PREF_KEY_MODEL_TEST_ID, "")
    }

    fun setGameUserSetting(userSettings: UserSettings) {
        mPrefs.edit().putString(PREF_KEY_USER_SETTINGS, Gson().toJson(userSettings)).apply()
    }

    fun getGameUserSetting(): UserSettings? {
        val userSetting = mPrefs.getString(PREF_KEY_USER_SETTINGS, null)
        if (userSetting.isNullOrEmpty()) return null
        return Gson().fromJson(
            userSetting, UserSettings::class.java
        )
    }

    fun setUserGlobalSetting(userSettings: GlobalSettings) {
        mPrefs.edit().putString(PREF_KEY_USER_GLOBAL_SETTINGS, Gson().toJson(userSettings)).apply()
    }

    fun getUserGlobalSetting(): GlobalSettings? {
        val userSetting = mPrefs.getString(PREF_KEY_USER_GLOBAL_SETTINGS, null)
        if (userSetting.isNullOrEmpty()) return null
        return Gson().fromJson(
            userSetting, GlobalSettings::class.java
        )
    }

    fun prefSetUserGameState(state: GameUserState?) {
        mPrefs.edit().putString(PREF_KEY_GAME_USER_STATE, Gson().toJson(state)).apply()
    }

    fun prefGetUserGameState(): GameUserState? {
        val state = mPrefs.getString(PREF_KEY_GAME_USER_STATE, "")
        return Gson().fromJson(
            state, GameUserState::class.java
        )
    }

    fun prefSetGameCurrentLevel(level: Level?) {
        mPrefs.edit().putString(PREF_KEY_GAME_USER_LEVEL, Gson().toJson(level)).apply()
    }

    fun prefGetGameCurrentLevel(): Level? {
        val level = mPrefs.getString(PREF_KEY_GAME_USER_LEVEL, "")
        return Gson().fromJson(
            level, Level::class.java
        )
    }

    fun prefSetUserHeart(userHearts: String) {
        var insertVal = userHearts
        if (userHearts.toInt() < 0) insertVal = "0"
        mPrefs.edit().putString(PREF_KEY_GAME_USER_HEART, insertVal).apply()
    }

    fun prefGetUserHeart(): String? {
        return mPrefs.getString(PREF_KEY_GAME_USER_HEART, "0")
    }

    fun setIsMusicOn(isOn: Boolean) {
        mPrefs.edit().putBoolean(PREF_KEY_GAME_BG_MUSIC, isOn).apply()
    }

    fun getIsMusicOn(): Boolean {
        return mPrefs.getBoolean(PREF_KEY_GAME_BG_MUSIC, false)
    }

    fun prefSetUserAdLimit(userAdLimit: String?) {
        mPrefs.edit().putString(PREF_KEY_GAME_USER_AD_LIMIT, userAdLimit).apply()
    }

    fun prefGetUserAdLimit(): String? {
        return mPrefs.getString(PREF_KEY_GAME_USER_AD_LIMIT, "0")
    }

    fun prefSetIsAdFree(adsFree: Boolean?) {
        mPrefs.edit().putBoolean(PREF_KEY_GAME_USER_AD_FREE, adsFree ?: false).apply()
    }

    fun prefGetIsAdFree(): Boolean {
        return mPrefs.getBoolean(PREF_KEY_GAME_USER_AD_FREE, false)
    }

    fun prefSetCurrentSubscription(subscription: HeartSubscription?) {
        mPrefs.edit().putString(PREF_KEY_GAME_USER_HEART_SUBSCRIPTION, Gson().toJson(subscription))
            .apply()
    }


    fun prefSetHeartGift(gift: HeartGift?) {
        mPrefs.edit().putString(PREF_KEY_GAME_USER_HEART_GIFT, Gson().toJson(gift))
            .apply()
    }

    fun prefGetCurrentSubscription(): HeartSubscription? {
        val subscription = mPrefs.getString(PREF_KEY_GAME_USER_HEART_SUBSCRIPTION, null)
        return Gson().fromJson(
            subscription, HeartSubscription::class.java
        )
    }

    fun prefGetCurrentHeartGift(): HeartGift? {
        val gift = mPrefs.getString(PREF_KEY_GAME_USER_HEART_GIFT, null)
        return Gson().fromJson(
            gift, HeartGift::class.java
        )
    }

    fun prefSetChallengeState(challengeState: GameChallengeState?) {
        var value = if (challengeState == null) null else Gson().toJson(challengeState)
        mPrefs.edit().putString(PREF_KEY_GAME_CHALLENGE_STATE, value)
            .apply()
    }

    fun prefGetChallengeState(): GameChallengeState? {
        val challengeState = mPrefs.getString(PREF_KEY_GAME_CHALLENGE_STATE, null)
        return Gson().fromJson(
            challengeState, GameChallengeState::class.java
        )
    }

    fun setReferenceCode(referenceToken: String?) {
        mPrefs.edit().putString(PREF_KEY_REFERENCE_TOKEN, referenceToken).apply()
    }

    fun getReferenceToken(): String? {
        return mPrefs.getString(PREF_KEY_REFERENCE_TOKEN, null)
    }

    fun setGameMsgShow() {
        mPrefs.edit().putBoolean(PREF_KEY_GAME_HOLD_MSG_SHOW, false).apply()
    }

    fun getGameMsgShow(): Boolean {
        return mPrefs.getBoolean(PREF_KEY_GAME_HOLD_MSG_SHOW, true)
    }

    fun setNoticeTracking(noticeTrack: NoticeTracking) {
        mPrefs.edit().putString(PREF_KEY_REFERENCE_NOTICE, Gson().toJson(noticeTrack)).apply()
    }

    fun getNoticeTracking(): NoticeTracking? {
        val notice = mPrefs.getString(PREF_KEY_REFERENCE_NOTICE, null)
        return Gson().fromJson(
            notice, NoticeTracking::class.java
        )
    }

    fun prefNavigateFromGame(game: Boolean) {
        mPrefs.edit().putBoolean(PREF_KEY_REFERENCE_NAVIGATE_FROM_GAME, game).apply()
    }

    fun prefIsNavigateFromGame(): Boolean {
        return mPrefs.getBoolean(PREF_KEY_REFERENCE_NAVIGATE_FROM_GAME, false)
    }

    fun prefSetPrefRefViewCount(count: String) {
        mPrefs.edit().putString(PREF_KEY_REFERRAL_POP_UP, count).apply()
    }

    fun prefGetPrefRefViewCount(): String? {
        return mPrefs.getString(PREF_KEY_REFERRAL_POP_UP, "")
    }

    fun prefSetIsFirstPhonePermDenial(isDenied: Boolean) {
        mPrefs.edit().putBoolean(PREF_KEY_IS_FIRST_DENIAL_PHONE, isDenied).apply()
    }

    fun prefGetIsFirstPhonePermDenial(): Boolean {
        return mPrefs.getBoolean(PREF_KEY_IS_FIRST_DENIAL_PHONE, true)
    }

    fun prefSetIsFirstTimeGesture(isGesture: String) {
        mPrefs.edit().putString(PREF_KEY_IS_FIRST_GESTURE, isGesture).apply()
    }

    fun prefGetIsFirstTimeGesture(): String? {
        return mPrefs.getString(PREF_KEY_IS_FIRST_GESTURE, "0")
    }

    fun prefGetIsFirstTimeGestureQuestionDetails() : Boolean {
        return mPrefs.getBoolean(PREF_KEY_IS_FIRST_GESTURE_QUESTION_DET, true)
    }

    fun prefSetIsFirstTimeGestureQuestionDetails(value : Boolean) {
        mPrefs.edit().putBoolean(PREF_KEY_IS_FIRST_GESTURE_QUESTION_DET, value).apply()
    }

    fun prefGetRewardFirstTime(): String? {
        return mPrefs.getString("isRewardFirstTime", "0")
    }

    fun prefSetRewardFirstTime(isFirstTime: String) {
        mPrefs.edit().putString("isRewardFirstTime", "1")
    }

    fun saveDateOfRatingShow(date: String) {
        mPrefs.edit().putString(PREF_KEY_RATING_DATE, date).apply()
    }

    fun getDateOfRatingShow(): String? {
        return mPrefs.getString(PREF_KEY_RATING_DATE, "")
    }

    fun prefSetRatingDialogShow(shouldShow: Boolean) {
        mPrefs.edit().putBoolean(PREF_KEY_RATING_DIALOG_SHOW, shouldShow).apply()
    }

    fun prefGetRatingDialogShow(): Boolean {
        return mPrefs.getBoolean(PREF_KEY_RATING_DIALOG_SHOW, true)
    }

}