package com.gmpire.guruklub.data.model.categoryAndSubject

import com.gmpire.guruklub.databinding.ActivityAllLibrarySubjectBinding
import com.gmpire.guruklub.view.activity.library.LibraryViewmodel

open class BaseItem(var id: String? = null, var name: String? = null) {
    var parentPos : Int? = 0
    var isSelected : Boolean = false
}
