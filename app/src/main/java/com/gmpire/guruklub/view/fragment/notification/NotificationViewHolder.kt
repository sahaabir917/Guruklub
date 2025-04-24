package com.gmpire.guruklub.view.fragment.notification

import android.content.Context
import android.os.Build
import android.text.Html
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.notification.NotificationModel
import com.gmpire.guruklub.databinding.ItemNotificationBinding
import com.gmpire.guruklub.util.DateUtil.Companion.simpleDateFormat
import com.gmpire.guruklub.util.DateUtil.Companion.simpleDateFormatServer
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import java.lang.reflect.InvocationTargetException

class NotificationViewHolder(itemView: ViewDataBinding, context: Context) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemNotificationBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as NotificationModel

        binding.notificationTitleTv.text = itemModel.title

        val details = "<body style=\"margin: 0; padding: 0\">" + itemModel.details + "</body>"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.notificationDetailsTv.text =
                Html.fromHtml(details, Html.FROM_HTML_MODE_COMPACT)
        } else {
            binding.notificationDetailsTv.text =
                Html.fromHtml(details)
        }

        try {
            if (itemModel.created_at.isNotEmpty()) {
                binding.dateTv.text =
                    simpleDateFormat.format(simpleDateFormatServer.parse(itemModel.created_at))
            }
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.root.setOnClickListener {
            listener.callBack(position, itemModel, binding.root)
        }


        val colorVal = position % 4

        when (colorVal) {
            0 -> {
                binding.viewNotification.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.notificationTitleTv.context,
                        R.color.lightOrange
                    )
                )
            }
            1 -> {
                binding.viewNotification.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.notificationTitleTv.context,
                        R.color.paleLime
                    )
                )
            }
            2 -> {
                binding.viewNotification.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.notificationTitleTv.context,
                        R.color.green
                    )
                )
            }
            3 -> {
                binding.viewNotification.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.notificationTitleTv.context,
                        R.color.indigo
                    )
                )
            }
        }

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }

    override fun onLastPosition() {
        // binding.view.visibility = View.GONE
    }
}