package com.example.vampire_system.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.features.backup.IncrementalBackupWorker
import com.example.vampire_system.features.today.RolloverWorker
import com.example.vampire_system.util.Dates
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lbl = view.findViewById<TextView>(R.id.lblLastSummary)
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.get(requireContext())
            val s = db.dayDao().byDate(Dates.todayLocal()) ?: db.dayDao().byDate(
                java.time.LocalDate.parse(Dates.todayLocal()).minusDays(1).toString()
            )
            val text = if (s == null) "No summary yet"
            else "Last Summary: ${s.xpNet} XP (Level ${s.levelId})"
            CoroutineScope(Dispatchers.Main).launch { lbl.text = text }
        }

        view.findViewById<Button>(R.id.btnRollover).setOnClickListener {
            WorkManager.getInstance(requireContext())
                .enqueue(OneTimeWorkRequestBuilder<RolloverWorker>().build())
        }
        view.findViewById<Button>(R.id.btnIncBackup).setOnClickListener {
            WorkManager.getInstance(requireContext())
                .enqueue(OneTimeWorkRequestBuilder<IncrementalBackupWorker>().build())
        }

        view.findViewById<Button>(R.id.btnOpenSettings).setOnClickListener {
            findNavController().navigate(R.id.navigation_settings)
        }
        view.findViewById<Button>(R.id.btnOpenAdmin).setOnClickListener {
            findNavController().navigate(R.id.navigation_admin)
        }

        // Share Today/Yesterday
        if (view is android.widget.LinearLayout) {
            val btnShareToday = Button(requireContext()).apply { text = "Share Today (PDF)" }
            view.addView(btnShareToday)
            btnShareToday.setOnClickListener {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    val date = com.example.vampire_system.util.Dates.todayLocal()
                    val file = com.example.vampire_system.features.export.DayExporter.exportPdfCached(requireContext(), date)
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        com.example.vampire_system.util.Share.shareFile(requireContext(), file, "application/pdf")
                    }
                }
            }

            val btnShareY = Button(requireContext()).apply { text = "Share Yesterday (PNG)" }
            view.addView(btnShareY)
            btnShareY.setOnClickListener {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    val y = java.time.LocalDate.parse(com.example.vampire_system.util.Dates.todayLocal()).minusDays(1).toString()
                    val file = com.example.vampire_system.features.export.DayExporter.exportPngCached(requireContext(), y)
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        com.example.vampire_system.util.Share.shareFile(requireContext(), file, "image/png")
                    }
                }
            }
        }
    }
}