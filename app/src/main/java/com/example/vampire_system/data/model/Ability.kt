package com.example.vampire_system.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Ability(
    val id: String,                 // stable key, e.g., "pushups", "reading"
    val name: String,
    val group: AbilityGroup,
    val unit: String?,              // "rep", "page", "min", "block", "day", or null
    val xpRule: XpRule,
    val dailyCapXp: Int? = null,    // e.g., 40 for reading
    val seriousness: Seriousness? = null,
    val evidenceKinds: Set<EvidenceKind> = emptySet(),
    val unlockLevel: Int            // first level where it can be used (after Core Gate)
)

@Serializable
enum class AbilityGroup { FOUNDATIONS, FITNESS, GENAI, NUTRITION, COMMUNICATION }

@Serializable
sealed class XpRule {
    @Serializable data class PerUnit(val xpPerUnit: Double): XpRule()      // e.g., 0.25 XP per rep
    @Serializable data class PerMinutes(val xpPer10Min: Int): XpRule()     // e.g., 5 XP per 10 min
    @Serializable data class Flat(val xp: Int): XpRule()                   // e.g., 8 XP for a day target
}

@Serializable
data class Seriousness(
    val minMinutes: Int? = null,
    val minRpe: Int? = null,
    val hrZone2Minutes: Int? = null,
    val requiresLog: Boolean = false,
    val structured: Boolean = false,              // for drills/matches
    val scoreOrDrillLog: Boolean = false,         // for badminton
    val focusNoteRequired: Boolean = false        // for swim drills
)

@Serializable
enum class EvidenceKind { 
    NOTE, PHOTO, VIDEO, AUDIO, TIMER, LINK, FILE, CHECKLIST,
    COUNTER, SCREENSHOT, LOG, METRICS, REPO, DEMO, TRANSCRIPT,
    RUBRIC, MANIFEST, TABLE, CHART, DASHBOARD, POLICY, TESTS,
    VALIDATION, COMPARISON, ANALYSIS, REPORT, PLAN, ROADMAP,
    // Additional evidence types for comprehensive abilities
    CONFIG, ENDPOINT, MODEL, EXAMPLES, RECIPE, SHEET, GRID,
    DATASET, REVIEW, BENCHMARK, CASES, ADR, FEEDBACK, RECORDING,
    CHANGELOG, PLAYBOOK, RECEIPT, DRAFT, LINKS, ACKNOWLEDGMENT,
    TAXONOMY, DIAGRAM, CODE, TEST, DIFF, ABLATION, SCRIPT, 
    CONFIG_FLAGS, THROUGHPUT_CSV, LEAKAGE_CHECK, PREFERENCE_SCHEMA, 
    WIN_RATE_TABLE, SESSION_LOGS, DATASET_STATS, SAMPLE_ROWS,
    TRAIN_LOG, ADAPTER_WEIGHTS, LOSS_CURVE, RESULTS_JSON, COMPARISON_TABLE, TAKEAWAYS,
    PR_LINK, DIFF_SUMMARY, REPO_LINK, EXAMPLE, PAGE_RANGE, TAKEAWAY, WORD_COUNT, SNIPPET,
    AUDIO_NOTE, FIXES, IMPROVEMENT, COUNTER_NOTE, NOTEBOOK, README, RESULTS,
    // Additional missing evidence types
    RUNBOOK, EVAL, GRAPH, REVIEWS, STATS, GUIDELINES, DECISION, RESULT, PDF, POST, PROOF
}


