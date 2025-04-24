package com.gmpire.guruklub.data.model.library

data class Recommended (
	val id : Int,
	val topic_id : Int,
	val section_id : Int,
	val subject_name : String,
	val title : String
)