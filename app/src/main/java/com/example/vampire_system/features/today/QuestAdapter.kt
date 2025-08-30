package com.example.vampire_system.features.today

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vampire_system.R
import com.example.vampire_system.data.db.QuestInstanceEntity
import com.example.vampire_system.data.model.QuestStatus

class QuestAdapter(
    private val onToggle: (QuestInstanceEntity) -> Unit,
    private val onAttach: (QuestInstanceEntity) -> Unit,
    private val onView: (QuestInstanceEntity) -> Unit
) : ListAdapter<QuestInstanceEntity, QuestAdapter.VH>(DIFF) {
    
    init { setHasStableIds(true) }
    
    override fun getItemId(position: Int) = getItem(position).id.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_quest, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position), onToggle, onAttach, onView)

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.questTitle)
        private val subtitle: TextView = itemView.findViewById(R.id.questSub)
        private val btn: Button = itemView.findViewById(R.id.questBtn)
        private val attach: Button = itemView.findViewById(R.id.btnAttach)
        private val viewBtn: Button = itemView.findViewById(R.id.btnView)
        fun bind(item: QuestInstanceEntity, onToggle: (QuestInstanceEntity) -> Unit,
                 onAttach: (QuestInstanceEntity) -> Unit, onView: (QuestInstanceEntity) -> Unit) {
            val name = item.abilityId ?: item.templateId ?: "Quest"
            title.text = name
            subtitle.text = if (item.status == QuestStatus.DONE) "DONE • +${item.xpAwarded} XP" else "PENDING"
            btn.text = if (item.status == QuestStatus.DONE) "Mark Pending" else "Mark Done"
            
            // Debounced click listener to prevent rapid taps
            btn.setOnClickListener { v ->
                v.isEnabled = false
                onToggle(item)
                v.postDelayed({ v.isEnabled = true }, 1000) // 1 second debounce
            }
            
            attach.setOnClickListener { onAttach(item) }
            viewBtn.setOnClickListener { onView(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<QuestInstanceEntity>() {
            override fun areItemsTheSame(old: QuestInstanceEntity, new: QuestInstanceEntity) = old.id == new.id
            override fun areContentsTheSame(old: QuestInstanceEntity, new: QuestInstanceEntity) = old == new
        }
    }
}


