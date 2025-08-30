package com.example.vampire_system.ui.dashboard

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase

class DashboardFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = com.example.vampire_system.ui.common.fadeThrough()
        returnTransition = com.example.vampire_system.ui.common.fadeThrough()
    }

    private val vm: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(AppDatabase.get(requireContext()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lblLevel = view.findViewById<TextView>(R.id.lblLevel)
        val xpBar = view.findViewById<ProgressBar>(R.id.xpBar)
        val xpText = view.findViewById<TextView>(R.id.xpText)
        val counts = view.findViewById<TextView>(R.id.counts)
        view.findViewById<Button>(R.id.btnRefresh).setOnClickListener { vm.refresh() }
        view.findViewById<Button>(R.id.btnRoadmap).setOnClickListener {
            findNavController().navigate(R.id.navigation_roadmap)
        }

        vm.state.observe(viewLifecycleOwner) { s ->
            lblLevel.text = "Level ${s.level}"
            xpBar.max = s.xpRequired
            xpBar.progress = s.xpInLevel.coerceAtMost(s.xpRequired)
            xpText.text = "${s.xpInLevel} / ${s.xpRequired} XP"
            counts.text = "${s.pendingQuests} pending • ${s.doneQuests} done"
        }
    }
}