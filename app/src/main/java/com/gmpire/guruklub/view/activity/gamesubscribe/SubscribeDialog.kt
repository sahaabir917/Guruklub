package com.gmpire.guruklub.view.activity.gamesubscribe

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.adsubscribepaymentresponse.AdSubscribePaymentResponse
import com.gmpire.guruklub.data.model.adsubscribepaymentresponse.PaymentInfo
import com.gmpire.guruklub.data.model.demomodel.MoneyModel
import com.gmpire.guruklub.data.model.gameheartpackages.GameHeartPackage
import com.gmpire.guruklub.data.model.paymentexc.PaymentExcForSubHeart
import com.gmpire.guruklub.databinding.DialogFragmentSubscribeBinding
import com.gmpire.guruklub.util.GameServiceHelper
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.appEvents.AppEvent
import com.gmpire.guruklub.view.activity.gameheart.UnLimitedHeartAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseDialogFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization
import com.sslwireless.sslcommerzlibrary.model.response.SSLCTransactionInfoModel
import com.sslwireless.sslcommerzlibrary.model.util.SSLCCurrencyType
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType
import com.sslwireless.sslcommerzlibrary.view.singleton.IntegrateSSLCommerz
import com.sslwireless.sslcommerzlibrary.viewmodel.listener.SSLCTransactionResponseListener
import okhttp3.ResponseBody
import retrofit2.Response

class SubscribeDialog : BaseDialogFragment(), SSLCTransactionResponseListener {
    private lateinit var paymentInfo: PaymentInfo
    private var packageId: String = ""
    private lateinit var binding: DialogFragmentSubscribeBinding
    private var moneyModel: ArrayList<MoneyModel> = ArrayList<MoneyModel>()
    private lateinit var viewModel: SubscribeActivityViewModel
    private lateinit var heartData: ArrayList<GameHeartPackage>
    private var copiedHeartData: ArrayList<GameHeartPackage> = ArrayList<GameHeartPackage>()
    private lateinit var subscriptionListener: SubscribeDialog.SubscriptionListener
    private var moneyAmount: Double = 0.0
    private var gamesound: Boolean = false
    private lateinit var paymentExcForSubHeart: PaymentExcForSubHeart

    override fun viewRelatedTask() {
        gamesound = (dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false) ?: false
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(SubscribeActivityViewModel::class.java)
        moneyAmount = 0.0
        viewModel.getUnlimitedHeartPackage(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle1)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_subscribe, container, false)

        binding.imageView32.setOnClickListener {
            if (gamesound) {
                playBtnSound()
            }
            if (moneyAmount < 10) {
                showToast(requireContext(), "Please select a package")
            } else {
                viewModel.getHeartAddPayment(this, packageId)
            }
        }

        binding.imageView35.setOnClickListener {
            subscriptionListener.onCloseBtnClicked()
        }

