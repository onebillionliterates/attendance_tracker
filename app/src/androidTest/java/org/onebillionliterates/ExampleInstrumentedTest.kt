package org.onebillionliterates

import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry

import org.junit.Test

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@MediumTest
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("org.onebillionliterates", appContext.packageName)
    }
}
