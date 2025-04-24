package com.gmpire.guruklub.view.activity.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.MyApp
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.GameHeartDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.Notice
import com.gmpire.guruklub.data.model.NoticeTracking
import com.gmpire.guruklub.data.model.findus.FindUsItem
import com.gmpire.guruklub.data.model.gamelevel.GameUserInfo
import com.gmpire.guruklub.data.model.infocenter.News
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.data.model.profile.performance.PerformanceSummery
import com.gmpire.guruklub.data.model.user.DeviceInfoResponse
import com.gmpire.guruklub.data.model.usersreference.ReferenceResponse
import com.gmpire.guruklub.databinding.ActivityMainBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.Add_HEARTS
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.DRAWER_MENU
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.GAME_FRIEND_REQUEST
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.GAME_PURCHASE_HEART
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.GAME_SCREEN
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.GURUKLUB_GAME
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.HELP_N_SUPPORT
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.HOME_QUESTION
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.HOME_SCREEN
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.HOME_SEARCH
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.INFO_CENTRE_MAIN
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.INFO_CENTRE_SCREEN
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.LIBRARY_SCREEN_MAIN
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.NOTIFICATION_DETAILS
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.NO_ACTION
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.PLAY_STORE
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.PROFILE_BOOKMARKS
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.PROFILE_ERROR
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.PROFILE_LEADERBOARD
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.PROFILE_MAIN
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService.Companion.VIDEOS
import com.gmpire.guruklub.view.BottomSheet.ContactUsBottomSheet
import com.gmpire.guruklub.view.BottomSheet.InviteFriendsBottomSheet
import com.gmpire.guruklub.view.activity.Registration.ACTIVITY_NAME
import com.gmpire.guruklub.view.activity.gameHelp.ContentActivity
import com.gmpire.guruklub.view.activity.gameResultActivity.GameResultActivity
import com.gmpire.guruklub.view.activity.gameheart.HeartAddActivity
import com.gmpire.guruklub.view.activity.gamelevel.FriendRequestActivity
import com.gmpire.guruklub.view.activity.gamelevel.GAME_HEART_MINUS
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelActivity
import com.gmpire.guruklub.view.activity.library.AllLibrarySubject
import com.gmpire.guruklub.view.activity.login.AllVideosActivity
import com.gmpire.guruklub.view.activity.login.FACEBOOK_LOGIN
import com.gmpire.guruklub.view.activity.login.GOOGLE_LOGIN
import com.gmpire.guruklub.view.activity.login.LoginActivity
import com.gmpire.guruklub.view.activity.newsDetails.NewsDetailsActivity
import com.gmpire.guruklub.view.activity.notification.NotificationActivity
import com.gmpire.guruklub.view.activity.notificationDetails.NotificationDetailsActivity
import com.gmpire.guruklub.view.activity.profile.*
import com.gmpire.guruklub.view.activity.question.QuestionAddActivity
import com.gmpire.guruklub.view.activity.questionSearch.QuestionSearchActivity
import com.gmpire.guruklub.view.activity.termsAndCondition.TermsConditionActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.adapter.ViewPagerAdapter
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.dialog.*
import com.gmpire.guruklub.view.dialog.notice.NoticeDetailsActivity
import com.gmpire.guruklub.view.fragment.dashboard.DashboardFragment
import com.gmpire.guruklub.view.fragment.game.GameFragment
import com.gmpire.guruklub.view.fragment.home.HomeFragment
import com.gmpire.guruklub.view.fragment.infoCentre.InfoCentreFragment
import com.gmpire.guruklub.view.fragment.library.LibraryFragmentNew
import com.gmpire.guruklub.view.fragment.previousQuestionsBatch.PreviousQuestionBatchFragment
import com.gmpire.guruklub.view.fragment.profile.ProfileMainFragment
import com.gmpire.guruklub.view.fragment.settings.SettingsFragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener
import com.michaelflisar.rxbus2.RxBusBuilder
import com.michaelflisar.rxbus2.rx.RxBusMode
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.dialog_box.view.*
import kotlinx.android.synthetic.main.item_tab_selected.view.*
import kotlinx.android.synthetic.main.navigation_header_layout.view.*
import kotlinx.android.synthetic.main.navigation_menu_layout.view.*
import okhttp3.ResponseBody
import org.apache.http.impl.cookie.DateParseException
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : BaseActivity(), ProgressBarListener,
    LoginDialog.LoginDialogButtonListener, HomeFragment.ShowLoginDialogListener,
    HomeFragment.MenuItemChangeListener, LibraryFragmentNew.GistListener,
    ContactUsBottomSheet.Listener,
    NoticeBoardDialog.NoticeBoardDialogButtonListener,
    DashboardFragment.DashboardOnClickListener,
    UpdateProfileDialog.UpdateProfileListeners,
    InviteFriendsBottomSheet.Listener, GameFragment.ExamStartListener, IDatabaseCallBack {

    private var isFromNotification: Boolean = false
    private var users_model: UserInfo? = null
    private var notificationCount: Int = 0
    private var isHomePage: Boolean = false
    private var loginCount: String? = null
    private var loginCounting: Int = 0
    private lateinit var inviteFriedsBottomSheet: InviteFriendsBottomSheet
    private lateinit var contactusBottomsheet: ContactUsBottomSheet
    private lateinit var adapter: ViewPagerAdapter
    private var doubleBackToExitPressedOnce = false
    private lateinit var timer: CountDownTimer
    lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var userInfo: UserInfo? = null
    private var adView: AdView? = null
    private lateinit var performanceSummery: PerformanceSummery
    private lateinit var auth: FirebaseAuth
    private lateinit var homeFragment: HomeFragment
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var callbackManager: CallbackManager
    private val loginDialog = LoginDialog.newInstance()
    private var noticeBoardDialog: NoticeBoardDialog? = null
    private val inviteFriendPromoCodeDialog = InviteFriendPromoCodeDialog.newInstance()
    private var numofCountNotification: Int = 0

    private lateinit var progressBar: ProgressBar
    private lateinit var reviewInfo: ReviewInfo
    private var questionString: String? = ""
    private lateinit var imageVIewNotificationCount: ImageView
    private var isFirstTime = true

    private var newsFromIntent: News = News()
    private var storedGist = ""
    private lateinit var findUsItem: FindUsItem
    private var isAdsFree: Boolean = false
    private var noticeList = arrayListOf<Notice>()
    private var selectedLoginFlow = -1
    private var currentHearts: Int = 0
    private var shouldShowRatingDialog = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        progressBar = binding.progressBarMain

        this.viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(MainViewModel::class.java)
        viewModel.iDatabaseCallBack = this

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("notification", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token
                // Log and toast
                dataManager.mPref.saveFcmToken(this, token)
                Log.d("notification_login", dataManager.mPref.getFcmToken(this))
                Log.d(
                    "device_info", Build.HARDWARE + " /" +
                            Build.MODEL + " /" +
                            Build.MANUFACTURER + " /" +
                            Build.VERSION.SDK_INT + " /" +
                            Build.VERSION.RELEASE + " /"
                )
                sendDeviceInfo()
            })

        //Load Ad
        isAdsFree = dataManager.mPref.prefGetIsAdFree() ?: false
        if (!isAdsFree) {
            adView = AdView(this);
            binding.rlAdContainer.addView(adView)
        }

        // Review info
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                reviewInfo = request.result
            }
        }

        binding.toolbar.appCompatImageViewLogo.setOnClickListener {
            if (reviewInfo != null) {
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { _ ->
                }
            }
        }

        //Get news from intent
        try {
            if (intent.extras?.containsKey("news_object") == true) {
                val newsString = intent.extras?.getString("news_object")
                val intent = Intent(this, NewsDetailsActivity::class.java)
                newsFromIntent = Gson().fromJson(newsString, News::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        try {
            if (intent.hasExtra("friend_request_object")) {
                val userInfo = intent.extras?.getString("friend_request_object")
                users_model = Gson().fromJson(userInfo, UserInfo::class.java)
                isFromNotification = true
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        loginCount = intent.extras?.get("login_count")?.toString()

        if (loginCount != null) {
            loginCounting = loginCount!!.toInt()
            if (loginCounting == 0 && dataManager.mPref.prefGetPrefRefViewCount() == "0") {
                inviteFriendPromoCodeDialog.show(
                    supportFragmentManager,
                    inviteFriendPromoCodeDialog.tag
                )
            }
        }

        viewModel.apiGetFindUs(this)
        viewModel.apiGetNotices(this)

    }

    private fun sendDeviceInfo() {
        dataManager.mPref.getFcmToken(this)?.let {
            viewModel.fetchDeviceFcmToken(
                Build.DEVICE,
                Build.MODEL,
                Build.MANUFACTURER,
                Build.VERSION.RELEASE,
                DeviceInfoHelper.getDeviceId(this) ?: "",
                dataManager.mPref.getFcmToken(this).toString(), this
            )
        }
    }


    private fun animateHeartUpdates(text: String, type: Int) {
        val anim = AnimationUtils.loadAnimation(this, R.anim.game_heart_update)
        anim.duration = 2000 //You can manage the blinking time with this parameter
        anim.startOffset = 20
        if (type == GAME_HEART_MINUS) {
            binding.toolbar.tvUpdateHeart.text = "-$text"
        } else {
            binding.toolbar.tvUpdateHeart.text = "+$text"
        }
        binding.toolbar.tvUpdateHeart.visibility = View.VISIBLE
        binding.toolbar.tvUpdateHeart.startAnimation(anim)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    }

    private fun storeDateOfRatingShow() {
        val date = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        val dateString = dateFormat.format(date)
        dataManager.mPref.saveDateOfRatingShow(dateString)
    }

    private fun checkIfShownWithinThisDay(): Boolean {
        try {
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("dd-MM-yyyy")
            val datePrevious = dateFormat.parse(dataManager.mPref.getDateOfRatingShow())
            val day = 24 * 60 * 60 * 1000
            return currentDate.before(Date(datePrevious.time + day))
        } catch (ex: DateParseException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return false
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("TAG", "New Intent called!")

        try {
            if (intent?.extras?.containsKey("goto") == true) {
                val goToVal = intent.extras!!["goto"] as String
                navigateAsIntent(goToVal, intent, true)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateAsIntent(
        goToVal: String?,
        workingIntent: Intent,
        isFromNewIntent: Boolean
    ) {
        when (goToVal) {
            HOME_SCREEN -> {
                initHomeFragment()
            }
            HOME_QUESTION -> {
                try {
                    if (workingIntent.extras?.containsKey("question_object") == true) {
                        //Get question from intent
                        questionString = workingIntent.extras?.getString("question_object")
                        initHomeFragment()
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            INFO_CENTRE_MAIN -> {
                changeFragment(3)
            }
            GAME_SCREEN -> {
                changeFragment(1)
            }
            GURUKLUB_GAME -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        GameLevelActivity::class.java
                    )
                )
            }
            INFO_CENTRE_SCREEN -> {
                try {
                    val intent = Intent(this, NewsDetailsActivity::class.java)
                    if (workingIntent.extras?.containsKey("news_object")!!) {
                        val newsString = workingIntent.extras?.getString("news_object")
                        newsFromIntent = Gson().fromJson(newsString, News::class.java)
                    }
                    intent.putExtra("news", newsFromIntent)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            PROFILE_MAIN -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        ProfileActivity::class.java
                    )
                )
            }
            PROFILE_LEADERBOARD -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        LeaderBoardActivity::class.java
                    )
                )
            }
            PROFILE_ERROR -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        ErrorActivity::class.java
                    )
                )
            }
            PROFILE_BOOKMARKS -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        FavouriteActivity::class.java
                    )
                )
            }
            HELP_N_SUPPORT -> {
                // showDrawerNavigationScreen(HelpAndSupportFragment(), "Help And Support")
            }
            LIBRARY_SCREEN_MAIN -> {
                changeFragment(2)
            }
            HOME_SEARCH -> {
                startActivity(Intent(this, QuestionSearchActivity::class.java))
            }
            NOTIFICATION_DETAILS -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        NotificationDetailsActivity::class.java
                    ).putExtra(
                        "notification",
                        workingIntent.extras?.getString("notification_model")
                    )
                )
            }
            PLAY_STORE -> {
                val appPackageName =
                    packageName
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

            GAME_FRIEND_REQUEST -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        FriendRequestActivity::class.java
                    ).putExtra(
                        "friend_request_object", Gson().toJson(users_model)
                    )
                )
            }

            GAME_PURCHASE_HEART -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }
                startActivity(
                    Intent(this, HeartAddActivity::class.java).putExtra(
                        "is_from_game",
                        "no"
                    )
                )
            }

            Add_HEARTS -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }
                startActivity(
                    Intent(this, HeartAddActivity::class.java).putExtra(
                        "is_from_game",
                        "yes"
                    )
                )
            }

            VIDEOS -> {
                startActivity(Intent(this, AllVideosActivity::class.java))
            }

            DRAWER_MENU -> {
                binding.drawerLayout.openDrawer(Gravity.LEFT)
            }


        }
    }


    private fun loadBanner() {
        adView?.adUnitId = getString(R.string.ad_unit_id_banner_test)
        adView?.adSize = adSize
        val adRequest = AdRequest
            .Builder()
            .build()
        adView?.loadAd(adRequest)

        adView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("LoadedAd->", "Successful")
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                Log.d("ErrorAd->", errorCode.toString())
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
    }

    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.rlAdContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }


    override fun viewRelatedTask() {

        setToolbar(this, binding.toolbar, "", false, true)

        binding.toolbar.optionsIcon.visibility = View.GONE
        viewModel.iDatabaseCallBack = this

        viewModel.fetchPerformanceSummery(this)
        binding.toolbar.appCompatTextViewLogo.visibility = View.VISIBLE
        binding.toolbar.frameLayoutNotificationIcon.visibility = View.VISIBLE
        binding.toolbar.searchingLayout.visibility = View.VISIBLE
        setBiasToHamburger(true)

        val text =
            "<font color=#000000>Guru</font><font color=#4A148C>Klub</font>"
        binding.toolbar.appCompatTextViewLogo.text =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(text)
            }

        imageVIewNotificationCount = binding.toolbar.ivNotificationCount

        binding.toolbar.drawerNavigationIcon.setOnClickListener(this)
        binding.toolbar.optionsIcon2.setOnClickListener(this)
        binding.toolbar.textViewFilterHome.setOnClickListener(this)
        binding.toolbar.relativeLayoutNewSearch.setOnClickListener(this)
        binding.navigationView.logout_nav_menu.setOnClickListener(this)
        binding.navigationView.help_Feed_nav_menu.setOnClickListener(this)
        binding.navigationView.settings_nav_menu.setOnClickListener(this)
        binding.navigationView.info_center_nav_menu.setOnClickListener(this)
        binding.navigationView.profile_nav_menu.setOnClickListener(this)
        binding.navigationView.settings_nav_menu.setOnClickListener(this)
        binding.navigationView.leaderboard_nav_menu.setOnClickListener(this)
        binding.navigationView.error_nav_menu.setOnClickListener(this)
        binding.navigationView.bookmarks_nav_menu.setOnClickListener(this)
        binding.navigationView.recent_performance_nav_menu.setOnClickListener(this)
        binding.navigationView.profile_nav_performance_history.setOnClickListener(this)
        binding.navigationView.contact_us_layout1.setOnClickListener(this)
        binding.navigationView.new_user_faq_layout2.setOnClickListener(this)
        binding.navigationView.about_xplore_layout2.setOnClickListener(this)
        binding.navigationView.rating_layout2.setOnClickListener(this)
        binding.navigationView.invite_friends_layout2.setOnClickListener(this)
        binding.navigationView.terms_condition_layout2.setOnClickListener(this)
        binding.navigationView.dashboard_nav_menu.setOnClickListener(this)
        binding.navigationView.allVideosLayout.setOnClickListener(this)
        binding.navigationView.previousQuestionLayout.setOnClickListener(this)
        binding.navigationView.purchase_now_btn.setOnClickListener(this)
        binding.navigationView.referbtn.setOnClickListener(this)
        binding.toolbar.editText2.setOnClickListener(this)
        binding.toolbar.frameLayoutLifeIcon.setOnClickListener(this)

        //setting user data
        userInfo = dataManager.mPref.prefGetUserInfo()

        setProfileInfo(userInfo)

        if (!dataManager.mPref.prefGetLoginMode()) {
            binding.navigationView.logout_nav_menu.visibility = View.GONE
            binding.navHeader.textView3.visibility = View.GONE
            binding.navHeader.lifeamountlayout.visibility = View.GONE
        } else {
            binding.navigationView.logout_nav_menu.visibility = View.VISIBLE
            binding.navHeader.textView3.visibility = View.VISIBLE
            binding.navHeader.lifeamountlayout.visibility = View.VISIBLE
        }

        RxBusBuilder.create(UpdateProfile::class.java)
            .withKey(RxBusEvents.PROFILE_UPDATED)
            .withBound(this)
            .withMode(RxBusMode.Main)
            .subscribe({
                setProfileInfo(it.profile)
            }, { it.printStackTrace() })

        RxBusBuilder.create(UpdateClass::class.java)
            .withKey(RxBusEvents.HEARTS_UPDATED)
            .withBound(this)
            .withMode(RxBusMode.Main)
            .subscribe {
                viewModel.dataGetUserHearts()
            }

        //New sign-in functionality
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestServerAuthCode(getString(R.string.default_web_client_id))
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
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

        //adding fragments to viewpager and tabLayout
        setViewPager()

        //setting tab icons
        setTabIcons()

        try {
            if (intent?.extras?.containsKey("goto") == true) {
                val goToVal = intent.extras!!["goto"] as String
                navigateAsIntent(goToVal, intent, false)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setViewPager() {
        adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.removeAll()

        if (questionString.isNullOrEmpty())
            adapter.addFragment(DashboardFragment.newInstance(), "Home")
        else
            adapter.addFragment(DashboardFragment.newInstance(), "Home")

        adapter.addFragment(GameFragment.newInstance("Exam Centre"), "Exam Centre")

        adapter.addFragment(LibraryFragmentNew.newInstance("Library"), "Library")

        adapter.addFragment(
            InfoCentreFragment.newInstance(),
            "InfoCentre"
        )

        binding.homeViewPager.adapter = adapter
        binding.homeTabLayout.setupWithViewPager(binding.homeViewPager)
        binding.homeViewPager.offscreenPageLimit = 3
        //for dashboard life icon show
        binding.toolbar.frameLayoutNotificationIcon.visibility = View.VISIBLE
        binding.toolbar.frameLayoutLifeIcon.visibility = View.VISIBLE
        binding.toolbar.optionsIcon3.visibility = View.VISIBLE
        //end of dashboard life icon show
        binding.homeViewPager.currentItem = -2
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

    override fun navigateToHome() {
        switchToBottomNavView()
        initHomeFragment()
    }

    private fun setProfileInfo(userInfo: UserInfo?) {
        binding.navHeader.userNameTv.text = userInfo?.name
        binding.navHeader.userPhoneTv.text = userInfo?.phone
        binding.navHeader.profileImageIv.clipToOutline = true
        var gameLevel = dataManager.mPref.prefGetGameCurrentLevel()?.level
        if (!gameLevel.isNullOrBlank())
            binding.navHeader.textView3.text = "level - $gameLevel"
        if (userInfo?.picture != null && userInfo.picture?.isNotEmpty()!!) {
            Glide.with(this).load(BuildConfig.SERVER_URL + userInfo.picture)
                .placeholder(R.drawable.placeholder)
                .into(binding.navHeader.profileImageIv)

        }
        binding.navHeader.imageViewCloseDrawer.setOnClickListener {
            closeDrawer()
        }
    }

    private fun setTabIcons() {
        val imageView1 = LayoutInflater.from(this).inflate(R.layout.item_tab_selected_left, null)
        imageView1.ivSelected.setImageResource(R.drawable.ic_home_png)
        imageView1.tvTitleTab.text = "Home"
        imageView1.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView1.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding.homeTabLayout.getTabAt(0)?.customView = imageView1

        val imageView2 = LayoutInflater.from(this).inflate(R.layout.item_tab_selected, null)
        imageView2.ivSelected.setImageResource(R.drawable.ic_exam_png)
        imageView2.tvTitleTab.text = "Exam"
        imageView2.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding.homeTabLayout.getTabAt(1)?.customView = imageView2


        val imageView3 = LayoutInflater.from(this).inflate(R.layout.item_tab_selected, null)
        imageView3.ivSelected.setImageResource(R.drawable.ic_library_png)
        imageView3.tvTitleTab.text = "Library"
        imageView3.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding.homeTabLayout.getTabAt(2)?.customView = imageView3

        val imageView5 = LayoutInflater.from(this).inflate(R.layout.item_tab_selected_right, null)
        imageView5.ivSelected.setImageResource(R.drawable.ic_info_news_png)
        imageView5.tvTitleTab.text = "InfoCentre"
        imageView5.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding.homeTabLayout.getTabAt(3)?.customView = imageView5

        modifyClickBehaviourTab()

        binding.homeViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (position != 0) {
                    binding.toolbar.drawerTitle.text = adapter.getPageTitle(position)
                    //binding.toolbar.appCompatImageViewLogo.visibility = View.GONE
                    binding.toolbar.textViewFilterHome.visibility = View.GONE
                    binding.toolbar.relativeLayoutNewSearch.visibility = View.GONE
                    binding.toolbar.appCompatTextViewLogo.visibility = View.GONE
                    imageVIewNotificationCount.visibility = View.GONE
                    binding.noticeRl.visibility = View.GONE
                    setBiasToHamburger(false)
                    binding.toolbar.searchingLayout.visibility = View.GONE
                    binding.toolbar.optionsIcon2.visibility = View.GONE
                    binding.toolbar.tvFilterLabel.visibility = View.GONE
                } else {
                    if (!isHomePage) {
                        isHomePage = false
                        var dashboardFragment = DashboardFragment.newInstance()
                        var size = noticeList.size - 1
                        if (size >= 0) {
                            binding.noticeRl.visibility = View.VISIBLE
                        } else if (size < 0) {
                            binding.noticeRl.visibility = View.GONE
                        }
                        if (numofCountNotification >= 1) {
                            imageVIewNotificationCount.visibility = View.VISIBLE
                        } else {
                            imageVIewNotificationCount.visibility = View.GONE
                        }
                        showDrawerNavigationScreen(dashboardFragment, "")
                    } else {
                        isHomePage = false
                        var size = noticeList.size - 1
                        if (size >= 0) {
                            binding.noticeRl.visibility = View.VISIBLE
                        } else if (size < 0) {
                            binding.noticeRl.visibility = View.GONE
                        }
                        homeFragment = HomeFragment.newInstance("", questionString ?: "")
                        showDrawerNavigationScreen(homeFragment, "")
                        try {
                            homeFragment.populateNotificationQuestion(questionString)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }

                    binding.toolbar.drawerTitle.text = ""
                    binding.toolbar.appCompatTextViewLogo.visibility = View.VISIBLE
                    binding.toolbar.frameLayoutNotificationIcon.visibility = View.VISIBLE
                    binding.toolbar.textViewFilterHome.visibility = View.GONE
                    binding.toolbar.relativeLayoutNewSearch.visibility = View.GONE
                    binding.toolbar.searchingLayout.visibility = View.VISIBLE
                    setBiasToHamburger(true)
                    binding.toolbar.optionsIcon2.visibility = View.VISIBLE
                    binding.toolbar.tvFilterLabel.visibility = View.VISIBLE
                }
                // Show gist if available
                if (position == 1) {
                    binding.toolbar.frameLayoutNotificationIcon.visibility = View.GONE
                    binding.toolbar.frameLayoutLifeIcon.visibility = View.VISIBLE
                    binding.toolbar.optionsIcon3.visibility = View.VISIBLE
                    binding.toolbar.lifeamount.visibility = View.VISIBLE
                }

                if (position == 2) {
                    manageGist(storedGist)
                    binding.toolbar.frameLayoutNotificationIcon.visibility = View.VISIBLE
                    binding.toolbar.frameLayoutLifeIcon.visibility = View.GONE
                    binding.toolbar.optionsIcon3.visibility = View.GONE
                    binding.toolbar.lifeamount.visibility = View.GONE
                }

                if (position == 3) {
                    imageVIewNotificationCount.visibility = View.GONE
                    binding.toolbar.frameLayoutNotificationIcon.visibility = View.VISIBLE
                    binding.toolbar.frameLayoutLifeIcon.visibility = View.GONE
                    binding.toolbar.optionsIcon3.visibility = View.GONE
                    binding.toolbar.lifeamount.visibility = View.GONE
                }
            }
        })
    }

    private fun modifyClickBehaviourTab() {
        // Overriding tab click behaviour
        binding.homeTabLayout.getTabAt(0)?.customView?.setOnClickListener {
            switchToBottomNavView()
            var dashboardFragment = DashboardFragment.newInstance()
            showDrawerNavigationScreen(dashboardFragment, "")
        }

        binding.homeTabLayout.getTabAt(1)?.customView?.setOnClickListener {
            switchToBottomNavView()
            binding.homeViewPager.currentItem = 1
        }

        if (dataManager.mPref.prefGetLoginMode()) {
            val tabStrip: LinearLayout = binding.homeTabLayout.getChildAt(0) as LinearLayout
            tabStrip.getChildAt(2).setOnTouchListener(null)
            tabStrip.getChildAt(3).setOnTouchListener(null)

            binding.homeTabLayout.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    switchToBottomNavView()
                    binding.progressBarMain.visibility = View.GONE
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    switchToBottomNavView()
                }
            })
        } else {
            val tabStrip: LinearLayout = binding.homeTabLayout.getChildAt(0) as LinearLayout
            tabStrip.getChildAt(2).setOnTouchListener(OnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (!dataManager.mPref.prefGetLoginMode())
                        showLoginDialog()
                }
                true
            }
            )
            tabStrip.getChildAt(3).setOnTouchListener(OnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (!dataManager.mPref.prefGetLoginMode())
                        showLoginDialog()
                }
                true
            })
        }
    }

    private fun setBiasToHamburger(isHome: Boolean) {
        val bias = if (isHome) 0.31f else 0.5f
        val params: ConstraintLayout.LayoutParams =
            binding.toolbar.drawerNavigationIcon.layoutParams as ConstraintLayout.LayoutParams
        params.verticalBias =
            bias // here is one modification for example. modify anything else you want :)
        binding.toolbar.drawerNavigationIcon.layoutParams = params
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "fetchDeviceFcmToken" -> {
                val type = object : TypeToken<BaseModel<DeviceInfoResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<DeviceInfoResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        dataManager.mPref.prefSetPrefRefViewCount(
                            baseData.data?.popup_viewed ?: ""
                        )
                        if (loginCount != null) {
                            if (loginCount == "0" && dataManager.mPref.prefGetPrefRefViewCount() == "0") {
                                showPromoCodeDialog()
                            }
                        }
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
                            dataManager.mPref.prefSetUserHeart(baseData.data?.user_hearts ?: "0")
                            dataManager.mPref.prefSetUserAdLimit(baseData.data?.user_settings?.user_ads_limit)
                            dataManager.mPref.prefSetToken(baseData.token)
                            if (userInfo?.category_id == null)
                                userInfo?.category_id = "1"

                            dataManager.mPref.prefLogin()
                            setProfileInfo(dataManager.mPref.prefGetUserInfo())
                            showAlertLoginSuccessful()
                            viewModel.apiGetUserInfo(this)
                            viewModel.fetchPerformanceSummery(this)
                            binding.navigationView.logout_nav_menu.visibility = View.VISIBLE
                            loginCount = baseData.data!!.user_settings?.login_count
                            sendDeviceInfo()
                        }
                    } else {
                        showToast(
                            this,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
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
                            dataManager.mPref.prefSetUserHeart(baseData.data?.user_hearts ?: "0")
                            dataManager.mPref.prefSetUserAdLimit(baseData.data?.user_settings?.user_ads_limit)
                            dataManager.mPref.prefSetToken(baseData.token)
                            if (userInfo?.category_id == null) {
                                userInfo?.category_id = "1"
                            }
                            dataManager.mPref.prefLogin()
                            setProfileInfo(dataManager.mPref.prefGetUserInfo())
                            showAlertLoginSuccessful()
                            modifyClickBehaviourTab()
                            sendDeviceInfo()
                            viewModel.apiGetUserInfo(this)
                            viewModel.fetchPerformanceSummery(this)
                            loginCount = baseData.data!!.user_settings?.login_count
                            binding.navigationView.logout_nav_menu.visibility = View.VISIBLE
                        }
                    } else {
                        showToast(
                            this,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiGetFindUs" -> {
                val type = object : TypeToken<BaseModel<FindUsItem>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<FindUsItem>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            try {
                                findUsItem = baseData.data!!
                                manageUrls("facebook", binding.navigationView.facebook_layout)
                                manageUrls("youtube", binding.navigationView.youtube_layout)
                                manageUrls("twitter", binding.navigationView.twitter_layout)
                                manageUrls("linkedIn", binding.navigationView.linked_in_layout)
                                manageUrls("instagram", binding.navigationView.instagram_in_layout)
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        showToast(
                            this,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "fetchPerformanceSummery" -> {
                val type = object :
                    com.google.common.reflect.TypeToken<BaseModel<PerformanceSummery>>() {}.type
                result.data?.body()?.let {
                    val baseModel = Gson().fromJson<BaseModel<PerformanceSummery>>(
                        result.data.body()?.string(), type
                    )
                    if (baseModel.status_code == 200 && baseModel.data != null) {
                        try {
                            performanceSummery = baseModel.data!!
                            notificationCount = baseModel.new_notification
                            updateNotificationCounts(notificationCount)
                        } catch (e: NullPointerException) {
                            e.printStackTrace()

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
                        showToast(
                            this,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
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
                        showToast(
                            this,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "getRefToken" -> {
                // Dummy
                val type = object : TypeToken<BaseModel<ReferenceResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ReferenceResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                inviteFriends(baseData.data!!.reference_token)
                                dataManager.mPref.setReferenceCode(baseData.data?.reference_token)
                            }
                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            "apiNoticeBoard" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Notice>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Notice>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                noticeList = baseData.data!!
                                if (noticeList.size > 0)
                                    bindNotices()
                                else
                                    binding.noticeRl.visibility = View.GONE

                            }
                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            "apiGetUserInfo" -> {
                val type = object : TypeToken<BaseModel<GameUserInfo>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameUserInfo>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                bindUserUpdatedInfo(baseData.data!!)
                                viewModel.dataGetUserHearts()
                                Log.d(
                                    "Current Hearts->",
                                    dataManager.mPref.getUserGlobalSetting()?.hearts_settings?.practice_hearts
                                        ?: "0"
                                )
                            }
                        } catch (ex: java.lang.Exception) {
                            ex.printStackTrace()
                        }
                    } else {
                        showToast(this@MainActivity, baseData.message[0])
                    }
                }
            }
        }

        onLoading(false, "All")
    }


    private fun bindNotices() {
        binding.noticeRl.layoutManager = LinearLayoutManager(this)
        binding.noticeRl.adapter =
            BaseRecyclerAdapter<Notice>(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                    when (view.id) {
                        R.id.btnlayout -> {
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    NoticeDetailsActivity::class.java
                                ).putExtra("noticevalues", Gson().toJson(noticeList[position]))
                            )
                        }
                        R.id.imageView16 -> {
                            changeNotificationDataset(position)
                        }
                    }
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return NoticeAdapter(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_notice_board,
                            parent,
                            false
                        ), this@MainActivity
                    )
                }

                override fun loadMoreItem() {

                }

            }, noticeList)

    }

    private fun changeNotificationDataset(position: Int) {
        noticeList.removeAt(position)

        binding.noticeRl.adapter?.notifyDataSetChanged()
        var size = noticeList.size - 1
        if (size < 0) {
            binding.noticeRl.visibility = View.GONE
        }
    }

    private fun updateNotificationCounts(notificationCount: Int) {
        if (isFirstTime) {
            if (notificationCount >= 1) {
                imageVIewNotificationCount.visibility = View.VISIBLE
                numofCountNotification = notificationCount
            }
            isFirstTime = false
        }
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "dataGetUserHearts" -> {
                currentHearts = dataManager.mPref.prefGetUserHeart()?.toInt() ?: 0
                val hearts = result as List<GameHeartDTO>
                hearts.forEach {
                    if (it.heart_type == GAME_HEART_MINUS) {
                        currentHearts -= 1
                    } else {
                        if (!it.practice.isNullOrEmpty()) {
                            currentHearts += if (it.practice == "0")
                                dataManager.mPref.getUserGlobalSetting()?.hearts_settings?.practice_hearts?.toInt()
                                    ?: 0
                            else
                                dataManager.mPref.getUserGlobalSetting()?.hearts_settings?.practice_random_hearts?.toInt()
                                    ?: 0
                        }
                    }
                }
                configureHearts()
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {

    }

    private fun configureHearts() {
        binding.navHeader.lifeamounts.text = ": $currentHearts"
        binding.toolbar.lifeamount.text = currentHearts.toString()
        var gameLevel = dataManager.mPref.prefGetGameCurrentLevel()?.level
        binding.navHeader.textView3.text = "level - $gameLevel"
        binding.navigationView.lifeamountlayout.visibility = View.VISIBLE
        binding.navHeader.textView3.visibility = View.VISIBLE
    }

    private fun bindUserUpdatedInfo(gameUserInfo: GameUserInfo) {
        gameUserInfo.run {
            dataManager.mPref.prefSetIsAdFree(user_settings.ads_free == "1")
            dataManager.mPref.prefSetUserAdLimit(user_settings.user_ads_limit)
            dataManager.mPref.prefSetGameCurrentLevel(current_level)
            dataManager.mPref.prefSetCurrentSubscription(hearts_subscription)
            dataManager.mPref.setGameUserSetting(user_settings)
            dataManager.mPref.setUserGlobalSetting(gameUserInfo.global_settings!!)
            dataManager.mPref.prefSetUserHeart(gameUserInfo.current_hearts!!)
            dataManager.mPref.prefSetHeartGift(hearts_gift)
        }
    }

    private fun dateHourDifference(d1: Date, d2: Date): Int {
        val differenceInTime: Long = d2.time - d1.time
        val hours: Long = ((differenceInTime
                / (1000 * 60 * 60))
                % 24)
        return hours.toInt()
    }

    private fun showNoticeBoard() {
        if (noticeList.isEmpty()) return
        try {
            val transaction = supportFragmentManager.beginTransaction()
            noticeBoardDialog = NoticeBoardDialog.newInstance(noticeList)
            if (noticeBoardDialog?.isAdded == false) {
                noticeBoardDialog?.show(transaction, "LoginDialog")

                dataManager.mPref.setNoticeTracking(NoticeTracking())
            }
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun inviteFriends(referenceToken: String) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        share.putExtra(Intent.EXTRA_SUBJECT, "Download " + getString(R.string.app_name))
        share.putExtra(
            Intent.EXTRA_TEXT,
            "Hello, I am learning from GuruKlub, you can also learn\n\nPlease use this REFERRAL CODE : \"$referenceToken\" after Download GuruKlub from https://play.google.com/store/apps/details?id=${packageName} '\n'"
        )
        startActivity(
            Intent.createChooser(
                share,
                "Download " + getString(R.string.app_name)
            )
        )
    }

    private fun showPromoCodeDialog() {
        inviteFriendPromoCodeDialog.show(
            supportFragmentManager,
            inviteFriendPromoCodeDialog.tag
        )
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (key == "Google" || key == "Facebook") {
            if (isLoader) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onError(err: Throwable, key: String) {
        onLoading(false, "All")
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.toolbar.drawerNavigationIcon -> {
                binding.drawerLayout.openDrawer(Gravity.LEFT)
            }
            binding.toolbar.optionsIcon2 -> {
                if (adapter.getItem(0) is DashboardFragment || adapter.getItem(0) is HomeFragment) {
                    if (!dataManager.mPref.prefGetLoginMode()) {
                        showLoginDialog()
                        return
                    }
                    numofCountNotification = 0
                    binding.toolbar.ivNotificationCount.visibility = View.GONE
                    startActivity(Intent(this, NotificationActivity::class.java))
                }
            }
            binding.toolbar.textViewFilterHome -> {
                if (binding.homeViewPager.currentItem == 0) {
                    if (adapter.getItem(0) is HomeFragment) {
                        if (!dataManager.mPref.prefGetLoginMode()) {
                            showLoginDialog()
                            return
                        }
                        val homeFrag = adapter.getItem(0) as HomeFragment
                        homeFrag.checkShouldOpenFilter()
                    }
                }
            }
            binding.toolbar.relativeLayoutNewSearch -> {
                if (adapter.getItem(0) is HomeFragment) {
                    if (!dataManager.mPref.prefGetLoginMode()) {
                        showLoginDialog()
                        return
                    }
                    startActivity(Intent(this, QuestionSearchActivity::class.java))
                }
            }

            binding.navigationView.logout_nav_menu -> {
                auth = FirebaseAuth.getInstance()
                if (auth.currentUser != null) {
                    auth.signOut()
                }
                dataManager.mPref.prefLogout(this)
                finishAffinity()
            }
            binding.navigationView.profile_nav_menu -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }

                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }

            binding.navigationView.leaderboard_nav_menu -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }

                val intent = Intent(this, LeaderBoardActivity::class.java)
                startActivity(intent)

            }

            binding.navigationView.error_nav_menu -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }

                val intent = Intent(this, ErrorActivity::class.java)
                startActivity(intent)
            }

            binding.navigationView.bookmarks_nav_menu -> {

                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }

                val intent = Intent(this, FavouriteActivity::class.java)
                startActivity(intent)
            }

            binding.navigationView.recent_performance_nav_menu -> {
                if (this::performanceSummery.isInitialized) {
                    if (!dataManager.mPref.prefGetLoginMode()) {
                        showLoginDialog()
                        return
                    }
                    startActivity(
                        Intent(this, GameResultActivity::class.java)
                            .putExtra("from_activity", "profile")
                            .putExtra(
                                "game_id",
                                performanceSummery.recent_game_id
                            )
                    )
                } else {
//                    Toast.makeText(this, "Please try again later....", Toast.LENGTH_SHORT).show()
                    if (!dataManager.mPref.prefGetLoginMode()) {
                        showLoginDialog()
                        return
                    }

                }
            }
            binding.navigationView.profile_nav_performance_history -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }

                val intent = Intent(this, PerformanceHistoryActivity::class.java)
                startActivity(intent)
            }

            binding.navigationView.settings_nav_menu -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }

                showDrawerNavigationScreen(SettingsFragment(), "Settings")
                closeDrawer()
            }


            binding.navigationView.dashboard_nav_menu -> {
                if (binding.navigationView.dashboard_linearlayout.visibility == View.VISIBLE) {
                    binding.navigationView.dashboard_linearlayout.visibility = View.GONE

                    //toggleDashboard(false)
                } else if (binding.navigationView.dashboard_linearlayout.visibility == View.GONE) {
                    toggleDashboard(true)
                }
                binding.navigationView.help_and_support_linearlayout.visibility = View.GONE
            }

            binding.navigationView.help_Feed_nav_menu -> {
                if (binding.navigationView.help_and_support_linearlayout.visibility == View.VISIBLE) {
                    binding.navigationView.help_and_support_linearlayout.visibility = View.GONE

                } else if (binding.navigationView.help_and_support_linearlayout.visibility == View.GONE) {
                    toggleHelpAndSupport(true)
                }
                binding.navigationView.dashboard_linearlayout.visibility = View.GONE

            }

            binding.navigationView.info_center_nav_menu -> {
                showDrawerNavigationScreen(InfoCentreFragment(), "Info Centre")
            }

            binding.navigationView.allVideosLayout -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }

                val intent = Intent(this, AllVideosActivity::class.java)
                startActivity(intent)
            }
            binding.navigationView.previousQuestionLayout -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }

                startActivity(
                    Intent(this, PreviousQuestionBatchFragment::class.java)
                )
            }


            binding.navigationView.purchase_now_btn -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }
                startActivity(
                    Intent(this, HeartAddActivity::class.java).putExtra(
                        "is_from_game",
                        "no"
                    )
                )
            }

            binding.toolbar.frameLayoutLifeIcon ->{
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }
                startActivity(
                    Intent(this, HeartAddActivity::class.java).putExtra(
                        "is_from_game",
                        "no"
                    )
                )
            }

            binding.navigationView.referbtn -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }
                showAlertDialog("reference_code")
            }


            binding.toolbar.editText2 -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                } else {
                    startActivity(Intent(this, QuestionSearchActivity::class.java))
                }
            }

            binding.navigationView.contact_us_layout1 -> {
                contactusBottomsheet =
                    ContactUsBottomSheet(dataManager.mPref.prefGetUserInfo().email, this)
                contactusBottomsheet.show(supportFragmentManager, contactusBottomsheet.tag)
            }

            binding.navigationView.navigationView.new_user_faq_layout2 -> {
                startActivity(
                    Intent(this, ContentActivity::class.java).putExtra(
                        ContentActivity.ACTIVITY_TITLE,
                        ContentActivity.TITLE_FAQ
                    )
                )
            }

            binding.navigationView.about_xplore_layout2 -> {
                startActivity(
                    Intent(this, ContentActivity::class.java).putExtra(
                        ContentActivity.ACTIVITY_TITLE,
                        ContentActivity.TITLE_ABOUT
                    )
                )
            }

            binding.navigationView.rating_layout2 -> {
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


            binding.navigationView.invite_friends_layout2 -> {

                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }

                val getReferenceToken = dataManager.mPref.getReferenceToken()
                if (getReferenceToken == null) {
                    viewModel.getRefToken(this)
                } else {
                    val share = Intent(Intent.ACTION_SEND)
                    share.type = "text/plain"
                    share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                    share.putExtra(Intent.EXTRA_SUBJECT, "Download " + getString(R.string.app_name))
                    share.putExtra(
                        Intent.EXTRA_TEXT,
                        "Hello, I am learning from GuruKlub, you can also learn\n\nPlease use this REFERRAL CODE : \"$getReferenceToken\" after Download GuruKlub from https://play.google.com/store/apps/details?id=${packageName} '\n'"
                    )
                    startActivity(
                        Intent.createChooser(
                            share,
                            "Download " + getString(R.string.app_name)
                        )
                    )
                }
            }

            binding.navigationView.settings_nav_menu -> {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                    return
                }
                closeDrawer()
            }

            binding.navigationView.terms_condition_layout2 -> {
                val intent = Intent(this, TermsConditionActivity::class.java)
                intent.putExtra(ACTIVITY_NAME, "content")
                startActivity(intent)
            }

        }
    }

    private fun toggleHelpAndSupport(show: Boolean) {
        val transition: Transition = Slide(Gravity.TOP)
        transition.duration = 300
        transition.addTarget(binding.navigationView.help_and_support_linearlayout)
        TransitionManager.beginDelayedTransition(
            binding.navigationView.help_Feed_nav_menu,
            transition
        )
        binding.navigationView.help_and_support_linearlayout.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    private fun toggleDashboard(show: Boolean) {
        val transition: Transition = Slide(Gravity.TOP)
        transition.duration = 300
        transition.addTarget(binding.navigationView.dashboard_linearlayout)
        TransitionManager.beginDelayedTransition(
            binding.navigationView.dashboard_nav_menu, transition
        )
        binding.navigationView.dashboard_linearlayout.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    private fun manageUrls(type: String, layout: View) {
        if (type.isNotEmpty()) {
            var url = ""

            when (type) {
                "facebook" -> {
                    url = findUsItem.facebook
                }
                "youtube" -> {
                    url = findUsItem.youtube
                }
                "twitter" -> {
                    url = findUsItem.twitter
                }
                "linkedIn" -> {
                    url = findUsItem.linked_in
                }
                "instagram" -> {
                    url = findUsItem.instagram
                }
            }

            if (url.isNotEmpty()) {
                layout.visibility = View.VISIBLE
                layout.setOnClickListener {
                    try {
                        val browserIntent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(browserIntent)
                    } catch (ex: ActivityNotFoundException) {
                        // Notify the user?
                        Toast.makeText(this, "No app found for this action.", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            } else {
                layout.visibility = View.GONE
            }
        } else {
            layout.visibility = View.GONE
        }
    }

    private fun showLoginDialog() {
        try {
            val transaction = supportFragmentManager.beginTransaction()
            if (!loginDialog.isAdded)
                loginDialog.show(transaction, "LoginDialog")
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun showDrawerNavigationScreen(fragment: BaseFragment, title: String) {
        switchToDrawerNavView()
        binding.toolbar.drawerTitle.text = title
        val transaction =
            supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout_nav_container, fragment, title)
        if (fragment is HomeFragment) {
            if (binding.homeViewPager.currentItem != 0) {
                isHomePage = true
                binding.homeViewPager.currentItem = 0
            }
            binding.toolbar.appCompatTextViewLogo.visibility = View.VISIBLE
            binding.toolbar.frameLayoutNotificationIcon.visibility = View.GONE
            binding.toolbar.frameLayoutLifeIcon.visibility = View.VISIBLE
            binding.toolbar.optionsIcon3.visibility = View.VISIBLE
            binding.toolbar.lifeamount.visibility = View.VISIBLE
            binding.toolbar.textViewFilterHome.visibility = View.VISIBLE
            binding.toolbar.relativeLayoutNewSearch.visibility = View.VISIBLE
            binding.toolbar.searchingLayout.visibility = View.GONE
            setBiasToHamburger(true)

            binding.toolbar.optionsIcon2.visibility = View.VISIBLE
            binding.toolbar.tvFilterLabel.visibility = View.VISIBLE
            binding.toolbar.textViewFilterHome.setOnClickListener {
                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                } else {
                    homeFragment.checkShouldOpenFilter()
                }
            }

            binding.toolbar.relativeLayoutNewSearch.setOnClickListener {

                if (!dataManager.mPref.prefGetLoginMode()) {
                    showLoginDialog()
                } else {
                    startActivity(Intent(this, QuestionSearchActivity::class.java))
                }

            }
        } else if (fragment is DashboardFragment) {
            binding.homeViewPager.currentItem = 0
            binding.toolbar.drawerTitle.text = ""
            //binding.toolbar.appCompatImageViewLogo.visibility = View.VISIBLE
            binding.toolbar.appCompatTextViewLogo.visibility = View.VISIBLE
            binding.toolbar.frameLayoutNotificationIcon.visibility = View.VISIBLE
            binding.toolbar.textViewFilterHome.visibility = View.GONE
            binding.toolbar.relativeLayoutNewSearch.visibility = View.GONE
            binding.toolbar.searchingLayout.visibility = View.VISIBLE
            setBiasToHamburger(true)
            binding.toolbar.frameLayoutNotificationIcon.visibility = View.VISIBLE
            binding.toolbar.frameLayoutLifeIcon.visibility = View.VISIBLE
            binding.toolbar.optionsIcon3.visibility = View.VISIBLE
            binding.toolbar.lifeamount.visibility = View.VISIBLE
            binding.toolbar.optionsIcon2.visibility = View.VISIBLE
            binding.toolbar.tvFilterLabel.visibility = View.VISIBLE
        }
        transaction.commit()
        closeDrawer()
    }

    fun navigateProfile(fragment: BaseFragment, title: String, navigateTo: Int) {
        switchToDrawerNavView()
        var profileFragment = supportFragmentManager.findFragmentByTag("Profile")
        binding.toolbar.drawerTitle.text = title
        val transaction =
            supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout_nav_container, fragment, title)
        transaction.commit()
        closeDrawer()

        if (profileFragment != null) {
            Log.d("profile_status->", "Still attached!")
            profileFragment = profileFragment as ProfileMainFragment
            profileFragment.navigate(navigateTo)
        } else {
            //Log.d("profile_status->", "Detached!")
            //showDrawerNavigationScreen(fragment, title)
        }

    }

    private fun switchToDrawerNavView() {
        binding.frameLayoutNavContainer.visibility = View.VISIBLE
        binding.homeViewPager.visibility = View.GONE
        binding.rlAdContainer.visibility = View.GONE
        binding.toolbar.optionsIcon2.visibility = View.GONE
        binding.toolbar.frameLayoutNotificationIcon.visibility = View.GONE
        binding.toolbar.tvFilterLabel.visibility = View.GONE
        binding.toolbar.appCompatTextViewLogo.visibility = View.GONE
        binding.toolbar.textViewFilterHome.visibility = View.GONE
        binding.toolbar.relativeLayoutNewSearch.visibility = View.GONE
        setBiasToHamburger(false)
        if (binding.toolbar.llGist.visibility == View.VISIBLE)
            binding.toolbar.llGist.visibility = View.GONE
    }

    private fun switchToBottomNavView() {
        binding.homeViewPager.visibility = View.VISIBLE
        binding.frameLayoutNavContainer.visibility = View.GONE
        binding.rlAdContainer.visibility = View.VISIBLE
        // binding.linearLayoutAdContainer.visibility = View.VISIBLE
        if (binding.homeViewPager.currentItem == 0) {
            binding.toolbar.drawerTitle.text = ""
            binding.toolbar.optionsIcon2.visibility = View.VISIBLE
            binding.toolbar.tvFilterLabel.visibility = View.VISIBLE
            // binding.toolbar.appCompatImageViewLogo.visibility = View.VISIBLE
            binding.toolbar.appCompatTextViewLogo.visibility = View.VISIBLE
            binding.toolbar.frameLayoutNotificationIcon.visibility = View.VISIBLE
            binding.toolbar.ivNotificationCount.visibility = View.GONE
            setBiasToHamburger(true)
            binding.toolbar.textViewFilterHome.visibility = View.GONE
            binding.toolbar.relativeLayoutNewSearch.visibility = View.GONE
            binding.toolbar.searchingLayout.visibility = View.VISIBLE
        } else {
            binding.toolbar.textViewFilterHome.visibility = View.GONE
            binding.toolbar.appCompatTextViewLogo.visibility = View.GONE
            binding.toolbar.relativeLayoutNewSearch.visibility = View.GONE
            binding.toolbar.optionsIcon2.visibility = View.GONE
            binding.toolbar.frameLayoutNotificationIcon.visibility = View.GONE
            binding.toolbar.drawerTitle.text =
                adapter.getPageTitle(binding.homeViewPager.currentItem)
        }
        if (binding.toolbar.llGist.visibility == View.VISIBLE)
            binding.toolbar.llGist.visibility = View.GONE
    }

    private fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (binding.homeViewPager.currentItem != 0) {
            binding.homeViewPager.currentItem = 0
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    override fun onResume() {
        super.onResume()

        if (ConnectivityUtil.isOnline(this))
            viewModel.apiGetUserInfo(this)
        else
            viewModel.dataGetUserHearts()

        var show = dataManager.mPref.prefGetRatingDialogShow()
        if (shouldShowRatingDialog && show && !checkIfShownWithinThisDay()) {
            showRateUsDialog()
            storeDateOfRatingShow()
            shouldShowRatingDialog = false
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(GAME_SCREEN))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(HOME_SCREEN))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(HOME_QUESTION))
        //LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter(LIBRARY_SCREEN_MAIN))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(NOTIFICATION_DETAILS))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(NO_ACTION))
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
        }
    }

    fun changeFragment(position: Int) {
        switchToBottomNavView()
        binding.toolbar.optionsIcon2.visibility = View.VISIBLE
        binding.toolbar.frameLayoutNotificationIcon.visibility = View.VISIBLE
        binding.homeViewPager.currentItem = position
    }

    fun changeFragmentLibrary() {
        switchToBottomNavView()
        binding.homeViewPager.currentItem = 2
        try {
            val libraryFragmentNew = adapter.getItem(2) as LibraryFragmentNew?
            libraryFragmentNew?.blinkTestExamButton()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun onGoogleLogin() {
        selectedLoginFlow = GOOGLE_LOGIN
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            askPhoneStateReadPermission()
        } else {
            signIn()
            onLoading(true, "Google")
        }
        loginDialog.dismiss()
    }


    override fun onFacebookLogin() {
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
        loginDialog.dismiss()
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
                        !this@MainActivity.shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)
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

    private fun proceedToLoginFlow() {
        when (selectedLoginFlow) {
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

    override fun onRegularLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    private fun showAlertLoginSuccessful() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Login Successful")
            .setMessage("You have successfully logged into GuruKlub. Now you can access all the exciting features!")
            .setPositiveButton(
                "Okay"
            ) { dialog, which ->
                dialog.dismiss()
            }.show()
    }

    override fun listenShowLoginDialog() {
        showLoginDialog()
    }

    override fun updateNotificationCount(count: Int, isFromMore: Boolean) {
        if (isFirstTime || isFromMore) {
            if (count > 1)
                imageVIewNotificationCount.visibility = View.VISIBLE
            isFirstTime = false
        }
    }

    override fun updateGameHeart(addAmount: String, type: Int) {
        try {
            var refinedAmount = addAmount.toInt() * if (type == GAME_HEART_MINUS) -1 else 1
            var addedheart = binding.toolbar.lifeamount.text.toString().toInt() + refinedAmount
            binding.toolbar.lifeamount.text = addedheart.toString()
            binding.toolbar.lifeamount.text = addedheart.toString()
            binding.navHeader.lifeamounts.text = addedheart.toString()
            animateHeartUpdates(addAmount, type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun showGist(gist: String) {
        manageGist(gist)
    }

    private fun manageGist(gist: String) {
        if (this::binding.isInitialized) {
            if (gist.isNotEmpty()) {
                binding.toolbar.llGist.visibility = View.VISIBLE
                binding.toolbar.llGist.setOnClickListener {
                    showMyAlert(gist)
                }
                storedGist = gist
            } else {
                storedGist = ""
                binding.toolbar.llGist.visibility = View.GONE
            }
        }
    }

    private fun showMyAlert(gist: String) {
        try {
            var builder = androidx.appcompat.app.AlertDialog.Builder(this)
            val alert: androidx.appcompat.app.AlertDialog? = builder.create()
            val my_view = layoutInflater.inflate(R.layout.dialog_box, null)
            alert?.setView(my_view)
            alert?.setCancelable(false)

            my_view.ivCross.setOnClickListener {
                alert?.dismiss()
                binding.toolbar.llGist.isEnabled = true
            }

            my_view.tvGist.text = Html.fromHtml(gist)
            my_view.tvGist.movementMethod = LinkMovementMethod()

            alert?.show()
            alert?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onContactUsSendBtnClicked(email: String, message: String) {
        viewModel.apiContactUs(email, message, this)
    }

    override fun onInviteBtnClicked(emails: String) {
        viewModel.apiInviteFriends(dataManager.mPref.prefGetUserInfo().email, emails, this)
    }

    override fun onNoticeDismiss() {
        MyApp.instance.displayNoticeBoard = true
    }

    override fun onPracticeClicked() {
        initHomeFragment()
    }

    private fun initHomeFragment() {
        if (questionString.isNullOrEmpty()) {
            homeFragment = HomeFragment.newInstance("")
            showDrawerNavigationScreen(homeFragment, "")
        } else {
            homeFragment = HomeFragment.newInstance("", questionString ?: "")
            showDrawerNavigationScreen(homeFragment, "")
            try {
                homeFragment.populateNotificationQuestion(questionString)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun onGameIconClicked() {
        if (!ConnectivityUtil.isOnline(this)) {
            showNoNetDialog("")
            return
        }
        if (!dataManager.mPref.prefGetLoginMode()) {
            showLoginDialog()
        } else if (dataManager.mPref.prefGetUserInfo().name.isNullOrEmpty() || dataManager.mPref.prefGetUserInfo().name == "") {
            showAlertDialog("profile_update")
        } else {
            startActivity(Intent(this, GameLevelActivity::class.java))
        }
    }

    override fun onLiveExamIconClicked() {
        switchToBottomNavView()
        binding.homeViewPager.currentItem = 1
    }

    override fun onModelTestIconClicked() {
        switchToBottomNavView()
        binding.homeViewPager.currentItem = 1
    }

    override fun onPreviousQuestionIconClicked() {
        if (!dataManager.mPref.prefGetLoginMode()) {
            showLoginDialog()
            return
        }

        startActivity(
            Intent(this, PreviousQuestionBatchFragment::class.java)
        )
    }

    override fun onCurrentAffairsIconClicked() {
        if (!dataManager.mPref.prefGetLoginMode()) {
            showLoginDialog()
        } else {
            binding.homeViewPager.currentItem = 3
        }
    }

    override fun onLibraryIconClicked() {
        if (!dataManager.mPref.prefGetLoginMode()) {
            showLoginDialog()
        } else {
            startActivity(Intent(this, AllLibrarySubject::class.java).putExtra("isSubject", "yes"))
        }
    }

    override fun onAllVideoIconClicked() {
        if (!dataManager.mPref.prefGetLoginMode()) {
            showLoginDialog()
            return
        }

        val intent = Intent(this, AllVideosActivity::class.java)
        startActivity(intent)

    }

    override fun onAddQuestionIconClicked() {
        if (dataManager.mPref.prefGetLoginMode()) {
            dataManager.mPref.prefNavigateFromGame(false)
            startActivity(
                Intent(
                    this,
                    QuestionAddActivity::class.java
                )
            )

        } else {
            showLoginDialog()
        }
    }

    fun showNoNetDialog(err: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("No Connection")
            .setMessage(
                "GuruKlub is facing some internet connectivity issues. " +
                        "Please check your internet connection. You can also browse our Offline Question set."
            )
            .setPositiveButton(
                "Go to home"
            ) { dialog, which ->
                dialog.dismiss()
                navigateToHome()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }.show()
    }

    private fun showAlertDialog(comingFor: String) {
        if (comingFor == "profile_update") {
            var updateProfileDialog =
                UpdateProfileDialog.newInstance(
                    "You need to update your profile before start the game. Do You want to update your profile?",
                    comingFor
                )
            updateProfileDialog.show(supportFragmentManager, updateProfileDialog.tag)
        } else if (comingFor == "reference_code") {
            var updateProfileDialog =
                UpdateProfileDialog.newInstance(getString(R.string.invite_friend_text2), comingFor)
            updateProfileDialog.show(supportFragmentManager, updateProfileDialog.tag)
        }
    }

    private fun showRateUsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Rate Us")
            .setMessage(
                "If you like GuruKlub, please give us 5 Star in play store! Your opinion is invaluable to us."
            )
            .setPositiveButton(
                "Rate"
            ) { dialog, _ ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
                dataManager.mPref.prefSetRatingDialogShow(false)
                dialog.dismiss()
            }
            .setNegativeButton("Not now") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Never") { dialog, _ ->
                dataManager.mPref.prefSetRatingDialogShow(false)
                dialog.dismiss()
            }.show()
    }


    override fun onOkShareBtnClicked() {
        val getReferenceToken = dataManager.mPref.getReferenceToken()
        if (getReferenceToken == null) {
            viewModel.getRefToken(this)
        } else {
            val share = Intent(Intent.ACTION_SEND)
            share.type = "text/plain"
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            share.putExtra(Intent.EXTRA_SUBJECT, "Download " + getString(R.string.app_name))
            share.putExtra(
                Intent.EXTRA_TEXT,
                "Hello, I am learning from GuruKlub, you can also learn\n\nPlease use this REFERRAL CODE : \"$getReferenceToken\" after Download GuruKlub from https://play.google.com/store/apps/details?id=${packageName} '\n'"
            )
            startActivity(
                Intent.createChooser(
                    share,
                    "Download " + getString(R.string.app_name)
                )
            )
        }
    }

    override fun onExamStart() {
        shouldShowRatingDialog = true
    }

}

