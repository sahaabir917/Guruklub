package com.gmpire.guruklub.view.activity.photoViewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.library.Images
import com.gmpire.guruklub.databinding.ActivityPhotoViewerBinding
import com.gmpire.guruklub.view.adapter.ImagesViewerAdapter

class PhotoViewerActivity : AppCompatActivity() {

    private var position: Int = 0
    private lateinit var images: ArrayList<Images>
    private lateinit var binding: ActivityPhotoViewerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_photo_viewer)

        images = intent.getSerializableExtra("images") as ArrayList<Images>
        position = intent.getIntExtra("position",0)

        initImages(images)

        binding.backBtnIv.setOnClickListener {
            onBackPressed()
        }

    }

    private fun initImages(newsImages: List<Images>) {
        binding.imageViewerViewPager.adapter = ImagesViewerAdapter(this, newsImages)

        binding.imageViewerViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                binding.imageViewerPageIndicatorView.selection = position
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        binding.imageViewerViewPager.setCurrentItem(position)
    }
}
