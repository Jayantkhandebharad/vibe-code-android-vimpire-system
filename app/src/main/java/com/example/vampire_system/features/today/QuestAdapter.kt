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
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.QuestInstanceEntity
import com.example.vampire_system.data.model.QuestStatus
import com.example.vampire_system.data.model.AbilityGroup
import com.example.vampire_system.data.model.TaskCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        private val details: TextView = itemView.findViewById(R.id.questDetails)
        private val accentBar: View = itemView.findViewById(R.id.questAccentBar)
        private val btn: Button = itemView.findViewById(R.id.questBtn)
        private val attach: Button = itemView.findViewById(R.id.btnAttach)
        private val viewBtn: Button = itemView.findViewById(R.id.btnView)
        
        fun bind(item: QuestInstanceEntity, onToggle: (QuestInstanceEntity) -> Unit,
                 onAttach: (QuestInstanceEntity) -> Unit, onView: (QuestInstanceEntity) -> Unit) {
            val name = item.abilityId ?: item.templateId ?: "Quest"
            
            // Add quest type icon to title
            val questTypeIcon = if (item.templateId != null) "📋" else "⚡"
            title.text = "$questTypeIcon $name"
            
            subtitle.text = if (item.status == QuestStatus.DONE) "DONE • +${item.xpAwarded} XP" else "PENDING"
            btn.text = if (item.status == QuestStatus.DONE) "Mark Pending" else "Mark Done"
            
            // Get quest details asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                val detailsText = getQuestDetails(item)
                val accentColor = getAccentColor(item)
                withContext(Dispatchers.Main) {
                    details.text = detailsText
                    accentBar.setBackgroundColor(accentColor)
                }
            }
            
            // Debounced click listener to prevent rapid taps
            btn.setOnClickListener { v ->
                v.isEnabled = false
                onToggle(item)
                v.postDelayed({ v.isEnabled = true }, 1000) // 1 second debounce
            }
            
            attach.setOnClickListener { onAttach(item) }
            viewBtn.setOnClickListener { onView(item) }
        }
        
        private suspend fun getQuestDetails(item: QuestInstanceEntity): String {
            val db = AppDatabase.get(itemView.context)
            val details = mutableListOf<String>()
            
            // Add quest level information
            details.add("Level ${item.levelId}")
            
            if (item.templateId != null) {
                // This is a level task quest
                val task = db.levelTaskDao().byId(item.templateId)
                if (task != null) {
                    details.add("Category: ${getCategoryDisplayName(task.category)}")
                    if (item.status == QuestStatus.PENDING) {
                        details.add("Potential XP: ${task.xpReward.toInt()}")
                    }
                    if (task.abilityId != null) {
                        val ability = db.abilityDao().byId(task.abilityId)
                        if (ability != null) {
                            details.add("Ability: ${ability.name}")
                        }
                    }
                }
            } else if (item.abilityId != null) {
                // This is an ability quest
                val ability = db.abilityDao().byId(item.abilityId)
                if (ability != null) {
                    details.add("Category: ${getCategoryFromAbilityGroup(ability.group)}")
                    if (item.status == QuestStatus.PENDING) {
                        val xpInfo = getXpInfo(ability)
                        details.add("Potential XP: $xpInfo")
                    }
                    if (ability.unit != null) {
                        details.add("Unit: ${ability.unit}")
                    }
                    if (ability.dailyCapXp != null) {
                        details.add("Daily Cap: ${ability.dailyCapXp} XP")
                    }
                }
            }
            
            return if (details.isNotEmpty()) details.joinToString(" • ") else "Loading details..."
        }
        
        private fun getCategoryFromAbilityGroup(group: AbilityGroup): String {
            return when (group) {
                AbilityGroup.FOUNDATIONS -> "Core"
                AbilityGroup.FITNESS -> "Strength"
                AbilityGroup.GENAI -> "Knowledge"
                AbilityGroup.COMMUNICATION -> "Communication"
                AbilityGroup.NUTRITION -> "Lifestyle"
            }
        }
        
        private fun getXpInfo(ability: com.example.vampire_system.data.db.AbilityEntity): String {
            return when (val rule = ability.xpRule) {
                is com.example.vampire_system.data.model.XpRule.Flat -> "${rule.xp} XP"
                is com.example.vampire_system.data.model.XpRule.PerUnit -> "${rule.xpPerUnit} XP per ${ability.unit ?: "unit"}"
                is com.example.vampire_system.data.model.XpRule.PerMinutes -> "${rule.xpPer10Min} XP per 10 min"
            }
        }
        
        private fun getCategoryDisplayName(category: TaskCategory): String {
            return when (category) {
                TaskCategory.COMMUNICATION -> "Communication"
                TaskCategory.STRENGTH -> "Strength"
                TaskCategory.KNOWLEDGE -> "Knowledge"
                TaskCategory.SOCIAL -> "Social"
                TaskCategory.LIFESTYLE -> "Lifestyle"
            }
        }
        
        private suspend fun getAccentColor(item: QuestInstanceEntity): Int {
            val db = AppDatabase.get(itemView.context)
            
            if (item.templateId != null) {
                // Level task quest
                val task = db.levelTaskDao().byId(item.templateId)
                if (task != null) {
                    return getCategoryColor(task.category)
                }
            } else if (item.abilityId != null) {
                // Ability quest
                val ability = db.abilityDao().byId(item.abilityId)
                if (ability != null) {
                    return getCategoryColorFromGroup(ability.group)
                }
            }
            
            return 0xFF4CAF50.toInt() // Default green
        }
        
        private fun getCategoryColor(category: TaskCategory): Int {
            return when (category) {
                TaskCategory.COMMUNICATION -> 0xFF2196F3.toInt() // Blue
                TaskCategory.STRENGTH -> 0xFFF44336.toInt() // Red
                TaskCategory.KNOWLEDGE -> 0xFF9C27B0.toInt() // Purple
                TaskCategory.SOCIAL -> 0xFFFF9800.toInt() // Orange
                TaskCategory.LIFESTYLE -> 0xFF4CAF50.toInt() // Green
            }
        }
        
        private fun getCategoryColorFromGroup(group: AbilityGroup): Int {
            return when (group) {
                AbilityGroup.FOUNDATIONS -> 0xFF607D8B.toInt() // Blue Grey
                AbilityGroup.FITNESS -> 0xFFF44336.toInt() // Red
                AbilityGroup.GENAI -> 0xFF9C27B0.toInt() // Purple
                AbilityGroup.COMMUNICATION -> 0xFF2196F3.toInt() // Blue
                AbilityGroup.NUTRITION -> 0xFF4CAF50.toInt() // Green
            }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<QuestInstanceEntity>() {
            override fun areItemsTheSame(old: QuestInstanceEntity, new: QuestInstanceEntity) = old.id == new.id
            override fun areContentsTheSame(old: QuestInstanceEntity, new: QuestInstanceEntity) = old == new
        }
    }
}


