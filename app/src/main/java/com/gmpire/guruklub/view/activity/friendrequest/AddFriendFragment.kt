package com.gmpire.guruklub.view.activity.friendrequest

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.gameheartpackages.GameHeartPackage
import com.gmpire.guruklub.data.model.gamelevel.GameLevelItem
import com.gmpire.guruklub.data.model.gamelevel.Level
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.databinding.FragmentAddFriendBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.gameheart.BuyHeartHolder
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelViewModel
import com.gmpire.guruklub.view.activity.infoCenter.InfoCenterViewModel
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response


class AddFriendFragment : BaseFragment() {


    private lateinit var friendData: ArrayList<UserInfo>
    private lateinit var binding : FragmentAddFriendBinding
    private lateinit var viewModel: GameLevelViewModel
    override fun viewRelatedTask() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(GameLevelViewModel::class.java)
        viewModel.getFriendSuggestion(this);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_friend, container, false)
        initSearchFriend()
        return binding.root
    }

    private fun initSearchFriend() {
        binding.searchFriendEdittext.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(s.toString().length == 0){
                    friendData.clear()
                    viewModel.getFriendSuggestion(this@AddFriendFragment)

                }
                else{
                    friendData.clear()
                    viewModel.searchFriend(this@AddFriendFragment,s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when(key){
            "getFriendSuggestion" ->{
                val type = object : TypeToken<BaseModel<ArrayList<UserInfo>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<UserInfo>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                Log.d("datas",baseData.data.toString())
                                friendData = baseData.data as ArrayList<UserInfo>
                                initAllFriendSuggestion()
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    } else {
                        showToast(context, baseData.message[0])
                    }
                }
            }
            "sendFriendRequest" ->{
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
                    else{
                        showToast(context!!,baseData.message[0])
                    }
                }
            }

            "searchFriend" ->{
                val type = object : TypeToken<BaseModel<ArrayList<UserInfo>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<UserInfo>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                Log.d("datas",baseData.data.toString())
                                var friendDatas = baseData.data as ArrayList<UserInfo>
                                friendData.addAll(friendDatas)
                                binding.rlfriendsuggestion.adapter?.notifyDataSetChanged()
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    } else {
                        showToast(context, baseData.message[0])
                    }
                }
            }

        }
    }

    private fun initAllFriendSuggestion() {
        binding.rlfriendsuggestion.layoutManager = LinearLayoutManager(context)
        binding.rlfriendsuggestion.adapter = BaseRecyclerAdapter(context, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {
                model as UserInfo
                viewModel.sendFriendRequest(this@AddFriendFragment,model.id)
            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                return FriendSuggestionViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_friend_suggestion,
                        parent,
                        false
                    ),
                    context
                )
            }

            override fun loadMoreItem() {
            }

        }, friendData)
    }

    override fun onClick(v: View?) {

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddFriendFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}