package com.example.vampire_system.data.db

import com.example.vampire_system.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PlanSeederComprehensive {

    suspend fun seedIfEmpty(db: AppDatabase) = withContext(Dispatchers.IO) {
        val abilityDao = db.abilityDao()
        val levelDao = db.levelDao()
        val taskDao = db.levelTaskDao()

        // Probe: if level 1 exists we assume seeded
        val probe = levelDao.byId(1)
        if (probe != null) return@withContext

        // 1) COMPREHENSIVE ABILITIES - All 100 levels covered
        val abilities = listOf(
            // LEVELS 1-10: FOUNDATIONS & CORE GATE
            AbilityEntity("pushups","Push-ups (Core Gate)",AbilityGroup.FOUNDATIONS,"rep", XpRule.PerUnit(0.25), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.PHOTO, EvidenceKind.COUNTER), unlockLevel = 1),
            AbilityEntity("reading","Reading XP",AbilityGroup.FOUNDATIONS,"page", XpRule.PerUnit(1.0), 40,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 2),
            AbilityEntity("walk_run","Walk/Run XP",AbilityGroup.FITNESS,"10min", XpRule.PerMinutes(5), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 3),
            AbilityEntity("notes","Notes XP",AbilityGroup.FOUNDATIONS,"250w", XpRule.PerUnit(3.0), null,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 4),
            AbilityEntity("meditation","Meditation XP",AbilityGroup.FOUNDATIONS,"10min", XpRule.PerMinutes(3), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 5),
            AbilityEntity("speaking_drills","Communication Speaking Drills",AbilityGroup.COMMUNICATION,"10min", XpRule.Flat(10), null,null,
                setOf(EvidenceKind.AUDIO, EvidenceKind.NOTE), unlockLevel = 5),
            AbilityEntity("coding_focus","Coding Focus XP",AbilityGroup.GENAI,"20min", XpRule.Flat(15), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 6),
            AbilityEntity("llm_dialogue","LLM Dialogue Role-play",AbilityGroup.COMMUNICATION,"20min", XpRule.Flat(20), null,null,
                setOf(EvidenceKind.TRANSCRIPT, EvidenceKind.RUBRIC, EvidenceKind.NOTE), unlockLevel = 6),
            AbilityEntity("mobility","Mobility XP",AbilityGroup.FITNESS,"10min", XpRule.PerMinutes(2), null,null,
                setOf(EvidenceKind.CHECKLIST, EvidenceKind.NOTE), unlockLevel = 7),
            AbilityEntity("code_review_serious","Code Review XP (Serious)",AbilityGroup.COMMUNICATION,"review", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.LINK, EvidenceKind.NOTE, EvidenceKind.RUBRIC), unlockLevel = 8),
            AbilityEntity("protein_target","Protein Target Day XP",AbilityGroup.NUTRITION,"day", XpRule.Flat(20), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.SCREENSHOT), unlockLevel = 9),
            AbilityEntity("shipping_tool","Ship Tiny Tool (CLI/API)",AbilityGroup.FOUNDATIONS,"feature", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.REPO, EvidenceKind.DEMO), unlockLevel = 10),
            
            // LEVELS 11-20: GENAI & SERIOUSNESS GATES
            AbilityEntity("data_curation","Data Curation",AbilityGroup.GENAI,"pairs", XpRule.PerUnit(0.1), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.TABLE), unlockLevel = 11),
            AbilityEntity("qlora_sft","QLoRA SFT",AbilityGroup.GENAI,"model", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.METRICS), unlockLevel = 12),
            AbilityEntity("eval_harness","Evaluation Harness v0",AbilityGroup.GENAI,"eval", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.TABLE), unlockLevel = 13),
            AbilityEntity("quantization","Quantization & Memory Lab",AbilityGroup.GENAI,"experiment", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.ANALYSIS), unlockLevel = 14),
            AbilityEntity("gym_serious","Gym Workout (Serious)",AbilityGroup.FITNESS,"session", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE, EvidenceKind.LOG), unlockLevel = 15),
            AbilityEntity("presentation_micro","Presentation Micro-talk",AbilityGroup.COMMUNICATION,"5min", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.VIDEO, EvidenceKind.NOTE), unlockLevel = 15),
            AbilityEntity("data_pipeline","Data Pipeline v1",AbilityGroup.GENAI,"pipeline", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.MANIFEST, EvidenceKind.VALIDATION), unlockLevel = 16),
            AbilityEntity("inference_eng","Inference Engineering",AbilityGroup.GENAI,"deployment", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.CONFIG), unlockLevel = 17),
            AbilityEntity("swimming_serious","Swimming (Serious)",AbilityGroup.FITNESS,"20min", XpRule.PerMinutes(4), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 17),
            AbilityEntity("alignment_101","Alignment 101 (DPO/KTO/RLAIF)",AbilityGroup.GENAI,"experiment", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.TABLE), unlockLevel = 18),
            AbilityEntity("interview_mock","Interview Mock",AbilityGroup.COMMUNICATION,"30min", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.VIDEO, EvidenceKind.NOTE), unlockLevel = 18),
            AbilityEntity("observability","Observability",AbilityGroup.GENAI,"dashboard", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.DASHBOARD, EvidenceKind.NOTE), unlockLevel = 19),
            AbilityEntity("badminton_serious","Badminton (Serious)",AbilityGroup.FITNESS,"match", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 19),
            AbilityEntity("production_finetune","Production Fine-tune",AbilityGroup.GENAI,"model", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.ENDPOINT, EvidenceKind.METRICS, EvidenceKind.REPORT), unlockLevel = 20),
            
            // LEVELS 21-30: ADVANCED GENAI & FITNESS
            AbilityEntity("tokenizer_lab","Tokenizer Lab",AbilityGroup.GENAI,"experiment", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.NOTE), unlockLevel = 21),
            AbilityEntity("multi_gpu","Multi-GPU Intro (FSDP/DeepSpeed)",AbilityGroup.GENAI,"experiment", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 22),
            AbilityEntity("curriculum_design","Curriculum Design",AbilityGroup.GENAI,"curriculum", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.NOTE), unlockLevel = 23),
            AbilityEntity("tiny_model","Tiny Model Training",AbilityGroup.GENAI,"model", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.CHART, EvidenceKind.ANALYSIS), unlockLevel = 24),
            AbilityEntity("mini_rag","Mini-RAG + Finetune",AbilityGroup.GENAI,"system", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.DEMO, EvidenceKind.METRICS), unlockLevel = 25),
            AbilityEntity("scheduling_lr","Scheduling & LR Tricks",AbilityGroup.GENAI,"experiment", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.CHART, EvidenceKind.NOTE), unlockLevel = 26),
            AbilityEntity("hydration_day","Hydration Day XP",AbilityGroup.NUTRITION,"day", XpRule.Flat(3), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.SCREENSHOT), unlockLevel = 26),
            AbilityEntity("fiber_target","Fiber Target Day XP",AbilityGroup.NUTRITION,"day", XpRule.Flat(3), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.SCREENSHOT), unlockLevel = 26),
            AbilityEntity("meal_prep_weekly","Meal Prep Weekly XP",AbilityGroup.NUTRITION,"week", XpRule.Flat(10), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.CHECKLIST), unlockLevel = 26),
            AbilityEntity("cooking_home","Cooking at Home XP",AbilityGroup.NUTRITION,"meal", XpRule.PerUnit(3.0), 2,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.PHOTO), unlockLevel = 26),
            AbilityEntity("trace_observability","Trace & Observability",AbilityGroup.GENAI,"dashboard", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.DASHBOARD, EvidenceKind.NOTE), unlockLevel = 27),
            AbilityEntity("data_mix_governance","Data-Mix Governance",AbilityGroup.GENAI,"governance", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.MANIFEST, EvidenceKind.CHART), unlockLevel = 28),
            AbilityEntity("cost_modeling","Cost Modeling",AbilityGroup.GENAI,"model", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.MODEL, EvidenceKind.VALIDATION), unlockLevel = 29),
            AbilityEntity("automated_pipeline","Automated Pipeline",AbilityGroup.GENAI,"pipeline", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.CODE, EvidenceKind.LOG, EvidenceKind.README), unlockLevel = 30),
            
            // LEVELS 31-40: INFRASTRUCTURE & OPTIMIZATION
            AbilityEntity("kv_cache_bench","KV-Cache Benchmarks",AbilityGroup.GENAI,"benchmark", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.NOTE), unlockLevel = 31),
            AbilityEntity("prefill_decode","Prefill/Decode Optimization",AbilityGroup.GENAI,"optimization", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.NOTE), unlockLevel = 32),
            AbilityEntity("scheduler_throttling","Scheduler & Throttling",AbilityGroup.GENAI,"optimization", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.DIFF, EvidenceKind.TEST), unlockLevel = 33),
            AbilityEntity("cost_model_v1","Cost Model v1",AbilityGroup.GENAI,"model", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.NOTEBOOK, EvidenceKind.CHART), unlockLevel = 34),
            AbilityEntity("continuous_5k","Continuous 5K Run",AbilityGroup.FITNESS,"run", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.SCREENSHOT, EvidenceKind.NOTE), unlockLevel = 35),
            AbilityEntity("distributed_eval","Distributed Eval Runner",AbilityGroup.GENAI,"system", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.SCRIPT, EvidenceKind.DASHBOARD), unlockLevel = 36),
            AbilityEntity("containerized_trainer","Containerized Trainer",AbilityGroup.GENAI,"deployment", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.CONFIG), unlockLevel = 37),
            AbilityEntity("spot_resilience","Spot Resilience",AbilityGroup.GENAI,"optimization", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.METRICS), unlockLevel = 38),
            AbilityEntity("checkpoint_hygiene","Checkpoint Hygiene",AbilityGroup.GENAI,"governance", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.SCREENSHOT, EvidenceKind.POLICY), unlockLevel = 39),
            AbilityEntity("cluster_autoscale","Cluster Autoscaling",AbilityGroup.GENAI,"infrastructure", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.CODE, EvidenceKind.DEMO, EvidenceKind.LOG), unlockLevel = 40),
            
            // LEVELS 41-50: SAFETY & ADVANCED SYSTEMS
            AbilityEntity("safety_taxonomy","Safety Taxonomy & Filters",AbilityGroup.GENAI,"safety", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.TAXONOMY, EvidenceKind.CODE, EvidenceKind.LOG), unlockLevel = 41),
            AbilityEntity("prompt_injection","Prompt Injection Eval",AbilityGroup.GENAI,"evaluation", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.EXAMPLES), unlockLevel = 42),
            AbilityEntity("tool_calling_safety","Tool-Calling Safety",AbilityGroup.GENAI,"safety", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.POLICY, EvidenceKind.TESTS), unlockLevel = 43),
            AbilityEntity("pii_detection","PII Detection & Scrubbing",AbilityGroup.GENAI,"safety", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.COMPARISON, EvidenceKind.METRICS), unlockLevel = 44),
            AbilityEntity("evals_baseline","Evals Beat Baseline",AbilityGroup.GENAI,"evaluation", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.REPORT, EvidenceKind.TAXONOMY), unlockLevel = 45),
            AbilityEntity("moe_finetune","MoE Finetune",AbilityGroup.GENAI,"model", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.COMPARISON), unlockLevel = 46),
            AbilityEntity("distillation","Distillation Pass",AbilityGroup.GENAI,"optimization", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.RECIPE, EvidenceKind.METRICS), unlockLevel = 47),
            AbilityEntity("latency_cost_tuning","Latency & Cost Tuning",AbilityGroup.GENAI,"optimization", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.REPORT, EvidenceKind.COMPARISON), unlockLevel = 48),
            AbilityEntity("multi_tenant_mvp","Multi-Tenant MVP",AbilityGroup.GENAI,"system", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.DEMO, EvidenceKind.METRICS, EvidenceKind.CONFIG), unlockLevel = 50),
            
            // LEVELS 51-60: LONG-CONTEXT & OBSERVABILITY
            AbilityEntity("long_context_strategy","Long-Context Strategy",AbilityGroup.GENAI,"experiment", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.CHART, EvidenceKind.NOTE), unlockLevel = 51),
            AbilityEntity("retrieval_quality","Retrieval Quality Lab",AbilityGroup.GENAI,"experiment", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.NOTE), unlockLevel = 52),
            AbilityEntity("flash_attention","FlashAttention Integration",AbilityGroup.GENAI,"integration", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.REPORT, EvidenceKind.CONFIG), unlockLevel = 53),
            AbilityEntity("long_context_evals","Long-Context Evals",AbilityGroup.GENAI,"evaluation", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.RESULTS, EvidenceKind.ANALYSIS), unlockLevel = 54),
            AbilityEntity("continuous_10k","Continuous 10K Run",AbilityGroup.FITNESS,"run", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.SCREENSHOT, EvidenceKind.NOTE), unlockLevel = 55),
            AbilityEntity("guardrails_training","Guardrails in Training Loop",AbilityGroup.GENAI,"experiment", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.RECIPE, EvidenceKind.METRICS), unlockLevel = 56),
            AbilityEntity("strength_progression","Strength Progression",AbilityGroup.FITNESS,"week", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.VIDEO), unlockLevel = 57),
            AbilityEntity("recovery_logging","Recovery Logging",AbilityGroup.FITNESS,"day", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.CHART, EvidenceKind.NOTE), unlockLevel = 58),
            AbilityEntity("evaluator_refactor","Evaluator Refactor",AbilityGroup.GENAI,"refactor", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.CODE, EvidenceKind.TEST), unlockLevel = 59),
            AbilityEntity("observability_slos","Observability SLOs",AbilityGroup.GENAI,"system", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.SCREENSHOT, EvidenceKind.RUNBOOK), unlockLevel = 60),
            
            // LEVELS 61-70: SYNTHETIC DATA & RAG
            AbilityEntity("synthetic_data_v1","Synthetic Data v1",AbilityGroup.GENAI,"dataset", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.DATASET, EvidenceKind.REVIEW), unlockLevel = 61),
            AbilityEntity("knowledge_distillation_v2","Knowledge Distillation v2",AbilityGroup.GENAI,"experiment", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.GRID, EvidenceKind.NOTE), unlockLevel = 62),
            AbilityEntity("speculative_decoding","Speculative Decoding",AbilityGroup.GENAI,"optimization", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.BENCHMARK, EvidenceKind.NOTE), unlockLevel = 63),
            AbilityEntity("long_context_rag","Long-Context RAG",AbilityGroup.GENAI,"system", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.EVAL, EvidenceKind.REPORT), unlockLevel = 64),
            AbilityEntity("body_composition","Body Composition Check",AbilityGroup.FITNESS,"check", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.PLAN), unlockLevel = 65),
            AbilityEntity("hardware_benchmarking","Hardware Benchmarking",AbilityGroup.GENAI,"benchmark", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.NOTE), unlockLevel = 66),
            AbilityEntity("rlaif_pipeline","RLAIF Pipeline",AbilityGroup.GENAI,"pipeline", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.DIAGRAM, EvidenceKind.METRICS), unlockLevel = 67),
            AbilityEntity("etl_lineage","ETL with Lineage",AbilityGroup.GENAI,"system", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.CODE, EvidenceKind.GRAPH), unlockLevel = 68),
            AbilityEntity("macro_nutrition","Macro Cycle Nutrition",AbilityGroup.NUTRITION,"cycle", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 69),
            AbilityEntity("production_rag","Production RAG + Caching",AbilityGroup.GENAI,"system", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.PLAYBOOK), unlockLevel = 70),
            
            // LEVELS 71-80: RESEARCH & COMMUNITY
            AbilityEntity("conference_abstracts","Conference Abstracts/CFP",AbilityGroup.COMMUNICATION,"submission", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.RECEIPT, EvidenceKind.DRAFT), unlockLevel = 71),
            AbilityEntity("hills_trails","Hills/Trails Training",AbilityGroup.FITNESS,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 72),
            AbilityEntity("llm_memory","LLM Memory Experiment",AbilityGroup.GENAI,"experiment", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.DEMO, EvidenceKind.CASES), unlockLevel = 73),
            AbilityEntity("breathwork_drills","Breathwork Drills",AbilityGroup.FITNESS,"session", XpRule.Flat(20), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 74),
            AbilityEntity("public_demo","Public Demo or Launch",AbilityGroup.COMMUNICATION,"launch", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.LINK, EvidenceKind.METRICS), unlockLevel = 75),
            AbilityEntity("code_review_culture","Code Review Culture",AbilityGroup.COMMUNICATION,"culture", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.RUBRIC, EvidenceKind.REVIEWS), unlockLevel = 76),
            AbilityEntity("strength_split","Strength Split (Tempo)",AbilityGroup.FITNESS,"week", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 77),
            AbilityEntity("dataset_labeling","Dataset Labeling",AbilityGroup.GENAI,"dataset", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.STATS, EvidenceKind.GUIDELINES), unlockLevel = 78),
            AbilityEntity("community_help","Community Help",AbilityGroup.COMMUNICATION,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.LINKS, EvidenceKind.NOTE), unlockLevel = 79),
            AbilityEntity("open_source_contribution","Open Source Contribution",AbilityGroup.COMMUNICATION,"contribution", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.LINK, EvidenceKind.ACKNOWLEDGMENT), unlockLevel = 80),
            
            // LEVELS 81-90: ADVANCED TECHNIQUES
            AbilityEntity("recovery_intensified","Recovery Intensified",AbilityGroup.FITNESS,"session", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 81),
            AbilityEntity("advanced_evals","Advanced Evals & Error Taxonomy",AbilityGroup.GENAI,"evaluation", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.TAXONOMY, EvidenceKind.PLAN), unlockLevel = 82),
            AbilityEntity("threshold_run","Threshold Run",AbilityGroup.FITNESS,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 83),
            AbilityEntity("architecture_diagramming","Architecture Diagramming",AbilityGroup.GENAI,"design", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.DIAGRAM, EvidenceKind.ADR), unlockLevel = 84),
            AbilityEntity("strength_prs","Strength PRs",AbilityGroup.FITNESS,"week", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.VIDEO), unlockLevel = 85),
            AbilityEntity("ab_experiments","A/B Experiments",AbilityGroup.GENAI,"experiment", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.NOTEBOOK, EvidenceKind.DECISION), unlockLevel = 86),
            AbilityEntity("speed_work","Speed Work",AbilityGroup.FITNESS,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 87),
            AbilityEntity("cost_latency_tuning","Cost & Latency Deep Tuning",AbilityGroup.GENAI,"optimization", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.SHEET, EvidenceKind.COMPARISON), unlockLevel = 88),
            AbilityEntity("teaching_reps","Teaching Reps",AbilityGroup.COMMUNICATION,"session", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.FEEDBACK, EvidenceKind.NOTE), unlockLevel = 89),
            AbilityEntity("masterclass_talk","Masterclass/Talk",AbilityGroup.COMMUNICATION,"presentation", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.RECORDING, EvidenceKind.REPO), unlockLevel = 90),
            
            // LEVELS 91-100: GRAND MASTERY
            AbilityEntity("tooling_polish","Tooling Polish",AbilityGroup.GENAI,"improvement", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.CHANGELOG, EvidenceKind.COMPARISON), unlockLevel = 91),
            AbilityEntity("long_aerobic","Long Aerobic",AbilityGroup.FITNESS,"session", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.REPORT), unlockLevel = 92),
            AbilityEntity("research_replication","Research Replication",AbilityGroup.GENAI,"reproduction", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.REPORT, EvidenceKind.NOTE), unlockLevel = 93),
            AbilityEntity("content_cadence","Content Cadence",AbilityGroup.COMMUNICATION,"week", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.LINKS, EvidenceKind.METRICS), unlockLevel = 94),
            AbilityEntity("endurance_event","Endurance Event",AbilityGroup.FITNESS,"event", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.RESULT, EvidenceKind.NOTE), unlockLevel = 95),
            AbilityEntity("system_resilience","System Resilience",AbilityGroup.GENAI,"testing", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.REPORT, EvidenceKind.NOTE), unlockLevel = 96),
            AbilityEntity("hiring_loop_practice","Hiring Loop Practice",AbilityGroup.COMMUNICATION,"practice", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.RUBRIC, EvidenceKind.NOTE), unlockLevel = 97),
            AbilityEntity("paper_writing","Paper Writing",AbilityGroup.COMMUNICATION,"paper", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.PDF, EvidenceKind.REPO), unlockLevel = 98),
            AbilityEntity("polish_scale_plan","Polish & Scale Plan",AbilityGroup.GENAI,"planning", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.PLAN, EvidenceKind.ROADMAP), unlockLevel = 99),
            AbilityEntity("grand_boss","Grand Boss: Best-in-World Trajectory",AbilityGroup.FOUNDATIONS,"milestone", XpRule.Flat(100), null,null,
                setOf(EvidenceKind.POST, EvidenceKind.DEMO, EvidenceKind.PROOF), unlockLevel = 100)
        )
        
        abilityDao.upsertAll(abilities)
        println("✅ Added ${abilities.size} comprehensive abilities to database")

        // 2) LEVELS 1..100 with XP requirements and bosses each decade
        val levels = (1..100).map { lvl ->
            LevelEntity(
                id = lvl,
                xpRequired = Xp.xpForLevel(lvl),
                title = if (lvl % 10 == 0) "Boss" else null,
                boss = (lvl % 10 == 0),
                acceptance = emptyList(),
                unlocks = emptyList()
            )
        }
        levelDao.upsertAll(levels)
        println("✅ Added ${levels.size} levels to database")

        // 3) LEVEL-SPECIFIC TASKS from your detailed design
        val tasks = listOf(
            // LEVEL 1-10 TASKS
            LevelTaskEntity("L1_PUSHUPS", 1, "pushups", "Push-ups only; daily 20-60 reps. Evidence: counter note or photo.", listOf("counter_note", "photo"), TaskCategory.STRENGTH, 10.0),
            LevelTaskEntity("L2_READING", 2, "reading", "5-10 pages/day; note one takeaway. Evidence: page range + one line.", listOf("page_range", "takeaway"), TaskCategory.KNOWLEDGE, 10.0),
            LevelTaskEntity("L3_WALKRUN", 3, "walk_run", "20-30 min brisk walk or jog. Evidence: timer/steps.", listOf("timer", "steps"), TaskCategory.STRENGTH, 10.0),
            LevelTaskEntity("L4_NOTES", 4, "notes", "250-word digest from reading. Evidence: word count + snippet.", listOf("word_count", "snippet"), TaskCategory.COMMUNICATION, 10.0),
            LevelTaskEntity("L5_MEDITATION", 5, "meditation", "10 min meditation; one 10-min speaking drill (record + 2 fixes). Evidence: timer + audio note.", listOf("timer", "audio_note"), TaskCategory.LIFESTYLE, 10.0),
            LevelTaskEntity("L5_SPEAKING", 5, "speaking_drills", "10-min speaking drill (record + 2 fixes). Evidence: audio + fixes.", listOf("audio", "fixes"), TaskCategory.COMMUNICATION, 10.0),
            LevelTaskEntity("L6_CODING", 6, "coding_focus", "One 20-min focused coding block. Evidence: timer + note.", listOf("timer", "note"), TaskCategory.KNOWLEDGE, 10.0),
            LevelTaskEntity("L6_LLM_DIALOGUE", 6, "llm_dialogue", "20-min LLM dialogue (transcript + rubric + one improvement).", listOf("transcript", "rubric", "improvement"), TaskCategory.COMMUNICATION, 10.0),
            LevelTaskEntity("L7_MOBILITY", 7, "mobility", "Hips/shoulders routine 10-20 min. Evidence: checklist.", listOf("checklist"), TaskCategory.STRENGTH, 10.0),
            LevelTaskEntity("L8_CODE_REVIEW", 8, "code_review_serious", "Review one PR for 20-40 min using checklist; provide actionable suggestions. Evidence: PR link or diff summary.", listOf("pr_link", "diff_summary"), TaskCategory.COMMUNICATION, 10.0),
            LevelTaskEntity("L9_PROTEIN", 9, "protein_target", "Hit protein ≈ 2.0 g/kg ± 10% and stay within calories. Evidence: simple log or app screenshot.", listOf("log", "screenshot"), TaskCategory.LIFESTYLE, 10.0),
            LevelTaskEntity("L10_SHIP", 10, "shipping_tool", "Ship a tiny tool (CLI/API) and show usage. Evidence: repo/gist link and example.", listOf("repo_link", "example"), TaskCategory.KNOWLEDGE, 10.0),
            
            // LEVEL 11-20 TASKS
            LevelTaskEntity("L11_CURATE", 11, "data_curation", "Curate ≥ 200 instruction pairs from your own logs; dedup, tag, compute stats. Evidence: dataset stats + sample rows.", listOf("dataset_stats", "sample_rows"), TaskCategory.KNOWLEDGE, 10.0),
            LevelTaskEntity("L12_QLORA", 12, "qlora_sft", "QLoRA SFT on 200-1,000 pairs; log loss; save adapters. Evidence: train log, adapter weights, loss curve.", listOf("train_log", "adapter_weights", "loss_curve"), TaskCategory.KNOWLEDGE, 10.0),
            LevelTaskEntity("L13_EVAL", 13, "eval_harness", "Eval harness v0 for ≥ 100 items (exact-match + rubric); set a baseline. Evidence: results JSON + table.", listOf("results_json", "table"), TaskCategory.KNOWLEDGE, 10.0),
            LevelTaskEntity("L14_QUANT", 14, "quantization", "Quantization and memory lab: 4-bit and 8-bit; record latency, peak RAM/VRAM, quality deltas. Evidence: comparison table with takeaways.", listOf("comparison_table", "takeaways"), TaskCategory.KNOWLEDGE, 10.0),
            LevelTaskEntity("L15_GYM", 15, "gym_serious", "Gym unlock with 3 structured sessions in 7 days (RPE ≥ 7; log sets). Evidence: session logs.", listOf("session_logs"), TaskCategory.STRENGTH, 10.0),
            LevelTaskEntity("L15_PRESENTATION", 15, "presentation_micro", "5-min video presentation. Evidence: video.", listOf("video"), TaskCategory.COMMUNICATION, 10.0),
            LevelTaskEntity("L16_PIPELINE", 16, "data_pipeline", "Data pipeline v1: normalize, dedup, split, prompt-template tagging, hash rows. Evidence: manifest + leakage check.", listOf("manifest", "leakage_check"), TaskCategory.KNOWLEDGE, 10.0),
            LevelTaskEntity("L17_INFERENCE", 17, "inference_eng", "Inference engineering: serve base/tuned with vLLM/TensorRT-LLM/Triton; record tokens/s on fixed batch. Evidence: throughput CSV + config flags.", listOf("throughput_csv", "config_flags"), TaskCategory.KNOWLEDGE, 10.0),
            LevelTaskEntity("L18_ALIGNMENT", 18, "alignment_101", "Alignment 101 (DPO/KTO/RLAIF) with ≥ 200 preference pairs; report win-rate uplift vs L12 SFT. Evidence: preference schema + win-rate table.", listOf("preference_schema", "win_rate_table"), TaskCategory.KNOWLEDGE, 10.0),
            LevelTaskEntity("L19_OBSERVABILITY", 19, "observability", "Observability: tracing for token-time breakdown and cache hit-rate; write 3 insights. Evidence: dashboard screenshot + notes.", listOf("dashboard_screenshot", "notes"), TaskCategory.KNOWLEDGE, 10.0),
            LevelTaskEntity("L20_PRODUCTION", 20, "production_finetune", "Production fine-tune shipped behind API; define SLOs (p95 latency and quality), 24-h canary, publish evals. Evidence: endpoint doc, SLOs, canary report, eval chart.", listOf("endpoint_doc", "slos", "canary_report", "eval_chart"), TaskCategory.KNOWLEDGE, 10.0)
        )
        
        taskDao.upsertAll(tasks)
        println("✅ Added ${tasks.size} level-specific tasks to database")

        // 4) INITIAL LEVEL PROGRESS
        val lpDao = db.levelProgressDao()
        if (lpDao.get() == null) {
            lpDao.upsert(
                LevelProgressEntity(
                    levelId = 1,
                    xpInLevel = 0,
                    startedAtDate = com.example.vampire_system.util.Dates.todayLocal(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            println("✅ Initialized level progress at Level 1")
        }
    }
    
    // Force add all abilities even if database already exists
    suspend fun forceAddAllAbilities(db: AppDatabase) = withContext(Dispatchers.IO) {
        val abilityDao = db.abilityDao()
        
        // Get the comprehensive abilities list (same as above)
        val abilities = listOf(
            // LEVELS 1-10: FOUNDATIONS & CORE GATE
            AbilityEntity("pushups","Push-ups (Core Gate)",AbilityGroup.FOUNDATIONS,"rep", XpRule.PerUnit(0.25), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.PHOTO, EvidenceKind.COUNTER), unlockLevel = 1),
            AbilityEntity("reading","Reading XP",AbilityGroup.FOUNDATIONS,"page", XpRule.PerUnit(1.0), 40,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 2),
            AbilityEntity("walk_run","Walk/Run XP",AbilityGroup.FITNESS,"10min", XpRule.PerMinutes(5), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 3),
            AbilityEntity("notes","Notes XP",AbilityGroup.FOUNDATIONS,"250w", XpRule.PerUnit(3.0), null,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 4),
            AbilityEntity("meditation","Meditation XP",AbilityGroup.FOUNDATIONS,"10min", XpRule.PerMinutes(3), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 5),
            AbilityEntity("speaking_drills","Communication Speaking Drills",AbilityGroup.COMMUNICATION,"10min", XpRule.Flat(10), null,null,
                setOf(EvidenceKind.AUDIO, EvidenceKind.NOTE), unlockLevel = 5),
            AbilityEntity("coding_focus","Coding Focus XP",AbilityGroup.GENAI,"20min", XpRule.Flat(15), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 6),
            AbilityEntity("llm_dialogue","LLM Dialogue Role-play",AbilityGroup.COMMUNICATION,"20min", XpRule.Flat(20), null,null,
                setOf(EvidenceKind.TRANSCRIPT, EvidenceKind.RUBRIC, EvidenceKind.NOTE), unlockLevel = 6),
            AbilityEntity("mobility","Mobility XP",AbilityGroup.FITNESS,"10min", XpRule.PerMinutes(2), null,null,
                setOf(EvidenceKind.CHECKLIST, EvidenceKind.NOTE), unlockLevel = 7),
            AbilityEntity("code_review_serious","Code Review XP (Serious)",AbilityGroup.COMMUNICATION,"review", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.LINK, EvidenceKind.NOTE, EvidenceKind.RUBRIC), unlockLevel = 8),
            AbilityEntity("protein_target","Protein Target Day XP",AbilityGroup.NUTRITION,"day", XpRule.Flat(20), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.SCREENSHOT), unlockLevel = 9),
            AbilityEntity("shipping_tool","Ship Tiny Tool (CLI/API)",AbilityGroup.FOUNDATIONS,"feature", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.REPO, EvidenceKind.DEMO), unlockLevel = 10),
            
            // LEVELS 11-20: GENAI & SERIOUSNESS GATES
            AbilityEntity("data_curation","Data Curation",AbilityGroup.GENAI,"pairs", XpRule.PerUnit(0.1), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.TABLE), unlockLevel = 11),
            AbilityEntity("qlora_sft","QLoRA SFT",AbilityGroup.GENAI,"model", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.METRICS), unlockLevel = 12),
            AbilityEntity("eval_harness","Evaluation Harness v0",AbilityGroup.GENAI,"eval", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.TABLE), unlockLevel = 13),
            AbilityEntity("quantization","Quantization & Memory Lab",AbilityGroup.GENAI,"experiment", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.ANALYSIS), unlockLevel = 14),
            AbilityEntity("gym_serious","Gym Workout (Serious)",AbilityGroup.FITNESS,"session", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE, EvidenceKind.LOG), unlockLevel = 15),
            AbilityEntity("presentation_micro","Presentation Micro-talk",AbilityGroup.COMMUNICATION,"5min", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.VIDEO, EvidenceKind.NOTE), unlockLevel = 15),
            AbilityEntity("data_pipeline","Data Pipeline v1",AbilityGroup.GENAI,"pipeline", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.MANIFEST, EvidenceKind.VALIDATION), unlockLevel = 16),
            AbilityEntity("inference_eng","Inference Engineering",AbilityGroup.GENAI,"deployment", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.CONFIG), unlockLevel = 17),
            AbilityEntity("swimming_serious","Swimming (Serious)",AbilityGroup.FITNESS,"20min", XpRule.PerMinutes(4), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 17),
            AbilityEntity("alignment_101","Alignment 101 (DPO/KTO/RLAIF)",AbilityGroup.GENAI,"experiment", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.TABLE), unlockLevel = 18),
            AbilityEntity("interview_mock","Interview Mock",AbilityGroup.COMMUNICATION,"30min", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.VIDEO, EvidenceKind.NOTE), unlockLevel = 18),
            AbilityEntity("observability","Observability",AbilityGroup.GENAI,"dashboard", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.DASHBOARD, EvidenceKind.NOTE), unlockLevel = 19),
            AbilityEntity("badminton_serious","Badminton (Serious)",AbilityGroup.FITNESS,"match", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 19),
            AbilityEntity("production_finetune","Production Fine-tune",AbilityGroup.GENAI,"model", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.ENDPOINT, EvidenceKind.METRICS, EvidenceKind.REPORT), unlockLevel = 20),
            
            // LEVELS 21-30: ADVANCED GENAI & FITNESS
            AbilityEntity("tokenizer_lab","Tokenizer Lab",AbilityGroup.GENAI,"experiment", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.NOTE), unlockLevel = 21),
            AbilityEntity("multi_gpu","Multi-GPU Intro (FSDP/DeepSpeed)",AbilityGroup.GENAI,"experiment", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 22),
            AbilityEntity("curriculum_design","Curriculum Design",AbilityGroup.GENAI,"curriculum", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.NOTE), unlockLevel = 23),
            AbilityEntity("tiny_model","Tiny Model Training",AbilityGroup.GENAI,"model", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.CHART, EvidenceKind.ANALYSIS), unlockLevel = 24),
            AbilityEntity("mini_rag","Mini-RAG + Finetune",AbilityGroup.GENAI,"system", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.DEMO, EvidenceKind.METRICS), unlockLevel = 25),
            AbilityEntity("scheduling_lr","Scheduling & LR Tricks",AbilityGroup.GENAI,"experiment", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.CHART, EvidenceKind.NOTE), unlockLevel = 26),
            AbilityEntity("hydration_day","Hydration Day XP",AbilityGroup.NUTRITION,"day", XpRule.Flat(3), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.SCREENSHOT), unlockLevel = 26),
            AbilityEntity("fiber_target","Fiber Target Day XP",AbilityGroup.NUTRITION,"day", XpRule.Flat(3), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.SCREENSHOT), unlockLevel = 26),
            AbilityEntity("meal_prep_weekly","Meal Prep Weekly XP",AbilityGroup.NUTRITION,"week", XpRule.Flat(10), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.CHECKLIST), unlockLevel = 26),
            AbilityEntity("cooking_home","Cooking at Home XP",AbilityGroup.NUTRITION,"meal", XpRule.PerUnit(3.0), 2,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.PHOTO), unlockLevel = 26),
            AbilityEntity("trace_observability","Trace & Observability",AbilityGroup.GENAI,"dashboard", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.DASHBOARD, EvidenceKind.NOTE), unlockLevel = 27),
            AbilityEntity("data_mix_governance","Data-Mix Governance",AbilityGroup.GENAI,"governance", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.MANIFEST, EvidenceKind.CHART), unlockLevel = 28),
            AbilityEntity("cost_modeling","Cost Modeling",AbilityGroup.GENAI,"model", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.MODEL, EvidenceKind.VALIDATION), unlockLevel = 29),
            AbilityEntity("automated_pipeline","Automated Pipeline",AbilityGroup.GENAI,"pipeline", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.CODE, EvidenceKind.LOG, EvidenceKind.README), unlockLevel = 30),
            
            // LEVELS 31-40: INFRASTRUCTURE & OPTIMIZATION
            AbilityEntity("kv_cache_bench","KV-Cache Benchmarks",AbilityGroup.GENAI,"benchmark", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.NOTE), unlockLevel = 31),
            AbilityEntity("prefill_decode","Prefill/Decode Optimization",AbilityGroup.GENAI,"optimization", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.NOTE), unlockLevel = 32),
            AbilityEntity("scheduler_throttling","Scheduler & Throttling",AbilityGroup.GENAI,"optimization", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.DIFF, EvidenceKind.TEST), unlockLevel = 33),
            AbilityEntity("cost_model_v1","Cost Model v1",AbilityGroup.GENAI,"model", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.NOTEBOOK, EvidenceKind.CHART), unlockLevel = 34),
            AbilityEntity("continuous_5k","Continuous 5K Run",AbilityGroup.FITNESS,"run", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.SCREENSHOT, EvidenceKind.NOTE), unlockLevel = 35),
            AbilityEntity("distributed_eval","Distributed Eval Runner",AbilityGroup.GENAI,"system", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.SCRIPT, EvidenceKind.DASHBOARD), unlockLevel = 36),
            AbilityEntity("containerized_trainer","Containerized Trainer",AbilityGroup.GENAI,"deployment", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.CONFIG), unlockLevel = 37),
            AbilityEntity("spot_resilience","Spot Resilience",AbilityGroup.GENAI,"optimization", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.METRICS), unlockLevel = 38),
            AbilityEntity("checkpoint_hygiene","Checkpoint Hygiene",AbilityGroup.GENAI,"governance", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.SCREENSHOT, EvidenceKind.POLICY), unlockLevel = 39),
            AbilityEntity("cluster_autoscale","Cluster Autoscaling",AbilityGroup.GENAI,"infrastructure", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.CODE, EvidenceKind.DEMO, EvidenceKind.LOG), unlockLevel = 40),
            
            // LEVELS 41-50: SAFETY & ADVANCED SYSTEMS
            AbilityEntity("safety_taxonomy","Safety Taxonomy & Filters",AbilityGroup.GENAI,"safety", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.TAXONOMY, EvidenceKind.CODE, EvidenceKind.LOG), unlockLevel = 41),
            AbilityEntity("prompt_injection","Prompt Injection Eval",AbilityGroup.GENAI,"evaluation", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.EXAMPLES), unlockLevel = 42),
            AbilityEntity("tool_calling_safety","Tool-Calling Safety",AbilityGroup.GENAI,"safety", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.POLICY, EvidenceKind.TESTS), unlockLevel = 43),
            AbilityEntity("pii_detection","PII Detection & Scrubbing",AbilityGroup.GENAI,"safety", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.COMPARISON, EvidenceKind.METRICS), unlockLevel = 44),
            AbilityEntity("evals_baseline","Evals Beat Baseline",AbilityGroup.GENAI,"evaluation", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.REPORT, EvidenceKind.TAXONOMY), unlockLevel = 45),
            AbilityEntity("moe_finetune","MoE Finetune",AbilityGroup.GENAI,"model", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.COMPARISON), unlockLevel = 46),
            AbilityEntity("distillation","Distillation Pass",AbilityGroup.GENAI,"optimization", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.RECIPE, EvidenceKind.METRICS), unlockLevel = 47),
            AbilityEntity("latency_cost_tuning","Latency & Cost Tuning",AbilityGroup.GENAI,"optimization", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.REPORT, EvidenceKind.COMPARISON), unlockLevel = 48),
            AbilityEntity("multi_tenant_mvp","Multi-Tenant MVP",AbilityGroup.GENAI,"system", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.DEMO, EvidenceKind.METRICS, EvidenceKind.CONFIG), unlockLevel = 50),
            
            // LEVELS 51-60: LONG-CONTEXT & OBSERVABILITY
            AbilityEntity("long_context_strategy","Long-Context Strategy",AbilityGroup.GENAI,"experiment", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.CHART, EvidenceKind.NOTE), unlockLevel = 51),
            AbilityEntity("retrieval_quality","Retrieval Quality Lab",AbilityGroup.GENAI,"experiment", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.NOTE), unlockLevel = 52),
            AbilityEntity("flash_attention","FlashAttention Integration",AbilityGroup.GENAI,"integration", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.REPORT, EvidenceKind.CONFIG), unlockLevel = 53),
            AbilityEntity("long_context_evals","Long-Context Evals",AbilityGroup.GENAI,"evaluation", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.RESULTS, EvidenceKind.ANALYSIS), unlockLevel = 54),
            AbilityEntity("continuous_10k","Continuous 10K Run",AbilityGroup.FITNESS,"run", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.SCREENSHOT, EvidenceKind.NOTE), unlockLevel = 55),
            AbilityEntity("guardrails_training","Guardrails in Training Loop",AbilityGroup.GENAI,"experiment", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.RECIPE, EvidenceKind.METRICS), unlockLevel = 56),
            AbilityEntity("strength_progression","Strength Progression",AbilityGroup.FITNESS,"week", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.VIDEO), unlockLevel = 57),
            AbilityEntity("recovery_logging","Recovery Logging",AbilityGroup.FITNESS,"day", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.CHART, EvidenceKind.NOTE), unlockLevel = 58),
            AbilityEntity("evaluator_refactor","Evaluator Refactor",AbilityGroup.GENAI,"refactor", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.CODE, EvidenceKind.TEST), unlockLevel = 59),
            AbilityEntity("observability_slos","Observability SLOs",AbilityGroup.GENAI,"system", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.SCREENSHOT, EvidenceKind.RUNBOOK), unlockLevel = 60),
            
            // LEVELS 61-70: SYNTHETIC DATA & RAG
            AbilityEntity("synthetic_data_v1","Synthetic Data v1",AbilityGroup.GENAI,"dataset", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.DATASET, EvidenceKind.REVIEW), unlockLevel = 61),
            AbilityEntity("knowledge_distillation_v2","Knowledge Distillation v2",AbilityGroup.GENAI,"experiment", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.GRID, EvidenceKind.NOTE), unlockLevel = 62),
            AbilityEntity("speculative_decoding","Speculative Decoding",AbilityGroup.GENAI,"optimization", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.BENCHMARK, EvidenceKind.NOTE), unlockLevel = 63),
            AbilityEntity("long_context_rag","Long-Context RAG",AbilityGroup.GENAI,"system", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.EVAL, EvidenceKind.REPORT), unlockLevel = 64),
            AbilityEntity("body_composition","Body Composition Check",AbilityGroup.FITNESS,"check", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.PLAN), unlockLevel = 65),
            AbilityEntity("hardware_benchmarking","Hardware Benchmarking",AbilityGroup.GENAI,"benchmark", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.TABLE, EvidenceKind.NOTE), unlockLevel = 66),
            AbilityEntity("rlaif_pipeline","RLAIF Pipeline",AbilityGroup.GENAI,"pipeline", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.DIAGRAM, EvidenceKind.METRICS), unlockLevel = 67),
            AbilityEntity("etl_lineage","ETL with Lineage",AbilityGroup.GENAI,"system", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.CODE, EvidenceKind.GRAPH), unlockLevel = 68),
            AbilityEntity("macro_nutrition","Macro Cycle Nutrition",AbilityGroup.NUTRITION,"cycle", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 69),
            AbilityEntity("production_rag","Production RAG + Caching",AbilityGroup.GENAI,"system", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.METRICS, EvidenceKind.PLAYBOOK), unlockLevel = 70),
            
            // LEVELS 71-80: RESEARCH & COMMUNITY
            AbilityEntity("conference_abstracts","Conference Abstracts/CFP",AbilityGroup.COMMUNICATION,"submission", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.RECEIPT, EvidenceKind.DRAFT), unlockLevel = 71),
            AbilityEntity("hills_trails","Hills/Trails Training",AbilityGroup.FITNESS,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 72),
            AbilityEntity("llm_memory","LLM Memory Experiment",AbilityGroup.GENAI,"experiment", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.DEMO, EvidenceKind.CASES), unlockLevel = 73),
            AbilityEntity("breathwork_drills","Breathwork Drills",AbilityGroup.FITNESS,"session", XpRule.Flat(20), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 74),
            AbilityEntity("public_demo","Public Demo or Launch",AbilityGroup.COMMUNICATION,"launch", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.LINK, EvidenceKind.METRICS), unlockLevel = 75),
            AbilityEntity("code_review_culture","Code Review Culture",AbilityGroup.COMMUNICATION,"culture", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.RUBRIC, EvidenceKind.REVIEWS), unlockLevel = 76),
            AbilityEntity("strength_split","Strength Split (Tempo)",AbilityGroup.FITNESS,"week", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 77),
            AbilityEntity("dataset_labeling","Dataset Labeling",AbilityGroup.GENAI,"dataset", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.STATS, EvidenceKind.GUIDELINES), unlockLevel = 78),
            AbilityEntity("community_help","Community Help",AbilityGroup.COMMUNICATION,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.LINKS, EvidenceKind.NOTE), unlockLevel = 79),
            AbilityEntity("open_source_contribution","Open Source Contribution",AbilityGroup.COMMUNICATION,"contribution", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.LINK, EvidenceKind.ACKNOWLEDGMENT), unlockLevel = 80),
            
            // LEVELS 81-90: ADVANCED TECHNIQUES
            AbilityEntity("recovery_intensified","Recovery Intensified",AbilityGroup.FITNESS,"session", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 81),
            AbilityEntity("advanced_evals","Advanced Evals & Error Taxonomy",AbilityGroup.GENAI,"evaluation", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.TAXONOMY, EvidenceKind.PLAN), unlockLevel = 82),
            AbilityEntity("threshold_run","Threshold Run",AbilityGroup.FITNESS,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 83),
            AbilityEntity("architecture_diagramming","Architecture Diagramming",AbilityGroup.GENAI,"design", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.DIAGRAM, EvidenceKind.ADR), unlockLevel = 84),
            AbilityEntity("strength_prs","Strength PRs",AbilityGroup.FITNESS,"week", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.VIDEO), unlockLevel = 85),
            AbilityEntity("ab_experiments","A/B Experiments",AbilityGroup.GENAI,"experiment", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.NOTEBOOK, EvidenceKind.DECISION), unlockLevel = 86),
            AbilityEntity("speed_work","Speed Work",AbilityGroup.FITNESS,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.NOTE), unlockLevel = 87),
            AbilityEntity("cost_latency_tuning","Cost & Latency Deep Tuning",AbilityGroup.GENAI,"optimization", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.SHEET, EvidenceKind.COMPARISON), unlockLevel = 88),
            AbilityEntity("teaching_reps","Teaching Reps",AbilityGroup.COMMUNICATION,"session", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.FEEDBACK, EvidenceKind.NOTE), unlockLevel = 89),
            AbilityEntity("masterclass_talk","Masterclass/Talk",AbilityGroup.COMMUNICATION,"presentation", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.RECORDING, EvidenceKind.REPO), unlockLevel = 90),
            
            // LEVELS 91-100: GRAND MASTERY
            AbilityEntity("tooling_polish","Tooling Polish",AbilityGroup.GENAI,"improvement", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.CHANGELOG, EvidenceKind.COMPARISON), unlockLevel = 91),
            AbilityEntity("long_aerobic","Long Aerobic",AbilityGroup.FITNESS,"session", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.LOG, EvidenceKind.REPORT), unlockLevel = 92),
            AbilityEntity("research_replication","Research Replication",AbilityGroup.GENAI,"reproduction", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.REPORT, EvidenceKind.NOTE), unlockLevel = 93),
            AbilityEntity("content_cadence","Content Cadence",AbilityGroup.COMMUNICATION,"week", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.LINKS, EvidenceKind.METRICS), unlockLevel = 94),
            AbilityEntity("endurance_event","Endurance Event",AbilityGroup.FITNESS,"event", XpRule.Flat(60), null,null,
                setOf(EvidenceKind.RESULT, EvidenceKind.NOTE), unlockLevel = 95),
            AbilityEntity("system_resilience","System Resilience",AbilityGroup.GENAI,"testing", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.REPORT, EvidenceKind.NOTE), unlockLevel = 96),
            AbilityEntity("hiring_loop_practice","Hiring Loop Practice",AbilityGroup.COMMUNICATION,"practice", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.RUBRIC, EvidenceKind.NOTE), unlockLevel = 97),
            AbilityEntity("paper_writing","Paper Writing",AbilityGroup.COMMUNICATION,"paper", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.PDF, EvidenceKind.REPO), unlockLevel = 98),
            AbilityEntity("polish_scale_plan","Polish & Scale Plan",AbilityGroup.GENAI,"planning", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.PLAN, EvidenceKind.ROADMAP), unlockLevel = 99),
            AbilityEntity("grand_boss","Grand Boss: Best-in-World Trajectory",AbilityGroup.FOUNDATIONS,"milestone", XpRule.Flat(100), null,null,
                setOf(EvidenceKind.POST, EvidenceKind.DEMO, EvidenceKind.PROOF), unlockLevel = 100)
        )
        
        // Force add all abilities (this will update existing ones and add new ones)
        abilityDao.upsertAll(abilities)
        
        println("✅ Added ${abilities.size} comprehensive abilities to database")
    }

    // Force seed method that clears existing data first
    suspend fun forceSeed(db: AppDatabase) = withContext(Dispatchers.IO) {
        val abilityDao = db.abilityDao()
        val levelDao = db.levelDao()
        val taskDao = db.levelTaskDao()

        // Clear existing data
        abilityDao.deleteAll()
        levelDao.deleteAll()
        taskDao.deleteAll()
        
        println("🧹 Cleared existing data")

        // Add comprehensive abilities, levels, and tasks
        seedIfEmpty(db)
        
        println("✅ Force seeded comprehensive data")
    }
}
