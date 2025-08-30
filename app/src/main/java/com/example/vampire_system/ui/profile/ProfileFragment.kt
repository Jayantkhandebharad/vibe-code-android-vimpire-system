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
import kotlinx.coroutines.*

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
        val tvPen = view.findViewById<TextView>(R.id.tvPenalties)

        scope.launch {
            try {
                val db = AppDatabase.get(requireContext())
                val prof = db.profileDao().get()
                val set = db.settingsDao().get()
                withContext(Dispatchers.Main) {
                    tvW.text = "Weight: ${prof?.weightKg ?: "-"} kg"
                    val proteinTarget = prof?.weightKg?.let { w -> (w * (prof.proteinGPerKg)) }?.toInt()
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

                var fit = 0; var gen = 0; var comm = 0
                var penCount = 0; var penSum = 0
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
                                        else -> {}
                                    }
                                }
                                AbilityGroup.FITNESS -> fit += e.deltaXp
                                AbilityGroup.GENAI -> gen += e.deltaXp
                                AbilityGroup.COMMUNICATION -> comm += e.deltaXp
                                else -> {}
                            }
                        }
                        LedgerType.PENALTY -> { penCount += 1; penSum += -e.deltaXp }
                        else -> {}
                    }
                }
                withContext(Dispatchers.Main) {
                    meterS.progress = fit.coerceAtMost(meterS.max)
                    meterK.progress = gen.coerceAtMost(meterK.max)
                    meterC.progress = comm.coerceAtMost(meterC.max)
                    tvPen.text = "Penalties (7d): ${penCount} (−${penSum} XP)"
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(requireContext(), "Profile failed: ${'$'}{t.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.btnEdit).setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Edit UI coming in Settings/Admin", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}


