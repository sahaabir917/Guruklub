package com.gmpire.guruklub.util.fcm

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.notification.NotificationModel
import com.gmpire.guruklub.data.prefence.PreferencesHelper
import com.gmpire.guruklub.util.SubscriptUtil
import com.gmpire.guruklub.view.activity.friendrequest.FriendRequestFragment
import com.gmpire.guruklub.view.activity.gamelevel.FriendRequestActivity
import com.gmpire.guruklub.view.activity.login.LoginActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM Service"
    private var notificationID: Int = System.currentTimeMillis().toInt() + 1
    lateinit var pendingIntent: PendingIntent
    lateinit var action: String
    var questionString = ""
    var newsString = ""
    var friendRequestString = ""

    companion object {
        const val NO_ACTION = "no_action"
        const val NOTIFICATION_DETAILS = "notification_details"
        const val GAME_SCREEN = "game_screen"
        const val GURUKLUB_GAME = "guruklub_game"
        const val HOME_SCREEN = "practice_screen"
        const val HOME_QUESTION = "home_question"
        const val LIBRARY_SCREEN_MAIN = "library_screen_main"
        const val INFO_CENTRE_SCREEN = "news_details"
        const val INFO_CENTRE_MAIN = "infocenter_main"
        const val PROFILE_MAIN = "profile_main"
        const val PROFILE_LEADERBOARD = "profile_leaderboard"
        const val PROFILE_ERROR = "profile_error"
        const val PROFILE_BOOKMARKS = "profile_bookmarks"
        const val HOME_SEARCH = "home_search"
        const val HELP_N_SUPPORT = "help_support_main"
        const val PLAY_STORE = "play_store"
        const val GAME_FRIEND_REQUEST = "friend_request"
        const val GAME_PURCHASE_HEART = "purchase_hearts"
        const val Add_HEARTS = "add_hearts"
        const val VIDEOS = "videos"
        const val DRAWER_MENU = "drawer_menu"
    }

    lateinit var mPref: PreferencesHelper


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: $remoteMessage")
        //Log.d(TAG, "Notification Message Body: " + remoteMessage.notification?.body!!)
        if (remoteMessage.data["title"] != null) {
            val params = remoteMessage.data
            val notificationObject = NotificationModel(
                params["action"].toString(),
                params["category_name"].toString(),
                params["created_at"].toString(),
                params["details"].toString(),
                params["id"].toString(),
                params["picture"],
                params["title"].toString(),
                params["link"].toString(),
                params["link_label"].toString()
            )
            // Just for question
            if (remoteMessage.data.containsKey("question_object")) {
                questionString = params["question_object"].toString()
                notificationObject.details = "Click to view the question"
            }
            //Just news
            if (remoteMessage.data.containsKey("news_object")) {
                newsString = params["news_object"].toString()
                notificationObject.details = "Click to view the news"
            }

            if(remoteMessage.data.containsKey("requester_object")){
                friendRequestString = params["requester_object"].toString()
                notificationObject.details = "Click to view the new request"
            }

            sendNotification(notificationObject)
        }
    }


    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        mPref = PreferencesHelper(this)
        mPref.saveFcmToken(this, token)
    }

    private fun sendNotification(
        notificationModel: NotificationModel
    ) {
        //ShareInfo.instance.saveFlag(this, "1")
        mPref = PreferencesHelper(this)
        val requestCode = System.currentTimeMillis().toInt()
        if (mPref.isAuthorized()) {
            val intent: Intent
            when (notificationModel.action) {
                NO_ACTION -> {
                    if (notificationModel.hasLink()) {
                        intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(notificationModel.link)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    } else
                        intent = Intent(this, MainActivity::class.java)
                }
                HOME_SCREEN -> {
                    intent = Intent(this, MainActivity::class.java).putExtra("goto", HOME_SCREEN)
                }
                HOME_QUESTION -> {
                    intent = Intent(this, MainActivity::class.java).putExtra("goto", HOME_QUESTION)
                    intent.putExtra("question_object", questionString)
                    notificationModel.details = "Click to view the question"
                }
                GAME_SCREEN -> {
                    intent = Intent(this, MainActivity::class.java).putExtra("goto", GAME_SCREEN)
                }
                GURUKLUB_GAME -> {
                    intent = Intent(this, MainActivity::class.java).putExtra("goto", GURUKLUB_GAME)
                }
                LIBRARY_SCREEN_MAIN -> {
                    intent =
                        Intent(this, MainActivity::class.java).putExtra("goto", LIBRARY_SCREEN_MAIN)
                }
                INFO_CENTRE_SCREEN -> {
                    intent =
                        Intent(this, MainActivity::class.java).putExtra("goto", INFO_CENTRE_SCREEN)
                    intent.putExtra("news_object", newsString)
                    notificationModel.details = "Click to view the news"
                }
                NOTIFICATION_DETAILS -> {
                    intent = Intent(this, MainActivity::class.java).putExtra(
                        "notification_model",
                        Gson().toJson(notificationModel)
                    ).putExtra("goto", NOTIFICATION_DETAILS)
                }
                INFO_CENTRE_MAIN -> {
                    intent =
                        Intent(this, MainActivity::class.java).putExtra("goto", INFO_CENTRE_MAIN)
                }
                PROFILE_MAIN -> {
                    intent = Intent(this, MainActivity::class.java).putExtra("goto", PROFILE_MAIN)
                }
                PROFILE_LEADERBOARD -> {
                    intent =
                        Intent(this, MainActivity::class.java).putExtra("goto", PROFILE_LEADERBOARD)
                }
                PROFILE_ERROR -> {
                    intent = Intent(this, MainActivity::class.java).putExtra("goto", PROFILE_ERROR)
                }
                PROFILE_BOOKMARKS -> {
                    intent =
                        Intent(this, MainActivity::class.java).putExtra("goto", PROFILE_BOOKMARKS)
                }
                HOME_SEARCH -> {
                    intent = Intent(this, MainActivity::class.java).putExtra("goto", HOME_SEARCH)
                }
                HELP_N_SUPPORT -> {
                    intent = Intent(this, MainActivity::class.java).putExtra("goto", HELP_N_SUPPORT)
                }
                PLAY_STORE -> {
                    intent = Intent(this, MainActivity::class.java).putExtra("goto", PLAY_STORE)
                }

                GAME_FRIEND_REQUEST->{
                    intent = Intent(this, MainActivity::class.java).putExtra("friend_request_object",Gson().toJson(friendRequestString)).putExtra("goto", GAME_FRIEND_REQUEST)
                }

                GAME_PURCHASE_HEART ->{
                    intent = Intent(this,MainActivity::class.java).putExtra("goto",GAME_PURCHASE_HEART)
                }

                Add_HEARTS ->{
                    intent = Intent(this,MainActivity::class.java).putExtra("goto", Add_HEARTS)
                }

                VIDEOS ->{
                    intent = Intent(this,MainActivity::class.java).putExtra("goto", VIDEOS)
                }

                DRAWER_MENU->{
                    intent = Intent(this,MainActivity::class.java).putExtra("goto", DRAWER_MENU)
                }


                else -> {
                    if (notificationModel.hasLink()) {
                        intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(notificationModel.link)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    } else
                        intent = Intent(this, MainActivity::class.java)
                }
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra("title", notificationModel.title)
            intent.putExtra("message", notificationModel.details)

            pendingIntent = PendingIntent.getActivity(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val broadcastIntent =
                Intent(notificationModel.action).putExtra("notification", notificationModel)
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)

        } else {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("title", notificationModel.title)
            intent.putExtra("message", notificationModel.details)
            pendingIntent = PendingIntent.getActivity(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        var contentTile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(notificationModel.title, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(notificationModel.title)
        }

        var isScript = false
        if (SubscriptUtil.checkIfContainsSubscript(notificationModel.title)) {
            contentTile = SubscriptUtil.getSubscriptSpan(contentTile)
            isScript = true
        }

        val channelId = "30000"
        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationLayout = RemoteViews(packageName, R.layout.layout_custom_notification_small)
        notificationLayout.setTextViewText(R.id.content_title, contentTile)
        notificationLayout.setTextViewText(R.id.content_text, notificationModel.details)

        val notificationLayoutExpanded =
            RemoteViews(packageName, R.layout.layout_custom_notification_large)
        notificationLayoutExpanded.setTextViewText(R.id.content_title, contentTile)
        notificationLayoutExpanded.setTextViewText(R.id.content_text, notificationModel.details)

        if (notificationModel.hasLink()) {
            isScript = true
            notificationLayoutExpanded.setTextViewText(
                R.id.tv_action_link,
                notificationModel.link_label
            )
            notificationLayoutExpanded.setInt(R.id.tv_action_link, "setVisibility", View.VISIBLE)

            val newIntent = Intent(Intent.ACTION_VIEW)
            newIntent.setData(Uri.parse(notificationModel.link))

            val apendingIntent = PendingIntent.getActivity(
                this, requestCode, newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            notificationLayoutExpanded.setOnClickPendingIntent(R.id.tv_action_link, apendingIntent)
        }

        // If not Super/Subscript then show default notification, else use custom layout
        var notificationBuilder = if (!isScript) {
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_large)
                .setContentTitle(contentTile)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationModel.details))
                .setContentText(notificationModel.details)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        } else {
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_large)
                .setContentTitle(contentTile)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setContentText(notificationModel.details)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        }

        if (notificationModel.picture != null) {
            Glide.with(this)
                .asBitmap()
                .load(BuildConfig.SERVER_URL + notificationModel.picture)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        notificationBuilder = notificationBuilder.setStyle(
                            NotificationCompat.BigPictureStyle().bigPicture(resource)
                        )
                        send(channelId, notificationBuilder)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })

        } else {
            send(channelId, notificationBuilder)
        }

    }

    private fun send(channelId: String, notificationBuilder: NotificationCompat.Builder) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(
            ++notificationID /* ID of notification */,
            notificationBuilder.build()
        )
    }

}

