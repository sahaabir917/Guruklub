package com.gmpire.guruklub.view.activity.profileSetup

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.databinding.ActivityProfileSetupBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.phoneVerification.PhoneVerificationActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.michaelflisar.rxbus2.RxBus
import id.zelory.compressor.Compressor
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.lang.reflect.InvocationTargetException
import java.text.SimpleDateFormat
import java.util.*

class ProfileSetupActivity : BaseActivity() {

    private var userAge: Int = 0
    private lateinit var viewModel: ProfileSetupViewModel
    var photo: Bitmap? = null
    var isPhotEdit = false
    var image_uri: Uri? = null
    private var fullName: String = ""
    private var displayName: String = ""
    private var phoneNo: String = ""
    private var email: String = ""
    private var gender: Int = -1
    private var dob: String = ""

    private var userInfo: UserInfo? = null

    private lateinit var binding: ActivityProfileSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_setup)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(ProfileSetupViewModel::class.java)

        binding.rootLayout.setOnClickListener(this)
        userInfo = dataManager.mPref.prefGetUserInfo()


        val r = checkIfAlreadyHavePermission()
        Log.d("TAG", ProfileSetupActivity::getLocalClassName.toString() + "=>" + r)
    }

    private fun initTextViews() {
        binding.fullNameTv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                fullName = s.toString()
                email = binding.emailTv.text.toString()
                when (binding.genderRg.checkedRadioButtonId) {
                    R.id.male_rb -> gender = 0
                    R.id.female_rb -> gender = 1
                    R.id.other_rb -> gender = 2
                }
                dob = binding.dateOfBirthTv.text.toString()
                phoneNo = binding.phoneNoTv.text.toString()

                binding.btnNext.isEnabled = validationOk()
                if (validationOk()) {
                    val drawableTop = binding.btnNext.background as GradientDrawable
                    var color = ColorUtil.getColorForableordisable("Able")
                    drawableTop.setColor(ContextCompat.getColor(this@ProfileSetupActivity, color))
                } else {
                    val drawableTop = binding.btnNext.background as GradientDrawable
                    var color = ColorUtil.getColorForableordisable("Disable")
                    drawableTop.setColor(ContextCompat.getColor(this@ProfileSetupActivity, color))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        binding.emailTv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                email = s.toString()
                fullName = binding.fullNameTv.text.toString()
                when (binding.genderRg.checkedRadioButtonId) {
                    R.id.male_rb -> gender = 0
                    R.id.female_rb -> gender = 1
                    R.id.other_rb -> gender = 2
                }
                dob = binding.dateOfBirthTv.text.toString()
                binding.btnNext.isEnabled = validationOk()

                if (validationOk()) {
                    val drawableTop = binding.btnNext.background as GradientDrawable
                    var color = ColorUtil.getColorForableordisable("Able")
                    drawableTop.setColor(ContextCompat.getColor(this@ProfileSetupActivity, color))
                } else {
                    val drawableTop = binding.btnNext.background as GradientDrawable
                    var color = ColorUtil.getColorForableordisable("Disable")
                    drawableTop.setColor(ContextCompat.getColor(this@ProfileSetupActivity, color))
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })


        binding.dateOfBirthTv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                dob = s.toString()
                email = binding.emailTv.toString()
                fullName = binding.fullNameTv.toString()
                when (binding.genderRg.checkedRadioButtonId) {
                    R.id.male_rb -> gender = 0
                    R.id.female_rb -> gender = 1
                    R.id.other_rb -> gender = 2
                }
                phoneNo = binding.phoneNoTv.text.toString()

                binding.btnNext.isEnabled = validationOk()

                if (validationOk()) {
                    val drawableTop = binding.btnNext.background as GradientDrawable
                    var color = ColorUtil.getColorForableordisable("Able")
                    drawableTop.setColor(ContextCompat.getColor(this@ProfileSetupActivity, color))
                } else {
                    val drawableTop = binding.btnNext.background as GradientDrawable
                    var color = ColorUtil.getColorForableordisable("Disable")
                    drawableTop.setColor(ContextCompat.getColor(this@ProfileSetupActivity, color))
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        binding.phoneNoTv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                phoneNo = s.toString()
                dob = binding.dateOfBirthTv.text.toString()
                email = binding.emailTv.toString()
                fullName = binding.fullNameTv.toString()
                when (binding.genderRg.checkedRadioButtonId) {
                    R.id.male_rb -> gender = 0
                    R.id.female_rb -> gender = 1
                    R.id.other_rb -> gender = 2
                }

                binding.btnNext.isEnabled = validationOk()

                if (validationOk()) {
                    val drawableTop = binding.btnNext.background as GradientDrawable
                    var color = ColorUtil.getColorForableordisable("Able")
                    drawableTop.setColor(ContextCompat.getColor(this@ProfileSetupActivity, color))
                } else {
                    val drawableTop = binding.btnNext.background as GradientDrawable
                    var color = ColorUtil.getColorForableordisable("Disable")
                    drawableTop.setColor(ContextCompat.getColor(this@ProfileSetupActivity, color))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    private fun checkIfAlreadyHavePermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun viewRelatedTask() {
        if (intent.hasExtra("isEdit") || intent.hasExtra("isSocial")) {
            setToolbar(this, binding.toolbar, "Edit Profile", true)
            setData(dataManager.mPref.prefGetUserInfo())
        } else {
            setToolbar(this, binding.toolbar, "Profile Setup", true)
        }

        initTextViews()

        binding.btnNext.setOnClickListener(this)
        binding.gobackbtn.setOnClickListener(this)
        binding.calenderLayout.setOnClickListener(this)

        binding.coverPicIv.clipToOutline = true
        binding.profilePicIv.setOnClickListener(this)

        if (userInfo?.reg_type == "3") {
            binding.emailTv.isEnabled = false
        } else if (userInfo?.reg_type == "2") {
            binding.phoneNoTv.isEnabled = false
        } else {
            binding.phoneNoTv.isEnabled = true
            binding.emailTv.isEnabled = true
        }

    }

    override fun navigateToHome() {
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiProfileSetup" -> {
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
                            if (intent.hasExtra("isEdit")) {
                                RxBus.get().withKey(RxBusEvents.PROFILE_UPDATED).send(
                                    userInfo?.let { it1 -> UpdateProfile(it1) }
                                )
                                finish()
                            } else {
                                startActivity(
                                    Intent(
                                        this,
                                        PhoneVerificationActivity::class.java
                                    ).putExtra("phone", userInfo?.phone)
                                )
                            }
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
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
                fullName = binding.fullNameTv.text.toString()
                displayName = binding.displayNameTv.text.toString()
                phoneNo = binding.phoneNoTv.text.toString()
                email = binding.emailTv.text.toString()

                when (binding.genderRg.checkedRadioButtonId) {
                    R.id.male_rb -> gender = 0
                    R.id.female_rb -> gender = 1
                    R.id.other_rb -> gender = 2
                }

                dob = binding.dateOfBirthTv.text.toString()
                var dobYear = if (dob.isNotEmpty()) dob.take(4).toInt() else 0
                if (dobYear != 0) userAge = checkUserAge(dobYear)
                if (validationOkProfile()) {
                    if (userAge > 10) {
                        viewModel.apiProfileSetup(
                            fullName,
                            displayName,
                            email,
                            phoneNo,
                            gender,
                            dob,
                            if (isPhotEdit) photo else null,
                            userInfo?.reg_type.toString(),
                            this
                        )
                    } else {
                        showToast(this, "Please use valid birthday")
                    }
                }
            }
            binding.gobackbtn -> {
                onBackPressed()
            }
            binding.calenderLayout -> {
                val c = Calendar.getInstance()
                val y = c.get(Calendar.YEAR)
                val m = c.get(Calendar.MONTH)
                val d = c.get(Calendar.DAY_OF_MONTH)
                val dpd = DatePickerDialog(
                    this, android.app.AlertDialog.THEME_HOLO_LIGHT,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        val cal = Calendar.getInstance()
                        cal.set(year, monthOfYear, dayOfMonth)
                        binding.dateOfBirthTv.text =
                            SimpleDateFormat("yyyy-MM-dd").format(Date(cal.timeInMillis))
                    },
                    y,
                    m,
                    d
                )
                dpd.show()

            }
            binding.profilePicIv -> {
                Dexter.withActivity(this)
                    .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            report?.let {
                                if (report.areAllPermissionsGranted()) {
                                    selectImage("Choose Question Image")
                                }
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            permissions?.forEach {
                                token?.continuePermissionRequest()
                            }
                        }

                    })
                    .withErrorListener { error -> Log.e("Permission", error.toString()) }
                    .check()
            }
        }
    }

    private fun checkUserAge(dobYear: Int): Int {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val userAge = currentYear - dobYear
        return userAge
    }


    private fun validationOkProfile(): Boolean {
        if (fullName.isEmpty() || fullName.length < 3) {
            binding.fullNameTv.error = "Please provide your full name"
            return false
        }


        if (email.isEmpty() && userInfo?.reg_type == "2") {
            if (!LocalValidator.isEmailValid(email)) {
                binding.emailTv.error = "Please provide your email name"
                return false
            }
        }

        if (email.isEmpty() && userInfo?.reg_type == "") {
            if (!LocalValidator.isEmailValid(email)) {
                binding.emailTv.error = "Please provide your email name"
                return false
            }
        }

        if (userInfo?.reg_type == "" || userInfo?.reg_type == "3") {
            if (!LocalValidator.isPhoneValid(phoneNo)) {
                binding.phoneNoTv.error = "Please provide a valid phone number"
                return false
            }
        }

        if (gender == -1) {
            showToast(this, "Please select your gender")
            return false
        }
        return true
    }


    private fun validationOk(): Boolean {
        if (fullName.isEmpty() || fullName.length < 3) {
            binding.fullNameTv.error = "Please provide your full name"
            return false
        }


        if (email.isEmpty() && userInfo?.reg_type == "2") {
            if (!LocalValidator.isEmailValid(email)) {
                binding.emailTv.error = "Please provide your email name"
                return false
            }
        }

        if (email.isEmpty() && userInfo?.reg_type == "") {
            if (!LocalValidator.isEmailValid(email)) {
                binding.emailTv.error = "Please provide your email name"
                return false
            }
        }

        if (userInfo?.reg_type == "" || userInfo?.reg_type == "3") {
            if (!LocalValidator.isPhoneValid(phoneNo)) {
                binding.phoneNoTv.error = "Please provide a valid phone number"
                return false
            }
        }

        if (gender == -1) {
            showToast(this, "Please select your gender")
            return false
        }
        return true
    }

    fun selectImage(title: String) {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.TITLE, "New Picture")
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
                    image_uri =
                        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    //camera intent
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
                    startActivityForResult(cameraIntent, 0)


                }
                options[item] == "Choose from Gallery" -> {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(
                            intent,
                            "Select Picture"
                        ), 1
                    )

                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == Activity.RESULT_OK && image_uri != null) {
                    val imagePath = image_uri?.let { getPath(it) }
                    val imageFile = File(imagePath)
                    val compressedImage = Compressor(this)
                        .setMaxWidth(640)
                        .setMaxHeight(480)
                        .setQuality(75)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .compressToFile(imageFile)

                    photo = BitmapFactory.decodeFile(compressedImage.path)
                    Glide.with(this)
                        .load(photo)
                        .apply(
                            RequestOptions.placeholderOf(R.drawable.placeholder).transforms(
                                CenterCrop(), RoundedCorners(
                                    16
                                )
                            )
                        )
                        .into(binding.coverPicIv)
                    binding.coverPicIv.clipToOutline = true
                    isPhotEdit = true

                }
                1 -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri: Uri? = data.data
                    isPhotEdit = true

                    val url = data.data.toString()

                    if (url.startsWith("content://com.google.android.apps.photos.content")) {
                        try {
                            val parcelFileDescriptor =
                                uri?.let { contentResolver.openFileDescriptor(it, "r") }
                            val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                            photo = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                            parcelFileDescriptor?.close()
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }
                    } else {
                        val imagePath = uri?.let { getPath(it) }
                        val imageFile = File(imagePath)
                        if (imageFile != null && imageFile.exists()) {
                            try {
                                val compressedImage = Compressor(this)
                                    .setMaxWidth(640)
                                    .setMaxHeight(480)
                                    .setQuality(75)
                                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                                    .compressToFile(imageFile)

                                photo = BitmapFactory.decodeFile(compressedImage.path)
                            } catch (ex: java.lang.Exception) {
                                ex.printStackTrace()
                            }
                        }
                    }

                    Glide.with(this)
                        .load(photo)
                        .apply(
                            RequestOptions.placeholderOf(R.drawable.placeholder).transforms(
                                CenterCrop(), RoundedCorners(
                                    16
                                )
                            )
                        )
                        .into(binding.coverPicIv)
                    binding.coverPicIv.clipToOutline = true
                }
            }
        }
    }

    private fun getPath(uri: Uri): String {
        var path = ""
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        if (cursor != null) {
            cursor.moveToFirst()
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                path = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        if (path == null) {
            path = "Not found!"
        }
        return path
    }

    fun setData(userInfo: UserInfo) {
        viewModel.profileData = userInfo
        binding.btnNext.text = "Update"
        binding.fullNameTv.setText(userInfo.name)
        binding.emailTv.setText(userInfo.email)
        binding.displayNameTv.setText(userInfo.display_name)
        userInfo.phone?.let { binding.phoneNoTv.setText(userInfo.phone) }
        userInfo.gender?.let {
            if (it.isNotEmpty()) {
                val rb = userInfo.gender?.toInt()
                    ?.let { it1 -> binding.genderRg.getChildAt(it1) } as RadioButton
                rb.isChecked = true
            }
            binding.dateOfBirthTv.text = userInfo.dob
        }

        if (intent.hasExtra("gender")) {
            val rb = binding.genderRg.getChildAt(
                if (intent.getStringExtra("gender")
                        .contentEquals("male")
                ) 0 else if (intent.getStringExtra("gender").contentEquals("female")) 1 else 2
            ) as RadioButton
            rb.isChecked = true
        }

        if (intent.hasExtra("dob")) {
            try {
                binding.dateOfBirthTv.text = SimpleDateFormat("yyyy-MM-dd").format(
                    SimpleDateFormat("yyyy-MM-dd").parse(
                        intent.getStringExtra("dob")
                    )
                )
            } catch (ex: InvocationTargetException) {
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        Glide.with(this).load(BuildConfig.SERVER_URL + userInfo.picture)
            .into(binding.coverPicIv)
    }


}