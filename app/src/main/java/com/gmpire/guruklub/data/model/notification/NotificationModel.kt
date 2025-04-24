package com.gmpire.guruklub.data.model.notification

import java.io.Serializable

data class NotificationModel(
    val action: String,
    val category_name: String,
    val created_at: String,
    var details: String,
    val id: String,
    val picture: String?,
    val title: String,
    val link: String?,
    val link_label: String?
) : Serializable {

    fun hasLink(): Boolean {
        if (!link.isNullOrEmpty() && !link_label.isNullOrEmpty()) {
            if (link == "null" || link_label == "null") {
                return false
            }
            return true
        }
        return false
    }

}