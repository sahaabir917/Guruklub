package com.gmpire.guruklub.view.activity.library

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.library.YoutubeMain
import com.gmpire.guruklub.data.model.library.YoutubeObject
import com.gmpire.guruklub.databinding.ActivitySingleVideoBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.BottomSheet.ReportBottomSheet
import com.gmpire.guruklub.view.activity.login.AddVideoActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBar
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit


private const val AD_SHOW_SEC = 30
class SingleVideoActivity : BaseActivity(), ReportBottomSheet.IBottomSheetDialogClicked,
    OnSeekBarChangeListener {
    private lateinit var viewModel: SingleVideoViewModel
    private lateinit var binding: ActivitySingleVideoBinding
    private var subscribe: Disposable? = null
    private var videoId: String? = null
    private var suggestedList = arrayListOf<YoutubeObject>()
    private var youTubePlayer: YouTubePlayer? = null
    private var videoTitle = ""
    private var videosId: String? = null
    private var isAdShown = false
    private var isAdsFree: Boolean? = false
    private lateinit var reportBottomSheet: ReportBottomSheet
    private var currentPlayingSecond = 0f
    private lateinit var youtubePlayerSeekBar: YouTubePlayerSeekBar
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var disposable: Disposable
    private var isForwardButtonPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_single_video)

        setToolbar(this, binding.toolbarVideo, "", true)
        binding.toolbarVideo.appCompatTextViewLogo.visibility = View.VISIBLE
        val text =
            "<font color=#000000>Guru</font><font color=#4A148C>Klub</font>"
        binding.toolbarVideo.appCompatTextViewLogo.text = (Html.fromHtml(text))

        binding.addVideoLayout.setOnClickListener(this)
        binding.reportLayout.setOnClickListener(this)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(SingleVideoViewModel::class.java)

        if (intent.hasExtra("video_id")) {
            videoId = intent.extras?.getString("video_id")
            if (!videoId.isNullOrEmpty())
                initVideoView(videoId)
        }

        if (intent.hasExtra("video_title")) {
            val title = intent.extras?.getString("video_title")
            binding.textViewVideoTitle.text = title
        }

        if (intent.hasExtra("videos_id")) {
            videosId = intent.extras?.getString("videos_id")
        }

        this.lifecycle.addObserver(binding.youtubePlayerView)
        isAdsFree = dataManager.mPref.prefGetIsAdFree() ?: false
        if (isAdsFree == false) {
            buildInterstitialAd()
        }
    }

    private fun buildInterstitialAd() {
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.ad_unit_id_interstitial_test)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    private fun showSeekMsg(msg: String) {
        binding.tvSeekMsg.text = msg
        binding.tvSeekMsg.visibility = View.VISIBLE
        Handler().postDelayed({
            binding.tvSeekMsg.visibility = View.GONE
        }, 1000L)
    }

    private fun initVideoView(videoId: String?) {
        val youtubePlayerController = binding.youtubePlayerView.getPlayerUiController()

        youtubePlayerController.setCustomAction2(getDrawable(R.drawable.ic_forward)!!,
            View.OnClickListener {
                youTubePlayer?.seekTo(currentPlayingSecond + 10)
                showSeekMsg("5 Seconds forward")
                isForwardButtonPressed = true
            })
        youtubePlayerController.setCustomAction1(getDrawable(R.drawable.ic_backward)!!,
            View.OnClickListener {
                youTubePlayer?.seekTo(currentPlayingSecond - 10)
                showSeekMsg("5 Seconds backward")
                isForwardButtonPressed = true
            })


        if (youTubePlayer != null) {
            if (videoId != null) {
                youTubePlayer?.loadVideo(videoId, 0f)
            }
        } else {
            binding.youtubePlayerView.addYouTubePlayerListener(object :
                AbstractYouTubePlayerListener() {
                override fun onReady(@NonNull youTubePlayer: YouTubePlayer) {
                    if (videoId != null) {
                        this@SingleVideoActivity.youTubePlayer = youTubePlayer
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    currentPlayingSecond = second
                    if (second.toInt() == AD_SHOW_SEC) {
                        if (!isAdShown) {
                            if (mInterstitialAd.isLoaded) {
                                if (isAdsFree == false) {
                                    youTubePlayer.pause()
                                        mInterstitialAd.show()
                                }
                            } else {
                                Log.d("TAG", "The interstitial wasn't loaded yet.")
                            }
                            isAdShown = true
                        }
                    }
                    super.onCurrentSecond(youTubePlayer, second)
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    super.onStateChange(youTubePlayer, state)
                    if (state.name == "PAUSED")
                        removeTimer()
                    else if (state.name == "PLAYING") {
                        if(!isForwardButtonPressed)
                            setTimer()
                        isForwardButtonPressed = false
                    }
                }
            })

            binding.youtubePlayerView.addFullScreenListener(object :
                YouTubePlayerFullScreenListener {
                override fun onYouTubePlayerEnterFullScreen() {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    binding.relativeLayoutTBWrapper.visibility = View.GONE
                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    if (isAdsFree == false) {
                        buildInterstitialAd()
                    }
                    binding.titleLayout.visibility = View.GONE
                    binding.titleLayout.alpha = 0.0f
                }

                override fun onYouTubePlayerExitFullScreen() {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    binding.relativeLayoutTBWrapper.visibility = View.VISIBLE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    binding.titleLayout.visibility = View.VISIBLE
                    binding.titleLayout.alpha = 1f
                }
            })
        }

        subscribe =
            io.reactivex.Observable.fromCallable { viewModel.apiGetYoutubeSuggestedList(videoId!!, getString(R.string.youtube_v3_key)) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ processSuccessResponse(it) }, { processFailed(it) })

        // Seekbar related code
        youtubePlayerSeekBar =
            binding.youtubePlayerView.findViewById(com.pierfrancescosoffritti.androidyoutubeplayer.R.id.youtube_player_seekbar)
        val seekBar: SeekBar = youtubePlayerSeekBar.seekBar
        seekBar.setOnSeekBarChangeListener(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isAdsFree == false) {
            buildInterstitialAd()
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.titleLayout.visibility = View.GONE
            binding.titleLayout.alpha = 0.0f
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.titleLayout.visibility = View.VISIBLE
            binding.titleLayout.alpha = 1f
        }
    }


    private fun processSuccessResponse(youtubeMain: YoutubeMain?) {
        val items = youtubeMain?.items
        val youtubeData = arrayListOf<YoutubeObject>()
        items?.forEach {
            val youtubeFinal = YoutubeObject(
                it.id?.videoId,
                it.snippet?.title,
                it.snippet?.description,
                it.snippet?.thumbnails?.high?.url,
                it.snippet?.channelTitle, it.snippet?.publishTime
            )
            youtubeData.add(youtubeFinal)
        }

        if (suggestedList.isEmpty()) {
            var filtered = youtubeData.filter { !it.title.isNullOrEmpty() }
            suggestedList.addAll(filtered)
            binding.videoViewerRecycler.adapter =
                BaseRecyclerAdapter(this, object : IAdapterListener {
                    override fun <T> callBack(position: Int, model: T, view: View) {
                        model as YoutubeObject
                        when (view.id) {
                            R.id.linearLayout_suggested_vid_containers -> {
                                initVideoView(model.videoId)
                                videoTitle = model.title.toString()
                                isAdShown = false
                                buildInterstitialAd()
                            }
                        }
                    }

                    override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                        return YoutubeRelatedVideoViewHolder(
                            DataBindingUtil.inflate(
                                LayoutInflater.from(
                                    this@SingleVideoActivity
                                ), R.layout.item_related_videos, parent, false
                            ), this@SingleVideoActivity
                        )
                    }

                    override fun loadMoreItem() {
                    }

                }, suggestedList)
        } else {
            binding.textViewVideoTitle.text = videoTitle
            suggestedList.clear()
            suggestedList.addAll(youtubeData.filter { !it.title.isNullOrEmpty() })
            binding.videoViewerRecycler.adapter?.notifyDataSetChanged()
        }
    }

    private fun processFailed(throwable: Throwable) {
        throwable.printStackTrace()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.youtubePlayerView.release()
    }

    override fun onPause() {
        super.onPause()
        removeTimer()
    }

    override fun viewRelatedTask() {

    }

    override fun navigateToHome() {

    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiSendVideoReport" -> {
                val type = object : TypeToken<BaseModel<ArrayList<EmptyModel>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<EmptyModel>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        showToast(this, baseData.message[0])
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.addVideoLayout -> {
                val intent = Intent(this, AddVideoActivity::class.java)
                startActivity(intent)
            }
            binding.reportLayout -> {
                reportBottomSheet = ReportBottomSheet(videosId!!)
                reportBottomSheet.setBottomDialogListener(this, false, true)
                supportFragmentManager.let {
                    reportBottomSheet.show(
                        it,
                        reportBottomSheet.tag
                    )
                }
            }
        }

    }

    override fun onReportSubmitted(question_id: String, type: String, details: String) {

    }

    override fun onVideoReportSubmitted(videoID: String, type: String, details: String) {
        viewModel.apiSendVideoReport(videoID, details, this)
    }

    override fun onReportDialogDismiss() {

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        youtubePlayerSeekBar.onProgressChanged(seekBar!!, progress, fromUser)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        youtubePlayerSeekBar.onStartTrackingTouch(seekBar!!)
        removeTimer()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        youtubePlayerSeekBar.onStopTrackingTouch(seekBar!!)
    }

    private fun setTimer() {
        Log.d("Timer->", "Started!")
        if (!isAdShown) {
            disposable = Completable.timer(30, TimeUnit.SECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (mInterstitialAd.isLoaded) {
                        if (isAdsFree == false) {
                            youTubePlayer?.pause()
                            mInterstitialAd.show()
                        }
                    }
                    isAdShown = true
                }
        }
    }

    private fun removeTimer() {
        Log.d("Timer->", "Stopped!")
        if (this::disposable.isInitialized)
            disposable.dispose()
    }
}