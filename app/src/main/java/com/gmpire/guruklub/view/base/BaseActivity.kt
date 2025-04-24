package com.gmpire.guruklub.view.base

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.databinding.ToolbarLayoutBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.activity.gameChallenge.GameChallengeActivity
import com.gmpire.guruklub.view.activity.gameLevelQuestion.GameLevelQuestionActivity
import com.gmpire.guruklub.view.activity.gameheart.HeartAddActivity
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.dialog.NoInternetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.base_image_choose.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import java.net.SocketException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), IObserverCallBack, View.OnClickListener,
    NoInternetDialog.Listener {

    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private lateinit var dialogs: ProgressDialog
    private var baseBottomSheet: BottomSheetDialogFragment? = null
    private var image_uri: Uri? = null
    open var isNoNetDialogShowing = false

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val isGameRelatedActivity =
        this is GameLevelActivity || this is GameLevelQuestionActivity || this is HeartAddActivity || this is GameChallengeActivity

    override fun onResume() {
        super.onResume()
        networkChangeReceiver = NetworkChangeReceiver()
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, filter);
        if (isGameRelatedActivity) {
            if (!GameServiceHelper.isMusicRunning && dataManager.mPref.getIsMusicOn())
                GameServiceHelper.playMusic(this)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
    }

    override fun gotOnline() {
        viewRelatedTask()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        dialogs = ProgressDialog(this)
        viewRelatedTask()
    }

    abstract fun viewRelatedTask()
    abstract fun navigateToHome()

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        this.overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.activity_in_back, R.anim.activity_out_back)
    }

    fun setToolbar(
        context: Context,
        toolbar: ToolbarLayoutBinding,
        title: String,
        isBackPressed: Boolean,
        isOptionEnabled: Boolean = false
    ) {
        toolbar.drawerTitle.text = title
        if (isBackPressed) {
            toolbar.drawerNavigationIcon.setImageResource(R.drawable.ic_back)
            toolbar.drawerNavigationIcon.setOnClickListener { onBackPressed() }
        } else {
            toolbar.drawerNavigationIcon.setImageResource(R.drawable.ic_hamburger)
            toolbar.drawerNavigationIcon.setOnClickListener { _ ->
            }
        }

        if (isOptionEnabled) {
            toolbar.optionsIcon.visibility = View.VISIBLE
            toolbar.optionsIcon2.visibility = View.VISIBLE
            toolbar.tvFilterLabel.visibility = View.VISIBLE
        } else {
            toolbar.optionsIcon.visibility = View.GONE
            toolbar.optionsIcon.visibility = View.GONE
            toolbar.tvFilterLabel.visibility = View.GONE
        }
    }


    fun showToast(context: Context, message: String) {
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.custom_toast_layout, null)

        val toastText = view.findViewById<TextView>(R.id.toastText)
        toastText.text = message

        toast.view = view
        toast.show()
    }

    fun hideKeyboard() {
        val inputManager = this.getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        val focusedView = this.getCurrentFocus()

        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(
                focusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    fun addFragment(isReplace: Boolean, container: Int, fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        if (isReplace) {
            transaction.replace(container, fragment)
        } else {
            transaction.add(container, fragment)
        }
        transaction.commit()
    }

    fun showDialog(isCancelAble: Boolean, dialogFragment: BaseDialogFragment) {
        dialogFragment.isCancelable = isCancelAble
        dialogFragment.show(supportFragmentManager.beginTransaction(), "dialog")
    }

    fun showProgressDialog(message: String) {
        if (TextUtils.isEmpty(message)) {
            dialogs.setMessage("")
        } else {
            dialogs.setMessage(message)
        }
        if (!dialogs.isShowing) {
            dialogs.setCancelable(false)
            dialogs.show()
        }
    }

    fun hideProgressDialog() {
        if (!this.isFinishing && dialogs.isShowing) {
            dialogs.dismiss()
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (isLoader) {
            showProgressDialog("Please wait")
        } else {
            hideProgressDialog()
        }
    }

    override fun onErrorCode(
        code: Int,
        message: String,
        result: LiveDataResult<Response<ResponseBody>>
    ) {
        if (code == 401 && dataManager.mPref.isAuthorized()) {
            dataManager.mPref.prefLogout(this)
            showToast(this, "Session expired")

        }
    }

    override fun onError(err: Throwable, key: String) {
        val isMainActivity = this is MainActivity
        //Toast.makeText(this, err.toString(), Toast.LENGTH_LONG).show()
        if (!isMainActivity && err !is SocketException) {
            if (!isNoNetDialogShowing)
                showNoNetDialog()
            isNoNetDialogShowing = true
        }
    }

    private fun chooseImage(title: String) {
        hideKeyboard()
        class BaseBottomSheet : BottomSheetDialogFragment() {

            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
            ): View? {

                val view = inflater.inflate(R.layout.base_image_choose, container)
                return view
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)

                tvTitle.text = title

                tvCamera.setOnClickListener {
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.TITLE, "New Picture")
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
                    image_uri =
                        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    //camera intent
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
                    startActivityForResult(cameraIntent, 0)
                    baseBottomSheet?.dismiss()
                }
                tvGallery.setOnClickListener {
                    val pickPhoto =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, 1)

                    baseBottomSheet?.dismiss()
                }
                btnCancel.setOnClickListener {
                    baseBottomSheet?.dismiss()
                }
            }

            override fun onAttach(context: Context) {
                super.onAttach(context)

            }
        }
        baseBottomSheet = BaseBottomSheet()
        baseBottomSheet?.show(supportFragmentManager, "choose_image")
    }


    fun hasPermissions(context: Context?, vararg permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            Log.e("log per", "granted 1")
            for (permission in permissions[0]) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e("log per", "granted 2")
                    return false
                }
            }
        }
        return true
    }

    fun getFixedOrientationBitmap(bitmap: Bitmap, pictureFilePath: String): Bitmap? {
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(pictureFilePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val orientation = exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        return orientation?.let { rotateBitmap(bitmap, it) }
    }


    protected fun shouldHeartDeduct(): Boolean {
        if (hasHeartsSubscription()) return false
        if (hasHeartsGift()) return false
        return true
    }

    protected fun hasHeartsSubscription(): Boolean {

        val subscription = dataManager.mPref.prefGetCurrentSubscription()

        subscription?.expiry_date?.let {
            try {
                val expDate: Date = SimpleDateFormat("yyyy-MM-dd").parse(it)

                if (expDate.after(Date())) {
                    return true
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        return false
    }

    protected fun hasHeartsGift(): Boolean {

        val gift = dataManager.mPref.prefGetCurrentHeartGift()

        gift?.expiry_date?.let {
            try {
                val expDate: Date = SimpleDateFormat("yyyy-MM-dd").parse(it)

                if (expDate.after(Date())) {
                    return true
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        return false
    }


    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap? {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }
        return try {
            val bmRotated =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            bmRotated
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        }
    }

    private fun showNoNetDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Connection")
            .setMessage(
                "GuruKlub is facing some internet connectivity issues. " +
                        "Please check your internet connection. You can also browse our Offline Question Set"
            )
            .setPositiveButton(
                "Go to home"
            ) { dialog, which ->
                navigateToHome()
                isNoNetDialogShowing = false
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                isNoNetDialogShowing = false
                dialog.dismiss()
            }.show()
    }

    fun NoInternetDialogs() {
        AlertDialog.Builder(this)
            .setTitle("No Connection")
            .setMessage(
                "GuruKlub is facing some internet connectivity issues. " +
                        "Please check your internet connection. You can also browse our Offline Question Set"
            )
            .setPositiveButton(
                "Go to Practice"
            ) { dialog, which ->
                navigateToHome()
                isNoNetDialogShowing = false
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                isNoNetDialogShowing = false
                dialog.dismiss()
            }.show()
    }


    internal fun showPermissionRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Please allow permission to further proceed.")
            .setPositiveButton(
                "OKAY"
            ) { dialog, which ->
                dialog.dismiss()
            }.show()
    }

    internal fun showPermissionRequiredDialogWithAppSettings() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Please allow permission from settings to further proceed.")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { dialog, which ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.dismiss()
            }.setNegativeButton(
                "CANCEL"
            ) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }


}