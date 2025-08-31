package com.example.vampire_system.ui.admin

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AbilityEntity
import com.example.vampire_system.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AbilitiesFragment : Fragment() {
    private var _adapter: AbilityAdapter? = null
    private val adapter get() = _adapter!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        try {
            android.util.Log.d("AbilitiesFragment", "Attempting to inflate fragment_admin_abilities layout")
        return inflater.inflate(R.layout.fragment_admin_abilities, container, false)
        } catch (e: Exception) {
            android.util.Log.e("AbilitiesFragment", "Error inflating layout", e)
            
            // Check if it's a theme-related error
            if (e.message?.contains("theme") == true || e.message?.contains("InheritanceMap") == true) {
                android.util.Log.e("AbilitiesFragment", "Theme-related layout inflation error detected")
                // Try to create a very simple layout without theme dependencies
                return createFallbackLayout()
            }
            
            // Return a simple TextView as fallback
            return android.widget.TextView(requireContext()).apply {
                text = "Error loading abilities layout: ${e.message}"
                textSize = 16f
                setPadding(32, 32, 32, 32)
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.RED)
            }
        }
    }
    
    private fun createFallbackLayout(): View {
        try {
            android.util.Log.d("AbilitiesFragment", "Creating fallback layout")
            
            // Create a simple LinearLayout programmatically
            val layout = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
                gravity = android.view.Gravity.CENTER
            }
            
            // Add a title
            val title = android.widget.TextView(requireContext()).apply {
                text = "Abilities (Fallback Mode)"
                textSize = 18f
                setTextColor(android.graphics.Color.BLUE)
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 32)
            }
            layout.addView(title)
            
            // Add a message about fallback mode
            val message = android.widget.TextView(requireContext()).apply {
                text = "Layout file had theme issues.\nUsing fallback layout.\n\nAbilities will be displayed here when loaded."
                textSize = 14f
                setTextColor(android.graphics.Color.rgb(128, 128, 128))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 0)
            }
            layout.addView(message)
            
            return layout
        } catch (e: Exception) {
            android.util.Log.e("AbilitiesFragment", "Error creating fallback layout", e)
            // Ultimate fallback - just a TextView
            return android.widget.TextView(requireContext()).apply {
                text = "Critical error: Could not create any layout.\n\nError: ${e.message}"
                textSize = 14f
                setPadding(32, 32, 32, 32)
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.RED)
            }
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            android.util.Log.d("AbilitiesFragment", "onViewCreated started")
            
        val rv = view.findViewById<RecyclerView>(R.id.rvAbilities)
            if (rv != null) {
                android.util.Log.d("AbilitiesFragment", "RecyclerView found, setting up adapter")
                _adapter = AbilityAdapter { openEdit(it) }
        rv.layoutManager = LinearLayoutManager(requireContext())
                rv.adapter = _adapter
                
                // Load data safely
                loadSafely()
            } else {
                android.util.Log.e("AbilitiesFragment", "RecyclerView not found")
                showError("RecyclerView not found in layout")
            }
        } catch (e: Exception) {
            android.util.Log.e("AbilitiesFragment", "Error in onViewCreated", e)
            showError("Error setting up abilities: ${e.message}")
        }
    }
    
    private fun showError(message: String) {
        try {
            val errorText = android.widget.TextView(requireContext()).apply {
                text = "Error: $message"
                textSize = 16f
                setPadding(32, 32, 32, 32)
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.RED)
            }
            
            // Try to add error text to the view
            view?.let { rootView ->
                if (rootView is android.view.ViewGroup) {
                    rootView.removeAllViews()
                    rootView.addView(errorText)
                }
            }
            
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            android.util.Log.e("AbilitiesFragment", "Error showing error message", e)
        }
    }
    private fun loadSafely() {
        try {
            android.util.Log.d("AbilitiesFragment", "Starting to load abilities safely")
            
            // Use lifecycle-aware coroutine scope
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    android.util.Log.d("AbilitiesFragment", "Accessing database")
        val db = AppDatabase.get(requireContext())
        val items = db.abilityDao().getAllOnce().sortedBy { it.unlockLevel }
                    
                    android.util.Log.d("AbilitiesFragment", "Found ${items.size} abilities")
                    
                    withContext(Dispatchers.Main) { 
                        try {
                            if (_adapter != null && isAdded) {
                                _adapter!!.submit(items)
                                android.util.Log.d("AbilitiesFragment", "Abilities loaded successfully")
                            } else {
                                android.util.Log.w("AbilitiesFragment", "Adapter is null or fragment not attached")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("AbilitiesFragment", "Error submitting items to adapter", e)
                            showError("Error displaying abilities: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AbilitiesFragment", "Error loading abilities from database", e)
                    withContext(Dispatchers.Main) {
                        showError("Error loading abilities: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AbilitiesFragment", "Error starting load coroutine", e)
            showError("Error starting data load: ${e.message}")
        }
    }
    private fun openEdit(item: AbilityEntity) {
        try {
            android.util.Log.d("AbilitiesFragment", "Opening edit dialog for ability: ${item.name}")
            
        val d = Dialog(requireContext())
            // Try to inflate the dialog layout, with fallback if it fails
            try {
        d.setContentView(R.layout.dialog_edit_ability)
            } catch (e: Exception) {
                android.util.Log.e("AbilitiesFragment", "Error inflating dialog layout, using fallback", e)
                                 // Create a fallback dialog layout programmatically
                 d.setContentView(createFallbackDialogLayout(item, d))
                 d.show()
                 return
            }
            
        val etName = d.findViewById<EditText>(R.id.etName)
        val etUnit = d.findViewById<EditText>(R.id.etUnit)
        val etCap = d.findViewById<EditText>(R.id.etCap)
        val etUnlock = d.findViewById<EditText>(R.id.etUnlock)
            
            if (etName != null && etUnit != null && etCap != null && etUnlock != null) {
        etName.setText(item.name)
        etUnit.setText(item.unit ?: "")
        etCap.setText(item.dailyCapXp?.toString() ?: "")
        etUnlock.setText(item.unlockLevel.toString())
                
                val btnSave = d.findViewById<Button>(R.id.btnSave)
                if (btnSave != null) {
                    btnSave.setOnClickListener {
                        try {
                            // Use lifecycle-aware coroutine scope
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                try {
                val db = AppDatabase.get(requireContext())
                                    val updatedItem = item.copy(
                        name = etName.text.toString().ifBlank { item.name },
                        unit = etUnit.text.toString().ifBlank { null },
                        dailyCapXp = etCap.text.toString().toIntOrNull(),
                        unlockLevel = etUnlock.text.toString().toIntOrNull() ?: item.unlockLevel
                    )
                                    
                                    db.abilityDao().upsert(updatedItem)
                                    
                                    withContext(Dispatchers.Main) { 
                                        d.dismiss()
                                        if (isAdded) {
                                            loadSafely()
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("AbilitiesFragment", "Error saving ability", e)
                                    withContext(Dispatchers.Main) {
                                        android.widget.Toast.makeText(requireContext(), "Error saving ability: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("AbilitiesFragment", "Error setting up save button", e)
                            android.widget.Toast.makeText(requireContext(), "Error setting up save: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    android.util.Log.e("AbilitiesFragment", "Save button not found in dialog")
                }
            } else {
                android.util.Log.e("AbilitiesFragment", "Some dialog fields not found")
            }
            
            d.show()
        } catch (e: Exception) {
            android.util.Log.e("AbilitiesFragment", "Error opening edit dialog", e)
            android.widget.Toast.makeText(requireContext(), "Error opening edit dialog: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
         private fun createFallbackDialogLayout(item: AbilityEntity, dialog: Dialog): View {
        try {
            android.util.Log.d("AbilitiesFragment", "Creating fallback dialog layout")
            
            // Create a simple but functional dialog layout programmatically
            val layout = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            
            // Add title
            val title = android.widget.TextView(requireContext()).apply {
                text = "Edit Ability: ${item.name}"
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.BLACK)
                setPadding(0, 0, 0, 16)
            }
            layout.addView(title)
            
                         // Add name field
             val nameLabel = android.widget.TextView(requireContext()).apply {
                 text = "Name:"
                 textSize = 16f
                 setTypeface(null, android.graphics.Typeface.BOLD)
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(0, 0, 0, 8)
             }
            layout.addView(nameLabel)
            
                         val nameInput = android.widget.EditText(requireContext()).apply {
                 id = R.id.etName
                 setText(item.name)
                 textSize = 16f
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(16, 12, 16, 12)
                 setBackgroundColor(android.graphics.Color.WHITE)
                 setHintTextColor(android.graphics.Color.rgb(128, 128, 128))
                 layoutParams = android.widget.LinearLayout.LayoutParams(
                     android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                     android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                 )
             }
            layout.addView(nameInput)
            
                         // Add unit field
             val unitLabel = android.widget.TextView(requireContext()).apply {
                 text = "Unit (e.g., rep, 10min, page):"
                 textSize = 16f
                 setTypeface(null, android.graphics.Typeface.BOLD)
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(0, 16, 0, 8)
             }
            layout.addView(unitLabel)
            
                         val unitInput = android.widget.EditText(requireContext()).apply {
                 id = R.id.etUnit
                 setText(item.unit ?: "")
                 textSize = 16f
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(16, 12, 16, 12)
                 setBackgroundColor(android.graphics.Color.WHITE)
                 setHintTextColor(android.graphics.Color.rgb(128, 128, 128))
                 layoutParams = android.widget.LinearLayout.LayoutParams(
                     android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                     android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                 )
             }
            layout.addView(unitInput)
            
                         // Add daily cap field
             val capLabel = android.widget.TextView(requireContext()).apply {
                 text = "Daily Cap XP (blank = none):"
                 textSize = 16f
                 setTypeface(null, android.graphics.Typeface.BOLD)
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(0, 16, 0, 8)
             }
            layout.addView(capLabel)
            
                         val capInput = android.widget.EditText(requireContext()).apply {
                 id = R.id.etCap
                 setText(item.dailyCapXp?.toString() ?: "")
                 inputType = android.text.InputType.TYPE_CLASS_NUMBER
                 textSize = 16f
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(16, 12, 16, 12)
                 setBackgroundColor(android.graphics.Color.WHITE)
                 setHintTextColor(android.graphics.Color.rgb(128, 128, 128))
                 layoutParams = android.widget.LinearLayout.LayoutParams(
                     android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                     android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                 )
             }
            layout.addView(capInput)
            
                         // Add unlock level field
             val unlockLabel = android.widget.TextView(requireContext()).apply {
                 text = "Unlock Level:"
                 textSize = 16f
                 setTypeface(null, android.graphics.Typeface.BOLD)
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(0, 16, 0, 8)
             }
            layout.addView(unlockLabel)
            
                         val unlockInput = android.widget.EditText(requireContext()).apply {
                 id = R.id.etUnlock
                 setText(item.unlockLevel.toString())
                 inputType = android.text.InputType.TYPE_CLASS_NUMBER
                 textSize = 16f
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(16, 12, 16, 12)
                 setBackgroundColor(android.graphics.Color.WHITE)
                 setHintTextColor(android.graphics.Color.rgb(128, 128, 128))
                 layoutParams = android.widget.LinearLayout.LayoutParams(
                     android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                     android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                 )
             }
            layout.addView(unlockInput)
            
                         // Add save button
             val saveButton = android.widget.Button(requireContext()).apply {
                 id = R.id.btnSave
                 text = "Save"
                 textSize = 16f
                 setPadding(24, 12, 24, 12)
                 setBackgroundColor(android.graphics.Color.rgb(0, 122, 255))
                 setTextColor(android.graphics.Color.WHITE)
                 layoutParams = android.widget.LinearLayout.LayoutParams(
                     android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                     android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                 ).apply {
                     topMargin = 24
                 }
                 
                 // Set up click listener for the save button
                 setOnClickListener {
                     try {
                         // Use lifecycle-aware coroutine scope
                         viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                             try {
                                 val db = AppDatabase.get(requireContext())
                                 val updatedItem = item.copy(
                                     name = nameInput.text.toString().ifBlank { item.name },
                                     unit = unitInput.text.toString().ifBlank { null },
                                     dailyCapXp = capInput.text.toString().toIntOrNull(),
                                     unlockLevel = unlockInput.text.toString().toIntOrNull() ?: item.unlockLevel
                                 )
                                 
                                 db.abilityDao().upsert(updatedItem)
                                 
                                 withContext(Dispatchers.Main) { 
                                     dialog.dismiss()
                                     if (isAdded) {
                                         loadSafely()
                                     }
                                 }
                             } catch (e: Exception) {
                                 android.util.Log.e("AbilitiesFragment", "Error saving ability in fallback dialog", e)
                                 withContext(Dispatchers.Main) {
                                     android.widget.Toast.makeText(requireContext(), "Error saving ability: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                 }
                             }
                         }
                     } catch (e: Exception) {
                         android.util.Log.e("AbilitiesFragment", "Error setting up save button in fallback dialog", e)
                         android.widget.Toast.makeText(requireContext(), "Error setting up save: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                     }
                 }
             }
             layout.addView(saveButton)
            
            return layout
        } catch (e: Exception) {
            android.util.Log.e("AbilitiesFragment", "Error creating fallback dialog layout", e)
            // Ultimate fallback - just a simple message
            return android.widget.TextView(requireContext()).apply {
                text = "Error creating edit dialog.\n\nPlease try again or contact support."
                textSize = 16f
                setPadding(32, 32, 32, 32)
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.RED)
            }
        }
    }
}

class AbilityAdapter(private val onEdit: (AbilityEntity)->Unit)
    : RecyclerView.Adapter<AbilityVH>() {
    private val items = mutableListOf<AbilityEntity>()
    
    fun submit(list: List<AbilityEntity>) { 
        try {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        } catch (e: Exception) {
            android.util.Log.e("AbilityAdapter", "Error submitting items", e)
        }
    }
    
    override fun onCreateViewHolder(p: ViewGroup, v: Int): AbilityVH {
        try {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_ability_admin, p, false)
        return AbilityVH(view)
        } catch (e: Exception) {
            android.util.Log.e("AbilityAdapter", "Error creating view holder", e)
            // Create a functional fallback view holder with proper layout
            return createFallbackViewHolder(p.context)
        }
    }
    
    private fun createFallbackViewHolder(context: android.content.Context): AbilityVH {
        try {
            // Create a simple but functional layout programmatically
            val layout = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            
                         // Add name TextView
             val nameText = android.widget.TextView(context).apply {
                 id = R.id.tvName
                 setTypeface(null, android.graphics.Typeface.BOLD)
                 textSize = 16f
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(0, 0, 0, 8)
             }
            layout.addView(nameText)
            
            // Add meta TextView
            val metaText = android.widget.TextView(context).apply {
                id = R.id.tvMeta
                textSize = 14f
                setTextColor(android.graphics.Color.rgb(64, 64, 64))
                setPadding(0, 0, 0, 8)
            }
            layout.addView(metaText)
            
            // Add edit button
            val editButton = android.widget.Button(context).apply {
                id = R.id.btnEdit
                text = "Edit"
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            layout.addView(editButton)
            
            return AbilityVH(layout)
        } catch (e: Exception) {
            android.util.Log.e("AbilityAdapter", "Error creating fallback view holder", e)
            // Ultimate fallback - just a TextView
            val fallbackView = android.widget.TextView(context).apply {
                text = "Error loading item"
                setTextColor(android.graphics.Color.RED)
                setPadding(16, 16, 16, 16)
            }
            return AbilityVH(fallbackView)
        }
    }
    
    override fun onBindViewHolder(h: AbilityVH, pos: Int) {
        try {
            if (pos < items.size) {
                h.bind(items[pos], onEdit)
            }
        } catch (e: Exception) {
            android.util.Log.e("AbilityAdapter", "Error binding view holder at position $pos", e)
        }
    }
    
    override fun getItemCount() = items.size
}
class AbilityVH(v: View) : RecyclerView.ViewHolder(v) {
    private val tvName = v.findViewById<TextView>(R.id.tvName)
    private val tvMeta = v.findViewById<TextView>(R.id.tvMeta)
    private val btn = v.findViewById<Button>(R.id.btnEdit)
    
    fun bind(a: AbilityEntity, onEdit: (AbilityEntity)->Unit) {
        try {
            if (tvName != null) {
        tvName.text = "${a.name} (${a.id})"
            }
            
            if (tvMeta != null) {
        tvMeta.text = "unit=${a.unit ?: "-"} • cap=${a.dailyCapXp ?: "-"} • unlock L${a.unlockLevel}"
            }
            
            if (btn != null) {
                btn.setOnClickListener { 
                    try {
                        onEdit(a)
                    } catch (e: Exception) {
                        android.util.Log.e("AbilityVH", "Error in edit click", e)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AbilityVH", "Error binding ability data", e)
        }
    }
}


