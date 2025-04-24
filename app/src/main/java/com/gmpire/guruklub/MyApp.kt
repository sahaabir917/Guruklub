package com.gmpire.guruklub

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.gmpire.guruklub.di.DaggerAppComponent
import com.gmpire.guruklub.sync.GameWorkRequest
import com.gmpire.guruklub.sync.GuruKlubWorkerFactory
import com.google.android.gms.ads.MobileAds
import com.splunk.mint.Mint
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject

class MyApp : DaggerApplication() {

    lateinit var context: Context
    var displayNoticeBoard = false

    private val appComponent = DaggerAppComponent.builder()
        .application(this)
        .build()

    @Inject
    lateinit var guruKlubWorkerFactory: GuruKlubWorkerFactory

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        instance = this
    }

    companion object {
        lateinit var instance: MyApp
        fun getInstance(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        Mint.initAndStartSession(this, "8a02d80a")
        MobileAds.initialize(this)
        appComponent.injectApplication(this)
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(guruKlubWorkerFactory)
                .build()
        )
        scheduleWork()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return appComponent
    }



    private fun scheduleWork() {
        val workRequest = GameWorkRequest.getGameHeartWorkRequest()
        WorkManager.getInstance(this)
            .enqueue(workRequest)

    }
}
