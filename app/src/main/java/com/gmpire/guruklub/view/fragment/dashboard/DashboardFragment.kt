package com.gmpire.guruklub.view.fragment.dashboard

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.FragmentDashboardBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.fragment.home.HomeFragment
import okhttp3.ResponseBody
import retrofit2.Response


class DashboardFragment : BaseFragment() {

    private lateinit var binding : FragmentDashboardBinding
    private lateinit var dashboardOnClickListener: DashboardFragment.DashboardOnClickListener
    private lateinit var mContext: Context
    override fun viewRelatedTask() {
        binding.practiceCardview.setOnClickListener(this)
        binding.dashboardGameLayout.setOnClickListener(this)
        binding.dashboardLiveExamLayout.setOnClickListener(this)
        binding.dashboardModelTestLayout.setOnClickListener(this)
        binding.dashboardPreviousQuestionLayout.setOnClickListener(this)
        binding.dashboardCurrentAffairsLayout.setOnClickListener(this)
        binding.dashboardLibraryLayout.setOnClickListener(this)
        binding.dashboardAllVideoLayout.setOnClickListener(this)
        binding.dashboardAllQuestionLayout.setOnClickListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dashboardOnClickListener = context as DashboardOnClickListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        return binding.root
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

    }

    override fun onClick(v: View?) {
        when(v){
            binding.practiceCardview ->{
                dashboardOnClickListener.onPracticeClicked()
            }
            binding.dashboardGameLayout ->{
                dashboardOnClickListener.onGameIconClicked()
            }
            binding.dashboardLiveExamLayout ->{
                dashboardOnClickListener.onLiveExamIconClicked()
            }
            binding.dashboardModelTestLayout ->{
                dashboardOnClickListener.onModelTestIconClicked()
            }
            binding.dashboardPreviousQuestionLayout->{
                dashboardOnClickListener.onPreviousQuestionIconClicked()
            }
            binding.dashboardCurrentAffairsLayout ->{
                dashboardOnClickListener.onCurrentAffairsIconClicked()
            }
            binding.dashboardLibraryLayout ->{
                dashboardOnClickListener.onLibraryIconClicked()
            }
            binding.dashboardAllVideoLayout ->{
                dashboardOnClickListener.onAllVideoIconClicked()
            }
            binding.dashboardAllQuestionLayout ->{
                dashboardOnClickListener.onAddQuestionIconClicked()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DashboardFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    interface DashboardOnClickListener{
        fun onPracticeClicked()
        fun onGameIconClicked()
        fun onLiveExamIconClicked()
        fun onModelTestIconClicked()
        fun onPreviousQuestionIconClicked()
        fun onCurrentAffairsIconClicked()
        fun onLibraryIconClicked()
        fun onAllVideoIconClicked()
        fun onAddQuestionIconClicked()
    }
}