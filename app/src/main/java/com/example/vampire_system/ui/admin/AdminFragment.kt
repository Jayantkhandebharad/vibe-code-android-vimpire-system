package com.example.vampire_system.ui.admin

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.vampire_system.R

class AdminFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.beginTransaction()
            .replace(R.id.adminContent, AbilitiesFragment())
            .commitNow()

        view.findViewById<Button>(R.id.btnTabAbilities).setOnClickListener {
            childFragmentManager.beginTransaction()
                .replace(R.id.adminContent, AbilitiesFragment())
                .commit()
        }
        view.findViewById<Button>(R.id.btnTabTasks).setOnClickListener {
            childFragmentManager.beginTransaction()
                .replace(R.id.adminContent, TasksFragment())
                .commit()
        }
    }
}


