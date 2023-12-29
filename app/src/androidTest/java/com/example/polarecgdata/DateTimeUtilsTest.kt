package com.example.polarecgdata

import com.example.polarecgdata.utils.getCurrentLocalDateTimeWithMillis
import org.junit.Assert
import org.junit.Test

class DateTimeUtilsTest {

    @Test
    fun testGetCurrentLocalDateTimeWithMillis() {
        val result = getCurrentLocalDateTimeWithMillis()

        // Assuming the format is "yyyy-MM-dd HH:mm:ss.SSS"
        val regex = Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}")

        // Assert that the result matches the expected format
        Assert.assertTrue("Result doesn't match the expected format", regex.matches(result))
    }
}
