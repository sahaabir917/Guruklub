package com.gmpire.guruklub.data.model.categoryAndSubject

open class HeaderFilter(var headerText : String) : BaseItem() {
    var isAllSelected : Boolean = false
    init {
        this.name = headerText
    }
}