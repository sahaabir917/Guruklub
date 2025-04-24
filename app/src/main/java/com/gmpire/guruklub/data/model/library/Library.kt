package com.gmpire.guruklub.data.model.library

data class Library (

	val id : Int,
	val topic_id : Int,
	val subject_id : Int,
	var subject_name : String,
	val section_id : Int,
	val title : String,
	val cover_image : String,
	val details : String,
	val gist : String,
	val status : Int,
	val position : Int,
	val created_by : Int,
	val updateby : Int,
	val approved_by : Int,
	val created_at : String,
	val library_link : String,
	val videos : ArrayList<Videos>,
	val images : ArrayList<Images>,
	val is_math: Int,
	val recommended : Recommended?
)