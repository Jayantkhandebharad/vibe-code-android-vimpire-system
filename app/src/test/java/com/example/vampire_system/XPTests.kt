package com.example.vampire_system

import com.example.vampire_system.data.model.Xp
import org.junit.Assert.assertEquals
import org.junit.Test

class XPTests {
    @Test fun ladderBasics() {
        assertEquals(100, Xp.xpForLevel(1))
        assertEquals(51200, Xp.xpForLevel(10))
        assertEquals(100, Xp.xpForLevel(11))
    }
}


