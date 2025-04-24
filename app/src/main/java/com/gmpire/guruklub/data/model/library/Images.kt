package com.gmpire.guruklub.data.model.library

import java.io.Serializable

data class Images (
	val id : Int,
	val library_id : Int,
	val slide_picture_title : String,
	val picture : String,
	val news_id : String,
	val created_at : String
):Serializable