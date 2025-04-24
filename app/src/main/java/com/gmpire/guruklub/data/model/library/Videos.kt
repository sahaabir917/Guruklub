package com.gmpire.guruklub.data.model.library

import java.io.Serializable

data class Videos (
	val id : Int = 0,
	val library_id : Int=0,
	val news_id : String?=null,
	val video_title : String="",
	val video_url : String="",
	val created_at : String=""
):Serializable