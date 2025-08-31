package com.example.vampire_system

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.click
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeFlowTest {
    
    @Test 
    fun open_tabs() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.navigation_home)).perform(click())
            onView(withId(R.id.navigation_dashboard)).perform(click())
            onView(withId(R.id.navigation_notifications)).perform(click())
        }
    }
}
