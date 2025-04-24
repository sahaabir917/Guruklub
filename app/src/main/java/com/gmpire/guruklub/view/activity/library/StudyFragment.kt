package com.gmpire.guruklub.view.activity.library

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.library.Images
import com.gmpire.guruklub.data.model.library.Library
import com.gmpire.guruklub.data.model.library.Videos
import com.gmpire.guruklub.databinding.FragmentLibraryStudyBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.RxBusEvents.Companion.RESUME_STUDY_CHANGED
import com.gmpire.guruklub.util.UpdateClass
import com.gmpire.guruklub.view.adapter.ImagesAdapter
import com.gmpire.guruklub.view.adapter.VideoPagerAdapter
import com.gmpire.guruklub.view.base.BaseFragment
import com.michaelflisar.rxbus2.RxBus
import kotlinx.android.synthetic.main.dialog_box.view.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*


class StudyFragment(var library: MutableLiveData<Library>, val subject_name: String) :
    BaseFragment() {

    private lateinit var binding: FragmentLibraryStudyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_library_study, container, false)
        return binding.root
    }

    override fun viewRelatedTask() {

        val observer = androidx.lifecycle.Observer<Library> { library ->
          //  showDetails(library)
        }

        library.observe(this, observer)
        library.postValue(library.value)

    }

    override fun onLoading(isLoader: Boolean, key: String) {

    }


    private fun initLibraryImage(libraryImages: ArrayList<Images>) {
        binding.imageViewerViewPager.adapter = ImagesAdapter(activity, libraryImages)
        binding.imageViewerViewPager.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                binding.imageViewerPageIndicatorView.selection = position
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        if (libraryImages.size < 2) {
            binding.imageViewerPageIndicatorView.visibility = View.GONE
        }
    }

    private fun initLibraryVideo(libraryVideos: ArrayList<Videos>) {
        binding.videoViewerViewPager.adapter = VideoPagerAdapter(activity, libraryVideos)
        binding.videoViewerViewPager.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                binding.videoViewerPageIndicatorView.selection = position
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        if (libraryVideos.size < 2) {
            binding.videoViewerPageIndicatorView.visibility = View.GONE
        }
    }


    private fun showMyAlert(gist: String) {
        try {
            var builder = activity?.let { AlertDialog.Builder(it) }
            val alert: AlertDialog? = builder?.create()
            val my_view = layoutInflater.inflate(R.layout.dialog_box, null)
            alert?.setView(my_view)
            alert?.setCancelable(false)

            my_view.ivCross.setOnClickListener {
                alert?.dismiss()
                binding.llGist.isEnabled = true
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


    private fun showDetails(library: Library) {
        binding.tvTitle.text = library.title


//        binding.tvDetails.loadData(Library.details, "text/html", "UTF-8")
//        binding.tvDetails.settings.javaScriptCanOpenWindowsAutomatically = true
//        binding.tvDetails.settings.domStorageEnabled = true

        if (library.is_math == 0) {
            binding.tvDetails.text = Html.fromHtml(library.details)
            binding.tvDetails.movementMethod = LinkMovementMethod.getInstance()
        } else {
            binding.tvDetailsMath.text = library.details
        }

        if (library.library_link.isNotEmpty()) {
            binding.tvLinks.visibility = View.VISIBLE
            binding.tvLinks.text = Html.fromHtml(library.library_link)
            binding.tvLinks.movementMethod = LinkMovementMethod.getInstance()
        } else {
            binding.tvLinks.visibility = View.GONE
        }

        //  binding.tvDetails.webViewClient = HelloWebViewClient()
        //  binding.tvDetails.settings.javaScriptEnabled = true
        // binding.tvDetails.settings.javaScriptCanOpenWindowsAutomatically = true

        if (library.gist.isNotEmpty()) {
            binding.llGist.setOnClickListener {
                binding.llGist.isEnabled = false
                showMyAlert(library.gist)
            }
        } else {
            binding.llGist.visibility = View.GONE
        }

        if (library.images.size > 0) {
            initLibraryImage(library.images)
            binding.imageViewerLayout.visibility = View.VISIBLE
        } else {
            binding.imageViewerLayout.visibility = View.GONE
        }

        if (library.videos.size > 0) {
            initLibraryVideo(library.videos)
            binding.videoViewerLayout.visibility = View.VISIBLE
        } else {
            binding.videoViewerLayout.visibility = View.GONE
        }

        binding.studyParentLayout.visibility = View.VISIBLE

        library.subject_name = subject_name
        dataManager.mPref.prefSetResumeStudy(library)
        RxBus.get().withKey(RESUME_STUDY_CHANGED).send(UpdateClass())
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
        }
    }


    override fun onClick(v: View?) {
    }

}