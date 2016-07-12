package br.com.wakim.eslpodclient.dagger.module

import android.app.Activity
import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.view.BaseActivity
import br.com.wakim.eslpodclient.view.PermissionRequester
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private val presenterActivity: BaseActivity) {

    @Provides @ActivityScope
    fun providesBaseActivity() = presenterActivity

    @Provides @ActivityScope
    fun providesActivity(): Activity = presenterActivity

    @Provides @ActivityScope
    fun providesPermissionRequests(): PermissionRequester = presenterActivity

    @Provides @ActivityScope
    fun providesFirebaseAnalytics() = FirebaseAnalytics.getInstance(presenterActivity)
}