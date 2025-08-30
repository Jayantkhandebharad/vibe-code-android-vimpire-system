package com.example.vampire_system.ui.admin

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AbilityEntity
import com.example.vampire_system.data.db.AppDatabase
import kotlinx.coroutines.*

class AbilitiesFragment : Fragment() {
    private val io = CoroutineScope(Dispatchers.IO)
    private lateinit var adapter: AbilityAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_admin_abilities, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvAbilities)
        adapter = AbilityAdapter { openEdit(it) }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        load()
    }
    private fun load() = io.launch {
        val db = AppDatabase.get(requireContext())
        val items = db.abilityDao().getAllOnce().sortedBy { it.unlockLevel }
        withContext(Dispatchers.Main) { adapter.submit(items) }
    }
    private fun openEdit(item: AbilityEntity) {
        val d = Dialog(requireContext())
        d.setContentView(R.layout.dialog_edit_ability)
        val etName = d.findViewById<EditText>(R.id.etName)
        val etUnit = d.findViewById<EditText>(R.id.etUnit)
        val etCap = d.findViewById<EditText>(R.id.etCap)
        val etUnlock = d.findViewById<EditText>(R.id.etUnlock)
        etName.setText(item.name)
        etUnit.setText(item.unit ?: "")
        etCap.setText(item.dailyCapXp?.toString() ?: "")
        etUnlock.setText(item.unlockLevel.toString())
        d.findViewById<Button>(R.id.btnSave).setOnClickListener {
            io.launch {
                val db = AppDatabase.get(requireContext())
                db.abilityDao().upsert(
                    item.copy(
                        name = etName.text.toString().ifBlank { item.name },
                        unit = etUnit.text.toString().ifBlank { null },
                        dailyCapXp = etCap.text.toString().toIntOrNull(),
                        unlockLevel = etUnlock.text.toString().toIntOrNull() ?: item.unlockLevel
                    )
                )
                withContext(Dispatchers.Main) { d.dismiss(); load() }
            }
        }
        d.show()
    }
}

class AbilityAdapter(private val onEdit: (AbilityEntity)->Unit)
    : RecyclerView.Adapter<AbilityVH>() {
    private val items = mutableListOf<AbilityEntity>()
    fun submit(list: List<AbilityEntity>) { items.clear(); items.addAll(list); notifyDataSetChanged() }
    override fun onCreateViewHolder(p: ViewGroup, v: Int): AbilityVH {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_ability_admin, p, false)
        return AbilityVH(view)
    }
    override fun onBindViewHolder(h: AbilityVH, pos: Int) = h.bind(items[pos], onEdit)
    override fun getItemCount() = items.size
}
class AbilityVH(v: View) : RecyclerView.ViewHolder(v) {
    private val tvName = v.findViewById<TextView>(R.id.tvName)
    private val tvMeta = v.findViewById<TextView>(R.id.tvMeta)
    private val btn = v.findViewById<Button>(R.id.btnEdit)
    fun bind(a: AbilityEntity, onEdit: (AbilityEntity)->Unit) {
        tvName.text = "${a.name} (${a.id})"
        tvMeta.text = "unit=${a.unit ?: "-"} • cap=${a.dailyCapXp ?: "-"} • unlock L${a.unlockLevel}"
        btn.setOnClickListener { onEdit(a) }
    }
}


