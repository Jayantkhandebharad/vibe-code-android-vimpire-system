package com.example.vampire_system.ui.roadmap

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.domain.repo.LevelRepo
import com.example.vampire_system.domain.repo.RoadmapRepo
import kotlinx.coroutines.*

class RoadmapFragment : Fragment() {
    private val io = CoroutineScope(Dispatchers.IO)
    private lateinit var adapter: RoadmapAdapter
    private var currentLevel: Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_roadmap, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        val rv = view.findViewById<RecyclerView>(R.id.rvRoadmap)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = RoadmapAdapter(
            onDetails = { level ->
                val tag = "level_detail_" + level
                if (isAdded && parentFragmentManager.findFragmentByTag(tag) == null) {
                    LevelDetailSheet.new(level).show(parentFragmentManager, tag)
                }
            }
        )
        rv.adapter = adapter

        loadRoadmap()
    }

    override fun onResume() {
        super.onResume()
        // Refresh roadmap when returning to this screen
        loadRoadmap()
    }

    private fun loadRoadmap() {
        io.launch {
            val db = AppDatabase.get(requireContext())
            currentLevel = LevelRepo(db).getCurrent().levelId
            val list = RoadmapRepo(db).build()
            withContext(Dispatchers.Main) { adapter.submit(list, currentLevel) }
        }
    }
}

class RoadmapAdapter(
    private val onDetails: (Int) -> Unit
) : RecyclerView.Adapter<RoadVH>() {
    private val items = mutableListOf<com.example.vampire_system.domain.repo.LevelCard>()
    private var current = 1

    fun submit(list: List<com.example.vampire_system.domain.repo.LevelCard>, currentLevel: Int) {
        items.clear(); items.addAll(list); current = currentLevel; notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): RoadVH {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_level_card, p, false)
        return RoadVH(view)
    }
    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: RoadVH, pos: Int) = h.bind(items[pos], current, onDetails)
}

class RoadVH(v: View) : RecyclerView.ViewHolder(v) {
    private val tvLevel = v.findViewById<TextView>(R.id.tvLevel)
    private val tvXp = v.findViewById<TextView>(R.id.tvXp)
    private val tvUnlocks = v.findViewById<TextView>(R.id.tvUnlocks)
    private val tvTasks = v.findViewById<TextView>(R.id.tvTasks)
    private val tvMilestone = v.findViewById<TextView>(R.id.tvMilestone)
    private val btn = v.findViewById<Button>(R.id.btnDetails)
    fun bind(c: com.example.vampire_system.domain.repo.LevelCard, current: Int, onDetails: (Int)->Unit) {
        // Show level with XP requirement prominently
        tvLevel.text = if (c.level == current) "L${c.level} • CURRENT" else "L${c.level}"
        
        // Show XP requirement clearly - this should now show the new progression
        tvXp.text = if (c.level == current && c.currentXp != null) {
            "XP: ${c.currentXp} / ${c.xpNeeded} (cum ${c.cumulativeXp})"
        } else {
            "XP: ${c.xpNeeded} (cum ${c.cumulativeXp})"
        }
        
        tvUnlocks.text = "Unlocks: " + (if (c.unlockedAbilities.isEmpty()) "-" else c.unlockedAbilities.joinToString(", "))
        
        // Show completed vs total tasks
        tvTasks.text = "Tasks: ${c.completedTasks} / ${c.taskCount} completed"
        if (c.milestone != null) {
            tvMilestone.visibility = View.VISIBLE
            tvMilestone.text = (if (c.milestone.isBoss) "BOSS • " else "Milestone • ") +
                c.milestone.title + (c.milestone.subtitle?.let { " — $it" } ?: "")
            c.milestone.colorHex?.let {
                try { tvMilestone.setTextColor(android.graphics.Color.parseColor(it)) } catch (_: Exception) {}
            }
        } else {
            tvMilestone.visibility = View.GONE
        }
        btn.setOnClickListener {
            it.isEnabled = false
            onDetails(c.level)
            it.postDelayed({ it.isEnabled = true }, 400)
        }
    }
}


