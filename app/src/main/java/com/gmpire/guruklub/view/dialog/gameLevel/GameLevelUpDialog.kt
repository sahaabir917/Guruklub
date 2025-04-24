package com.gmpire.guruklub.view.dialog.gameLevel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.gamelevel.Level
import com.gmpire.guruklub.databinding.FragmentLevelUpBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.base.BaseDialogFragment
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class GameLevelUpDialog : BaseDialogFragment() {
    private lateinit var binding: FragmentLevelUpBinding
    private lateinit var actionListener: ActionListener
    private var level: Level? = null
    private var nextLevel: Level? = null
    var milestoneBonusHearts = 0
    override fun onAttach(context: Context) {
        super.onAttach(context)
        actionListener = context as ActionListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialog)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_level_up, container, false)
        return binding.root
    }

    override fun viewRelatedTask() {
        level = arguments?.getSerializable("level") as Level
        nextLevel = arguments?.getSerializable("next_level") as Level
        milestoneBonusHearts = arguments?.getInt("bonus_hearts", 0) ?: 0
        binding.rlPlay.setOnClickListener {
            actionListener.playNextLevel()
        }

        binding.rlBack.setOnClickListener {
            actionListener.backToLevelList()
        }

        binding.tvLevelNo.text = nextLevel?.level ?: ""
        binding.tvUserName.text = dataManager.mPref.prefGetUserInfo().name
        binding.tvShare.setOnClickListener {
            val imageView = binding.imageView
            val totalWidth = imageView.width
            val totalHeight = imageView.height
            saveImage(
                getBitmapFromViewAndroid(
                    binding.constraintLayoutCont,
                    totalWidth,
                    totalHeight
                )
            )?.let { it1 -> shareImageUri(it1) }
        }

        val txt = getString(R.string.game_level_up_congrats) + " " + level?.level
        binding.tvCongratsMsg.text = txt
        if (milestoneBonusHearts > 0) {
            val bonusTxt = "You got bonus hearts: $milestoneBonusHearts "
            binding.tvBonus.text = bonusTxt
            binding.tvBonus.visibility = View.VISIBLE
        }
        bindProfileImage()

    }

    private fun bindProfileImage() {
        val userInfo = dataManager.mPref.prefGetUserInfo()

        Glide.with(this)
            .load(BuildConfig.SERVER_URL + userInfo.picture)
            .placeholder(R.drawable.ic_placeholder_user)
            .error(R.drawable.ic_placeholder_user)
            .into(binding.icvUserIcon)
    }

    private fun getBitmapFromViewAndroid(view: View, width: Int, height: Int): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background;
        if (bgDrawable != null)
            bgDrawable.draw(canvas)
        else
            canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return returnedBitmap
    }

    private fun saveImage(image: Bitmap): Uri? {
        val imagesFolder = File(activity?.cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")

            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            uri =
                FileProvider.getUriForFile(
                    activity!!,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    file
                )

        } catch (e: IOException) {
            Log.d("TAG", "IOException while trying to write file for sharing: " + e.message);
        }
        return uri
    }

    private fun shareImageUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(intent)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

    }

    override fun onLoading(isLoader: Boolean, key: String) {

    }

    override fun onError(err: Throwable, key: String) {

    }

    override fun onErrorCode(
        code: Int,
        message: String,
        result: LiveDataResult<Response<ResponseBody>>
    ) {

    }


    companion object {
        @JvmStatic
        fun newInstance(level: Level, nextLevel: Level, milestoneBonusHearts: Int) =
            GameLevelUpDialog()
                .apply {
                    arguments = Bundle().apply {
                        putSerializable("level", level)
                        putSerializable("next_level", nextLevel)
                        putInt("bonus_hearts", milestoneBonusHearts)
                    }
                }
    }

    interface ActionListener {
        fun playNextLevel()
        fun backToLevelList()
    }
}