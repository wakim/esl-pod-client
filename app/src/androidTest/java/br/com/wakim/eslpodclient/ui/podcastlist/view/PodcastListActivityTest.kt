package br.com.wakim.eslpodclient.ui.podcastlist.view

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
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
        onView(withId(R.id.pb_loading))
                .check(matches(isDisplayed()))
    }
}