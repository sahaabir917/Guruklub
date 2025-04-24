package com.gmpire.guruklub.view.activity.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.ActivityAddVideoSuccessFullBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.base.BaseActivity
import okhttp3.ResponseBody
import retrofit2.Response

class AddVideoSuccessFull : BaseActivity() {
    private lateinit var binding: ActivityAddVideoSuccessFullBinding
    private var isUpload: String = ""
    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Add Videos", true)
        isUpload = intent.extras?.get("Uploaded").toString()
        binding.btnBack.setOnClickListener(this)
        binding.btnAddQuestion.setOnClickListener(this)
        if(isUpload == "unsuccessfull"){
            binding.viewTitle.text = "Video Add Failed"
            binding.ivSuccessOrFail.setImageResource(R.drawable.fail_ic)
            binding.tvTitle.text = "Failed"
            binding.tvDescription.text = "Sorry your video uploading is failed, please try again"
        }
    }

    override fun navigateToHome() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_add_video_success_full)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_video_success_full)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

    }

    override fun onClick(v: View?) {
        when(v){
            binding.btnBack ->{
                onBackPressed()
            }
            binding.btnAddQuestion ->{
                val intent = Intent(this, AddVideoActivity::class.java)
                startActivity(intent)
            }
        }
    }
}