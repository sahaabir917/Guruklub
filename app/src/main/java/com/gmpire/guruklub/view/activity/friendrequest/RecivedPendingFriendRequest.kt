package com.gmpire.guruklub.view.activity.friendrequest

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.databinding.FragmentRecivedPendingFriendRequestBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelViewModel
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.adapter.PendingRecivedFriendRequestViewHolder
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response


class RecivedPendingFriendRequest : BaseFragment() {


    private lateinit var binding: FragmentRecivedPendingFriendRequestBinding
    private lateinit var viewModel: GameLevelViewModel
    private var friendData: ArrayList<UserInfo>?=null
    override fun viewRelatedTask() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(GameLevelViewModel::class.java)
        viewModel.getPendingReceivedRequest(this)
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

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recived_pending_friend_request, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when(key){
            "getPendingReceivedRequest" ->{
                val type = object : TypeToken<BaseModel<ArrayList<UserInfo>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<UserInfo>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null && baseData.data?.size?:0 >0) {
                                binding.pendingRecivedFriendRequestRv.visibility = View.VISIBLE
                                binding.noPendingRequestLayout.visibility = View.GONE
                                Log.d("datas",baseData.data.toString())
                                friendData = baseData.data as ArrayList<UserInfo>
                                initPendingRecivedFriendRequest()
                            }
                            else if(baseData.data?.size ?: 0 <=0){
                                binding.pendingRecivedFriendRequestRv.visibility = View.GONE
                                binding.noPendingRequestLayout.visibility = View.VISIBLE
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    } else {
                        showToast(context, baseData.message[0])
                    }
                }
            }
            "responseFriendRequest" ->{
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        showToast(context!!, baseData.message[0])
                    }
                }
            }
        }
    }

    private fun initPendingRecivedFriendRequest() {
        binding.pendingRecivedFriendRequestRv.layoutManager = LinearLayoutManager(context)
        binding.pendingRecivedFriendRequestRv.adapter = BaseRecyclerAdapter(context, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {
                model as UserInfo
//                viewModel.sendFriendRequest(this@RequestFriendFragment,model.id)
                when(view.id){
                    R.id.button3 ->{
                        viewModel.responseFriendRequest(model.id,"1",this@RecivedPendingFriendRequest)
                    }
                    R.id.button4 ->{
                        viewModel.responseFriendRequest(model.id,"0",this@RecivedPendingFriendRequest)
                    }
                }
            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                return PendingRecivedFriendRequestViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_pending_recived_friend_request,
                        parent,
                        false
                    ),
                    context
                )
            }

            override fun loadMoreItem() {
            }

        }, friendData!!)
    }

    override fun onClick(v: View?) {

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecivedPendingFriendRequest().apply {
                arguments = Bundle().apply {

                }
            }
    }
}