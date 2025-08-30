package com.example.vampire_system.ui.home

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.features.today.QuestAdapter
import com.example.vampire_system.domain.repo.LevelRepo
import com.example.vampire_system.domain.engine.QuestEngine
import com.example.vampire_system.util.Dates
import kotlinx.coroutines.*
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.flow.first

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = com.example.vampire_system.ui.common.fadeThrough()
        returnTransition = com.example.vampire_system.ui.common.fadeThrough()
    }

    private val vm: HomeViewModel by viewModels {
        HomeViewModel.Factory(AppDatabase.get(requireContext()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val list = view.findViewById<RecyclerView>(R.id.questList)
        list.setHasFixedSize(true)
        val adapter = QuestAdapter(
            onToggle = { item ->
                val db = AppDatabase.get(requireContext())
                val xpEngine = com.example.vampire_system.domain.engine.XpEngine(db, com.example.vampire_system.domain.repo.LevelRepo(db))

                if (item.status == com.example.vampire_system.data.model.QuestStatus.PENDING) {
                    val abilityId = item.abilityId
                    if (abilityId == null) {
                        CoroutineScope(Dispatchers.IO).launch { xpEngine.completeQuest(item.id, 1.0) }
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            val ability = db.abilityDao().byId(abilityId)
                            // Core Gate: block non-foundations until unlocked and show info
                            val levelRepo = com.example.vampire_system.domain.repo.LevelRepo(db)
                            val level = levelRepo.getCurrent().levelId
                            val unlocked = com.example.vampire_system.domain.engine.CoreGateEngine(db).isUnlockedForToday(level)
                            val isFoundation = ability?.group == com.example.vampire_system.data.model.AbilityGroup.FOUNDATIONS
                            if (!isFoundation && !unlocked) {
                                withContext(Dispatchers.Main) {
                                    val unlockAt = ability?.unlockLevel
                                    val msg = buildString {
                                        append("This is locked right now.\n\n")
                                        append("Unlock extras by either:\n")
                                        append("• 3 foundations two days in a row, or\n")
                                        append("• All 5 foundations in one day.\n")
                                        if (unlockAt != null) append("\nAvailable from Level $unlockAt.")
                                    }
                                    android.app.AlertDialog.Builder(requireContext())
                                        .setTitle("Core gate locked")
                                        .setMessage(msg)
                                        .setPositiveButton("OK", null)
                                        .show()
                                }
                                return@launch
                            }

                            if (ability == null) {
                                withContext(Dispatchers.Main) {
                                    android.widget.Toast.makeText(requireContext(), "Ability not available yet", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                return@launch
                            }

                            when (ability.xpRule) {
                                is com.example.vampire_system.data.model.XpRule.Flat -> xpEngine.completeQuest(item.id, 1.0)
                                is com.example.vampire_system.data.model.XpRule.PerUnit,
                                is com.example.vampire_system.data.model.XpRule.PerMinutes -> {
                                    withContext(Dispatchers.Main) {
                                        val input = android.widget.EditText(requireContext())
                                        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                                        val label = if (ability.xpRule is com.example.vampire_system.data.model.XpRule.PerUnit) (ability.unit ?: "units") else "minutes"
                                        android.app.AlertDialog.Builder(requireContext())
                                            .setTitle("Enter $label")
                                            .setView(input)
                                            .setPositiveButton("OK") { _, _ ->
                                                val amt = input.text?.toString()?.toDoubleOrNull() ?: 0.0
                                                CoroutineScope(Dispatchers.IO).launch { xpEngine.completeQuest(item.id, amt) }
                                            }.setNegativeButton("Cancel", null)
                                            .show()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.IO).launch { xpEngine.revertQuestToPending(item.id) }
                }
            },
            onAttach = { item ->
                com.example.vampire_system.features.evidence.EvidenceBottomSheet
                    .newInstance(item.id)
                    .show(parentFragmentManager, "evidence_sheet")
            },
            onView = { item ->
                val b = android.os.Bundle().apply { putString("qi", item.id) }
                findNavController().navigate(R.id.navigation_gallery, b)
            }
        )
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        vm.quests.observe(viewLifecycleOwner) { items -> adapter.submitList(items) }

        view.findViewById<Button>(R.id.btnGenerate).setOnClickListener {
            // regenerate today
            val db = AppDatabase.get(requireContext())
            val lr = LevelRepo(db)
            val date = Dates.todayLocal()
            CoroutineScope(Dispatchers.IO).launch {
                QuestEngine(db, lr).generateDaily(date)
            }
            Toast.makeText(requireContext(), "Generated/Refreshed", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.btnQuickFoundations).setOnClickListener { v ->
            // Debounce to prevent rapid taps
            v.isEnabled = false
            // pushups, reading, notes quick XP
            vm.quickFoundationXp("pushups")
            vm.quickFoundationXp("reading")
            vm.quickFoundationXp("notes")
            Toast.makeText(requireContext(), "Quick foundations logged", Toast.LENGTH_SHORT).show()
            v.postDelayed({ v.isEnabled = true }, 2000) // 2 second debounce for this bulk action
        }

        // quick permissive permissions request for camera + mic
        val reqPerms = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }
        reqPerms.launch(arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        ))
    }
}