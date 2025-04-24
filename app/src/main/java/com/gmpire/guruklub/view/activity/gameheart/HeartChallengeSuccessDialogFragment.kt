package com.gmpire.guruklub.view.activity.gameheart

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.model.gameChallenge.GameChallenge
import com.gmpire.guruklub.databinding.FragmentHeartChallengeSuccessDialogBinding
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelActivity
import dagger.android.support.DaggerDialogFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


class HeartChallengeSuccessDialogFragment : DaggerDialogFragment() {

    private lateinit var binding: FragmentHeartChallengeSuccessDialogBinding
    private var doubleBackToExitPressedOnce = false
    private lateinit var actionListener: ChallengeSuccessListener

    @Inject
    lateinit var dataManager: DataManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ChallengeSuccessListener) {
            actionListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle1)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_heart_challenge_success_dialog,
            container,
            false
        )

        try {
            var heartPoints = arguments?.getString("heartPoints")
            binding.tvRewardHearts.text = heartPoints
            val txt = "You have obtained $heartPoints hearts!"
            binding.tvChallengeText.text = txt
            binding.tvUserNameSuccess.text = arguments?.getString("userName")
            binding.relativeLayoutCollectNow.setOnClickListener {
                actionListener.onCollectHearts()
            }

            binding.tvShareSuccess.setOnClickListener {
                val imageView = binding.constraintLayoutContSuccess
                val totalWidth = imageView.width
                val totalHeight = imageView.height
                saveImage(
                    getBitmapFromViewAndroid(
                        binding.constraintLayoutContSuccess,
                        totalWidth,
                        totalHeight
                    )
                )?.let { it1 -> shareImageUri(it1) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bindProfileImage()
        return binding.root
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

    companion object {
        @JvmStatic
        fun newInstance(heartPoints: String, userName: String) =
            HeartChallengeSuccessDialogFragment().apply {
                this.arguments = Bundle().apply {
                    this.putString("heartPoints", heartPoints)
                    this.putString("userName", userName)
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(activity!!,R.style.DialogStyle1) {
            override fun onBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    activity?.finish()
                    startActivity(Intent(activity, GameLevelActivity::class.java))
                }
                doubleBackToExitPressedOnce = true
                Toast.makeText(context, "Press again to exit, click collect now for heart bonus!", Toast.LENGTH_SHORT)
                    .show()
                Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
        }
    }

    interface ChallengeSuccessListener {
        fun onCollectHearts()
    }

}