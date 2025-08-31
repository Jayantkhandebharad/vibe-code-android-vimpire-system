package com.example.vampire_system.data.model

object Xp {
    // Doubling ladder inside each decade: 100, 200, 400, 800, then reset every 10 levels
    val DECADE_SEQUENCE = intArrayOf(100, 200, 400, 800, 1600, 3200, 6400, 12800, 25600, 51200)

    fun xpForLevel(level: Int): Int {
        // Calculate position within the current decade (0-9)
        val positionInDecade = (level - 1) % 10
        
        // Doubling progression that resets every 10 levels
        // More balanced: 100, 200, 400, 800, 1600, 3200, but cap at reasonable levels
        return when (positionInDecade) {
            0 -> 100    // Levels 1, 11, 21, 31, etc.
            1 -> 200    // Levels 2, 12, 22, 32, etc.
            2 -> 400    // Levels 3, 13, 23, 33, etc.
            3 -> 800    // Levels 4, 14, 24, 34, etc.
            4 -> 1600   // Levels 5, 15, 25, 35, etc.
            5 -> 3200   // Levels 6, 16, 26, 36, etc.
            6 -> 6400   // Levels 7, 17, 27, 37, etc.
            7 -> 12800  // Levels 8, 18, 28, 38, etc.
            8 -> 25600  // Levels 9, 19, 29, 39, etc.
            9 -> 51200  // Levels 10, 20, 30, 40, etc. (boss levels)
            else -> 100 // Fallback
        }
    }

    val FOUNDATIONS: List<String> = listOf(
        "pushups", "reading", "notes", "meditation", "mobility"
    )

    fun totalXpToReach(level: Int): Int {
        var sum = 0
        for (l in 1 until level) sum += xpForLevel(l)
        return sum
    }
    
    // DEBUG: Test function to verify XP progression
    fun debugXpProgression() {
        println("=== XP PROGRESSION DEBUG ===")
        for (level in 1..15) {
            val xpNeeded = xpForLevel(level)
            val cumulative = totalXpToReach(level)
            println("Level $level: $xpNeeded XP needed, $cumulative cumulative")
        }
        println("=== END DEBUG ===")
    }
}


