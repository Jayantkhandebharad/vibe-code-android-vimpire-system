package com.example.vampire_system.data.model

object Xp {
    // Doubling ladder inside each decade
    val DECADE_SEQUENCE = intArrayOf(100, 200, 400, 800, 1600, 3200, 6400, 12800, 25600, 51200)

    fun xpForLevel(level: Int): Int = DECADE_SEQUENCE[(level - 1) % 10]

    val FOUNDATIONS: List<String> = listOf(
        "pushups", "reading", "notes", "meditation", "mobility"
    )

    fun totalXpToReach(level: Int): Int {
        var sum = 0
        for (l in 1 until level) sum += xpForLevel(l)
        return sum
    }
}


