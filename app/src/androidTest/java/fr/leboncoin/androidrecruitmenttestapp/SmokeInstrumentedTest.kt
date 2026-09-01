package fr.leboncoin.androidrecruitmenttestapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmokeInstrumentedTest {

    @Test
    fun usesTheExpectedApplicationId() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("fr.leboncoin.androidrecruitmenttestapp", context.packageName)
    }
}
