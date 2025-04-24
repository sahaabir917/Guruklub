package com.gmpire.guruklub.view.activity.friendrequest

import android.content.Intent
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
import com.gmpire.guruklub.databinding.FragmentMyFriendBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.gamelevel.FriendRequestActivity
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelViewModel
import com.gmpire.guruklub.view.activity.library.QuestionDetailsActivity
import com.gmpire.guruklub.view.activity.playwithfriends.PlayWithFriendActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.adapter.MyFriendViewHolder
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.dialog.gameLevel.GameLevelInfoDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response


class MyFriendFragment : BaseFragment() {
    private lateinit var binding:FragmentMyFriendBinding
    private lateinit var viewModel: GameLevelViewModel
    private lateinit var friendData: ArrayList<UserInfo>
    private lateinit var heartChooseDialog: HeartChooseDialog

    override fun viewRelatedTask() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(GameLevelViewModel::class.java)
        viewModel.getFriends(this);
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_friend, container, false)
        return binding.root
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when(key){
            "getFriends"->{
                val type = object : TypeToken<BaseModel<ArrayList<UserInfo>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<UserInfo>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200 ) {
                        try {
                            if (baseData.data != null && baseData.data?.size ?: 0 > 0) {
                                binding.rlMyFriend.visibility = View.VISIBLE
                                binding.noPendingRequestLayout.visibility = View.GONE
                                Log.d("datas",baseData.data.toString())
                                friendData = baseData.data as ArrayList<UserInfo>
                                initMyFriend()
                            }
                            else if (baseData.data?.size ?: 0 <= 0) {
                                binding.rlMyFriend.visibility = View.GONE
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
            "unFriend" ->{
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

    private fun initMyFriend() {
        binding.rlMyFriend.layoutManager = LinearLayoutManager(context)
        binding.rlMyFriend.adapter = BaseRecyclerAdapter(context, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {
                model as UserInfo
                when(view.id){
                    R.id.button3 ->{
                        viewModel.unFriend(this@MyFriendFragment,model.id)
                    }

                    R.id.playBtn ->{
//                        val intent = Intent(activity, PlayWithFriendActivity::class.java)
//                        startActivity(intent)
                        heartChooseDialog = HeartChooseDialog.newInstance(model.user_hearts?:"0")
                        heartChooseDialog.show(childFragmentManager, heartChooseDialog.tag)
                    }

                }
            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                return MyFriendViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_my_friend,
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
        fun newInstance() =
            MyFriendFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}