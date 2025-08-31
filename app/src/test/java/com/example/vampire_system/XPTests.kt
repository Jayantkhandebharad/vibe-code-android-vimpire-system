package com.example.vampire_system

import com.example.vampire_system.data.model.Xp
import org.junit.Assert.assertEquals
import org.junit.Test

class XPTests {
    @Test fun ladderBasics() {
        // Test first decade progression
        assertEquals(100, Xp.xpForLevel(1))    // Level 1 needs 100 XP
        assertEquals(200, Xp.xpForLevel(2))    // Level 2 needs 200 XP
        assertEquals(400, Xp.xpForLevel(3))    // Level 3 needs 400 XP
        assertEquals(800, Xp.xpForLevel(4))    // Level 4 needs 800 XP
        assertEquals(51200, Xp.xpForLevel(10)) // Level 10 needs 51200 XP
        
        // Test reset at level 11 (second decade)
        assertEquals(100, Xp.xpForLevel(11))   // Level 11 resets to 100 XP
        assertEquals(200, Xp.xpForLevel(12))   // Level 12 needs 200 XP
        assertEquals(51200, Xp.xpForLevel(20)) // Level 20 needs 51200 XP
        
        // Test third decade
        assertEquals(100, Xp.xpForLevel(21))   // Level 21 resets to 100 XP
    }
    
    @Test fun totalXpCalculation() {
        // Level 1 should need 0 total XP to reach (you start at level 1)
        assertEquals(0, Xp.totalXpToReach(1))
        
        // Level 2 should need 100 XP total (level 1 requirement)
        assertEquals(100, Xp.totalXpToReach(2))
        
        // Level 3 should need 300 XP total (100 + 200)
        assertEquals(300, Xp.totalXpToReach(3))
        
        // Level 4 should need 700 XP total (100 + 200 + 400)
        assertEquals(700, Xp.totalXpToReach(4))
    }
}


