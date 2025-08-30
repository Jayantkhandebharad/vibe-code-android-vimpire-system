package com.example.vampire_system
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.click

@RunWith(AndroidJUnit4::class)
class HomeFlowTest {
    @Rule @JvmField
    val rule = ActivityTestRule(MainActivity::class.java)

    @Test fun open_tabs() {
        onView(withId(R.id.navigation_home)).perform(click())
        onView(withId(R.id.navigation_dashboard)).perform(click())
        onView(withId(R.id.navigation_notifications)).perform(click())
    }
}
