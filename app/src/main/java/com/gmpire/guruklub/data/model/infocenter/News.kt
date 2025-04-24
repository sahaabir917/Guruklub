package com.gmpire.guruklub.data.model.infocenter

import com.gmpire.guruklub.data.model.library.Images
import com.gmpire.guruklub.data.model.library.Videos
import java.io.Serializable

data class News(
    val category_id: String = "",
    val category_name: String = "",
    val cover_image: String = "",
    val created_at: String = "",
    val date: String = "",
    val details: String = "",
    val id: String = "",
    val is_popular: String = "",
    val status: String = "",
    val title: String = "",
    var image_list: ArrayList<Images>? = arrayListOf(),
    var video_list: ArrayList<Videos>? = arrayListOf()
) : Serializable