package com.example.vampire_system.ui.roadmap

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.domain.engine.QuestEngine
import com.example.vampire_system.domain.repo.LevelRepo
import com.example.vampire_system.util.Dates
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*

class LevelDetailSheet : BottomSheetDialogFragment() {
    companion object {
        fun new(level: Int) = LevelDetailSheet().apply {
            arguments = Bundle().apply { putInt("level", level) }
        }
    }

    private val io = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val ui = CoroutineScope(Dispatchers.Main)
    private val level by lazy { arguments?.getInt("level") ?: 1 }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.sheet_level_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lblTitle = view.findViewById<TextView>(R.id.lblTitle) ?: return
        val lblXp = view.findViewById<TextView>(R.id.lblXp) ?: return
        val lblUnlocks = view.findViewById<TextView>(R.id.lblUnlocks) ?: return
        val lblTasks = view.findViewById<TextView>(R.id.lblTasks) ?: return
        val btnGoToday = view.findViewById<Button>(R.id.btnGoToday) ?: return
        val btnEdit = view.findViewById<Button>(R.id.btnEditLevelTasks) ?: return

        io.launch {
            val ctx = requireContext()
            val db = AppDatabase.get(ctx)
            val lr = LevelRepo(db)
            val isCurrent = lr.getCurrent().levelId == level
            val abilities = db.abilityDao().getAllOnce().filter { it.unlockLevel == level }
            val tasks = db.levelTaskDao().forLevelOnce(level)
            val milestone = db.levelMilestoneDao().byLevel(level)
            val xp = com.example.vampire_system.data.model.Xp.xpForLevel(level)

            ui.launch {
                val title = buildString {
                    append("Level $level")
                    milestone?.let { append(" — "); append(it.title) }
                    if (isCurrent) append(" • CURRENT")
                }
                lblTitle.text = title
                lblXp.text = "XP needed: $xp"
                lblUnlocks.text = "Unlocks: " + (if (abilities.isEmpty()) "-" else abilities.joinToString { it.name })
                lblTasks.text = "Tasks on this level: ${tasks.size}"
            }
        }

        btnGoToday.setOnClickListener {
            io.launch {
                val ctx = requireContext()
                val db = AppDatabase.get(ctx)
                QuestEngine(db, LevelRepo(db)).generateDaily(Dates.todayLocal())
                ui.launch { android.widget.Toast.makeText(requireContext(), "Today regenerated", android.widget.Toast.LENGTH_SHORT).show(); dismiss() }
            }
        }
        btnEdit.setOnClickListener {
            ui.launch {
                android.widget.Toast.makeText(requireContext(), "Open Admin → Level Tasks to edit", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        io.cancel()
        ui.coroutineContext.cancelChildren()
    }
}


