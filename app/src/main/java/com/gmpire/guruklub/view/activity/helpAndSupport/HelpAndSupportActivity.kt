package com.gmpire.guruklub.view.activity.helpAndSupport

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.findus.FindUsItem
import com.gmpire.guruklub.databinding.ActivityHelpAndSupportBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.BottomSheet.ContactUsBottomSheet
import com.gmpire.guruklub.view.BottomSheet.FindUsBottomSheet
import com.gmpire.guruklub.view.BottomSheet.InviteFriendsBottomSheet
import com.gmpire.guruklub.view.activity.Registration.ACTIVITY_NAME
import com.gmpire.guruklub.view.activity.gameHelp.ContentActivity
import com.gmpire.guruklub.view.activity.gameHelp.ContentViewModel
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.termsAndCondition.TermsConditionActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response


class HelpAndSupportActivity : BaseActivity(), ContactUsBottomSheet.Listener,
    InviteFriendsBottomSheet.Listener {

    private lateinit var inviteFriedsBottomSheet: InviteFriendsBottomSheet
    private lateinit var contactusBottomsheet: ContactUsBottomSheet
    private lateinit var viewModel: ContentViewModel
    private lateinit var binding: ActivityHelpAndSupportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_help_and_support)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(ContentViewModel::class.java)


        binding.aboutXploreLayout.setOnClickListener(this)
        binding.newUserFaqLayout.setOnClickListener(this)
        binding.findUsOnLayout.setOnClickListener(this)
        binding.contactUsLayout.setOnClickListener(this)
        binding.inviteFriendsLayout.setOnClickListener(this)
        binding.ratingLayout.setOnClickListener(this)
        binding.termsConditionLayout.setOnClickListener(this)

    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Help And Support", true)
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetFindUs" -> {
                val type = object : TypeToken<BaseModel<FindUsItem>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<FindUsItem>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            try {
                                val findUsBottomSheet = FindUsBottomSheet(baseData.data!!)
                                findUsBottomSheet.show(
                                    supportFragmentManager,
                                    findUsBottomSheet.tag
                                )
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
            "apiContactUs" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        contactusBottomsheet.updateView()
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
            "apiInviteFriends" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        inviteFriedsBottomSheet.updateView()
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.aboutXploreLayout -> {
                startActivity(
                    Intent(this, ContentActivity::class.java).putExtra(
                        ContentActivity.ACTIVITY_TITLE,
                        ContentActivity.TITLE_ABOUT
                    )
                )

            }
            binding.newUserFaqLayout -> {
                startActivity(
                    Intent(this, ContentActivity::class.java).putExtra(
                        ContentActivity.ACTIVITY_TITLE,
                        ContentActivity.TITLE_FAQ
                    )
                )

            }
            binding.findUsOnLayout -> {
                viewModel.apiGetFindUs(this)
            }

            binding.contactUsLayout -> {
                contactusBottomsheet =
                    ContactUsBottomSheet(dataManager.mPref.prefGetUserInfo().email, this)
                contactusBottomsheet.show(supportFragmentManager, contactusBottomsheet.tag)
            }

            binding.inviteFriendsLayout -> {
                /*inviteFriedsBottomSheet =
                    InviteFriendsBottomSheet( this)
                inviteFriedsBottomSheet.show(supportFragmentManager, inviteFriedsBottomSheet.tag)*/
                val share = Intent(Intent.ACTION_SEND)
                share.type = "text/plain"
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                share.putExtra(Intent.EXTRA_SUBJECT, "Download Xplorer")
                share.putExtra(
                    Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=${packageName}"
                )
                startActivity(Intent.createChooser(share, "Download Xplorer"))
            }
            binding.ratingLayout -> {
                val appPackageName =
                    packageName // getPackageName() from Context or Activity object
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appPackageName")
                        )
                    )
                } catch (anfe: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                        )
                    )
                }
            }
            binding.termsConditionLayout -> {
                val intent = Intent(this, TermsConditionActivity::class.java)
                intent.putExtra(ACTIVITY_NAME, "content")
                startActivity(intent)
            }
        }
    }

    override fun onContactUsSendBtnClicked(email: String, message: String) {
        viewModel.apiContactUs(email, message, this)
    }

    override fun onInviteBtnClicked(emails: String) {
        viewModel.apiInviteFriends(dataManager.mPref.prefGetUserInfo().email, emails, this)
    }
}
