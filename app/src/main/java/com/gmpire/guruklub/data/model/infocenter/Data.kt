package com.gmpire.guruklub.data.model.infocenter

import com.gmpire.guruklub.data.model.library.Images
import com.gmpire.guruklub.data.model.library.Videos

data class Data(
    val image_list: ArrayList<Images>,
    val video_list: ArrayList<Videos>,
    val news: List<News>
)