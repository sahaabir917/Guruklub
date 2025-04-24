package com.gmpire.guruklub.view.fragment.game

import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.model.categoryAndSubject.Section
import com.gmpire.guruklub.data.model.categoryAndSubject.Subject
import com.gmpire.guruklub.data.model.categoryAndSubject.Topic
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.view.base.BaseViewModel
import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class GameFragmentViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {
    val dataManager = dataManager
    var iDatabaseCallBack: IDatabaseCallBack? = null

    fun apiGetGameChallenges(
        slug: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetGameChallenges(
            slug,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetGameChallenges"))
        )
    }

    fun apiGetCheckModelTest(
        user_id: String,
        category_id: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetCheckModelTest(
            user_id,
            category_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetCheckModelTest"))
        )
    }


    fun apiGetTime(
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetTime(
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetTime"))
        )
    }

    fun apiGetTimeForGameStart(
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiGetTime(
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetTimeForGameStart"))
        )
    }

    fun apiModelTestRegister(
        user_id: String,
        model_test_id: String,
        exam_status : Boolean,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiModelTestRegister(
            user_id,
            model_test_id,
            exam_status.toString(),
            ApiCallbackHelper(livedata(lifecycleOwner, "apiModelTestRegister"))
        )
    }

    fun apiModelTestRegistrationPayment(
        model_test_id: String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiModelTestRegistrationPayment(
            model_test_id,
            ApiCallbackHelper(livedata(lifecycleOwner, "purchaseLiveExam"))
        )
    }

    fun apiGetSubjectListByCategory(categoryId: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSubjectListByCategory(
            categoryId, "",
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetSubjectListByCategory"))
        )
    }

    fun apiGetSectionsBySubject(subject_ids: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetSectionsBySubject(
            subject_ids,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetSectionsBySubject"))
        )
    }

    fun apiGetTopicBySection(section_ids: String, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetTopicBySection(
            section_ids,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiGetTopicBySection"))
        )
    }


    // Subject
    fun dataInsertAllSubjectList(subjectList: List<Subject>) {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().subjectDao()
                .insertAll(Subject.toSubjectDTOs(subjectList)),
            "dataInsertAllSubjectList", iDatabaseCallBack
        )
    }

    fun dataGetSubjectListByCategory() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().subjectDao().getAll(),
            "dataGetSubjectListByCategory", iDatabaseCallBack
        )
    }

    fun dataDeleteAllSubjectList() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().subjectDao().deleteAll(),
            "dataDeleteAllSubjectList", iDatabaseCallBack
        )
    }


    // Sections
    fun dataInsertAllSectionList(sectionList: List<Section>) {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().sectionDao()
                .insertAll(Section.toSectionDTOs(sectionList)),
            "dataInsertAllSectionList", iDatabaseCallBack
        )
    }

    fun dataDeleteAllSectionList() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().sectionDao().deleteAll(),
            "dataDeleteAllSectionList", iDatabaseCallBack
        )
    }

    // Topic
    fun dataInsertAllTopicList(topicList: List<Topic>) {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().topicDao()
                .insertAll(Topic.toTopicDTOs(topicList)),
            "dataInsertAllTopicList", iDatabaseCallBack
        )
    }

    fun dataDeleteAllTopicList() {
        dataManager.databaseHelper.executeDBOperation(
            dataManager.roomHelper.getDatabase().topicDao().deleteAll(),
            "dataDeleteAllTopicList", iDatabaseCallBack
        )
    }
    fun getPaymentExecution(
        lifeCycleOwner: LifecycleOwner,
        status: String?,
        tranDate: String?,
        tranId: String?,
        valId: String?,
        amount: String?,
        storeAmount: String?,
        bankTranId: String?,
        cardType: String?,
        cardNo: String?
    ) {
        dataManager.apiHelper.getPaymentExecution(
            status ?: "", tranDate ?: "", tranId ?: "", valId
                ?: "", amount ?: "", storeAmount ?: "", bankTranId ?: "", cardType ?: "", cardNo
                ?: "", ApiCallbackHelper(livedata(lifeCycleOwner, "getPaymentExecution"))
        )
    }

    fun getFailedTransaction(status: String, errorMsg : String, tnxId: String, lifeCycleOwner: LifecycleOwner) {
        dataManager.apiHelper.getFailedTransaction(
            status,
            errorMsg,
            tnxId,
            ApiCallbackHelper(livedata(lifeCycleOwner, "getFailedTransaction"))
        )
    }

    fun apiGetPracticeLiveExam(categoryId:String?, lifecycleOwner: LifecycleOwner) {
        dataManager.apiHelper.apiGetPracticeLiveExam(categoryId?:"1",ApiCallbackHelper(livedata(lifecycleOwner,"apiGetPracticeLiveExam")))
    }

}