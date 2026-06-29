package com.abhijit.footlog.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CalorieUtilTest {

    @Test
    fun estimateCalories_walk_returnsCorrectValue() {
        val calories = estimateCalories(5000f, "walk")
        assertEquals(300, calories)
    }

    @Test
    fun estimateCalories_run_returnsCorrectValue() {
        val calories = estimateCalories(10000f, "run")
        assertEquals(800, calories)
    }

    @Test
    fun estimateCalories_cycle_returnsCorrectValue() {
        val calories = estimateCalories(20000f, "cycle")
        assertEquals(800, calories)
    }

    @Test
    fun estimateCalories_zeroDistance_returnsZero() {
        assertEquals(0, estimateCalories(0f, "walk"))
        assertEquals(0, estimateCalories(0f, "run"))
        assertEquals(0, estimateCalories(0f, "cycle"))
    }

    @Test
    fun estimateCalories_caseInsensitive() {
        val upper = estimateCalories(1000f, "RUN")
        val lower = estimateCalories(1000f, "run")
        assertEquals(lower, upper)
    }

    @Test
    fun estimateCalories_unknownType_defaultsToWalk() {
        val calories = estimateCalories(5000f, "swim")
        assertEquals(300, calories)
    }
}
