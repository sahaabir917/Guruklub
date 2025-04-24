package com.gmpire.guruklub.data.model.question

import java.io.Serializable

data class LibraryQuestionModel(
    var data : ArrayList<Question>,
    var next_page : Int
):Serializable