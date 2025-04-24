package com.gmpire.guruklub.data.model.game

import com.gmpire.guruklub.data.model.question.GameResponseQuestion
import java.io.Serializable

class GameResultSubmitRequestItem : Serializable {
    var type: String? = null
    var category_id:String? = null
    var model_test_id: String? = null
    var slug: String? = null
    var time: Long = 0L
    var questions: ArrayList<GameResponseQuestion>?= null
    var exam_status : Boolean = true
}
