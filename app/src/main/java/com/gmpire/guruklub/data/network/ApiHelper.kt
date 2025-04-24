package com.gmpire.guruklub.data.network

import android.graphics.Bitmap
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.data.model.QuestionRequest
import com.gmpire.guruklub.data.model.game.GameResultSubmitRequestItem
import com.gmpire.guruklub.data.model.gameheartpackages.GameLevelOverRequestItem
import com.gmpire.guruklub.data.model.gameheartpackages.GameLevelUpRequestItem
import com.gmpire.guruklub.data.model.gameheartpackages.GamePauseUserStateRequestItem
import com.gmpire.guruklub.data.model.gamequestions.GameChallengeFinishRequestItem
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.network.api_call_factory.ApiGetCall
import com.gmpire.guruklub.data.network.api_call_factory.ApiPostCall
import com.gmpire.guruklub.data.network.api_call_factory.ApiPostCallWithDocument
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import io.reactivex.Maybe
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import java.io.ByteArrayOutputStream


class ApiHelper(apiService: IApiService) {


    val apiService = apiService

    //call type
    val CALL_TYPE_GET = "get"
    val CALL_TYPE_POST = "post"
    val CALL_TYPE_POST_WITH_DOCUMENT = "post with document"

    //endpoint
    val ENDPOINT_LOGIN = "user/login"
    val ENDPOINT_GET_ALL_CATEGORY = "blog/getAllCategories"
    val ENDPOINT_REGISTRATION = "user/register"

    //api method field key
    val KEY_USER_NAME = "username"
    val KEY_PASSWORD = "password"

    val KEY_FULL_NAME = "full_name"
    val KEY_EMAIL = "email"
    val KEY_CONFIRM_PASSWORD = "confirm_password"
    val KEY_USER_TYPE = "user_type"

    val KEY_CATEGORY_ID = "category_id"

