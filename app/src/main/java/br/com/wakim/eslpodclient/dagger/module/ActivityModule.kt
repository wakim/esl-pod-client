package br.com.wakim.eslpodclient.dagger.module

import android.app.Activity
import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.view.BaseActivity
import br.com.wakim.eslpodclient.view.PermissionRequester
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private val activity: BaseActivity<*>) {

    @Provides @ActivityScope
    fun providesBaseActivity(): BaseActivity<*> = activity

    @Provides @ActivityScope
    fun providesActivity(): Activity = activity

    @Provides @ActivityScope
    fun providesPermissionRequests(): PermissionRequester = activity
}