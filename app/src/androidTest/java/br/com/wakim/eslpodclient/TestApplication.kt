package br.com.wakim.eslpodclient

import br.com.wakim.eslpodclient.dagger.DaggerAppComponent
import br.com.wakim.eslpodclient.dagger.module.AppModule
import br.com.wakim.eslpodclient.dagger.module.TestInteractorModule

open class TestApplication : Application() {

    override fun createComponent() {
        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).interactorModule(TestInteractorModule()).build()
    }
}