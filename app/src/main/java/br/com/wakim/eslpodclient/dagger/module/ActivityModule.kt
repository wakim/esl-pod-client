package br.com.wakim.eslpodclient.dagger.module

import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.view.BaseActivity
import br.com.wakim.eslpodclient.view.PermissionRequester
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private val activity: BaseActivity<*>) {

    @Provides @ActivityScope
    fun providesActivity() : BaseActivity<*> = activity

    @Provides @ActivityScope
    fun providesPermissionRequests() : PermissionRequester = activity
}