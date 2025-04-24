package com.gmpire.guruklub.data.model.library

import java.io.Serializable

data class LastSeenLibrary(
   var id : String,
   var user_id :String,
   var category_id : String,
   var subject_id :String,
   var section_id :String,
   var topic_id :String,
   var subject_name:String,
   var section_name :String,
   var topic_name :String
):Serializable