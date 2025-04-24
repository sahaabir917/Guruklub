package com.gmpire.guruklub.view.activity.questionShare

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.ActivityQuestionShareBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.SubscriptUtil
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.BasePermissionListener
import io.github.kexanie.library.MathView
import kotlinx.android.synthetic.main.activity_question_share.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class QuestionShareActivity : BaseActivity() {

    lateinit var binding: ActivityQuestionShareBinding
    lateinit var question: Question
    private var action: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_question_share)

        Dexter.withActivity(this)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : BasePermissionListener() {
                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken
                ) {
                    showStoragePermissionRationale(token)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    super.onPermissionDenied(response)
                    showContactPermissionDeniedDialog()
                }
            })
            .withErrorListener { error -> Log.e("Permission", error.toString()) }
            .check()

        question = intent.getSerializableExtra("question") as Question
        if (intent.hasExtra("action"))
            action = intent.getStringExtra("action")

        setContents(question)

        if (action == "download")
            binding.shareBtn.text = "Download"

        val text =
            "<font color=#000000>Guru</font><font color=#4A148C>Klub</font>"
        binding.textViewAppTypo.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(text)
        }

        binding.shareBtn.setOnClickListener {
            val scrollView = binding.scrollViewContent
            val totalWidth = scrollView.getChildAt(0).width
            val totalHeight = scrollView.getChildAt(0).height

            when (action) {
                "share" -> {
                    saveImage(
                        getBitmapFromViewAndroid(
                            binding.contentLayout,
                            totalWidth,
                            totalHeight
                        )
                    )?.let { it1 ->
                        shareImageUri(
                            it1
                        )
                    }
                }
                "download" -> {
                    saveImageToStorage(
                        getBitmapFromViewAndroid(
                            binding.contentLayout,
                            totalWidth,
                            totalHeight
                        )
                    )
                    showToast(this, "Question successfully downloaded to device.")
                    finish()
                }
                else -> {
                    showToast(this, "No action available.")
                }
            }
        }
        close_iv.setOnClickListener {
            onBackPressed()
        }
    }


    private fun saveImageToExternal(finalBitmap: Bitmap) {
        val path = Environment.getExternalStorageDirectory().path + "/guruKlub/"
        val myDir = File(path)
        if (!myDir.exists())
            myDir.mkdirs()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fname = "$timeStamp.jpeg"

        val file = File(myDir, fname)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
            //saveToInternalStorage2(finalBitmap)
        }

        notifyGalleryAboutNewImage(file.absolutePath)
    }

    @Throws(IOException::class)
    private fun saveImageToStorage(bitmap: Bitmap) {
        var image: File? = null
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fname = "$timeStamp.jpeg"
        val imageOutStream: OutputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                fname
            )
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/" + "GuruKlub"
            )
            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            contentResolver.openOutputStream(uri!!)!!
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/guruKlub"
            val myDir = File(imagesDir)
            if (!myDir.exists())
                myDir.mkdirs()
            image = File(imagesDir, fname)
            FileOutputStream(image)

        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)
        imageOutStream.close()

        if (image != null) {
            notifyGalleryAboutNewImage(image.absolutePath)
        }

    }

    private fun notifyGalleryAboutNewImage(path: String) {
        MediaScannerConnection.scanFile(
            this@QuestionShareActivity,
            arrayOf(path),
            null
        ) { path, uri ->
            Log.i("ExternalStorage", "Scanned $path:")
            Log.i("ExternalStorage", "-> uri=$uri")
        }
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }


    private fun saveImage(image: Bitmap): Uri? {
        val imagesFolder = File(cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")

            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            uri =
                FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file)

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

    private fun setContents(question: Question) {
        if (question.is_math == 0 && question.has_image == 0) {
            val titleText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(question.title, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(question.title)
            }

            if (SubscriptUtil.checkIfContainsSubscript(question.title)) {
                binding.questionTitleTv.setText(
                    SubscriptUtil.getSubscriptSpan(titleText),
                    TextView.BufferType.SPANNABLE
                )
            } else {
                binding.questionTitleTv.text = titleText
            }
        } else {
            binding.questionTitleTvMath.text = question.title
        }

        if (!question.picture.isNullOrEmpty()) {
            Glide.with(this).load(BuildConfig.SERVER_URL + question.picture)
                .into(binding.questionImageIv)
            binding.questionImageIv.visibility = View.VISIBLE
        } else {
            binding.questionImageIv.visibility = View.GONE
        }

        var i = 0

        binding.optionsll.removeAllViews()
        question.options.forEach {
            var view: View
            if (question.is_math == 0 && question.has_image == 0) {
                view = LayoutInflater.from(this).inflate(R.layout.radio_btn_layout_share, null)
                var radioButton = view.findViewById<RadioButton>(R.id.radio_btn)
                val rbText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    Html.fromHtml(it)
                }

                if (SubscriptUtil.checkIfContainsSubscript(question.title)) {
                    radioButton.setText(
                        SubscriptUtil.getSubscriptSpan(rbText),
                        TextView.BufferType.SPANNABLE
                    )
                } else {
                    radioButton.text = rbText
                }
            } else {
                view = LayoutInflater.from(this).inflate(R.layout.radio_btn_layout_math, null)
                var mathview = view.findViewById<MathView>(R.id.radio_btn_text)
                mathview.text = it
            }

            var radioBtn = view.findViewById<RadioButton>(R.id.radio_btn)
            radioBtn.tag = it
            radioBtn.id = i

            view.tag = question.options.indexOf(it)

            radioBtn.setOnClickListener {
                for (inc in question.options.indices) {
                    binding.optionsll.getChildAt(inc)
                        .findViewWithTag<RadioButton>(question.options[inc]).isChecked =
                        false
                    question.answered_position = -1
                }
                radioBtn.isChecked = true
                question.answered_position = view.tag.toString().toInt()
            }

            view.setOnClickListener {
                for (inc in 0..question.options.size - 1) {
                    binding.optionsll.getChildAt(inc)
                        .findViewWithTag<RadioButton>(question.options[inc]).isChecked =
                        false
                    question.answered_position = -1
                }

                radioBtn.isChecked = true
                question.answered_position = view.tag.toString().toInt()
            }

            if (question.answered) {
                if (radioBtn.id == question.answered_position) {
                    radioBtn.isChecked = true
                }
                radioBtn.isEnabled = false
                view.isEnabled = false
            }

            binding.optionsll.addView(view)
        }
    }

    override fun viewRelatedTask() {

    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.hold, R.anim.fade_out)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
    }

    override fun onClick(v: View?) {
    }

    private fun showStoragePermissionRationale(token: PermissionToken) {
        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("Storage permission required in order to save the image")
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                token.cancelPermissionRequest()
            }
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                token.continuePermissionRequest()
            }
            .setOnDismissListener { token.cancelPermissionRequest() }
            .show()
    }

    private fun showContactPermissionDeniedDialog() {
        AlertDialog.Builder(this).setTitle("Permission is required")
            .setMessage("Storage permission required in order to save the image")
            .setCancelable(true)
            .setNegativeButton("No Thanks") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Try Again") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}