package com.gmpire.guruklub.view.activity.profileSetup

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.view.base.BaseViewModel

import sslwireless.android.easy.loyal.merchant.viewmodel.util.ApiCallbackHelper
import javax.inject.Inject

class ProfileSetupViewModel @Inject constructor(dataManager: DataManager) : BaseViewModel() {


    val dataManager = dataManager
    var profileData: UserInfo? = null

    fun apiProfileSetup(
        name: String,
        display_name: String,
        email: String,
        phone: String,
        gender: Int,
        dob: String,
        picture: Bitmap?,
        reg_type : String,
        lifecycleOwner: LifecycleOwner
    ) {
        dataManager.apiHelper.apiProfileSetup(
            name,
            display_name,
            email,
            phone,
            gender,
            dob,
            picture,
            reg_type,
            ApiCallbackHelper(livedata(lifecycleOwner, "apiProfileSetup"))
        )
    }

}