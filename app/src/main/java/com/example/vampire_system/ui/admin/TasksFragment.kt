package com.example.vampire_system.ui.admin

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.LevelTaskEntity
import kotlinx.coroutines.*

class TasksFragment : Fragment() {
    private val io = CoroutineScope(Dispatchers.IO)
    private lateinit var adapter: TaskAdapter
    private var level: Int = 10

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_admin_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val etLevel = view.findViewById<EditText>(R.id.etLevel)
        etLevel.setText(level.toString())
        view.findViewById<Button>(R.id.btnLoad).setOnClickListener {
            level = etLevel.text.toString().toIntOrNull() ?: level
            load()
        }
        view.findViewById<Button>(R.id.btnNew).setOnClickListener { openEdit(null) }

        val rv = view.findViewById<RecyclerView>(R.id.rvTasks)
        adapter = TaskAdapter(
            onEdit = { openEdit(it) },
            onDelete = { deleteTask(it) }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        load()
    }

    private fun load() = io.launch {
        val db = AppDatabase.get(requireContext())
        val items = db.levelTaskDao().forLevelOnce(level)
        withContext(Dispatchers.Main) { adapter.submit(items) }
    }

    private fun deleteTask(item: LevelTaskEntity) = io.launch {
        val db = AppDatabase.get(requireContext())
        db.levelTaskDao().deleteById(item.id)
        refreshToday()
        load()
    }

    private fun openEdit(item: LevelTaskEntity?) {
        val d = Dialog(requireContext())
        d.setContentView(R.layout.dialog_edit_task)
        val etId = d.findViewById<EditText>(R.id.etId)
        val etAb = d.findViewById<EditText>(R.id.etAbilityId)
        val etSpec = d.findViewById<EditText>(R.id.etSpec)
        val etAcc = d.findViewById<EditText>(R.id.etAcceptance)

        if (item != null) {
            etId.setText(item.id); etId.isEnabled = false
            etAb.setText(item.abilityId ?: "")
            etSpec.setText(item.spec)
            etAcc.setText(item.acceptance.joinToString("\n"))
        } else {
            etId.setText("L${level}_${System.currentTimeMillis().toString().takeLast(5)}")
        }

        d.findViewById<Button>(R.id.btnSave).setOnClickListener {
            io.launch {
                val db = AppDatabase.get(requireContext())
                val entity = LevelTaskEntity(
                    id = etId.text.toString(),
                    levelId = level,
                    abilityId = etAb.text.toString().ifBlank { null },
                    spec = etSpec.text.toString(),
                    acceptance = etAcc.text.toString()
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                )
                db.levelTaskDao().upsert(entity)
                refreshToday()
                withContext(Dispatchers.Main) { d.dismiss(); load() }
            }
        }
        d.show()
    }

    private fun refreshToday() = io.launch {
        val db = AppDatabase.get(requireContext())
        com.example.vampire_system.domain.engine.QuestEngine(db, com.example.vampire_system.domain.repo.LevelRepo(db)).generateDaily(com.example.vampire_system.util.Dates.todayLocal())
    }
}

class TaskAdapter(
    private val onEdit: (LevelTaskEntity)->Unit,
    private val onDelete: (LevelTaskEntity)->Unit
) : RecyclerView.Adapter<TaskVH>() {
    private val items = mutableListOf<LevelTaskEntity>()
    fun submit(list: List<LevelTaskEntity>) { items.clear(); items.addAll(list); notifyDataSetChanged() }
    override fun onCreateViewHolder(p: ViewGroup, v: Int): TaskVH {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_task_admin, p, false)
        return TaskVH(view)
    }
    override fun onBindViewHolder(h: TaskVH, pos: Int) = h.bind(items[pos], onEdit, onDelete)
    override fun getItemCount() = items.size
}
class TaskVH(v: View) : RecyclerView.ViewHolder(v) {
    private val tvId = v.findViewById<TextView>(R.id.tvId)
    private val tvSpec = v.findViewById<TextView>(R.id.tvSpec)
    private val tvAcc = v.findViewById<TextView>(R.id.tvAcc)
    private val btnEdit = v.findViewById<Button>(R.id.btnEdit)
    private val btnDelete = v.findViewById<Button>(R.id.btnDelete)
    fun bind(t: LevelTaskEntity, onEdit: (LevelTaskEntity)->Unit, onDelete: (LevelTaskEntity)->Unit) {
        tvId.text = "${t.id} (L${t.levelId}) ${t.abilityId ?: ""}"
        tvSpec.text = t.spec
        tvAcc.text = "Acceptance:\n• " + t.acceptance.joinToString("\n• ")
        btnEdit.setOnClickListener { onEdit(t) }
        btnDelete.setOnClickListener { onDelete(t) }
    }
}


