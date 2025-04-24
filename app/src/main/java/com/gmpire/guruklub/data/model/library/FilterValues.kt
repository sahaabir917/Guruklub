package com.gmpire.guruklub.data.model.library

import java.io.Serializable

class FilterValues : Serializable {
    var category_id: String = ""
    var subject_id: String = ""
    var section_id: String = ""
    var topic_id: String = ""
    var difficulty_id: String = ""
    var batch_id: String = ""
    var type: String? = ""

    fun clearValues() {
        subject_id = ""
        section_id = ""
        topic_id = ""
        difficulty_id = ""
        batch_id = ""
        type = ""
    }
}