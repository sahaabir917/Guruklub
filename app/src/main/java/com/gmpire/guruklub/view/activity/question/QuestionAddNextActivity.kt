package com.gmpire.guruklub.view.activity.question

import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.QuestionRequest
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.QuestionAddRespons
import com.gmpire.guruklub.databinding.ActivityQuestionAddNextBinding
import com.gmpire.guruklub.util.GeneratDropdownItem
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.SuccessOrFailActivity
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.base.BaseActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.request.RequestOptions
import com.gmpire.guruklub.R.color
import com.gmpire.guruklub.R.color.newwhite
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_forget_password.view.*
import kotlinx.android.synthetic.main.navigation_menu_layout.view.*
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class QuestionAddNextActivity : BaseActivity() {

    private lateinit var binding: ActivityQuestionAddNextBinding
    private var questionRequest = QuestionRequest()
    private var questionTitle = ""
    private var isGame = ""
    private lateinit var viewModel: QuestionViewModel
    lateinit var spinnerAdapterAnswer: CustomSpinnerAdapter
    var options = ArrayList<Common>()

    var photo: Bitmap? = null
    private var baseBottomSheet: BottomSheetDialogFragment? = null
    var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_question_add_next)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(QuestionViewModel::class.java)
    }

    override fun viewRelatedTask() {

        setToolbar(this, binding.toolbar, "", true, false)
        val PERMISSIONS = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (intent.hasExtra("isGame")) {
            isGame = intent.getStringExtra("isGame")
            if (isGame == "yes") {

                val colorList = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_enabled),  // Disabled
                        intArrayOf(android.R.attr.state_enabled)    // Enabled
                    ),
                    intArrayOf(
                       Color.WHITE,
                        Color.WHITE
                    )
                )


                binding.toolbar.drawerNavigationIcon.visibility = View.GONE
                binding.titleLayout.visibility = View.GONE
                binding.titleLayout2.visibility = View.VISIBLE
                binding.rootlayouts.setBackgroundResource(R.drawable.page_level_background)
                binding.btnBack.setBackgroundResource(R.drawable.yellow_box_with_colorfull_bg)
                binding.btnNext.setBackgroundResource(R.drawable.yellow_box_with_colorfull_bg)
                binding.btnBack.layoutParams.width = 0
                binding.btnBack.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                binding.btnNext.layoutParams.width = 0
                binding.btnNext.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                binding.tieQOption1.setTextColor(Color.parseColor("#ffffff"))
                binding.tieQOption2.setTextColor(Color.parseColor("#ffffff"))
                binding.tieQOption3.setTextColor(Color.parseColor("#ffffff"))
                binding.tieQOption4.setTextColor(Color.parseColor("#ffffff"))
                binding.tieQuestionTitle.setTextColor(Color.parseColor("#ffffff"))
                binding.tilOption1.defaultHintTextColor = colorList
                binding.tilOption2.defaultHintTextColor = colorList
                binding.tilOption3.defaultHintTextColor = colorList
                binding.tilOption4.defaultHintTextColor = colorList
                val param = binding.questionAdds.layoutParams as ViewGroup.MarginLayoutParams
                param.topMargin = 80
                binding.questionAdds.layoutParams = param

            }
        }

        //String PERMISSIONS = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        /*if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                102
            )
        }*/

        questionRequest.filterValues =
            Gson().fromJson(intent.getStringExtra("filter"), FilterValues::class.java)
        if (intent.hasExtra("question_title")) {
            questionTitle = intent.getStringExtra("question_title")
            binding.tieQuestionTitle.text = questionTitle
        }

        binding.btnBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.llUploadImage.setOnClickListener(this)
        binding.rootLayout.setOnClickListener(this)
        binding.rlDifficulty.setOnClickListener {
            binding.spAnswer.performClick()
        }

        options.add(Common("-1", "Oprion 1: N/A"))
        options.add(Common("-1", "Oprion 2: N/A"))
        options.add(Common("-1", "Oprion 3: N/A"))
        options.add(Common("-1", "Oprion 4: N/A"))

        //textChangeListener(binding.tieQuestionTitle)
        textChangeListener(binding.tieQOption1)
        textChangeListener(binding.tieQOption2)
        textChangeListener(binding.tieQOption3)
        textChangeListener(binding.tieQOption4)
        initMakeChoiceItemSpinner()

    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun lunchSuccessOrFailActivity(isSuccess: Boolean, title: String, description: String) {
        val intent = Intent(this, SuccessOrFailActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("description", description)
        intent.putExtra("isSuccess", isSuccess)
        intent.putExtra("isGame",isGame)
        startActivity(intent)
    }

    fun textChangeListener(tie: TextInputEditText) {
        tie.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when (tie) {
                    /*binding.tieQuestionTitle -> {
                        removeError(binding.tilQuestionTitle)
                    }*/
                    binding.tieQOption1 -> {
                        removeError(binding.tilOption1)
                        options[0] = Common("1", binding.tieQOption1.text.toString())
                    }
                    binding.tieQOption2 -> {
                        removeError(binding.tilOption2)
                        options[1] = Common("2", binding.tieQOption2.text.toString())
                    }
                    binding.tieQOption3 -> {
                        removeError(binding.tilOption3)
                        options[2] = Common("3", binding.tieQOption3.text.toString())
                    }
                    binding.tieQOption4 -> {
                        removeError(binding.tilOption4)
                        options[3] = Common("4", binding.tieQOption4.text.toString())
                    }
                }

                spinnerAdapterAnswer.setDropDown(
                    GeneratDropdownItem.getDropdownItems(
                        JSONArray(Gson().toJson(options)),
                        "name",
                        "id",
                        "Select Your Answer"
                    )
                )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }


    fun initMakeChoiceItemSpinner() {
        spinnerAdapterAnswer =
            CustomSpinnerAdapter(
                this,
                GeneratDropdownItem.getDropdownItems(
                    JSONArray(Gson().toJson(options)),
                    "name",
                    "id",
                    "Select Your Answer"
                )
            )
        binding.spAnswer.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerAdapterAnswer.setPosition(position)
                questionRequest.answer = spinnerAdapterAnswer.getSelectedId()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        })
        binding.spAnswer.adapter = spinnerAdapterAnswer.getAdapter()
    }

    fun selectImage(title: String) {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
            if (options[item] == "Take Photo") {

//                val values = ContentValues()
//                values.put(MediaStore.Images.Media.TITLE, "New Picture")
//                values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
//                image_uri =
//                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//                //camera intent
//                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
//                startActivityForResult(cameraIntent, 0)

                val values = ContentValues()
                values.put(MediaStore.Images.Media.TITLE, "New Picture")
                values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
                image_uri =
                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                //camera intent
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
                startActivityForResult(cameraIntent, 0)


            } else if (options[item] == "Choose from Gallery") {
                val pickPhoto =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhoto, 1)

            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == Activity.RESULT_OK && image_uri != null) {
                    val selectedImage =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, image_uri)
                    val out = ByteArrayOutputStream()
                    selectedImage.compress(Bitmap.CompressFormat.PNG, 100, out)
                    val decoded =
                        BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))
                    photo =
                        Bitmap.createScaledBitmap(
                            decoded,
                            decoded.width / 4,
                            decoded.height / 4,
                            false
                        )
                    //photo = decoded
                    try {
                        questionRequest.picture = TransformationUtils.rotateImage(photo!!, 90)
                        Glide.with(this)
                            .load(questionRequest.picture)
                            .apply(
                                RequestOptions.placeholderOf(R.drawable.placeholder).transforms(
                                    CenterCrop(), RoundedCorners(
                                        16
                                    )
                                )
                            )
                            .into(binding.ivPreview)
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                1 -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val selectedImage = data.data
                    val uriString = selectedImage.toString()
                    val myFile = File(uriString)
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        val cursor = contentResolver.query(
                            selectedImage,
                            filePathColumn, null, null, null
                        )
                        if (cursor != null) {
                            cursor.moveToFirst()

                            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                            val picturePath = cursor.getString(columnIndex)
                            val selectedImage = BitmapFactory.decodeFile(picturePath)
                            val out = ByteArrayOutputStream()
                            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, out)
                            val decoded =
                                BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))

                            photo =
                                Bitmap.createScaledBitmap(
                                    decoded,
                                    decoded.width / 4,
                                    decoded.height / 4,
                                    false
                                )
                            try {
                                questionRequest.picture =
                                    TransformationUtils.rotateImage(photo!!, 90)
                                binding.ivPreview.setImageBitmap(photo)
                            } catch (e: java.lang.NullPointerException) {
                                e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

//                            Glide.with(this)
//                                .load(questionRequest.picture)
//                                .apply(
//                                    RequestOptions.placeholderOf(R.drawable.placeholder).transforms(
//                                        CenterCrop(), RoundedCorners(
//                                            16
//                                        )
//                                    )
//                                )
//                                .into(binding.ivPreview)


                            cursor.close()
                        }
                    }
                }
            }
        }
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "addQuestion" -> {
                val type = object : TypeToken<QuestionAddRespons>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<QuestionAddRespons>(
                            result.data.body()?.string(),
                            type
                        )
                    lunchSuccessOrFailActivity(
                        baseData.status,
                        if (baseData.status) "Congratulations" else "Fail to Question Upload",
                        baseData.message[0]
                    )
                }
            }
        }

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.rootLayout -> {
                hideKeyboard()
            }

            binding.btnNext -> {
                questionRequest.title = binding.tieQuestionTitle.text.toString()
                questionRequest.option_a = binding.tieQOption1.text.toString()
                questionRequest.option_b = binding.tieQOption2.text.toString()
                questionRequest.option_c = binding.tieQOption3.text.toString()
                questionRequest.option_d = binding.tieQOption4.text.toString()
                questionRequest.answer_explain = binding.edtQuestionExplanation.text.toString()

                /*if (questionRequest.title == "") {
                    setError(binding.tilQuestionTitle, "Enter question title")
                    return
                }*/

                if (questionRequest.option_a == "") {
                    setError(binding.tilOption1, "Enter option")
                    return
                }

                if (questionRequest.option_b == "") {
                    setError(binding.tilOption2, "Enter option")
                    return
                }

                if (questionRequest.option_c == "") {
                    setError(binding.tilOption3, "Enter option")
                    return
                }

                if (questionRequest.option_d == "") {
                    setError(binding.tilOption4, "Enter option")
                    return
                }

                if (questionRequest.answer == "0") {
                    showToast(this, "Select correct answer for this question")
                    return
                }

                if (questionRequest.answer_explain == "") {
                    binding.edtQuestionExplanation.error = "Write some explanation for the answer"
                    return
                }


                viewModel.fetchAddQuestion(questionRequest, this)

            }

            binding.btnBack ->{
                onBackPressed()
            }

            binding.llUploadImage -> {
                selectImage("Choose Question Image")
            }

        }
    }

    private fun setError(til: TextInputLayout, s: String) {
        til.isErrorEnabled = true
        til.error = s
    }

    private fun removeError(til: TextInputLayout) {
        til.error = null
        til.isErrorEnabled = false
    }
}
