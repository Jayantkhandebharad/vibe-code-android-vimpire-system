package com.example.vampire_system

import com.example.vampire_system.data.model.Xp
import org.junit.Test
import org.junit.Assert.*

class XpProgressionTest {
    
    @Test
    fun testXpProgression() {
        // Test the new XP progression system
        println("Testing XP Progression...")
        
        // First decade (levels 1-10)
        assertEquals("Level 1 should need 100 XP", 100, Xp.xpForLevel(1))
        assertEquals("Level 2 should need 200 XP", 200, Xp.xpForLevel(2))
        assertEquals("Level 3 should need 400 XP", 400, Xp.xpForLevel(3))
        assertEquals("Level 4 should need 800 XP", 800, Xp.xpForLevel(4))
        assertEquals("Level 5 should need 1600 XP", 1600, Xp.xpForLevel(5))
        assertEquals("Level 10 should need 51200 XP", 51200, Xp.xpForLevel(10))
        
        // Second decade (levels 11-20) - should reset
        assertEquals("Level 11 should reset to 100 XP", 100, Xp.xpForLevel(11))
        assertEquals("Level 12 should need 200 XP", 200, Xp.xpForLevel(12))
        assertEquals("Level 13 should need 400 XP", 400, Xp.xpForLevel(13))
        assertEquals("Level 20 should need 51200 XP", 51200, Xp.xpForLevel(20))
        
        // Third decade (levels 21-30) - should reset again
        assertEquals("Level 21 should reset to 100 XP", 100, Xp.xpForLevel(21))
        assertEquals("Level 22 should need 200 XP", 200, Xp.xpForLevel(22))
        
        println("All XP progression tests passed!")
    }
    
    @Test
    fun testTotalXpCalculation() {
        // Test cumulative XP calculation
        assertEquals("Level 1 should need 0 total XP", 0, Xp.totalXpToReach(1))
        assertEquals("Level 2 should need 100 total XP", 100, Xp.totalXpToReach(2))
        assertEquals("Level 3 should need 300 total XP", 300, Xp.totalXpToReach(3))
        assertEquals("Level 4 should need 700 total XP", 700, Xp.totalXpToReach(4))
        assertEquals("Level 5 should need 1500 total XP", 1500, Xp.totalXpToReach(5))
        
        println("All total XP calculation tests passed!")
    }
    
    @Test
    fun testDecadeReset() {
        // Test that decades reset properly
        val level1Xp = Xp.xpForLevel(1)
        val level11Xp = Xp.xpForLevel(11)
        val level21Xp = Xp.xpForLevel(21)
        
        assertEquals("Level 1 and 11 should have same XP requirement", level1Xp, level11Xp)
        assertEquals("Level 1 and 21 should have same XP requirement", level1Xp, level21Xp)
        assertEquals("Level 11 and 21 should have same XP requirement", level11Xp, level21Xp)
        
        println("Decade reset tests passed!")
    }
}
