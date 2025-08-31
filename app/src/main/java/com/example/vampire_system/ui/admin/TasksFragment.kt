package com.example.vampire_system.ui.admin

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.LevelTaskEntity
import com.example.vampire_system.data.model.TaskCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TasksFragment : Fragment() {
    private var _adapter: TaskAdapter? = null
    private val adapter get() = _adapter!!
    private var level: Int = 10
    
    init {
        try {
            android.util.Log.d("TasksFragment", "TasksFragment initialized")
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error in init block", e)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        try {
            android.util.Log.d("TasksFragment", "Attempting to inflate fragment_admin_tasks layout")
            
            // Check if we have a valid context
            if (!isAdded || context == null) {
                android.util.Log.w("TasksFragment", "Fragment not attached or context is null, using fallback")
                return createFallbackLayout()
            }
            
            // Try to inflate the layout with error handling
            try {
                // First try the simple layout without MaterialButton
                try {
                    android.util.Log.d("TasksFragment", "Trying simple layout first")
                    val simpleView = inflater.inflate(R.layout.fragment_admin_tasks_simple, container, false)
                    android.util.Log.d("TasksFragment", "Simple layout inflated successfully")
                    return simpleView
                } catch (simpleException: Exception) {
                    android.util.Log.w("TasksFragment", "Simple layout failed, trying original layout", simpleException)
                    // Continue to try the original layout
                }
                
                // If simple layout fails, try the original layout
                try {
                    val view = inflater.inflate(R.layout.fragment_admin_tasks, container, false)
                    android.util.Log.d("TasksFragment", "Original layout inflated successfully")
                    return view
                } catch (originalException: Exception) {
                    android.util.Log.e("TasksFragment", "Original layout also failed", originalException)
                    
                    // Check if it's a theme-related error
                    if (originalException.message?.contains("theme") == true || 
                        originalException.message?.contains("InheritanceMap") == true ||
                        originalException.message?.contains("Resource") == true ||
                        originalException.message?.contains("not found") == true ||
                        originalException.message?.contains("MaterialButton") == true ||
                        originalException.message?.contains("ClassNotFoundException") == true ||
                        originalException.message?.contains("NoClassDefFoundError") == true) {
                        android.util.Log.e("TasksFragment", "Theme/resource-related layout inflation error detected")
                        // Try to create a very simple layout without theme dependencies
                        return createFallbackLayout()
                    }
                    
                    // If it's not a theme error, re-throw to be caught by outer catch
                    throw originalException
                }
            } catch (inflationException: Exception) {
                android.util.Log.e("TasksFragment", "All layout inflation attempts failed", inflationException)
                // Try to create a very simple layout without theme dependencies
                return createFallbackLayout()
            }
            
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error in onCreateView", e)
            
            // Return a simple TextView as fallback
            return try {
                android.widget.TextView(requireContext()).apply {
                    text = "Error loading tasks layout: ${e.message}"
                    textSize = 16f
                    setPadding(32, 32, 32, 32)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(android.graphics.Color.RED)
                }
            } catch (contextException: Exception) {
                android.util.Log.e("TasksFragment", "Error creating fallback TextView", contextException)
                // Ultimate fallback - create a TextView with a dummy context
                android.widget.TextView(android.content.ContextWrapper(android.app.Activity())).apply {
                    text = "Critical error: Could not create any view"
                    textSize = 16f
                    setPadding(32, 32, 32, 32)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(android.graphics.Color.RED)
                }
            }
        }
    }
    
    private fun createFallbackLayout(): View {
        try {
            android.util.Log.d("TasksFragment", "Creating fallback layout")
            
            // Check if we have a valid context
            if (!isAdded || context == null) {
                android.util.Log.e("TasksFragment", "Cannot create fallback layout - fragment not attached or context is null")
                // Return a simple TextView as ultimate fallback
                return android.widget.TextView(requireContext() ?: android.content.ContextWrapper(requireActivity())).apply {
                    text = "Critical error: Fragment not properly attached"
                    textSize = 16f
                    setPadding(32, 32, 32, 32)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(android.graphics.Color.RED)
                }
            }
            
            // Create a simple LinearLayout programmatically
            val layout = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
                gravity = android.view.Gravity.CENTER
            }
            
            // Add a title
            val title = android.widget.TextView(requireContext()).apply {
                text = "Level Tasks (Fallback Mode)"
                textSize = 18f
                setTextColor(android.graphics.Color.BLUE)
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 32)
            }
            layout.addView(title)
            
            // Add level input
            val levelLayout = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 16)
            }
            
            val levelLabel = android.widget.TextView(requireContext()).apply {
                text = "Level: "
                textSize = 16f
            }
            levelLayout.addView(levelLabel)
            
            val levelInput = android.widget.EditText(requireContext()).apply {
                id = R.id.etLevel
                setText(level.toString())
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                layoutParams = android.widget.LinearLayout.LayoutParams(80, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            levelLayout.addView(levelInput)
            
            val loadButton = android.widget.Button(requireContext()).apply {
                id = R.id.btnLoad
                text = "Load"
                layoutParams = android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            levelLayout.addView(loadButton)
            
            val newButton = android.widget.Button(requireContext()).apply {
                id = R.id.btnNew
                text = "New Task"
                layoutParams = android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            levelLayout.addView(newButton)
            
            layout.addView(levelLayout)
            
            // Add a message about fallback mode
            val message = android.widget.TextView(requireContext()).apply {
                text = "Layout file had theme issues.\nUsing fallback layout.\n\nClick 'Load' to see tasks for the selected level."
                textSize = 14f
                setTextColor(android.graphics.Color.rgb(128, 128, 128))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 0)
            }
            layout.addView(message)
            
            return layout
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error creating fallback layout", e)
            // Ultimate fallback - just a TextView
            return try {
                android.widget.TextView(requireContext()).apply {
                    text = "Critical error: Could not create any layout.\n\nError: ${e.message}"
                    textSize = 14f
                    setPadding(32, 32, 32, 32)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(android.graphics.Color.RED)
                }
            } catch (contextException: Exception) {
                android.util.Log.e("TasksFragment", "Error creating ultimate fallback TextView", contextException)
                // Ultimate fallback - create a TextView with a dummy context
                android.widget.TextView(android.content.ContextWrapper(android.app.Activity())).apply {
                    text = "Critical error: Could not create any view at all"
                    textSize = 14f
                    setPadding(32, 32, 32, 32)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(android.graphics.Color.RED)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            android.util.Log.d("TasksFragment", "onViewCreated started")
            android.util.Log.d("TasksFragment", "View type: ${view.javaClass.simpleName}")
            android.util.Log.d("TasksFragment", "View ID: ${view.id}")
            android.util.Log.d("TasksFragment", "Fragment attached: $isAdded")
            android.util.Log.d("TasksFragment", "Context: ${context?.javaClass?.simpleName}")
            
            // Check if the view is properly inflated
            if (view.id == android.view.View.NO_ID) {
                android.util.Log.w("TasksFragment", "View has no ID, this might indicate an inflation issue")
            }
            
            // Check if the view is a ViewGroup and has children
            if (view is android.view.ViewGroup) {
                android.util.Log.d("TasksFragment", "View is ViewGroup with ${view.childCount} children")
            } else {
                android.util.Log.w("TasksFragment", "View is not a ViewGroup, this might cause issues")
            }
            
            // Set up level input
            android.util.Log.d("TasksFragment", "Looking for EditText with ID: etLevel")
            val etLevel = view.findViewById<EditText>(R.id.etLevel)
            if (etLevel != null) {
                android.util.Log.d("TasksFragment", "EditText found, setting text")
                etLevel.setText(level.toString())
            } else {
                android.util.Log.e("TasksFragment", "Level EditText not found")
                // Try to find any EditText in the view hierarchy
                if (view is android.view.ViewGroup) {
                    val allEditTexts = view.findViewById<EditText>(android.R.id.text1) // This won't work, but let's see what happens
                    android.util.Log.d("TasksFragment", "All EditTexts found: $allEditTexts")
                    
                    // Try to find any EditText by searching through the hierarchy
                    val foundEditText = findViewByTypeGeneric<EditText>(view)
                    if (foundEditText != null) {
                        android.util.Log.d("TasksFragment", "Found EditText by type search: ${foundEditText.id}")
                        foundEditText.setText(level.toString())
                    }
                }
            }
            
            // Set up load button
            android.util.Log.d("TasksFragment", "Looking for Load button with ID: btnLoad")
            var btnLoad: android.view.View? = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLoad)
            if (btnLoad == null) {
                // Try regular Button as fallback
                btnLoad = view.findViewById<Button>(R.id.btnLoad)
            }
            if (btnLoad != null) {
                android.util.Log.d("TasksFragment", "Load button found, setting click listener")
                                 btnLoad.setOnClickListener {
                     try {
                         level = etLevel?.text?.toString()?.toIntOrNull() ?: level
                         loadSafely()
                         
                         // Also refresh the home screen quests to show any new tasks
                         com.example.vampire_system.util.EventBus.notifyTaskAdded()
                     } catch (e: Exception) {
                         android.util.Log.e("TasksFragment", "Error in load button click", e)
                         android.widget.Toast.makeText(requireContext(), "Error loading tasks: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                     }
                 }
            } else {
                android.util.Log.e("TasksFragment", "Load button not found")
                // Try to find any button in the view hierarchy
                if (view is android.view.ViewGroup) {
                    val allButtons = view.findViewById<android.view.View>(android.R.id.button1) // This won't work, but let's see what happens
                    android.util.Log.d("TasksFragment", "All buttons found: $allButtons")
                    
                    // Try to find any button by searching through the hierarchy
                    val foundButton = findViewByTypeGeneric<android.view.View>(view)
                    if (foundButton != null) {
                        android.util.Log.d("TasksFragment", "Found button by type search: ${foundButton.javaClass.simpleName}")
                    }
                }
            }
            
            // Set up new button
            android.util.Log.d("TasksFragment", "Looking for New button with ID: btnNew")
            var btnNew: android.view.View? = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnNew)
            if (btnNew == null) {
                // Try regular Button as fallback
                btnNew = view.findViewById<Button>(R.id.btnNew)
            }
            if (btnNew != null) {
                android.util.Log.d("TasksFragment", "New button found, setting click listener")
                                 btnNew.setOnClickListener { 
                     try {
                         openEdit(null)
                         
                         // Notify that a new task dialog was opened
                         // The actual notification will happen when the task is saved
                     } catch (e: Exception) {
                         android.util.Log.e("TasksFragment", "Error in new button click", e)
                         android.widget.Toast.makeText(requireContext(), "Error opening new task dialog: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                     }
                 }
            } else {
                android.util.Log.e("TasksFragment", "New button not found")
            }

            // Set up RecyclerView
            android.util.Log.d("TasksFragment", "Looking for RecyclerView with ID: rvTasks")
            val rv = view.findViewById<RecyclerView>(R.id.rvTasks)
                         if (rv != null) {
                 android.util.Log.d("TasksFragment", "RecyclerView found, setting up adapter")
                 _adapter = TaskAdapter(
                     onEdit = { task -> 
                         android.util.Log.d("TasksFragment", "Edit callback triggered for task: ${task.id}")
                         openEdit(task) 
                     },
                     onDelete = { task -> 
                         android.util.Log.d("TasksFragment", "Delete callback triggered for task: ${task.id}")
                         deleteTask(task) 
                     }
                 )
                 rv.layoutManager = LinearLayoutManager(requireContext())
                 rv.adapter = _adapter
                 
                 // Load data safely
                 loadSafely()
             } else {
                 android.util.Log.e("TasksFragment", "RecyclerView not found")
                 // Try to find any RecyclerView by searching through the hierarchy
                 if (view is android.view.ViewGroup) {
                     val foundRecyclerView = findViewByTypeGeneric<RecyclerView>(view)
                     if (foundRecyclerView != null) {
                         android.util.Log.d("TasksFragment", "Found RecyclerView by type search: ${foundRecyclerView.id}")
                         // Set up the found RecyclerView
                         _adapter = TaskAdapter(
                             onEdit = { task -> 
                                 android.util.Log.d("TasksFragment", "Edit callback triggered for task: ${task.id}")
                                 openEdit(task) 
                             },
                             onDelete = { task -> 
                                 android.util.Log.d("TasksFragment", "Delete callback triggered for task: ${task.id}")
                                 deleteTask(task) 
                             }
                         )
                         foundRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                         foundRecyclerView.adapter = _adapter
                         
                         // Load data safely
                         loadSafely()
                     } else {
                         showError("RecyclerView not found in layout")
                     }
                 } else {
                     showError("RecyclerView not found in layout")
                 }
             }
            
            // Debug: Dump view hierarchy
            try {
                android.util.Log.d("TasksFragment", "Dumping view hierarchy:")
                dumpViewHierarchy(view, 0)
            } catch (e: Exception) {
                android.util.Log.e("TasksFragment", "Error dumping view hierarchy", e)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error in onViewCreated", e)
            showError("Error setting up tasks: ${e.message}")
        }
    }
    
    private fun dumpViewHierarchy(view: android.view.View, depth: Int) {
        try {
            val indent = "  ".repeat(depth)
            val viewInfo = "${view.javaClass.simpleName} (ID: ${view.id})"
            android.util.Log.d("TasksFragment", "$indent$viewInfo")
            
            if (view is android.view.ViewGroup) {
                for (i in 0 until view.childCount) {
                    dumpViewHierarchy(view.getChildAt(i), depth + 1)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error dumping view hierarchy", e)
        }
    }
    
    private fun findViewByType(root: android.view.View, targetClass: Class<*>): android.view.View? {
        return try {
            if (targetClass.isInstance(root)) {
                return root
            }
            if (root is android.view.ViewGroup) {
                for (i in 0 until root.childCount) {
                    val child = root.getChildAt(i)
                    val found = findViewByType(child, targetClass)
                    if (found != null) {
                        return found
                    }
                }
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error finding view by type", e)
            null
        }
    }
    
    private inline fun <reified T : android.view.View> findViewByTypeGeneric(root: android.view.View): T? {
        return findViewByType(root, T::class.java) as? T
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
                } else {
                    // If the root view is not a ViewGroup, try to create a new layout
                    val newLayout = android.widget.LinearLayout(requireContext()).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        setPadding(32, 32, 32, 32)
                        gravity = android.view.Gravity.CENTER
                    }
                    newLayout.addView(errorText)
                    
                    // Try to replace the current view
                    try {
                        val parent = rootView.parent as? android.view.ViewGroup
                        parent?.let { p ->
                            val index = p.indexOfChild(rootView)
                            p.removeView(rootView)
                            p.addView(newLayout, index)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("TasksFragment", "Error replacing view with error layout", e)
                    }
                }
            }
            
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error showing error message", e)
        }
    }

    private fun loadSafely() {
        try {
            android.util.Log.d("TasksFragment", "Starting to load tasks safely for level $level")
            
            // Check if the fragment is still attached
            if (!isAdded || context == null) {
                android.util.Log.w("TasksFragment", "Fragment not attached, skipping load")
                return
            }
            
            // Use lifecycle-aware coroutine scope
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    android.util.Log.d("TasksFragment", "Accessing database")
                    val db = AppDatabase.get(requireContext())
                    val items = db.levelTaskDao().forLevelOnce(level)
                    
                    android.util.Log.d("TasksFragment", "Found ${items.size} tasks for level $level")
                    
                    withContext(Dispatchers.Main) { 
                                                 try {
                             if (_adapter != null && isAdded) {
                                 _adapter!!.submit(items)
                                 android.util.Log.d("TasksFragment", "Tasks loaded successfully")
                                 
                                 // Notify home screen that tasks were loaded
                                 com.example.vampire_system.util.EventBus.notifyTaskAdded()
                             } else {
                                 android.util.Log.w("TasksFragment", "Adapter is null or fragment not attached")
                             }
                         } catch (e: Exception) {
                             android.util.Log.e("TasksFragment", "Error submitting items to adapter", e)
                             showError("Error displaying tasks: ${e.message}")
                         }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TasksFragment", "Error loading tasks from database", e)
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            showError("Error loading tasks: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error starting load coroutine", e)
            if (isAdded) {
                showError("Error starting data load: ${e.message}")
            }
        }
    }

    private fun deleteTask(item: LevelTaskEntity) {
        try {
            android.util.Log.d("TasksFragment", "deleteTask called for task: ${item.id}")
            
            // Show a toast to confirm the method is being called
            try {
                android.widget.Toast.makeText(requireContext(), "Deleting task: ${item.id}", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("TasksFragment", "Error showing delete toast", e)
            }
            
            // Check if the fragment is still attached
            if (!isAdded || context == null) {
                android.util.Log.w("TasksFragment", "Fragment not attached, skipping delete")
                return
            }
            
            // Use lifecycle-aware coroutine scope
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                                         val db = AppDatabase.get(requireContext())
                     db.levelTaskDao().deleteById(item.id)
                     
                     // Refresh and reload
                     refreshToday()
                     
                     // Notify that a task was deleted so home screen can refresh
                     com.example.vampire_system.util.EventBus.notifyTaskAdded()
                     
                     withContext(Dispatchers.Main) {
                         if (isAdded) {
                             loadSafely()
                         }
                     }
                } catch (e: Exception) {
                    android.util.Log.e("TasksFragment", "Error deleting task", e)
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            android.widget.Toast.makeText(requireContext(), "Error deleting task: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error starting delete coroutine", e)
            if (isAdded) {
                android.widget.Toast.makeText(requireContext(), "Error starting delete: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openEdit(item: LevelTaskEntity?) {
        try {
            android.util.Log.d("TasksFragment", "Opening edit dialog for task: ${item?.id ?: "new"}")
            
            // Check if the fragment is still attached
            if (!isAdded || context == null) {
                android.util.Log.w("TasksFragment", "Fragment not attached, skipping edit dialog")
                return
            }
            
                         val d = Dialog(requireContext())
             // Try to inflate the dialog layout, with fallback if it fails
             try {
                 android.util.Log.d("TasksFragment", "Attempting to inflate XML dialog layout")
                 d.setContentView(R.layout.dialog_edit_task)
                 android.util.Log.d("TasksFragment", "Successfully inflated XML dialog layout - using XML dialog")
             } catch (e: Exception) {
                 android.util.Log.e("TasksFragment", "Error inflating dialog layout, using fallback", e)
                 android.util.Log.d("TasksFragment", "Using fallback programmatic dialog")
                 // Create a fallback dialog layout programmatically
                 d.setContentView(createFallbackTaskDialogLayout(item, d))
                 d.show()
                 return
             }
            
                         val etId = d.findViewById<EditText>(R.id.etId)
             val etAb = d.findViewById<EditText>(R.id.etAbilityId)
             val spinnerCategory = d.findViewById<Spinner>(R.id.spinnerCategory)
             val etSpec = d.findViewById<EditText>(R.id.etSpec)
             val etAcc = d.findViewById<EditText>(R.id.etAcceptance)
             val etXp = d.findViewById<EditText>(R.id.etXpReward)
             
             android.util.Log.d("TasksFragment", "Dialog fields found - ID: ${etId != null}, Ability: ${etAb != null}, Category: ${spinnerCategory != null}, Spec: ${etSpec != null}, Acceptance: ${etAcc != null}, XP: ${etXp != null}")
             
             if (etId != null && etAb != null && spinnerCategory != null && etSpec != null && etAcc != null && etXp != null) {
                 // Set up category spinner
                 val categories = arrayOf("COMMUNICATION", "STRENGTH", "KNOWLEDGE", "SOCIAL", "LIFESTYLE")
                 val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
                 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                 spinnerCategory.adapter = adapter
                 
                 if (item != null) {
                     etId.setText(item.id)
                     etId.isEnabled = false
                     etAb.setText(item.abilityId ?: "")
                     etSpec.setText(item.spec)
                     etAcc.setText(item.acceptance.joinToString("\n"))
                     etXp.setText(item.xpReward.toString()) // Use actual XP from existing task
                     // Set category spinner to current value
                     val categoryIndex = categories.indexOf(item.category.name)
                     if (categoryIndex >= 0) {
                         spinnerCategory.setSelection(categoryIndex)
                     }
                 } else {
                     etId.setText("L${level}_${System.currentTimeMillis().toString().takeLast(5)}")
                     etXp.setText("") // Empty XP field for new tasks - user must enter value
                     // Default to KNOWLEDGE category
                     spinnerCategory.setSelection(categories.indexOf("KNOWLEDGE"))
                 }
                
                val btnSave = d.findViewById<Button>(R.id.btnSave)
                if (btnSave != null) {
                    btnSave.setOnClickListener {
                        try {
                            // Use lifecycle-aware coroutine scope
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    val db = AppDatabase.get(requireContext())
                                                                     val selectedCategory = TaskCategory.valueOf(spinnerCategory.selectedItem.toString())
                                 val xpText = etXp.text.toString().trim()
                                 if (xpText.isEmpty()) {
                                     android.widget.Toast.makeText(requireContext(), "Please enter an XP value", android.widget.Toast.LENGTH_SHORT).show()
                                     return@launch
                                 }
                                 
                                 val xpValue = xpText.toDoubleOrNull()
                                 if (xpValue == null || xpValue <= 0) {
                                     android.widget.Toast.makeText(requireContext(), "Please enter a valid positive XP value (e.g., 13 or 13.0)", android.widget.Toast.LENGTH_SHORT).show()
                                     return@launch
                                 }
                                 android.util.Log.d("TasksFragment", "Task XP value: $xpValue (from XML dialog)")
                                 android.util.Log.d("TasksFragment", "Raw XP text: '${etXp.text}'")
                                 
                                 val entity = LevelTaskEntity(
                                     id = etId.text.toString(),
                                     levelId = level,
                                     abilityId = etAb.text.toString().ifBlank { null },
                                     spec = etSpec.text.toString(),
                                     acceptance = etAcc.text.toString()
                                         .lines()
                                         .map { it.trim() }
                                         .filter { it.isNotEmpty() },
                                     category = selectedCategory,
                                     xpReward = xpValue
                                 )
                                 
                                                                  android.util.Log.d("TasksFragment", "Saving task: id=${entity.id}, category=${entity.category}, xpReward=${entity.xpReward}")
                                 db.levelTaskDao().upsert(entity)
                                 
                                 // Verify the task was saved correctly
                                 val savedTask = db.levelTaskDao().byId(entity.id)
                                 android.util.Log.d("TasksFragment", "Retrieved saved task: id=${savedTask?.id}, category=${savedTask?.category}, xpReward=${savedTask?.xpReward}")
                                 
                                 refreshToday()
                                     
                                     // Notify that a task was added so home screen can refresh
                                     com.example.vampire_system.util.EventBus.notifyTaskAdded()
                                     
                                     withContext(Dispatchers.Main) { 
                                         d.dismiss()
                                         if (isAdded) {
                                             loadSafely()
                                         }
                                     }
                                } catch (e: Exception) {
                                    android.util.Log.e("TasksFragment", "Error saving task", e)
                                    withContext(Dispatchers.Main) {
                                        android.widget.Toast.makeText(requireContext(), "Error saving task: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("TasksFragment", "Error setting up save button", e)
                            android.widget.Toast.makeText(requireContext(), "Error setting up save: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    android.util.Log.e("TasksFragment", "Save button not found in dialog")
                }
            } else {
                android.util.Log.e("TasksFragment", "Some dialog fields not found")
            }
            
            d.show()
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error opening edit dialog", e)
            android.widget.Toast.makeText(requireContext(), "Error opening edit dialog: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshToday() {
        try {
            // Check if the fragment is still attached
            if (!isAdded || context == null) {
                android.util.Log.w("TasksFragment", "Fragment not attached, skipping refresh")
                return
            }
            
            // Use lifecycle-aware coroutine scope
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.get(requireContext())
                                         com.example.vampire_system.domain.engine.QuestEngine(db, com.example.vampire_system.domain.repo.LevelRepo(db)).generateDaily(com.example.vampire_system.util.Dates.todayLocal())
                     android.util.Log.d("TasksFragment", "Daily quests refreshed successfully")
                     
                     // Notify home screen that quests were refreshed
                     com.example.vampire_system.util.EventBus.notifyTaskAdded()
                } catch (e: Exception) {
                    android.util.Log.e("TasksFragment", "Error refreshing daily quests", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error starting refresh coroutine", e)
        }
    }
    
         private fun createFallbackTaskDialogLayout(item: LevelTaskEntity?, dialog: Dialog): View {
        try {
            android.util.Log.d("TasksFragment", "Creating fallback task dialog layout")
            
            // Check if the fragment is still attached
            if (!isAdded || context == null) {
                android.util.Log.e("TasksFragment", "Cannot create fallback dialog layout - fragment not attached or context is null")
                // Return a simple TextView as ultimate fallback
                return android.widget.TextView(requireContext() ?: android.content.ContextWrapper(requireActivity())).apply {
                    text = "Critical error: Fragment not properly attached"
                    textSize = 16f
                    setPadding(32, 32, 32, 32)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(android.graphics.Color.RED)
                }
            }
            
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
                text = if (item != null) "Edit Task: ${item.id}" else "New Task"
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.BLACK)
                setPadding(0, 0, 0, 16)
            }
            layout.addView(title)
            
                         // Add ID field
             val idLabel = android.widget.TextView(requireContext()).apply {
                 text = "Task ID:"
                 textSize = 16f
                 setTypeface(null, android.graphics.Typeface.BOLD)
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(0, 0, 0, 8)
             }
            layout.addView(idLabel)
            
                         val idInput = android.widget.EditText(requireContext()).apply {
                 id = R.id.etId
                 setText(if (item != null) item.id else "L${level}_${System.currentTimeMillis().toString().takeLast(5)}")
                 isEnabled = item == null
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
            layout.addView(idInput)
            
                         // Add ability ID field
             val abilityLabel = android.widget.TextView(requireContext()).apply {
                 text = "Ability ID (optional):"
                 textSize = 16f
                 setTypeface(null, android.graphics.Typeface.BOLD)
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(0, 16, 0, 8)
             }
            layout.addView(abilityLabel)
            
                         val abilityInput = android.widget.EditText(requireContext()).apply {
                 id = R.id.etAbilityId
                 setText(item?.abilityId ?: "")
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
            layout.addView(abilityInput)
            
            // Add category field
            val categoryLabel = android.widget.TextView(requireContext()).apply {
                text = "Category:"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.BLACK)
                setPadding(0, 16, 0, 8)
            }
            layout.addView(categoryLabel)
            
            val categorySpinner = android.widget.Spinner(requireContext()).apply {
                id = R.id.spinnerCategory
                val categories = arrayOf("COMMUNICATION", "STRENGTH", "KNOWLEDGE", "SOCIAL", "LIFESTYLE")
                val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                this.adapter = adapter
                
                // Set default selection based on item or default to KNOWLEDGE
                if (item != null) {
                    val categoryIndex = categories.indexOf(item.category.name)
                    if (categoryIndex >= 0) {
                        setSelection(categoryIndex)
                    }
                } else {
                    setSelection(categories.indexOf("KNOWLEDGE"))
                }
                
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            layout.addView(categorySpinner)
            
            // Add spec field
             val specLabel = android.widget.TextView(requireContext()).apply {
                 text = "Task Specification:"
                 textSize = 16f
                 setTypeface(null, android.graphics.Typeface.BOLD)
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(0, 16, 0, 8)
             }
            layout.addView(specLabel)
            
                         val specInput = android.widget.EditText(requireContext()).apply {
                 id = R.id.etSpec
                 setText(item?.spec ?: "")
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
            layout.addView(specInput)
            
                         // Add acceptance field
             val acceptanceLabel = android.widget.TextView(requireContext()).apply {
                 text = "Acceptance Criteria (one per line):"
                 textSize = 16f
                 setTypeface(null, android.graphics.Typeface.BOLD)
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(0, 16, 0, 8)
             }
            layout.addView(acceptanceLabel)
            
                         val acceptanceInput = android.widget.EditText(requireContext()).apply {
                 id = R.id.etAcceptance
                 setText(if (item != null) item.acceptance.joinToString("\n") else "")
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
                         layout.addView(acceptanceInput)
             
             // Add XP Reward field
             val xpLabel = android.widget.TextView(requireContext()).apply {
                 text = "XP Reward:"
                 textSize = 16f
                 setTypeface(null, android.graphics.Typeface.BOLD)
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(0, 16, 0, 8)
             }
             layout.addView(xpLabel)
             
             val xpInput = android.widget.EditText(requireContext()).apply {
                 id = R.id.etXpReward
                 setText(if (item != null) item.xpReward.toString() else "")
                 textSize = 16f
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(16, 12, 16, 12)
                 setBackgroundColor(android.graphics.Color.WHITE)
                 setHintTextColor(android.graphics.Color.rgb(128, 128, 128))
                 inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                 hint = "Enter XP amount (e.g., 10.0)"
                 layoutParams = android.widget.LinearLayout.LayoutParams(
                     android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                     android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                 )
             }
             layout.addView(xpInput)
             
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
                                                                   val selectedCategory = TaskCategory.valueOf(categorySpinner.selectedItem.toString())
                                 val xpText = xpInput.text.toString().trim()
                                 if (xpText.isEmpty()) {
                                     android.widget.Toast.makeText(requireContext(), "Please enter an XP value", android.widget.Toast.LENGTH_SHORT).show()
                                     return@launch
                                 }
                                 
                                 val xpValue = xpText.toDoubleOrNull()
                                 if (xpValue == null || xpValue <= 0) {
                                     android.widget.Toast.makeText(requireContext(), "Please enter a valid positive XP value (e.g., 13 or 13.0)", android.widget.Toast.LENGTH_SHORT).show()
                                     return@launch
                                 }
                                 android.util.Log.d("TasksFragment", "Task XP value (fallback): $xpValue")
                                 android.util.Log.d("TasksFragment", "Raw XP text (fallback): '${xpInput.text}'")
                                 
                                 val entity = LevelTaskEntity(
                                     id = idInput.text.toString(),
                                     levelId = level,
                                     abilityId = abilityInput.text.toString().ifBlank { null },
                                     spec = specInput.text.toString(),
                                     acceptance = acceptanceInput.text.toString()
                                         .lines()
                                         .map { it.trim() }
                                         .filter { it.isNotEmpty() },
                                     category = selectedCategory,
                                     xpReward = xpValue
                                 )
                                 
                                 android.util.Log.d("TasksFragment", "Saving task (fallback): id=${entity.id}, category=${entity.category}, xpReward=${entity.xpReward}")
                                 db.levelTaskDao().upsert(entity)
                                 
                                 // Verify the task was saved correctly
                                 val savedTask = db.levelTaskDao().byId(entity.id)
                                 android.util.Log.d("TasksFragment", "Retrieved saved task: id=${savedTask?.id}, category=${savedTask?.category}, xpReward=${savedTask?.xpReward}")
                                 
                                 refreshToday()
                                     
                                     // Notify that a task was added so home screen can refresh
                                     com.example.vampire_system.util.EventBus.notifyTaskAdded()
                                     
                                     withContext(Dispatchers.Main) { 
                                         dialog.dismiss()
                                         if (isAdded) {
                                             loadSafely()
                                         }
                                     }
                             } catch (e: Exception) {
                                 android.util.Log.e("TasksFragment", "Error saving task in fallback dialog", e)
                                 withContext(Dispatchers.Main) {
                                     android.widget.Toast.makeText(requireContext(), "Error saving task: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                 }
                             }
                         }
                     } catch (e: Exception) {
                         android.util.Log.e("TasksFragment", "Error setting up save button in fallback dialog", e)
                         android.widget.Toast.makeText(requireContext(), "Error saving task: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                     }
                 }
             }
             layout.addView(saveButton)
            
            return layout
        } catch (e: Exception) {
            android.util.Log.e("TasksFragment", "Error creating fallback task dialog layout", e)
            // Ultimate fallback - just a simple message
            return try {
                android.widget.TextView(requireContext()).apply {
                    text = "Error creating edit dialog.\n\nPlease try again or contact support."
                    textSize = 16f
                    setPadding(32, 32, 32, 32)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(android.graphics.Color.RED)
                }
            } catch (contextException: Exception) {
                android.util.Log.e("TasksFragment", "Error creating ultimate fallback dialog TextView", contextException)
                // Ultimate fallback - create a TextView with a dummy context
                android.widget.TextView(android.content.ContextWrapper(android.app.Activity())).apply {
                    text = "Critical error: Could not create any dialog view"
                    textSize = 16f
                    setPadding(32, 32, 32, 32)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(android.graphics.Color.RED)
                }
            }
        }
    }
}

class TaskAdapter(
    private val onEdit: (LevelTaskEntity)->Unit,
    private val onDelete: (LevelTaskEntity)->Unit
) : RecyclerView.Adapter<TaskVH>() {
    private val items = mutableListOf<LevelTaskEntity>()
    
    fun submit(list: List<LevelTaskEntity>) { 
        try {
            // Check if the list is valid
            if (list.isEmpty()) {
                android.util.Log.d("TaskAdapter", "Submitting empty list")
            }
            
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
            android.util.Log.d("TaskAdapter", "Successfully submitted ${list.size} items")
        } catch (e: Exception) {
            android.util.Log.e("TaskAdapter", "Error submitting items", e)
        }
    }
    
    override fun onCreateViewHolder(p: ViewGroup, v: Int): TaskVH {
        try {
            // Check if the context is valid
            if (p.context == null) {
                android.util.Log.e("TaskAdapter", "Context is null, using fallback")
                return createFallbackViewHolder(p.context ?: android.content.ContextWrapper(android.app.Activity()))
            }
            
            android.util.Log.d("TaskAdapter", "Creating regular view holder with XML layout")
            val view = LayoutInflater.from(p.context).inflate(R.layout.item_task_admin, p, false)
            android.util.Log.d("TaskAdapter", "Successfully inflated XML layout, view type: ${view.javaClass.simpleName}")
            return TaskVH(view)
        } catch (e: Exception) {
            android.util.Log.e("TaskAdapter", "Error creating view holder, using fallback", e)
            // Create a functional fallback view holder with proper layout
            return createFallbackViewHolder(p.context)
        }
    }
    
    private fun createFallbackViewHolder(context: android.content.Context): TaskVH {
        try {
            android.util.Log.d("TaskAdapter", "Creating fallback view holder")
            // Check if the context is valid
            if (context == null) {
                android.util.Log.e("TaskAdapter", "Context is null in createFallbackViewHolder")
                // Return a simple TextView as ultimate fallback
                val fallbackView = android.widget.TextView(android.content.ContextWrapper(android.app.Activity())).apply {
                    text = "Error: Invalid context"
                    setTextColor(android.graphics.Color.RED)
                    setPadding(16, 16, 16, 16)
                }
                return TaskVH(fallbackView)
            }
            
            // Create a simple but functional layout programmatically
            val layout = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            
                                      // Add ID TextView
             val idText = android.widget.TextView(context).apply {
                 id = R.id.tvId
                 setTypeface(null, android.graphics.Typeface.BOLD)
                 textSize = 16f
                 setTextColor(android.graphics.Color.BLACK)
                 setPadding(0, 0, 0, 8)
                 text = "Task ID - XP" // Placeholder, will be set in bind method
             }
             layout.addView(idText)
            
            // Add spec TextView
            val specText = android.widget.TextView(context).apply {
                id = R.id.tvSpec
                textSize = 14f
                setTextColor(android.graphics.Color.rgb(64, 64, 64))
                setPadding(0, 0, 0, 8)
            }
            layout.addView(specText)
            
            // Add acceptance TextView
            val accText = android.widget.TextView(context).apply {
                id = R.id.tvAcc
                textSize = 12f
                setTextColor(android.graphics.Color.rgb(96, 96, 96))
                setPadding(0, 0, 0, 8)
            }
            layout.addView(accText)
            
            // Add button layout
            val buttonLayout = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            
            // Add edit button
            val editButton = android.widget.Button(context).apply {
                id = R.id.btnEdit
                text = "Edit"
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 8
                }
            }
            buttonLayout.addView(editButton)
            
            // Add delete button
            val deleteButton = android.widget.Button(context).apply {
                id = R.id.btnDelete
                text = "Delete"
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            buttonLayout.addView(deleteButton)
            
            android.util.Log.d("TaskAdapter", "Fallback view holder created with delete button ID: ${deleteButton.id}")
            android.util.Log.d("TaskAdapter", "Fallback layout has ${layout.childCount} children")
            
            layout.addView(buttonLayout)
            
            android.util.Log.d("TaskAdapter", "After adding button layout, fallback layout has ${layout.childCount} children")
            
            return TaskVH(layout)
        } catch (e: Exception) {
            android.util.Log.e("TaskAdapter", "Error creating fallback view holder", e)
            // Ultimate fallback - just a TextView
            val fallbackView = try {
                android.widget.TextView(context).apply {
                    text = "Error loading task item"
                    setTextColor(android.graphics.Color.RED)
                    setPadding(16, 16, 16, 16)
                }
            } catch (contextException: Exception) {
                android.util.Log.e("TaskAdapter", "Error creating ultimate fallback TextView", contextException)
                // Ultimate fallback - create a TextView with a dummy context
                android.widget.TextView(android.content.ContextWrapper(android.app.Activity())).apply {
                    text = "Critical error: Could not create any view"
                    setTextColor(android.graphics.Color.RED)
                    setPadding(16, 16, 16, 16)
                }
            }
            return TaskVH(fallbackView)
        }
    }
    
    override fun onBindViewHolder(h: TaskVH, pos: Int) {
        try {
            // Check if the position is valid
            if (pos < 0) {
                android.util.Log.w("TaskAdapter", "Invalid position: $pos")
                return
            }
            
            if (pos < items.size) {
                h.bind(items[pos], onEdit, onDelete)
            } else {
                android.util.Log.w("TaskAdapter", "Position $pos is out of bounds (size: ${items.size})")
            }
        } catch (e: Exception) {
            android.util.Log.e("TaskAdapter", "Error binding view holder at position $pos", e)
        }
    }
    
    override fun getItemCount(): Int {
        return try {
            val count = items.size
            android.util.Log.d("TaskAdapter", "Item count: $count")
            count
        } catch (e: Exception) {
            android.util.Log.e("TaskAdapter", "Error getting item count", e)
            0
        }
    }
}
class TaskVH(v: View) : RecyclerView.ViewHolder(v) {
    private val rootView = v
    
    // We'll find the views in the bind method to ensure they exist when needed
    
    fun bind(t: LevelTaskEntity, onEdit: (LevelTaskEntity)->Unit, onDelete: (LevelTaskEntity)->Unit) {
        try {
            android.util.Log.d("TaskVH", "Binding task: ${t.id}, rootView type: ${rootView.javaClass.simpleName}")
            
            // Check if the task data is valid
            if (t.id.isBlank()) {
                android.util.Log.w("TaskVH", "Task ID is blank")
            }
            
                         // Debug: Log what views are available in the rootView
             if (rootView is android.view.ViewGroup) {
                 android.util.Log.d("TaskVH", "RootView is ViewGroup with ${rootView.childCount} children")
                 for (i in 0 until rootView.childCount) {
                     val child = rootView.getChildAt(i)
                     android.util.Log.d("TaskVH", "Child $i: ${child.javaClass.simpleName}, ID: ${child.id}")
                     
                     // If this is the button layout, explore its children
                     if (child is android.widget.LinearLayout && child.id == android.view.View.NO_ID) {
                         android.util.Log.d("TaskVH", "Button layout has ${child.childCount} children")
                         for (j in 0 until child.childCount) {
                             val buttonChild = child.getChildAt(j)
                             android.util.Log.d("TaskVH", "  Button child $j: ${buttonChild.javaClass.simpleName}, ID: ${buttonChild.id}")
                         }
                     }
                 }
             } else {
                 android.util.Log.d("TaskVH", "RootView is not a ViewGroup: ${rootView.javaClass.simpleName}")
             }
            
            // Find views each time bind is called to ensure they exist
            val tvId = try { 
                rootView.findViewById<TextView>(R.id.tvId) 
            } catch (e: Exception) { 
                android.util.Log.e("TaskVH", "Error finding tvId", e); null 
            }
            
            val tvSpec = try { 
                rootView.findViewById<TextView>(R.id.tvSpec) 
            } catch (e: Exception) { 
                android.util.Log.e("TaskVH", "Error finding tvSpec", e); null 
            }
            
            val tvAcc = try { 
                rootView.findViewById<TextView>(R.id.tvAcc) 
            } catch (e: Exception) { 
                android.util.Log.e("TaskVH", "Error finding tvAcc", e); null 
            }
            
            val btnEdit: View? = try { 
                // Only search for regular Button since that's what's in the XML layout
                val regularBtn = rootView.findViewById<Button>(R.id.btnEdit)
                
                android.util.Log.d("TaskVH", "Edit button search - Regular Button: ${regularBtn != null}")
                
                regularBtn
            } catch (e: Exception) { 
                android.util.Log.e("TaskVH", "Error finding btnEdit", e); null 
            }
            
            val btnDelete: View? = try { 
                // Only search for regular Button since that's what's in the XML layout
                val regularBtn = rootView.findViewById<Button>(R.id.btnDelete)
                
                android.util.Log.d("TaskVH", "Delete button search - Regular Button: ${regularBtn != null}")
                
                regularBtn
            } catch (e: Exception) { 
                android.util.Log.e("TaskVH", "Error finding btnDelete", e); null 
            }
            
            // Set text content
            if (tvId != null) {
                // Use the actual XP reward from the task
                val xpReward = t.xpReward
                tvId.text = "${t.id} (L${t.levelId}) ${t.abilityId ?: ""} - ${xpReward} XP"
            } else {
                android.util.Log.w("TaskVH", "tvId is null")
            }
            
            if (tvSpec != null) {
                tvSpec.text = t.spec
            } else {
                android.util.Log.w("TaskVH", "tvSpec is null")
            }
            
            if (tvAcc != null) {
                tvAcc.text = "Acceptance:\n• " + t.acceptance.joinToString("\n• ")
            } else {
                android.util.Log.w("TaskVH", "tvAcc is null")
            }
            
                         // Set up button click listeners
             if (btnEdit != null) {
                 android.util.Log.d("TaskVH", "Edit button found: ${btnEdit.javaClass.simpleName}")
                 btnEdit.setOnClickListener { 
                     try {
                         android.util.Log.d("TaskVH", "Edit button clicked for task: ${t.id}")
                         onEdit(t)
                     } catch (e: Exception) {
                         android.util.Log.e("TaskVH", "Error in edit click", e)
                     }
                 }
             } else {
                 android.util.Log.w("TaskVH", "btnDelete is null")
             }
             
             if (btnDelete != null) {
                 android.util.Log.d("TaskVH", "Delete button found: ${btnDelete.javaClass.simpleName}, setting up click listener")
                 btnDelete.setOnClickListener { 
                     try {
                         android.util.Log.d("TaskVH", "Delete button clicked for task: ${t.id}")
                         
                         // Show confirmation dialog before deleting
                         val alertDialog = android.app.AlertDialog.Builder(rootView.context)
                             .setTitle("Confirm Delete")
                             .setMessage("Are you sure you want to delete task '${t.id}'?")
                             .setPositiveButton("Delete") { _, _ ->
                                 try {
                                     android.util.Log.d("TaskVH", "Delete confirmed for task: ${t.id}")
                                     onDelete(t)
                                 } catch (e: Exception) {
                                     android.util.Log.e("TaskVH", "Error in confirmed delete", e)
                                     android.widget.Toast.makeText(rootView.context, "Error deleting task: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                 }
                             }
                             .setNegativeButton("Cancel", null)
                             .create()
                         
                         alertDialog.show()
                         
                     } catch (e: Exception) {
                         android.util.Log.e("TaskVH", "Error in delete click", e)
                         android.widget.Toast.makeText(rootView.context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                     }
                 }
                 android.util.Log.d("TaskVH", "Delete button click listener set successfully")
             } else {
                 android.util.Log.w("TaskVH", "btnDelete is null")
             }
        } catch (e: Exception) {
            android.util.Log.e("TaskVH", "Error binding task data", e)
        }
    }
}