        return binding.root
    }

    private fun initSslPayment() {
        val sslCommerzInitialization = SSLCommerzInitialization(
            "gurukluborglive",
            "607FA869551A380646",
            moneyAmount.toDouble(),
            SSLCCurrencyType.BDT,
            paymentInfo.tnx_id,
            "education",
            SSLCSdkType.LIVE
        )
        IntegrateSSLCommerz
            .getInstance(context)
            .addSSLCommerzInitialization(sslCommerzInitialization)
            .buildApiCall(this)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "getUnlimitedHeartPackage" -> {
                val type = object : TypeToken<BaseModel<ArrayList<GameHeartPackage>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<GameHeartPackage>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                heartData =
                                    baseData.data ?: arrayListOf()
                                initBuyLife()
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            "getHeartAddPayment" -> {
                val type = object : TypeToken<BaseModel<AdSubscribePaymentResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<AdSubscribePaymentResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (GameServiceHelper.isMusicRunning)
                            GameServiceHelper.stopMusic(requireContext())
                        try {
                            paymentInfo = baseData?.data!!.payment_info
                            initSslPayment()
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }

                    } else if (baseData.status_code == 500) {
                        showToast(requireContext(), baseData.message[0])
                    }
                }
            }

            "getPaymentExecution" -> {
                val type = object : TypeToken<BaseModel<PaymentExcForSubHeart>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<PaymentExcForSubHeart>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                paymentExcForSubHeart = baseData.data!!
                                AppEvent.getInstance(requireContext())
                                    .logUnLimitedHeartPurchaseEvent(
                                        dataManager.mPref.prefGetUserInfo().name ?: "",
                                        paymentExcForSubHeart.days.toInt()
                                    )
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    } else {
                        showToast(requireContext(), baseData.message[0])
                    }
                }
            }

            "getFailedTransaction" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        showToast(requireContext(), baseData.message[0])
                    }
                }
            }
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {

    }

    override fun onError(err: Throwable, key: String) {

    }

    override fun onErrorCode(
        code: Int,
        message: String,
        result: LiveDataResult<Response<ResponseBody>>
    ) {
        Log.d("error", message.toString())
    }


    private fun initBuyLife() {
        binding.subscribeRl.layoutManager = LinearLayoutManager(requireContext())
        binding.subscribeRl.adapter = BaseRecyclerAdapter(context, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {
                subscriptionListener.switchBtnClicked()
                model as GameHeartPackage
                if (model.switchOnOff) {
                    moneyAmount = 0.0
                    packageId = ""
                } else {
                    moneyAmount = heartData[position].price.toDouble()
                    packageId = heartData[position].id
                    CopiedHeartData(position)
                }
            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                return UnLimitedHeartAdapter(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_subscribe_details,
                        parent,
                        false
                    ),
                    context
                )
            }

            override fun loadMoreItem() {
            }

        }, heartData)
    }

    private fun CopiedHeartData(position: Int) {
        var sizeofheartdata = heartData.size - 1
        copiedHeartData.clear()
        for (i in 0..sizeofheartdata) {
            if (i != position) {
                var heartdatas = heartData[i].copy(
                    heartData[i].active,
                    heartData[i].days,
                    heartData[i].hearts,
                    heartData[i].id,
                    heartData[i].price,
                    heartData[i].type,
                    false
                )
                copiedHeartData.add(heartdatas)
            } else if (i == position) {
                var heartdatas = heartData[i].copy(
                    heartData[i].active,
                    heartData[i].days,
                    heartData[i].hearts,
                    heartData[i].id,
                    heartData[i].price,
                    heartData[i].type,
                    true
                )
                copiedHeartData.add(heartdatas)
            }
        }
        ChangeHeartData()
    }

    private fun ChangeHeartData() {
        var sizeofcopiedheartdata = copiedHeartData.size - 1
        heartData.clear()
        for (i in 0..sizeofcopiedheartdata) {
            var heartDatas = copiedHeartData[i].copy(
                copiedHeartData[i].active,
                copiedHeartData[i].days,
                copiedHeartData[i].hearts,
                copiedHeartData[i].id,
                copiedHeartData[i].price,
                copiedHeartData[i].type,
                copiedHeartData[i].switchOnOff
            )
            heartData.add(heartDatas)
        }
        binding.subscribeRl.adapter?.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SubscribeDialog().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun transactionFail(p0: String?) {
        viewModel.getFailedTransaction("FAILED", p0.toString(), paymentInfo.tnx_id, this)
    }

    override fun merchantValidationError(p0: String?) {
        viewModel.getFailedTransaction("INVALID", p0.toString(), paymentInfo.tnx_id, this)
    }

    override fun transactionSuccess(p0: SSLCTransactionInfoModel?) {
        viewModel.getPaymentExecution(
            this,
            p0?.status,
            p0?.tranDate,
            paymentInfo.tnx_id,
            p0?.valId,
            p0?.amount,
            p0?.storeAmount,
            p0?.bankTranId,
            p0?.cardType,
            p0?.cardNo
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SubscribeDialog.SubscriptionListener) {
            subscriptionListener = context
        }
    }

    private fun playBtnSound() {
        gamesound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        var backgroundMusic = MediaPlayer.create(context, R.raw.button_press)
        backgroundMusic.isLooping = false
        backgroundMusic.start()
    }

    interface SubscriptionListener {
        fun onCloseBtnClicked()
        fun switchBtnClicked()
    }
}