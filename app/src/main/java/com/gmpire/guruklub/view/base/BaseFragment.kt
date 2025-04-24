package com.gmpire.guruklub.view.base

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.util.IObserverCallBack
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.NoConnectivityException
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.main.ProgressBarListener
import com.gmpire.guruklub.view.dialog.NoInternetDialog
import com.gmpire.guruklub.view.dialog.UpdateProfileDialog
import com.gmpire.guruklub.view.fragment.game.GameFragment
import com.gmpire.guruklub.view.fragment.home.HomeFragment
import com.gmpire.guruklub.view.fragment.library.LibraryFragmentNew
import dagger.android.support.DaggerFragment
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.UnknownHostException
import javax.inject.Inject


abstract class BaseFragment : DaggerFragment(), IObserverCallBack, View.OnClickListener,
    NoInternetDialog.Listener {


    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var dialogs: ProgressBar
    private var isNoDialogShowing = false
    private var parentActivity: BaseActivity? = null
    var progressBarListener: ProgressBarListener? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialogs = ProgressBar(activity)
        if (activity is BaseActivity) {
            parentActivity = activity as BaseActivity
        }
        viewRelatedTask()

        initiateProgressBar()

    }

    private fun initiateProgressBar() {

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            progressBarListener = context
        }
    }

    abstract fun viewRelatedTask()

    fun showToast(context: Context?, message: String) {
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT

        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.custom_toast_layout, null)

        val toastText = view.findViewById<TextView>(R.id.toastText)
        toastText.text = message

        toast.view = view
        toast.show()
    }


    fun showDialog(isCancelAble: Boolean, dialogFragment: BaseDialogFragment) {
        (activity as BaseActivity).showDialog(isCancelAble, dialogFragment)
    }

    override fun onLoading(isLoader: Boolean, key: String) {
//        if (isLoader) {
//            showProgressDialog("Please wait")
//        } else {
//            hideProgressDialog()
//        }

    }

    fun hideKeyboard() {
        val inputManager = activity?.getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        val focusedView = activity!!.getCurrentFocus()

        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(
                focusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    fun showProgressDialog() {
        if (progressBarListener != null) {
            progressBarListener?.showProgress()
        }
    }

    fun hideProgressDialog() {
        try {
            if (progressBarListener != null) {
                progressBarListener?.hideProgress()
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onErrorCode(
        code: Int,
        message: String,
        result: LiveDataResult<Response<ResponseBody>>
    ) {
        if (code == 401 && dataManager.mPref.isAuthorized()) {
            activity?.let { dataManager.mPref.prefLogout(it) }
            showToast(activity, "Session expired")
        }
    }

    override fun onError(err: Throwable, key: String) {
        val isHomeFragment =
            this is HomeFragment || this is GameFragment || this is LibraryFragmentNew
        when {
            err.message.toString().contains("No address associated with hostname") -> {
                //  showNoInternetDialog()
            }
            err.message.toString().contains("failed to connect to demo.gmpire.com") -> {
                // showNoInternetDialog()
            }
            err is NoConnectivityException -> {
                if (!isHomeFragment)
                    initiateNoNetDialog(err.message.toString())
            }
            err is UnknownHostException -> {
                if (!isHomeFragment)
                    initiateNoNetDialog(err.message.toString())
            }
        }
    }

    private fun initiateNoNetDialog(err : String) {
        try {
            if (!parentActivity?.isNoNetDialogShowing!!) {
                showNoNetDialog(err)
                parentActivity?.isNoNetDialogShowing = true
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun gotOnline() {
        viewRelatedTask()
    }

    fun showNoInternetDialog() {
        activity?.let {
            NoInternetDialog("No Internet", it, this).show(
                childFragmentManager,
                "no internet dialog"
            )
        }
    }

    fun showNoNetDialog(err: String) {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("No Connection")
            .setMessage(
                "GuruKlub is facing some internet connectivity issues. " +
                        "Please check your internet connection. You can also browse our Offline Question set."
            )
            .setPositiveButton(
                "Go to Practice"
            ) { dialog, which ->
                dialog.dismiss()
                if (parentActivity is MainActivity) {
                    parentActivity?.navigateToHome()
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }.show()

        dialog.setOnDismissListener { parentActivity?.isNoNetDialogShowing = false }
    }

    private fun showAlertDialog() {
        var updateProfileDialog =
            UpdateProfileDialog.newInstance("You need to update your profile before start the game. Do You want to update your profile?","profile_update")
        activity?.supportFragmentManager?.let {
            updateProfileDialog.show(
                it,
                updateProfileDialog.tag
            )
        }
    }


}