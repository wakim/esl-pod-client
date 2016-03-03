package br.com.wakim.eslpodclient.podcastlist.view

import android.support.test.espresso.Espresso.*
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import br.com.wakim.eslpodclient.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PodcastListActivityTest() {

    @get:Rule
    val activityRule = ActivityTestRule<PodcastListActivity>(PodcastListActivity::class.java)

    @Test
    fun test() {
        onView(withId(R.id.recycler_view))
    }
}