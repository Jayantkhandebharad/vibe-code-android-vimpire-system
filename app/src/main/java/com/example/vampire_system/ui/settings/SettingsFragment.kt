package com.example.vampire_system.ui.settings

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.features.today.RolloverWorker
import kotlinx.coroutines.*

class SettingsFragment : Fragment() {
    private val io = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set up back button to navigate to notifications
        view.findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            findNavController().navigate(R.id.navigation_notifications)
        }
        
        val etHour = view.findViewById<EditText>(R.id.etResetHour)
        val etTz = view.findViewById<EditText>(R.id.etTimezone)
        val etW = view.findViewById<EditText>(R.id.etWeight)
        val etP = view.findViewById<EditText>(R.id.etProteinPerKg)

        io.launch {
            val db = AppDatabase.get(requireContext())
            val s = db.settingsDao().get()
            val p = db.profileDao().get()
            withContext(Dispatchers.Main) {
                etHour.setText((s?.resetHour ?: 5).toString())
                etTz.setText(s?.timezone ?: "Asia/Kolkata")
                etW.setText((p?.weightKg ?: "").toString())
                etP.setText((p?.proteinGPerKg ?: 2.0).toString())
            }
        }

        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            io.launch {
                val db = AppDatabase.get(requireContext())
                val hour = etHour.text.toString().toIntOrNull()?.coerceIn(0,23) ?: 5
                val tz = etTz.text.toString().ifBlank { "Asia/Kolkata" }
                val w = etW.text.toString().toDoubleOrNull()
                val gkg = etP.text.toString().toDoubleOrNull() ?: 2.0

                db.settingsDao().upsert(
                    com.example.vampire_system.data.db.SettingsEntity(
                        id = 1, resetHour = hour, timezone = tz,
                        encryptBackups = (db.settingsDao().get()?.encryptBackups ?: true),
                        wifiOnlyBackups = (db.settingsDao().get()?.wifiOnlyBackups ?: false)
                    )
                )
                db.profileDao().upsert(
                    com.example.vampire_system.data.db.UserProfileEntity(
                        id = 1, weightKg = w, proteinGPerKg = gkg
                    )
                )
                withContext(Dispatchers.Main) {
                    RolloverWorker.ensureScheduled(requireContext(), hour, tz)
                    com.example.vampire_system.features.backup.IncrementalBackupWorker.scheduleNext(requireContext(), hour, tz)
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.btnRegenerateToday).setOnClickListener {
            io.launch {
                val db = AppDatabase.get(requireContext())
                com.example.vampire_system.domain.engine.QuestEngine(db, com.example.vampire_system.domain.repo.LevelRepo(db)).generateDaily(com.example.vampire_system.util.Dates.todayLocal())
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Today regenerated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