    fun apiLoginRequest(email: String, password: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap["email"] = email
        hashMap["password"] = password
        getApiCallObservable(CALL_TYPE_POST, "user/login", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiRegistrationRequest(
        email: String,
        password: String,
        confirm_password: String,
        toc: Int,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("email", email)
        hashMap.put("password", password)
        hashMap.put("confirm_password", confirm_password)
        hashMap.put("toc", toc.toString())
        getApiCallObservable(CALL_TYPE_POST, "user/register", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiEmailVerification(email: String, code: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap.put("email", email)
        hashMap.put("code", code)
        getApiCallObservable(CALL_TYPE_POST, "user/email-verification", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetGameQuestions(
        category_id: String,
        game_type_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("category_id", category_id)
        hashMap.put("game_type_id", game_type_id)
        getApiCallObservable(CALL_TYPE_POST, "game-question", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetCustomGameQuestions(
        category_id: String,
        game_type_id: String,
        question_number: Int,
        subject_ids: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, Any>()
        hashMap.put("category_id", category_id)
        hashMap.put("question_number", question_number)
        hashMap.put("subject_id", subject_ids)
        hashMap.put("game_type_id", game_type_id)
        getApiCallObservable(CALL_TYPE_POST, "custom-game-question", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiFbLogin(
        currentUser: FirebaseUser,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["fb_id"] = currentUser.uid
        hashMap["name"] = currentUser.displayName.toString()
        currentUser.email?.let { hashMap.put("email", currentUser.email.toString()) }
        currentUser.phoneNumber?.let { hashMap.put("phone", currentUser.phoneNumber.toString()) }
//        hashMap.put("gender", currentUser.pr)
        getApiCallObservable(CALL_TYPE_POST, "facebook-login", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGoogleLogin(
        currentUser: FirebaseUser,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["google_id"] = currentUser.uid
        hashMap["name"] = currentUser.displayName.toString()
        currentUser.email?.let { hashMap.put("email", currentUser.email.toString()) }
        currentUser.phoneNumber?.let { hashMap.put("phone", currentUser.phoneNumber.toString()) }
        getApiCallObservable(CALL_TYPE_POST, "google-login", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiDeviceFcmToken(
        device_id: String,
        model: String,
        manufacture: String,
        version: String?,
        device_unique_id: String,
        fcm_token: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("device_id", device_id)
        hashMap.put("model", model)
        hashMap.put("manufacture", manufacture)
        hashMap.put("version", version.toString())
        hashMap.put("device_unique_id", device_unique_id)
        hashMap.put("fcm_token", fcm_token)
        hashMap.put("app_version", BuildConfig.VERSION_CODE.toString())
        getApiCallObservable(CALL_TYPE_POST, "user/device-info", hashMap).subscribe(
            apiCallbackHelper
        )
    }


    fun apiGameResult(
        gameItem: GameResultSubmitRequestItem,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val raw = Gson().toJson(gameItem).toString()

        apiService.postRequestForRaw(
            "game-finish",
            raw.toRequestBody(MediaType?.let { "text/plain".toMediaTypeOrNull() })
        ).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGameResultByGameId(
        game_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["game_id"] = game_id
        getApiCallObservable(CALL_TYPE_GET, "game-result-summary", hashMap).subscribe(
            apiCallbackHelper
        )
    }


    fun apiProfileSetup(
        name: String,
        display_name: String,
        email: String,
        phone: String,
        gender: Int,
        dob: String,
        picture: Bitmap?,
        reg_type: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {

        var image: MultipartBody.Part? = null


        if (picture != null) {

            val hashMap = HashMap<String, RequestBody>()
            if (picture != null) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                picture.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    byteArrayOutputStream
                );
                val requestBody = RequestBody.create(
                    "image/jpeg".toMediaTypeOrNull(),
                    byteArrayOutputStream.toByteArray()
                )
                hashMap.put(
                    "picture\"; filename=\"image_" + System.currentTimeMillis() + ".jpg\"",
                    requestBody
                )
            }
            hashMap.put(
                "name",
                name.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            hashMap.put(
                "display_name",
                display_name.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            if (reg_type != "2") {
                hashMap.put(
                    "phone",
                    phone.toRequestBody("text/plain".toMediaTypeOrNull())
                )
            }
            hashMap.put(
                "gender",
                gender.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            )
            hashMap.put(
                "dob",
                dob.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            if (email.isNotEmpty()) {
                if (reg_type != "1") {
                    hashMap.put(
                        "email",
                        email.toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                }
            }
            getApiCallObservable(
                CALL_TYPE_POST_WITH_DOCUMENT,
                "user/profile-setup",
                hashMap
            ).subscribe(
                apiCallbackHelper
            )
        } else {
            val hashMap = HashMap<String, String>()
            hashMap.put("name", name)
            hashMap.put("display_name", display_name)
            if (reg_type != "2") {
                hashMap.put("phone", phone)
            }
            hashMap.put("gender", gender.toString())
            hashMap.put("dob", dob)
            if (reg_type != "1") {
                if (email.isNotEmpty()) {
                    hashMap.put("email", email)
                }
            }

            getApiCallObservable(CALL_TYPE_POST, "user/profile-setup", hashMap).subscribe(
                apiCallbackHelper
            )

        }


        //  getApiCallObservable(CALL_TYPE_POST_WITH_DOCUMENT, "user/profile-setup", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiPhoneVerification(phone: String, code: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap.put("phone", phone)
        hashMap.put("code", code)
        getApiCallObservable(CALL_TYPE_POST, "user/phone-verification", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiPhoneVerificationResend(phone: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap.put("phone", phone)
        getApiCallObservable(CALL_TYPE_POST, "user/phone-resend", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiEmailVerificationResend(email: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap.put("email", email)
        getApiCallObservable(CALL_TYPE_POST, "user/email-resend", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetTermsAndCondition(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "user/toc", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiGetContent(slug: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap.put("slug", slug)
        getApiCallObservable(CALL_TYPE_GET, "get-content", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiGetFindUs(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "find-us", hashMap).subscribe(apiCallbackHelper)
    }


    fun apiContactUs(email: String, message: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap.put("email", email)
        hashMap.put("message", message)
        getApiCallObservable(CALL_TYPE_POST, "contact-us", hashMap).subscribe(apiCallbackHelper)
    }


    fun apiInviteFriends(email: String, emails: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap.put("email", email)
        hashMap.put("emails", emails)
        getApiCallObservable(CALL_TYPE_POST, "invite-friends", hashMap).subscribe(apiCallbackHelper)
    }


    fun apiForgetPassword(email: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap.put("email", email)
        getApiCallObservable(
            CALL_TYPE_POST,
            "forgot-password",
            hashMap
        ).subscribe(apiCallbackHelper)
    }

    fun apiSubjectPerformance(gameId: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap.put("game_id", gameId)
        getApiCallObservable(
            CALL_TYPE_GET,
            "subject-performance",
            hashMap
        ).subscribe(apiCallbackHelper)
    }


    fun apiForgetPasswordCodeVerify(
        email: String,
        code: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("email", email)
        hashMap.put("code", code)
        getApiCallObservable(CALL_TYPE_POST, "code-verification", hashMap).subscribe(
            apiCallbackHelper
        )
    }


    fun apiChangePassword(
        email: String,
        password: String,
        confirm_password: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("email", email)
        hashMap.put("password", password)
        hashMap.put("confirm_password", confirm_password)
        getApiCallObservable(
            CALL_TYPE_POST,
            "change-password",
            hashMap
        ).subscribe(apiCallbackHelper)
    }

    fun apiGetCategoryList(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "category-list", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiGetSubjectListByCategory(
        categoryId: String,
        slug: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("category_id", categoryId)
        hashMap.put("slug", slug)
        getApiCallObservable(CALL_TYPE_GET, "subject-by-category", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiSetCategoryAndSubject(
        category_id: String,
        subject_ids: String,
        notification: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("category_id", category_id.toString())
        hashMap.put("subject_id", subject_ids)
        hashMap.put("notification", notification)
        getApiCallObservable(CALL_TYPE_POST, "user/category-setup", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetQuestionListForBatch(
        user_id: String?,
        category_id: String,
        subject_ids: String,
        section_ids: String,
        topic_ids: String,
        batch_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("user_id", user_id.toString())
        hashMap.put("category_id", category_id)
        hashMap.put("subject_id", subject_ids)
        hashMap.put("section_id", section_ids)
        hashMap.put("topic_id", topic_ids)
        hashMap.put("batch_id", batch_id)
        hashMap.put("question_num", "all")
        getApiCallObservable(CALL_TYPE_POST, "question-list", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiGetQuestionListForHome(
        user_id: String?,
        category_id: String,
        subject_ids: String,
        section_ids: String,
        topic_ids: String,
        batch_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("user_id", user_id.toString())
        hashMap.put("category_id", category_id)
        hashMap.put("subject_id", subject_ids)
        hashMap.put("section_id", section_ids)
        hashMap.put("topic_id", topic_ids)
        hashMap.put("batch_id", batch_id)
        getApiCallObservable(CALL_TYPE_POST, "question-list", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiGetOfflineQuestionListForHome(
        category_id: String,
        subject_ids: String,
        section_ids: String,
        topic_ids: String,
        batch_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("category_id", category_id)
        hashMap.put("subject_id", subject_ids)
        hashMap.put("section_id", section_ids)
        hashMap.put("topic_id", topic_ids)
        hashMap.put("batch_id", batch_id)
        getApiCallObservable(CALL_TYPE_POST, "question-list-offline", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetQuestionById(
        user_id: String?,
        category_id: String,
        question_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        if (!user_id.isNullOrEmpty())
            hashMap.put("user_id", user_id.toString())
        if (!category_id.isNullOrEmpty())
            hashMap.put("category_id", category_id)
        else
            hashMap.put("category_id", "1")

        hashMap.put("question_id", question_id)
        getApiCallObservable(CALL_TYPE_POST, "question-details", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetSectionsBySubject(
        subject_ids: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("subject_id", subject_ids)
        getApiCallObservable(CALL_TYPE_POST, "get-section-by-subject", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetTopicBySection(
        section_ids: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("section_id", section_ids)
        getApiCallObservable(CALL_TYPE_POST, "get-topic-by-section", hashMap).subscribe(
            apiCallbackHelper
        )
    }


    fun apiGetAllNotifications(
        userId: String,
        page: String,
        category_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("user_id", userId)
        hashMap.put("page", page)
        hashMap.put("category_id", category_id)
        getApiCallObservable(CALL_TYPE_POST, "get-all-notification", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetNewsById(
        newsId: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("news_id", newsId)
        getApiCallObservable(CALL_TYPE_POST, "get-news-details", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetNewsCategory(
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "get-news-category", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetNews(
        page: String,
        category_id: String,
        search_key: String,
        dateFrom: String,
        dateTo: String,
        is_popular: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["page"] = page
        hashMap["category_id"] = category_id
        hashMap["search_key"] = search_key
        hashMap["is_popular"] = is_popular
        hashMap["date_from"] = dateFrom      //"2020-03-10"
        hashMap["date_to"] = dateTo         //"2020-11-10"
        getApiCallObservable(CALL_TYPE_POST, "get-news-list", hashMap).subscribe(
            apiCallbackHelper
        )
    }


    fun apiGetCheckUpdateVersion(
        appVersion: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["app_version"] = appVersion
        getApiCallObservable(
            CALL_TYPE_POST,
            "user/version_check",
            hashMap
        ).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetGameSubjectSectionTopic(
        game_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("game_id", game_id)
        getApiCallObservable(CALL_TYPE_GET, "game-subject-section-topic", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetSubjectByCategory(
        categoryId: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        val refinedID = if (categoryId.isEmpty()) {
            "1"
        } else {
            categoryId
        }
        hashMap.put("category_id", refinedID)
        getApiCallObservable(CALL_TYPE_GET, "subject-by-category", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetBatchByCategory(
        categoryId: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("category_id", categoryId)
        getApiCallObservable(CALL_TYPE_GET, "get-batch", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetSAllCategory(
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "category-list", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiReportAboutQuestion(
        question_id: String,
        type: String,
        details: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("question_id", question_id)
        hashMap.put("type", type)
        hashMap.put("details", details)
        getApiCallObservable(CALL_TYPE_POST, "question/reports", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetGameChallenges(
        slug: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("slug", slug)
        getApiCallObservable(CALL_TYPE_GET, "game-challenge", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetPlayedModelTestItems(
        category_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["category_id"] = category_id
        getApiCallObservable(CALL_TYPE_GET, "get-played-model-test", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetLeaderboard(
        model_test_id: String,
        page: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["model_test_id"] = model_test_id
        hashMap["page"] = page
        getApiCallObservable(CALL_TYPE_POST, "get-leaderboard", hashMap).subscribe(
            apiCallbackHelper
        )
    }


    fun apiGetCheckModelTest(
        userId: String,
        category_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["user_id"] = userId
        hashMap["category_id"] = category_id
        getApiCallObservable(CALL_TYPE_GET, "check-model-test", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetTime(
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "server-time", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiModelTestRegister(
        userId: String,
        model_test_id: String,
        exam_status : String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["user_id"] = userId
        hashMap["model_test_id"] = model_test_id
        hashMap["exam_status"] = exam_status
        getApiCallObservable(CALL_TYPE_POST, "model-test-register", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiModelTestRegistrationPayment(
        model_test_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["model_test_id"] = model_test_id
        getApiCallObservable(CALL_TYPE_POST, "purchase-live-exam", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetLibrary(
        userId: String,
        filterValues: FilterValues,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        if (filterValues.category_id.isNullOrEmpty()) {
            filterValues.category_id = "1"
        }

        hashMap.put("category_id", filterValues.category_id.toString())
        hashMap.put("subject_id", filterValues.subject_id.toString())
        hashMap.put("section_id", filterValues.section_id.toString())
        hashMap.put("topic_id", filterValues.topic_id.toString())
        hashMap["user_id"] = userId
        getApiCallObservable(CALL_TYPE_POST, "library-data", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiGetQuestionLibrary(
        userId: String,
        filterValues: FilterValues,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("user_id", userId)
        if (filterValues.category_id.isNullOrEmpty()) {
            filterValues.category_id = "1"
        }
        hashMap.put("category_id", filterValues.category_id.toString())
        hashMap.put("subject_id", filterValues.subject_id.toString())
        hashMap.put("section_id", filterValues.section_id.toString())
        hashMap.put("topic_id", filterValues.topic_id.toString())
        hashMap.put("question_num", "all")
        getApiCallObservable(CALL_TYPE_POST, "question-list-lib", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetQuestionLibraryPage(
        userId: String,
        filterValues: FilterValues,
        pageNum: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("user_id", userId)
        if (filterValues.category_id.isNullOrEmpty()) {
            filterValues.category_id = "1"
        }
        hashMap.put("category_id", filterValues.category_id.toString())
        hashMap.put("subject_id", filterValues.subject_id.toString())
        hashMap.put("section_id", filterValues.section_id.toString())
        hashMap.put("topic_id", filterValues.topic_id.toString())
        hashMap.put("question_num", pageNum)
        getApiCallObservable(CALL_TYPE_POST, "question-list-lib-page", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetFavouriteQuestion(
        page: String,
        filterValues: FilterValues,
        userId: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, Long>()
        hashMap["page"] = page.toLong()

        if (!userId.isNullOrEmpty()) {
            hashMap["user_id"] = userId.toLong()
        }

        if (filterValues.category_id.isNullOrEmpty()) {
            filterValues.category_id = "1"
        }

        hashMap["category_id"] = filterValues.category_id.toLong() ?: 1

        if (!filterValues.subject_id.isNullOrEmpty() && filterValues.subject_id != "0") {
            hashMap["subject_id"] = filterValues.subject_id.toLong() ?: -1
        }
        if (!filterValues.section_id.isNullOrEmpty() && filterValues.section_id != "0") {
            hashMap["section_id"] = filterValues.section_id.toLong() ?: -1
        }
        if (!filterValues.topic_id.isNullOrEmpty() && filterValues.topic_id != "0") {
            hashMap["topic_id"] = filterValues.topic_id.toLong() ?: -1
        }
        if (!filterValues.difficulty_id.isNullOrEmpty() && filterValues.difficulty_id != "0") {
            hashMap["difficulty"] = filterValues.difficulty_id.toLong() ?: -1
        }
        if (!filterValues.batch_id.isNullOrEmpty() && filterValues.batch_id != "0") {
            hashMap["batch_id"] = filterValues.batch_id.toLong() ?: -1
        }
        getApiCallObservable(CALL_TYPE_POST, "get-all-favourite-question", hashMap).subscribe(
            apiCallbackHelper
        )
    }


    fun apiGetGameSolution(
        page: String,
        userId: String,
        game_id: String,
        slug: String,
        filterValues: FilterValues,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("page", page)
        hashMap.put("user_id", userId)
        hashMap.put("game_id", game_id)
        hashMap.put("slug", slug)

        if (filterValues.type != null && filterValues.type != "0") {
            hashMap.put("type", filterValues.type ?: "")
        }
        if (!filterValues.subject_id.isNullOrEmpty() && filterValues.subject_id != "0") {
            hashMap.put("subject_id", filterValues.subject_id ?: "")
        }
        if (!filterValues.section_id.isNullOrEmpty() && filterValues.section_id != "0") {
            hashMap.put("section_id", filterValues.section_id ?: "")
        }
        if (!filterValues.topic_id.isNullOrEmpty() && filterValues.topic_id != "0") {
            hashMap.put("topic_id", filterValues.topic_id ?: "")
        }
        if (!filterValues.batch_id.isNullOrEmpty() && filterValues.batch_id != "0") {
            hashMap.put("batch_id", filterValues.batch_id ?: "")
        }
        if (!filterValues.difficulty_id.isNullOrEmpty() && filterValues.difficulty_id != "0") {
            hashMap.put("difficulty", filterValues.difficulty_id ?: "")
        }

        getApiCallObservable(CALL_TYPE_POST, "get-game-solution", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiAddQuestion(
        questionRequest: QuestionRequest,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, RequestBody>()
        if (questionRequest.picture != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            questionRequest.picture?.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                byteArrayOutputStream
            );
            val requestBody = RequestBody.create(
                "image/jpeg".toMediaTypeOrNull(),
                byteArrayOutputStream.toByteArray()
            )
            hashMap.put(
                "picture\"; filename=\"image_" + System.currentTimeMillis() + ".jpg\"",
                requestBody
            )
        }
        hashMap.put(
            "category_id",
            questionRequest.filterValues.category_id.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
        )
        hashMap.put(
            "topic_id",
            questionRequest.filterValues.topic_id.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
        )
        hashMap.put(
            "difficulty",
            questionRequest.filterValues.difficulty_id.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
        )
        hashMap.put(
            "title",
            questionRequest.title.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        )
        hashMap.put(
            "option_a",
            RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                questionRequest.option_a.toString()
            )
        )
        hashMap.put(
            "option_b",
            RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                questionRequest.option_b.toString()
            )
        )
        hashMap.put(
            "option_c",
            RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                questionRequest.option_c.toString()
            )
        )
        hashMap.put(
            "option_d",
            RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                questionRequest.option_d.toString()
            )
        )
        hashMap.put(
            "answer",
            RequestBody.create("text/plain".toMediaTypeOrNull(), questionRequest.answer.toString())
        )
        hashMap.put(
            "answer_explain",
            RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                questionRequest.answer_explain.toString()
            )
        )
        getApiCallObservable(CALL_TYPE_POST_WITH_DOCUMENT, "question-add", hashMap)
            .subscribe(
                apiCallbackHelper
            )

    }


    fun apiBookmarkQuestion(
        question_id: String,
        category_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("question_id", question_id)
        hashMap.put("category_id", category_id)
        getApiCallObservable(CALL_TYPE_POST, "question/bookmark", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiUnBookmarkQuestion(
        question_id: String,
        category_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("question_id", question_id)
        hashMap.put("category_id", category_id)
        getApiCallObservable(CALL_TYPE_POST, "question/unbookmark", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiRecentlyLearn(
        category_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("category_id", category_id)
        getApiCallObservable(CALL_TYPE_GET, "recently-learn", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiPerformanceHistory(
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "performance-history", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiPerformanceSummery(
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "game-performance-summary", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiMostPopular(
        category_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        if (category_id.isNullOrEmpty()) {
            hashMap["category_id"] = "1"
        } else {
            hashMap["category_id"] = category_id
        }

        getApiCallObservable(CALL_TYPE_GET, "most-popular", hashMap).subscribe(apiCallbackHelper)
    }

    fun <T> getApiCallObservable(
        callType: String,
        path: String,
        hashMap: HashMap<String, T>
    ): Maybe<Response<ResponseBody>> {
        return when (callType) {
            CALL_TYPE_GET -> {
                ApiGetCall().getMaybeObserVable(apiService, path, hashMap)
            }
            CALL_TYPE_POST -> {
                ApiPostCall().getMaybeObserVable(apiService, path, hashMap)
            }
            else -> {
                ApiPostCallWithDocument().getMaybeObserVable(apiService, path, hashMap)
            }
        }
    }

    fun apiGetQuestionsByKeyword(
        userId: String,
        keyword: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["user_id"] = userId
        hashMap["keyword"] = keyword
        getApiCallObservable(CALL_TYPE_POST, "get-questions-by-keyword-search", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetQuestionsByKeywords(
        userId: String,
        keyword: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["user_id"] = userId
        hashMap["keyword"] = keyword
        getApiCallObservable(CALL_TYPE_POST, "get-questions-by-keyword", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetOverallPerformance(
        userId: Long,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, Long>()
        hashMap["user_id"] = userId
        getApiCallObservable(
            CALL_TYPE_POST,
            "get-subject-based-overall-performance",
            hashMap
        ).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetYoutubeSuggestedList(
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(
            CALL_TYPE_GET,
            "https://www.googleapis.com/youtube/v3/search?part=snippet&relatedToVideoId=pS-gbqbVd8c&type=video&key=AIzaSyD7p38ADJFmAVUBPzOoFayhMmhGm2aH5eo",
            hashMap
        ).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetGameLevel(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "level-list", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiGetQuestionListForGame(
        categoryId: String,
        gameLevelId: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["category_id"] = categoryId
        hashMap["game_level_id"] = gameLevelId
        getApiCallObservable(CALL_TYPE_POST, "guruklub-game-questions", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun getLimitedHeartPackage(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "limited-hearts-packages", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun getUnlimitedHeartPackage(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "unlimited-hearts-packages", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun getRefToken(apiCallbackHelper: ApiCallbackHelper) {
        getApiCallObservable(
            CALL_TYPE_POST,
            "social_share",
            HashMap<String, String>()
        ).subscribe(apiCallbackHelper)
    }


    fun getHeartSettings(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "hearts-settings", hashMap).subscribe(apiCallbackHelper)
    }

    fun updateAdHearts(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_POST, "ads-hearts", hashMap).subscribe(apiCallbackHelper)
    }


    fun apiGameLevelOver(
        data: GameLevelOverRequestItem,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val raw = Gson().toJson(data).toString()
        apiService.postRequestForRaw(
            "level-over",
            raw.toRequestBody(MediaType.let { "text/plain".toMediaTypeOrNull() })
        ).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGameLevelUp(
        data: GameLevelUpRequestItem,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val raw = Gson().toJson(data).toString()
        apiService.postRequestForRaw(
            "level-up",
            raw.toRequestBody(MediaType.let { "text/plain".toMediaTypeOrNull() })
        ).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGamePauseGameState(
        data: GamePauseUserStateRequestItem,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val raw = Gson().toJson(data).toString()

        apiService.postRequestForRaw(
            "level-resume",
            raw.toRequestBody(MediaType.let { "text/plain".toMediaTypeOrNull() })
        ).subscribe(
            apiCallbackHelper
        )
    }


    fun apiMilestoneChallenge(
        categoryId: String,
        challengeId: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["category_id"] = categoryId
        hashMap["challenge_id"] = challengeId
        getApiCallObservable(CALL_TYPE_POST, "milestone-challenge", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiHeartsUpdate(
        type: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["heart_variable"] = type
        getApiCallObservable(CALL_TYPE_POST, "hearts-variation", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun getGameRules(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "game-rules", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiGetAdFreePaymentInfo(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap["ads_pricing_id"] = "1"
        getApiCallObservable(CALL_TYPE_POST, "subscribe-ads", hashMap).subscribe(apiCallbackHelper)
    }

    fun getAdsPricingList(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(
            CALL_TYPE_GET,
            "ads-pricing-list",
            hashMap
        ).subscribe(apiCallbackHelper)
    }

    fun getHeartAddPayment(packageID: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap["package_id"] = packageID
        getApiCallObservable(
            CALL_TYPE_POST,
            "purchase-hearts",
            hashMap
        ).subscribe(apiCallbackHelper)
    }


    fun getPaymentExecution(
        status: String,
        tranDate: String,
        tranId: String,
        valId: String,
        amount: String,
        storeAmount: String,
        bankTranId: String,
        cardType: String,
        cardNo: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["status"] = status
        hashMap["tran_date"] = tranDate
        hashMap["tran_id"] = tranId
        hashMap["val_id"] = valId
        hashMap["amount"] = amount
        hashMap["store_amount"] = storeAmount
        hashMap["bank_tran_id"] = bankTranId
        hashMap["card_type"] = cardType
        hashMap["card_no"] = cardNo
        getApiCallObservable(CALL_TYPE_POST, "payment-execution", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiMilestoneChallengeDone(
        gameChallengeRequest: GameChallengeFinishRequestItem?,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val raw = Gson().toJson(gameChallengeRequest).toString()
        apiService.postRequestForRaw(
            "milestone-challenge-finish",
            raw.toRequestBody(MediaType.let { "text/plain".toMediaTypeOrNull() })
        ).subscribe(apiCallbackHelper)
    }

    fun apiGetUserInfo(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "game-user-info", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiSendPromoCode(
        promoCode: String,
        deviceId: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        if (!promoCode.isNullOrEmpty()) hashMap["referral_code"] = promoCode
        hashMap["device_unique_id"] = deviceId
        getApiCallObservable(CALL_TYPE_POST, "invite_hearts", hashMap).subscribe(apiCallbackHelper)
    }

    fun getFailedTransaction(status: String, errorMsg : String, tranId: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap["status"] = status
        hashMap["failedreason"] = errorMsg
        hashMap["tran_id"] = tranId
        getApiCallObservable(CALL_TYPE_POST, "payment-execution", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiSetGameSound(
        backgroundMusic: String,
        gameBtnSound: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["game_background_music"] = backgroundMusic
        hashMap["game_sound"] = gameBtnSound
        getApiCallObservable(CALL_TYPE_POST, "game-user-settings", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetNoticeList(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "noticeboard", hashMap).subscribe(apiCallbackHelper)
    }

    fun apiGetAllVideos(
        categoryId: String,
        subject_id: String,
        section_id: String,
        topic_id: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["category_id"] = categoryId
        hashMap["subject_id"] = subject_id
        if (section_id != "null" && section_id != "0")
            hashMap["section_id"] = section_id
        if (topic_id != "null" && topic_id != "0")
            hashMap["topic_id"] = topic_id
        getApiCallObservable(CALL_TYPE_POST, "library-videos", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiAddVideos(
        categoryId: String,
        topicId: String,
        videoTitle: String,
        videoUrl: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["category_id"] = categoryId
        hashMap["topic_id"] = topicId
        hashMap["video_title"] = videoTitle
        hashMap["video_url"] = videoUrl
        getApiCallObservable(CALL_TYPE_POST, "video-add", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiSendVideoReport(videoID: String, details: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap["video_id"] = videoID
        hashMap["details"] = details
        getApiCallObservable(CALL_TYPE_POST, "video-report", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetResumeable(
        subjectId: String,
        sectionId: String,
        topicId: String,
        categoryId: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["subject_id"] = subjectId
        hashMap["section_id"] = sectionId
        hashMap["topic_id"] = topicId
        hashMap["category_id"] = categoryId
        getApiCallObservable(CALL_TYPE_POST, "topic-resume", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiShowResumeAbleState(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "last-seen-topic", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetQuestionLibraryPageForResumeable(
        userId: String,
        filterValues: FilterValues,
        page: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap.put("user_id", userId)
        if (filterValues.category_id.isNullOrEmpty()) {
            filterValues.category_id = "1"
        }
        hashMap.put("category_id", filterValues.category_id)
        hashMap.put("subject_id", filterValues.subject_id)
        hashMap.put("section_id", filterValues.section_id)
        hashMap.put("topic_id", filterValues.topic_id)
        hashMap.put("question_num", page)
        getApiCallObservable(CALL_TYPE_POST, "question-list-lib-page", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun getFriendSuggestion(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "friend-suggestions", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun sendFriendRequest(userId: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap["requested_user"] = userId
        getApiCallObservable(CALL_TYPE_POST, "send-friend-request", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiPracticeRewardHeart(
        heartVariable: String,
        practice: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["heart_variable"] = heartVariable
        hashMap["practice"] = practice
        getApiCallObservable(CALL_TYPE_POST, "hearts-variation", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiModelTestParticipation(
        model_test_id: String,
        userId: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["model_test_id"] = model_test_id
        hashMap["user_id"] = userId
        getApiCallObservable(CALL_TYPE_POST, "model-test-participate", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun searchFriend(keywords: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap["search_key"] = keywords
        getApiCallObservable(CALL_TYPE_GET, "friend-suggestions", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun getFriend(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        getApiCallObservable(CALL_TYPE_GET, "friend-list", hashMap).subscribe(apiCallbackHelper)
    }

    fun unFriend(userId: String,apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap["friend_id"] = userId
        getApiCallObservable(CALL_TYPE_POST, "unfriend", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun FriendRequestAlreadySent(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String,String>()
        getApiCallObservable(CALL_TYPE_GET,"pending-sent-friend-requests",hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun getPendingReceivedRequest(apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String,String>()
        getApiCallObservable(CALL_TYPE_GET,"pending-received-friend-requests",hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun responseFriendRequest(
        requesterId: String,
        response: String,
        apiCallbackHelper: ApiCallbackHelper
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["requester"] = requesterId
        hashMap["response"] = response
        getApiCallObservable(CALL_TYPE_POST, "control-friend-request", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun cancelSentRequest(requesterId: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String, String>()
        hashMap["requested_user"] = requesterId
        getApiCallObservable(CALL_TYPE_POST, "cancel-sent-request", hashMap).subscribe(
            apiCallbackHelper
        )
    }

    fun apiGetPracticeLiveExam(categoryId: String, apiCallbackHelper: ApiCallbackHelper) {
        val hashMap = HashMap<String,String>()
        hashMap["category_id"] = categoryId
        getApiCallObservable(CALL_TYPE_GET,"get-practice-model-test",hashMap).subscribe(apiCallbackHelper)
    }


}