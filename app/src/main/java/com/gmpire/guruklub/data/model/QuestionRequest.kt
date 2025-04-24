package com.gmpire.guruklub.data.model

import android.graphics.Bitmap
import com.gmpire.guruklub.data.model.library.FilterValues

class QuestionRequest {
    lateinit var filterValues:FilterValues
    var title:String? = null
    var option_a:String? = null
    var option_b:String? = null
    var option_c:String? = null
    var option_d:String? = null
    var answer:String? = null
    var picture:Bitmap? = null
    var answer_explain:String? = null
}