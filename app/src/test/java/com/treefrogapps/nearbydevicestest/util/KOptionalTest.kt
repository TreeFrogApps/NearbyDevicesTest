package com.treefrogapps.nearbydevicestest.util

import org.junit.Assert.assertEquals
import org.junit.Test

class KOptionalTest {

    @Test
    fun name() {
        val input = 5
        val expected = "5"

        val result = KMap(input).map { it.toString() }.get()

        assertEquals(expected, result)
    }
}