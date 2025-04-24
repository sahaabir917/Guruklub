package com.gmpire.guruklub.di

import android.app.Application
import com.gmpire.guruklub.MyApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (AndroidInjectionModule::class),
        (AndroidSupportInjectionModule::class),
        (ActivityModule::class),
        (ViewModelModule::class),
        (AppModule::class),
        (WorkerModule::class)
    ]
)
interface AppComponent : AndroidInjector<DaggerApplication> {

    fun injectApplication(app: MyApp)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): AppComponent
    }
}