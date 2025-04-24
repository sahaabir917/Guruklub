package com.gmpire.guruklub.view.activity.login

import android.Manifest
import android.accounts.Account
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.databinding.ActivityLoginBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.LocalValidator
import com.gmpire.guruklub.view.activity.Registration.RegistrationActivity
import com.gmpire.guruklub.view.activity.forgetPassword.ForgetPasswordActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.gmpire.guruklub.data.model.gamelevel.Level
import com.gmpire.guruklub.util.DeviceInfoHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.people.v1.People
import com.google.api.services.people.v1.model.Person
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.BasePermissionListener
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

const val BASIC_LOGIN = 0
const val GOOGLE_LOGIN = 1
const val FACEBOOK_LOGIN = 2

class LoginActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var callbackManager: CallbackManager

    lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var emailOrPhone: String
    private lateinit var password: String
    lateinit var gender: String
    lateinit var dob: String
    private var loginCount: String? = ""
    private var selectedLoginFlow = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        this.viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(LoginViewModel::class.java)

        binding.rootLayout.setOnClickListener(this)
        binding.showPasswordCb.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.passwordEt.transformationMethod =
                    HideReturnsTransformationMethod.getInstance();
            } else {
                binding.passwordEt.transformationMethod =
                    PasswordTransformationMethod.getInstance();
            }
        }
    }

    override fun viewRelatedTask() {
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnSignUp.setOnClickListener(this)
        binding.tvForgotPassword.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.relativeLayoutGoogleLoginMain.setOnClickListener(this)
        binding.relativeLayoutFbLoginMain.setOnClickListener(this)

        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("auth", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("auth", "facebook:onCancel")
                // ...
                onLoading(false, "Facebook")
            }

            override fun onError(error: FacebookException) {
                Log.d("auth", "facebook:onError", error)
                // ...
                onLoading(false, "Facebook")
            }
        })
    }

    @SuppressLint("NewApi")
    private fun askPhoneStateReadPermission() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.READ_PHONE_STATE)
            .withListener(object : BasePermissionListener() {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    proceedToLoginFlow()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    if (!dataManager.mPref.prefGetIsFirstPhonePermDenial() &&
                        !this@LoginActivity.shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)
                    ) {
                        showPermissionRequiredDialogWithAppSettings()
                    } else {
                        showPermissionRequiredDialog()
                    }
                    dataManager.mPref.prefSetIsFirstPhonePermDenial(false)
                }

            })
            .withErrorListener { error -> Log.e("Permission", error.toString()) }
            .check()
    }

    private fun sendDeviceInfo() {
        dataManager.mPref.getFcmToken(this)?.let {
            viewModel.fetchDeviceFcmToken(
                android.os.Build.DEVICE,
                android.os.Build.MODEL,
                android.os.Build.PRODUCT,
                android.os.Build.VERSION.RELEASE,
                DeviceInfoHelper.getDeviceId(this) ?: "",
                dataManager.mPref.getFcmToken(this).toString(), this
            )
        }
    }

    private fun proceedToLoginFlow() {
        when (selectedLoginFlow) {
            BASIC_LOGIN -> {
                if (emailOrPhoneValid(emailOrPhone) && passwordValid(password)) {
                    viewModel.apiLoginRequest(emailOrPhone, password, this)
                }
            }
            GOOGLE_LOGIN -> {
                signIn()
                onLoading(true, "Google")
            }
            FACEBOOK_LOGIN -> {
                LoginManager.getInstance().logOut()
                LoginManager.getInstance()
                    .logInWithReadPermissions(
                        this,
                        listOf("email", "public_profile")
                    )
                onLoading(true, "Facebook")
            }
        }
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.rootLayout -> {
                hideKeyboard()
            }
            binding.btnSignUp -> {
                startActivity(Intent(this, RegistrationActivity::class.java))
            }
            binding.tvForgotPassword -> {
                startActivity(Intent(this, ForgetPasswordActivity::class.java))
            }
            binding.relativeLayoutGoogleLoginMain -> {
                selectedLoginFlow = GOOGLE_LOGIN
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    askPhoneStateReadPermission()
                } else {
                    signIn()
                    onLoading(true, "Google")
                }

            }
            binding.relativeLayoutFbLoginMain -> {
                selectedLoginFlow = FACEBOOK_LOGIN
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    askPhoneStateReadPermission()
                } else {
                    LoginManager.getInstance().logOut()
                    LoginManager.getInstance()
                        .logInWithReadPermissions(
                            this,
                            listOf("email", "public_profile")
                        )
                    onLoading(true, "Facebook")
                }
            }
            binding.btnLogin -> {
                selectedLoginFlow = BASIC_LOGIN
                emailOrPhone = binding.emailEt.text.toString()
                password = binding.passwordEt.text.toString()
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    askPhoneStateReadPermission()
                } else {
                    if (emailOrPhoneValid(emailOrPhone) && passwordValid(password)) {
                        viewModel.apiLoginRequest(emailOrPhone, password, this)
                    }
                }
            }
        }
    }

    override fun navigateToHome() {

    }

    public override fun onStart() {
        super.onStart()
    }

    private fun updateUI(currentUser: FirebaseUser?, loginType: String) {
        Log.d("auth_login", currentUser?.uid.toString())
        currentUser?.photoUrl
        currentUser?.let {
            when (loginType) {
                "g" -> {
                    viewModel.fetchGoogleLogin(currentUser, this)
                }
                "fb" -> {
                    viewModel.fetchFbLogin(currentUser, this)
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("auth", "firebaseAuthWithGoogle:" + acct.id)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("auth", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user, "g")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("auth", "signInWithCredential:failure", task.exception)
                    updateUI(null, "g")
                }
                // ...
                onLoading(false, "Google")
            }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, 103)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("auth", "handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("auth", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user, "fb")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("auth", "signInWithCredential:failure", task.exception)
                    showToast(this, "Authentication failed.")
                    updateUI(null, "fb")
                }
                // ...
                onLoading(false, "Facebook")
            }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 103) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("aurth", "Google sign in failed", e)
                // ...
                onLoading(false, "Google")
            }
        } else if (requestCode == 64206) {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiLoginRequest" -> {
                val type = object : TypeToken<BaseModel<UserInfo>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<UserInfo>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            val userInfo = baseData.data
                            if (userInfo?.category_id.isNullOrEmpty()) {
                                userInfo?.category_id = "1"
                            }
                            dataManager.mPref.prefSetUserInfo(userInfo)
                            dataManager.mPref.prefSetGameCurrentLevel(
                                Level(
                                    "",
                                    userInfo?.user_game_level ?: "1", "", "", "", "", "", "", 0
                                )
                            )
                            dataManager.mPref.prefSetUserHeart(baseData.data?.user_hearts ?: "0")
                            dataManager.mPref.prefSetUserAdLimit(baseData.data?.user_settings?.user_ads_limit)
                            dataManager.mPref.prefSetCurrentSubscription(baseData.data?.hearts_subscription)
                            dataManager.mPref.prefSetHeartGift(baseData.data?.hearts_gift)
                            var isAdsFree = baseData.data?.user_settings?.ads_free
                            if (isAdsFree == "1") {
                                dataManager.mPref.prefSetIsAdFree(true)
                            } else {
                                dataManager.mPref.prefSetIsAdFree(false)
                            }
                            dataManager.mPref.prefSetToken(baseData.token)
                            if (userInfo?.category_id == null) {
                                // manually assigning category
                                userInfo?.category_id = "1"
                            }
                            dataManager.mPref.prefLogin()
                            loginCount = baseData.data?.user_settings?.login_count
                            sendDeviceInfo()
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
            "fetchFbLogin" -> {
                val type = object : TypeToken<BaseModel<UserInfo>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<UserInfo>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            val userInfo = baseData.data
                            if (userInfo?.category_id.isNullOrEmpty()) {
                                userInfo?.category_id = "1"
                            }
                            dataManager.mPref.prefSetUserInfo(userInfo)
                            dataManager.mPref.prefSetGameCurrentLevel(
                                Level(
                                    "",
                                    userInfo?.user_game_level ?: "1", "", "", "", "", "", "", 0
                                )
                            )
                            dataManager.mPref.prefSetUserHeart(baseData.data?.user_hearts ?: "0")
                            dataManager.mPref.prefSetUserAdLimit(baseData.data?.user_settings?.user_ads_limit)
                            var isAdsFree = baseData.data?.user_settings?.ads_free
                            if (isAdsFree == "1") {
                                dataManager.mPref.prefSetIsAdFree(true)
                            } else {
                                dataManager.mPref.prefSetIsAdFree(false)
                            }
                            dataManager.mPref.prefSetToken(baseData.token)
                            if (userInfo?.category_id == null) {
                                userInfo?.category_id = "1"
                            }
                            dataManager.mPref.prefLogin()
                            loginCount = baseData.data?.user_settings?.login_count
                            sendDeviceInfo()
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
            "fetchGoogleLogin" -> {
                val type = object : TypeToken<BaseModel<UserInfo>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<UserInfo>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            val userInfo = baseData.data
                            if (userInfo?.category_id.isNullOrEmpty()) {
                                userInfo?.category_id = "1"
                            }
                            dataManager.mPref.prefSetUserInfo(userInfo)
                            dataManager.mPref.prefSetGameCurrentLevel(
                                Level(
                                    "",
                                    userInfo?.user_game_level ?: "1", "", "", "", "", "", "", 0
                                )
                            )
                            dataManager.mPref.prefSetUserHeart(baseData.data?.user_hearts ?: "0")
                            dataManager.mPref.prefSetUserAdLimit(baseData.data?.user_settings?.user_ads_limit)
                            var isAdsFree = baseData.data?.user_settings?.ads_free
                            if (isAdsFree == "1") {
                                dataManager.mPref.prefSetIsAdFree(true)
                            } else {
                                dataManager.mPref.prefSetIsAdFree(false)
                            }
                            dataManager.mPref.prefSetToken(baseData.token)

                            if (userInfo?.category_id == null) {
                                userInfo?.category_id = "1"
                            }
                            dataManager.mPref.prefLogin()
                            loginCount = baseData.data?.user_settings?.login_count
                            sendDeviceInfo()
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
            "fetchDeviceFcmToken" -> {
                val type = object : TypeToken<BaseModel<UserInfo>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<UserInfo>>(result.data.body()?.string(), type)
                    startActivity(
                        Intent(this, MainActivity::class.java).putExtra(
                            "login_count",
                            baseData.data?.user_settings?.login_count
                        )
                    )
                    finishAffinity()
                }
            }
        }
    }

    private fun passwordValid(password: String): Boolean {
        if (password.isEmpty()) {
            binding.passwordEt.error = "Enter your password to continue"
            return false
        } else {
            if (password.length < 4) {
                binding.passwordEt.error = "Password length should not less then four digit"
                return false
            }
        }
        return true
    }

    private fun emailOrPhoneValid(emailOrPhone: String): Boolean {
        if (emailOrPhone.isEmpty()) {
            binding.emailEt.error = "Enter your email/mobile to continue"
            return false
        } else {
            if (!LocalValidator.isEmailValid(emailOrPhone)
                && !LocalValidator.isPhoneValid(emailOrPhone)
            ) {
                binding.emailEt.error = "Please provide valid email/mobile no."
                return false
            }
        }
        return true
    }

    private class GetContactsTask constructor(
        context: LoginActivity,
        account: GoogleSignInAccount
    ) :
        AsyncTask<Void, Void, Person>() {

        var mAccount = account
        var mContext = context

        override fun doInBackground(vararg params: Void?): Person {
            /** Global instance of the HTTP transport. */
            /** Global instance of the HTTP transport.  */
            val HTTP_TRANSPORT: HttpTransport = AndroidHttp.newCompatibleTransport()
            /** Global instance of the JSON factory. */
            /** Global instance of the JSON factory.  */
            val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()

            var c = ArrayList<String>()
            val add = c.add(Scopes.PROFILE)

            val credential: GoogleAccountCredential =
                GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(Scopes.PROFILE))
            credential.selectedAccount = Account(mAccount.email, "com.google")

            val service: People = People.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("guruklub" /* whatever you like */)
                .build()

            val meProfile: Person =
                service.people().get("people/me")
                    .setRequestMaskIncludeField("person.birthdays,person.genders")
                    .execute()

            return meProfile;
        }

        override
        fun onCancelled() {
        }

        override
        fun onPostExecute(meProfile: Person) {
            val genders = meProfile.genders
            val dateBirths = meProfile.birthdays

            if (genders != null && genders.size > 0) {
                mContext.gender = genders[0].value
            }

            try {
                for (bd in dateBirths) {
                    val day = bd.date.day
                    val month = bd.date.month
                    val year = bd.date.year
                    mContext.dob = "$year-$month-$day"

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mContext.firebaseAuthWithGoogle(mAccount)
        }
    }
}





