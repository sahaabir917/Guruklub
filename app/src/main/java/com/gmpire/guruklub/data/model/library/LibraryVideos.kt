package com.gmpire.guruklub.data.model.library

import java.io.Serializable

data class LibraryVideos(
    var id : String,
    var library_id :String,
    var video_title :String,
    var video_url : String,
    var created_at :String,
    var topic_id :String
):Serializable