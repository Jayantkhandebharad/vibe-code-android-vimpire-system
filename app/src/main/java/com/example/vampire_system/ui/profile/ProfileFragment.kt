package com.example.vampire_system.ui.profile

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.LedgerType
import com.example.vampire_system.data.model.AbilityGroup
import com.example.vampire_system.data.model.TaskCategory
import kotlinx.coroutines.*
import com.example.vampire_system.ui.profile.SimpleRadarChart

class ProfileFragment : Fragment() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvW = view.findViewById<TextView>(R.id.tvWeight)
        val tvP = view.findViewById<TextView>(R.id.tvProtein)
        val tvTz = view.findViewById<TextView>(R.id.tvTz)
        val tvReset = view.findViewById<TextView>(R.id.tvReset)
        val meterS = view.findViewById<ProgressBar>(R.id.meterStrength)
        val meterK = view.findViewById<ProgressBar>(R.id.meterKnowledge)
        val meterC = view.findViewById<ProgressBar>(R.id.meterComm)
        val meterSocial = view.findViewById<ProgressBar>(R.id.meterSocial)
        val meterLifestyle = view.findViewById<ProgressBar>(R.id.meterLifestyle)
        val tvPen = view.findViewById<TextView>(R.id.tvPenalties)
        
        // All-Time XP totals for main categories
        val tvStrengthTotal = view.findViewById<TextView>(R.id.tvStrengthTotal)
        val tvKnowledgeTotal = view.findViewById<TextView>(R.id.tvKnowledgeTotal)
        val tvCommunicationTotal = view.findViewById<TextView>(R.id.tvCommunicationTotal)
        val tvSocialTotal = view.findViewById<TextView>(R.id.tvSocialTotal)
        val tvLifestyleTotal = view.findViewById<TextView>(R.id.tvLifestyleTotal)
        val tvMiscellaneousTotal = view.findViewById<TextView>(R.id.tvMiscellaneousTotal)
        
        // XP Contribution meters
        val meterAwards = view.findViewById<ProgressBar>(R.id.meterAwards)
        val meterBonus = view.findViewById<ProgressBar>(R.id.meterBonus)
        val meterPenalty = view.findViewById<ProgressBar>(R.id.meterPenalty)
        val meterAdjustment = view.findViewById<ProgressBar>(R.id.meterAdjustment)
        val meterLevelUp = view.findViewById<ProgressBar>(R.id.meterLevelUp)
        
        // XP Contribution totals
        val tvAwardsTotal = view.findViewById<TextView>(R.id.tvAwardsTotal)
        val tvBonusTotal = view.findViewById<TextView>(R.id.tvBonusTotal)
        val tvPenaltyTotal = view.findViewById<TextView>(R.id.tvPenaltyTotal)
        val tvAdjustmentTotal = view.findViewById<TextView>(R.id.tvAdjustmentTotal)
        val tvLevelUpTotal = view.findViewById<TextView>(R.id.tvLevelUpTotal)
        val tvTotalXP = view.findViewById<TextView>(R.id.tvTotalXP)
        
        // All-Time XP radar chart
        val radarChartAllTime = view.findViewById<SimpleRadarChart>(R.id.radarChartAllTime)
        val tvAllTimeTotal = view.findViewById<TextView>(R.id.tvAllTimeTotal)
        
        // Hide radar chart initially in case of any issues
        radarChartAllTime?.visibility = android.view.View.GONE

        scope.launch {
            try {
                val db = AppDatabase.get(requireContext())
                val prof = db.profileDao().get()
                val set = db.settingsDao().get()
                withContext(Dispatchers.Main) {
                    tvW.text = "Weight: ${prof?.weightKg ?: "-"} kg"
                    val proteinTarget =
                        prof?.weightKg?.let { w -> (w * (prof.proteinGPerKg)) }?.toInt()
                    tvP.text = "Protein target: ${proteinTarget ?: "-"} g"
                    tvTz.text = "Timezone: ${set?.timezone ?: "Asia/Kolkata"}"
                    tvReset.text = "Reset hour: ${set?.resetHour ?: 5}:00"
                }

                val today = java.time.LocalDate.now()
                val from = today.minusDays(6).toString()
                val to = today.toString()
                val ledger = db.xpLedgerDao().range(from, to)
                val abilities = db.abilityDao().getAllOnce()
                val groupByAbilityId = abilities.associate { it.id to it.group }

                // Get all tasks to categorize their XP for weekly meters
                val allTasks = db.levelTaskDao().getAllOnce()
                val taskCategoryMap = allTasks.associate { it.id to it.category }

                var fit = 0;
                var gen = 0;
                var comm = 0;
                var social = 0;
                var lifestyle = 0
                var penCount = 0;
                var penSum = 0
                ledger.forEach { e ->
                    when (e.type) {
                        LedgerType.AWARD -> {
                            val g = e.abilityId?.let { groupByAbilityId[it] }
                            when (g) {
                                AbilityGroup.FOUNDATIONS -> {
                                    // Map representative foundations to meters: pushups->Strength, reading->Knowledge, notes->Communication
                                    when (e.abilityId) {
                                        "pushups" -> fit += e.deltaXp
                                        "reading" -> gen += e.deltaXp
                                        "notes" -> comm += e.deltaXp
                                        "meditation" -> lifestyle += e.deltaXp
                                        "planning" -> lifestyle += e.deltaXp
                                        "learning" -> gen += e.deltaXp
                                        "hydration" -> lifestyle += e.deltaXp
                                        "shipping_tool" -> gen += e.deltaXp
                                        else -> {}
                                    }
                                }

                                AbilityGroup.FITNESS -> fit += e.deltaXp
                                AbilityGroup.GENAI -> gen += e.deltaXp
                                AbilityGroup.COMMUNICATION -> comm += e.deltaXp
                                AbilityGroup.NUTRITION -> lifestyle += e.deltaXp
                                else -> {
                                    // Map other abilities to appropriate categories
                                    when (e.abilityId) {
                                        "social_activity", "networking", "teamwork", "community_help", "open_source_contribution", "public_demo", "conference_abstracts" -> social += e.deltaXp
                                        "meditation", "sleep", "hobby", "breathwork_drills", "body_composition", "recovery_logging", "recovery_intensified" -> lifestyle += e.deltaXp
                                else -> {}
                            }
                        }
                            }

                            // Also categorize XP from quest instances that are linked to tasks
                            if (e.questInstanceId != null) {
                                val questInstance = db.questInstanceDao().byId(e.questInstanceId)
                                if (questInstance != null && questInstance.templateId != null) {
                                    // For level tasks, templateId should match the task ID
                                    val task = allTasks.find { task ->
                                        task.id == questInstance.templateId
                                    }
                                    if (task != null) {
                                        when (task.category) {
                                            TaskCategory.STRENGTH -> fit += e.deltaXp
                                            TaskCategory.KNOWLEDGE -> gen += e.deltaXp
                                            TaskCategory.COMMUNICATION -> comm += e.deltaXp
                                            TaskCategory.SOCIAL -> social += e.deltaXp
                                            TaskCategory.LIFESTYLE -> lifestyle += e.deltaXp
                                        }
                                    }
                                }
                            }

                            // Additional specific ability mappings for better categorization
                            when (e.abilityId) {
                                // Social activities
                                "speaking_drills", "llm_dialogue", "code_review_serious", "presentation_micro",
                                "interview_mock", "code_review_culture" -> comm += e.deltaXp

                                // Knowledge/GenAI activities  
                                "coding_focus", "data_curation", "qlora_sft", "eval_harness", "quantization",
                                "data_pipeline", "inference_eng", "alignment_101", "observability", "production_finetune",
                                "tokenizer_lab", "multi_gpu", "curriculum_design", "tiny_model", "mini_rag",
                                "scheduling_lr", "trace_observability", "data_mix_governance", "cost_modeling",
                                "automated_pipeline", "kv_cache_bench", "prefill_decode", "scheduler_throttling",
                                "cost_model_v1", "distributed_eval", "containerized_trainer", "spot_resilience",
                                "checkpoint_hygiene", "cluster_autoscale", "safety_taxonomy", "prompt_injection",
                                "tool_calling_safety", "pii_detection", "evals_baseline", "moe_finetune",
                                "distillation", "latency_cost_tuning", "multi_tenant_mvp", "long_context_strategy",
                                "retrieval_quality", "flash_attention", "long_context_evals", "guardrails_training",
                                "evaluator_refactor", "observability_slos", "synthetic_data_v1", "knowledge_distillation_v2",
                                "speculative_decoding", "long_context_rag", "hardware_benchmarking", "rlaif_pipeline",
                                "etl_lineage", "production_rag", "llm_memory", "dataset_labeling", "advanced_evals",
                                "architecture_diagramming", "ab_experiments", "cost_latency_tuning" -> gen += e.deltaXp

                                // Strength/Fitness activities
                                "walk_run", "mobility", "gym_serious", "swimming_serious", "continuous_5k",
                                "continuous_10k", "strength_progression", "hills_trails", "strength_split",
                                "recovery_intensified", "threshold_run", "strength_prs", "speed_work" -> fit += e.deltaXp

                                // Lifestyle activities
                                "protein_target", "hydration_day", "fiber_target", "meal_prep_weekly", "cooking_home",
                                "macro_nutrition" -> lifestyle += e.deltaXp

                                else -> {} // Already handled by group mapping above
                            }
                        }

                        LedgerType.PENALTY -> {
                            penCount += 1; penSum += -e.deltaXp
                        }

                        else -> {}
                    }
                }
                withContext(Dispatchers.Main) {
                    meterS.progress = fit.coerceAtMost(meterS.max)
                    meterK.progress = gen.coerceAtMost(meterK.max)
                    meterC.progress = comm.coerceAtMost(meterC.max)
                    meterSocial.progress = social.coerceAtMost(meterSocial.max)
                    meterLifestyle.progress = lifestyle.coerceAtMost(meterLifestyle.max)
                    tvPen.text = "Penalties (7d): ${penCount} (−${penSum} XP)"
                }
                
                // Get all-time ledger data for both calculations
                val allLedger = db.xpLedgerDao().getAllOnce()
                android.util.Log.d("ProfileFragment", "All-time ledger entries: ${allLedger.size}")
                
                try {
                // Calculate all-time XP totals for main categories
                var strengthTotal = 0
                var knowledgeTotal = 0
                var communicationTotal = 0
                var socialTotal = 0
                var lifestyleTotal = 0
                    var miscellaneousTotal = 0
                
                    android.util.Log.d("ProfileFragment", "Starting all-time XP calculation...")
                allLedger.forEach { entry ->
                        if (entry.type == LedgerType.AWARD) {
                            // Track if this entry was categorized
                            var entryCategorized = false

                            if (entry.abilityId != null) {
                                android.util.Log.d(
                                    "ProfileFragment",
                                    "Processing AWARD entry: abilityId=${entry.abilityId}, deltaXp=${entry.deltaXp}, questInstanceId=${entry.questInstanceId}"
                                )
                        val group = entry.abilityId?.let { groupByAbilityId[it] }
                                android.util.Log.d(
                                    "ProfileFragment",
                                    "Ability group for ${entry.abilityId}: $group"
                                )
                        when (group) {
                            AbilityGroup.FOUNDATIONS -> {
                                        android.util.Log.d(
                                            "ProfileFragment",
                                            "Processing FOUNDATIONS ability: ${entry.abilityId}"
                                        )
                                // Map representative foundations to categories
                                when (entry.abilityId) {
                                            "pushups" -> {
                                                strengthTotal += entry.deltaXp; entryCategorized =
                                                    true; android.util.Log.d(
                                                    "ProfileFragment",
                                                    "Added ${entry.deltaXp} XP to Strength (pushups)"
                                                )
                                            }

                                            "reading" -> {
                                                knowledgeTotal += entry.deltaXp; entryCategorized =
                                                    true; android.util.Log.d(
                                                    "ProfileFragment",
                                                    "Added ${entry.deltaXp} XP to Knowledge (reading)"
                                                )
                                            }

                                            "notes" -> {
                                                communicationTotal += entry.deltaXp; entryCategorized =
                                                    true; android.util.Log.d(
                                                    "ProfileFragment",
                                                    "Added ${entry.deltaXp} XP to Communication (notes)"
                                                )
                                            }

                                            "meditation" -> {
                                                lifestyleTotal += entry.deltaXp; entryCategorized =
                                                    true; android.util.Log.d(
                                                    "ProfileFragment",
                                                    "Added ${entry.deltaXp} XP to Lifestyle (meditation)"
                                                )
                                            }

                                            "planning" -> {
                                                lifestyleTotal += entry.deltaXp; entryCategorized =
                                                    true; android.util.Log.d(
                                                    "ProfileFragment",
                                                    "Added ${entry.deltaXp} XP to Lifestyle (planning)"
                                                )
                                            }

                                            "learning" -> {
                                                knowledgeTotal += entry.deltaXp; entryCategorized =
                                                    true; android.util.Log.d(
                                                    "ProfileFragment",
                                                    "Added ${entry.deltaXp} XP to Knowledge (learning)"
                                                )
                                            }

                                            "hydration" -> {
                                                lifestyleTotal += entry.deltaXp; entryCategorized =
                                                    true; android.util.Log.d(
                                                    "ProfileFragment",
                                                    "Added ${entry.deltaXp} XP to Lifestyle (hydration)"
                                                )
                                            }

                                            "shipping_tool" -> {
                                                knowledgeTotal += entry.deltaXp; entryCategorized =
                                                    true; android.util.Log.d(
                                                    "ProfileFragment",
                                                    "Added ${entry.deltaXp} XP to Knowledge (shipping_tool)"
                                                )
                                            }

                                            else -> {
                                                android.util.Log.d(
                                                    "ProfileFragment",
                                                    "No mapping found for FOUNDATIONS ability: ${entry.abilityId}"
                                                )
                                            }
                                        }
                                    }

                                    AbilityGroup.FITNESS -> {
                                        strengthTotal += entry.deltaXp; entryCategorized =
                                            true; android.util.Log.d(
                                            "ProfileFragment",
                                            "Added ${entry.deltaXp} XP to Strength (FITNESS group)"
                                        )
                                    }

                                    AbilityGroup.GENAI -> {
                                        knowledgeTotal += entry.deltaXp; entryCategorized =
                                            true; android.util.Log.d(
                                            "ProfileFragment",
                                            "Added ${entry.deltaXp} XP to Knowledge (GENAI group)"
                                        )
                                    }

                                    AbilityGroup.COMMUNICATION -> {
                                        communicationTotal += entry.deltaXp; entryCategorized =
                                            true; android.util.Log.d(
                                            "ProfileFragment",
                                            "Added ${entry.deltaXp} XP to Communication (COMMUNICATION group)"
                                        )
                                    }

                                    AbilityGroup.NUTRITION -> {
                                        lifestyleTotal += entry.deltaXp; entryCategorized =
                                            true; android.util.Log.d(
                                            "ProfileFragment",
                                            "Added ${entry.deltaXp} XP to Lifestyle (NUTRITION group)"
                                        )
                                    }

                                    else -> {
                                        android.util.Log.d(
                                            "ProfileFragment",
                                            "Processing ability not in main groups: ${entry.abilityId}"
                                        )
                                        // Map other abilities to appropriate categories
                                        when (entry.abilityId) {
                                            "social_activity", "networking", "teamwork", "community_help", "open_source_contribution", "public_demo", "conference_abstracts" -> {
                                                socialTotal += entry.deltaXp; entryCategorized =
                                                    true; android.util.Log.d(
                                                    "ProfileFragment",
                                                    "Added ${entry.deltaXp} XP to Social (${entry.abilityId})"
                                                )
                                            }

                                            "meditation", "sleep", "hobby", "breathwork_drills", "body_composition", "recovery_logging", "recovery_intensified" -> {
                                                lifestyleTotal += entry.deltaXp; entryCategorized =
                                                    true; android.util.Log.d(
                                                    "ProfileFragment",
                                                    "Added ${entry.deltaXp} XP to Lifestyle (${entry.abilityId})"
                                                )
                                            }

                            else -> {
                                                // Additional specific ability mappings for abilities not in main groups
                                when (entry.abilityId) {
                                                    // Social activities
                                                    "speaking_drills", "llm_dialogue", "code_review_serious", "presentation_micro",
                                                    "interview_mock", "code_review_culture" -> {
                                                        communicationTotal += entry.deltaXp; entryCategorized =
                                                            true; android.util.Log.d(
                                                            "ProfileFragment",
                                                            "Added ${entry.deltaXp} XP to Communication (${entry.abilityId})"
                                                        )
                                                    }

                                                    // Knowledge/GenAI activities
                                                    "coding_focus", "data_curation", "qlora_sft", "eval_harness", "quantization",
                                                    "data_pipeline", "inference_eng", "alignment_101", "observability", "production_finetune",
                                                    "tokenizer_lab", "multi_gpu", "curriculum_design", "tiny_model", "mini_rag",
                                                    "scheduling_lr", "trace_observability", "data_mix_governance", "cost_modeling",
                                                    "automated_pipeline", "kv_cache_bench", "prefill_decode", "scheduler_throttling",
                                                    "cost_model_v1", "distributed_eval", "containerized_trainer", "spot_resilience",
                                                    "checkpoint_hygiene", "cluster_autoscale", "safety_taxonomy", "prompt_injection",
                                                    "tool_calling_safety", "pii_detection", "evals_baseline", "moe_finetune",
                                                    "distillation", "latency_cost_tuning", "multi_tenant_mvp", "long_context_strategy",
                                                    "retrieval_quality", "flash_attention", "long_context_evals", "guardrails_training",
                                                    "evaluator_refactor", "observability_slos", "synthetic_data_v1", "knowledge_distillation_v2",
                                                    "speculative_decoding", "long_context_rag", "hardware_benchmarking", "rlaif_pipeline",
                                                    "etl_lineage", "production_rag", "llm_memory", "dataset_labeling", "advanced_evals",
                                                    "architecture_diagramming", "ab_experiments", "cost_latency_tuning" -> {
                                                        knowledgeTotal += entry.deltaXp; entryCategorized =
                                                            true; android.util.Log.d(
                                                            "ProfileFragment",
                                                            "Added ${entry.deltaXp} XP to Knowledge (${entry.abilityId})"
                                                        )
                                                    }

                                                    // Strength/Fitness activities
                                                    "walk_run", "mobility", "gym_serious", "swimming_serious", "continuous_5k",
                                                    "continuous_10k", "strength_progression", "hills_trails", "strength_split",
                                                    "recovery_intensified", "threshold_run", "strength_prs", "speed_work" -> {
                                                        strengthTotal += entry.deltaXp; entryCategorized =
                                                            true; android.util.Log.d(
                                                            "ProfileFragment",
                                                            "Added ${entry.deltaXp} XP to Strength (${entry.abilityId})"
                                                        )
                                                    }

                                                    // Lifestyle activities
                                                    "protein_target", "hydration_day", "fiber_target", "meal_prep_weekly", "cooking_home",
                                                    "macro_nutrition" -> {
                                                        lifestyleTotal += entry.deltaXp; entryCategorized =
                                                            true; android.util.Log.d(
                                                            "ProfileFragment",
                                                            "Added ${entry.deltaXp} XP to Lifestyle (${entry.abilityId})"
                                                        )
                                                    }

                                                    else -> {
                                                        android.util.Log.d(
                                                            "ProfileFragment",
                                                            "No mapping found for ability: ${entry.abilityId}"
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }


                    }
                    
                    // Also categorize XP from quest instances that are linked to tasks
                    if (entry.type == LedgerType.AWARD && entry.questInstanceId != null) {
                                val questInstance =
                                    db.questInstanceDao().byId(entry.questInstanceId)
                        if (questInstance != null && questInstance.templateId != null) {
                            // For level tasks, templateId should match the task ID
                            val task = allTasks.find { task -> 
                                task.id == questInstance.templateId
                            }
                            if (task != null) {
                                        android.util.Log.d(
                                            "ProfileFragment",
                                            "Found task ${task.id} with category ${task.category} for quest ${questInstance.id}"
                                        )
                                when (task.category) {
                                            TaskCategory.STRENGTH -> {
                                                strengthTotal += entry.deltaXp; entryCategorized =
                                                    true
                                            }

                                            TaskCategory.KNOWLEDGE -> {
                                                knowledgeTotal += entry.deltaXp; entryCategorized =
                                                    true
                                            }

                                            TaskCategory.COMMUNICATION -> {
                                                communicationTotal += entry.deltaXp; entryCategorized =
                                                    true
                                            }

                                            TaskCategory.SOCIAL -> {
                                                socialTotal += entry.deltaXp; entryCategorized =
                                                    true
                                            }

                                            TaskCategory.LIFESTYLE -> {
                                                lifestyleTotal += entry.deltaXp; entryCategorized =
                                                    true
                                            }
                                }
                            } else {
                                        android.util.Log.w(
                                            "ProfileFragment",
                                            "No task found for templateId ${questInstance.templateId} in quest ${questInstance.id}"
                                        )
                                    }
                                }
                            }

                            // Add uncategorized XP to Miscellaneous
                            if (entry.type == LedgerType.AWARD && !entryCategorized) {
                                miscellaneousTotal += entry.deltaXp
                                android.util.Log.d(
                                    "ProfileFragment",
                                    "Added ${entry.deltaXp} XP to Miscellaneous (uncategorized: abilityId=${entry.abilityId}, questInstanceId=${entry.questInstanceId})"
                                )
                            }
                        }

                        android.util.Log.d(
                            "ProfileFragment",
                            "=== ALL-TIME XP CALCULATION SUMMARY ==="
                        )
                        android.util.Log.d("ProfileFragment", "Strength Total: $strengthTotal XP")
                        android.util.Log.d("ProfileFragment", "Knowledge Total: $knowledgeTotal XP")
                        android.util.Log.d(
                            "ProfileFragment",
                            "Communication Total: $communicationTotal XP"
                        )
                        android.util.Log.d("ProfileFragment", "Social Total: $socialTotal XP")
                        android.util.Log.d("ProfileFragment", "Lifestyle Total: $lifestyleTotal XP")
                        android.util.Log.d(
                            "ProfileFragment",
                            "Miscellaneous Total: $miscellaneousTotal XP"
                        )
                        val allTimeTotal =
                            strengthTotal + knowledgeTotal + communicationTotal + socialTotal + lifestyleTotal + miscellaneousTotal
                        android.util.Log.d("ProfileFragment", "All-Time Total: $allTimeTotal XP")
                        android.util.Log.d(
                            "ProfileFragment",
                            "=========================================="
                        )
                
                withContext(Dispatchers.Main) {
                    tvStrengthTotal.text = "${strengthTotal} XP"
                    tvKnowledgeTotal.text = "${knowledgeTotal} XP"
                    tvCommunicationTotal.text = "${communicationTotal} XP"
                    tvSocialTotal.text = "${socialTotal} XP"
                    tvLifestyleTotal.text = "${lifestyleTotal} XP"
                            tvMiscellaneousTotal.text = "${miscellaneousTotal} XP"
                    
                    // Update all-time total
                    tvAllTimeTotal.text = "Total: ${allTimeTotal} XP"
                    
                                            // Setup and populate all-time XP radar chart
                                                    setupAllTimeRadarChart(
                            radarChartAllTime,
                                strengthTotal,
                                knowledgeTotal,
                                communicationTotal,
                                socialTotal,
                                lifestyleTotal,
                                miscellaneousTotal
                            )
                    
                    // Show the radar chart if it was successfully configured
                    radarChartAllTime?.visibility = android.view.View.VISIBLE
                }
                
                // Calculate total XP contributions by category (all time)
                var awardsTotal = 0
                var bonusTotal = 0
                var penaltyTotal = 0
                var adjustmentTotal = 0
                var levelUpTotal = 0
                
                allLedger.forEach { entry ->
                    when (entry.type) {
                        LedgerType.AWARD -> awardsTotal += entry.deltaXp
                        LedgerType.BONUS -> bonusTotal += entry.deltaXp
                        LedgerType.PENALTY -> penaltyTotal += entry.deltaXp
                        LedgerType.ADJUSTMENT -> adjustmentTotal += entry.deltaXp
                        LedgerType.LEVEL_UP -> levelUpTotal += entry.deltaXp
                    }
                }
                
                        val totalXP =
                            awardsTotal + bonusTotal + penaltyTotal + adjustmentTotal + levelUpTotal
                        val maxXP = maxOf(
                            awardsTotal,
                            bonusTotal,
                            penaltyTotal,
                            adjustmentTotal,
                            levelUpTotal
                        ).coerceAtLeast(1)
                
                withContext(Dispatchers.Main) {
                    // Update progress bars (normalized to max value)
                    meterAwards.progress = ((awardsTotal.toFloat() / maxXP) * 100).toInt()
                    meterBonus.progress = ((bonusTotal.toFloat() / maxXP) * 100).toInt()
                    meterPenalty.progress = ((penaltyTotal.toFloat() / maxXP) * 100).toInt()
                            meterAdjustment.progress =
                                ((adjustmentTotal.toFloat() / maxXP) * 100).toInt()
                    meterLevelUp.progress = ((levelUpTotal.toFloat() / maxXP) * 100).toInt()
                    
                    // Update total displays
                    tvAwardsTotal.text = "${awardsTotal} XP"
                    tvBonusTotal.text = "${bonusTotal} XP"
                    tvPenaltyTotal.text = "${penaltyTotal} XP"
                    tvAdjustmentTotal.text = "${adjustmentTotal} XP"
                    tvLevelUpTotal.text = "${levelUpTotal} XP"
                    tvTotalXP.text = "${totalXP} XP"
                        }
                }
            } catch (t: Throwable) {
                    android.util.Log.e(
                        "ProfileFragment",
                        "Error in all-time XP calculation: ${t.message}",
                        t
                    )
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Profile failed: ${t.message}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (ttx: Throwable) {
                android.util.Log.e(
                    "ProfileFragment",
                    "Error in all-time XP calculation: ${ttx.message}",
                    ttx
                )
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Profile failed: ${ttx.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        view.findViewById<Button>(R.id.btnEdit).setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Edit UI coming in Settings/Admin", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupAllTimeRadarChart(
        radarChart: SimpleRadarChart?,
        strengthTotal: Int,
        knowledgeTotal: Int,
        communicationTotal: Int,
        socialTotal: Int,
        lifestyleTotal: Int,
        miscellaneousTotal: Int
    ) {
        try {
            radarChart ?: return
            
            val points = mutableListOf<SimpleRadarChart.RadarPoint>()
            
            if (strengthTotal > 0) {
                points.add(SimpleRadarChart.RadarPoint(
                    strengthTotal.toFloat(),
                    "Strength",
                    android.graphics.Color.parseColor("#FF6B6B")
                ))
            }
            if (knowledgeTotal > 0) {
                points.add(SimpleRadarChart.RadarPoint(
                    knowledgeTotal.toFloat(),
                    "Knowledge",
                    android.graphics.Color.parseColor("#4ECDC4")
                ))
            }
            if (communicationTotal > 0) {
                points.add(SimpleRadarChart.RadarPoint(
                    communicationTotal.toFloat(),
                    "Communication",
                    android.graphics.Color.parseColor("#45B7D1")
                ))
            }
            if (socialTotal > 0) {
                points.add(SimpleRadarChart.RadarPoint(
                    socialTotal.toFloat(),
                    "Social",
                    android.graphics.Color.parseColor("#FFA726")
                ))
            }
            if (lifestyleTotal > 0) {
                points.add(SimpleRadarChart.RadarPoint(
                    lifestyleTotal.toFloat(),
                    "Lifestyle",
                    android.graphics.Color.parseColor("#AB47BC")
                ))
            }
            if (miscellaneousTotal > 0) {
                points.add(SimpleRadarChart.RadarPoint(
                    miscellaneousTotal.toFloat(),
                    "Miscellaneous",
                    android.graphics.Color.parseColor("#95A5A6")
                ))
            }
            
            if (points.isNotEmpty()) {
                radarChart.setData(points)
                radarChart.visibility = android.view.View.VISIBLE
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Error setting up radar chart: ${e.message}")
            // Hide the chart if there's an error
            radarChart?.visibility = android.view.View.GONE
        }
    }
}


