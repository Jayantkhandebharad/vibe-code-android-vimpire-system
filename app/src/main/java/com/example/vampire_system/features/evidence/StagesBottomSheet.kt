package com.example.vampire_system.features.evidence

import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.LinearLayout
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*

class StagesBottomSheet : BottomSheetDialogFragment() {
    companion object {
        fun newInstance(qi: String) = StagesBottomSheet().apply {
            arguments = Bundle().apply { putString("qi", qi) }
        }
    }
    private val qi by lazy { requireArguments().getString("qi")!! }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.sheet_stages, container, false)
        val list = v.findViewById<LinearLayout>(R.id.stageList)
        scope.launch {
            val db = AppDatabase.get(requireContext())
            val items = db.stageDao().forQuest(qi)
            withContext(Dispatchers.Main) {
                list.removeAllViews()
                items.forEach { s ->
                    val cb = CheckBox(requireContext())
                    cb.text = s.label
                    cb.isChecked = s.done
                    cb.setOnCheckedChangeListener { _, isChecked ->
                        scope.launch { db.stageDao().setDone(s.id, isChecked, if (isChecked) System.currentTimeMillis() else null) }
                    }
                    list.addView(cb)
                }
            }
        }
        return v
    }
}


