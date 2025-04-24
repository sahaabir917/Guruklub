package com.gmpire.guruklub.view.fragment.previousQuestionsBatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.databinding.FragmentPreviousQuestionsBatchBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.batchQuestions.BatchQuestionActivity
import com.gmpire.guruklub.view.activity.infoCenter.InfoCenterViewModel
import com.gmpire.guruklub.view.activity.library.BatchViewHolder
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response

class PreviousQuestionBatchFragment : BaseActivity() {

    fun PreviousQuestionBatchFragment() {
        // doesn't do anything special
    }

    private var filterlist = ArrayList<Common>()
    private lateinit var binding: FragmentPreviousQuestionsBatchBinding
    private var batch = arrayListOf<Common>()
    private var copiedbatch = arrayListOf<Common>()
    private lateinit var viewmodel: InfoCenterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_previous_questions_batch)
    }

    fun populateView(batch: ArrayList<Common>) {
        this.batch.clear()
        this.batch.addAll(batch)
        initRecview()
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetBatchByCategory" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            baseData.data?.let {
                                populateView(baseData.data ?: arrayListOf())
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.appBar -> {
                hideKeyboard()
            }
            binding.appBar2 ->{
                hideKeyboard()
            }
            binding.root ->{
                hideKeyboard()
            }
        }
    }

    override fun viewRelatedTask() {
        viewmodel =
            ViewModelProviders.of(this, viewModelFactory).get(InfoCenterViewModel::class.java)
        viewmodel.apiGetBatchByCategory(
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            this
        )

        setToolbar(this, binding.toolbar, "Previous Questions", true)
        initSearchEditText()
        binding.appBar.setOnClickListener(this)
        binding.appBar2.setOnClickListener(this)
        binding.root.setOnClickListener(this)

        binding.searchEt.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboards(v)
            }
        }

    }

    private fun hideKeyboards(view: View?) {
        val inputMethodManager: InputMethodManager =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun navigateToHome() {

    }

    private fun initSearchEditText() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                fitler(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                copyPlayedModelTest()
            }

        })

    }


    private fun fitler(userInput: String) {
        this.filterlist.clear()
        for (item in batch) {
            if (item.name.toLowerCase().contains(userInput.toLowerCase())) {
                this.filterlist.add(item)
            }
        }


        var sizeoffilterlist = this.filterlist.size - 1
        if (sizeoffilterlist >= 0) {
            binding.rvBatch.visibility = View.VISIBLE
            binding.emptyMessage.visibility = View.GONE
            this.batch.clear()
            this.batch.addAll(filterlist)
            binding.rvBatch.adapter?.notifyDataSetChanged()
        } else if (sizeoffilterlist < 0) {
            binding.emptyMessage.visibility = View.VISIBLE
            binding.rvBatch.visibility = View.GONE
        }


    }

    override fun onLoading(isLoader: Boolean, key: String) {
        when (key) {
            "apiGetBatchByCategory" -> {
                if (isLoader)
                    showProgressDialog("")
                else
                    hideProgressDialog()
            }
        }
    }

    private fun copyPlayedModelTest() {
        var sizeofCopiedBatchData = this.copiedbatch.size - 1
        this.batch.clear()
        for (i in 0..sizeofCopiedBatchData) {
            var copyDatas = copiedbatch[i].copy(
                copiedbatch[i].id,
                copiedbatch[i].name
            )
            this.batch.add(copyDatas)
        }
    }

    private fun initRecview() {

        initCopyBatchData()

        binding.rvBatch.layoutManager = LinearLayoutManager(this)
        binding.rvBatch.adapter = BaseRecyclerAdapter(this, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {
                model as Common
                startActivity(
                    Intent(
                        this@PreviousQuestionBatchFragment,
                        BatchQuestionActivity::class.java
                    ).putExtra(
                        "batch",
                        model
                    )
                )

            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

                return BatchViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context)
                        , R.layout.item_previous_questions_batch_item
                        , parent, false
                    )
                    , this@PreviousQuestionBatchFragment
                )
            }

            override fun loadMoreItem() {

            }
        }, batch)
    }

    private fun initCopyBatchData() {
        var sizeofBatchData = this.batch.size - 1
        this.copiedbatch.clear()
        for (i in 0..sizeofBatchData) {
            var copyDatas = this.batch[i].copy(
                this.batch[i].id,
                this.batch[i].name
            )
            this.copiedbatch.add(copyDatas)
        }
    }

}

