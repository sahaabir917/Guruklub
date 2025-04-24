package com.gmpire.guruklub.di

import androidx.work.WorkerFactory
import com.gmpire.guruklub.sync.ChildWorkerFactory
import com.gmpire.guruklub.sync.GameHeartUpdateWorker
import com.gmpire.guruklub.sync.GuruKlubWorkerFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface WorkerModule {

    @Binds
    fun bindWorkerFactory(factory: GuruKlubWorkerFactory): WorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(GameHeartUpdateWorker::class)
    fun bindGameHeartUpdateWorker(factory: GameHeartUpdateWorker.Factory): ChildWorkerFactory
}