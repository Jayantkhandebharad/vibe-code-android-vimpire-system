package com.example.vampire_system.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vampire_system.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import androidx.lifecycle.lifecycleScope

class AdminFragment : Fragment() {
    private var contentView: android.widget.FrameLayout? = null
    
    private fun loadInitialContent() {
        try {
            val localContentView = contentView
            if (localContentView != null) {
                localContentView.removeAllViews()
                
                val testText = android.widget.TextView(requireContext()).apply {
                    text = "Admin Studio Loaded Successfully!\n\nClick 'Abilities' or 'Level Tasks' to continue."
                    textSize = 16f
                    setPadding(32, 32, 32, 32)
                    gravity = android.view.Gravity.CENTER
                }
                localContentView.addView(testText)
                
                // Test database connectivity and show updated counts
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val db = com.example.vampire_system.data.db.AppDatabase.get(requireContext())
                        val abilityCount = db.abilityDao().getAllOnce().size
                        val levelCount = db.levelDao().getAll().firstOrNull()?.size ?: 0
                        
                        withContext(Dispatchers.Main) {
                            testText.text = "Admin Studio Loaded Successfully!\n\nDatabase Status:\n• Abilities: $abilityCount\n• Levels: $levelCount\n\nClick 'Abilities' or 'Level Tasks' to continue."
                            // Update button state based on current data
                            updateForceAddButtonState(abilityCount, levelCount)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminFragment", "Database test failed", e)
                        withContext(Dispatchers.Main) {
                            testText.text = "Admin Studio Loaded!\n\nDatabase Error: ${e.message}\n\nClick 'Abilities' or 'Level Tasks' to continue."
                            testText.setTextColor(android.graphics.Color.rgb(255, 165, 0)) // Orange color
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AdminFragment", "Error in loadInitialContent", e)
        }
    }
    
    private fun updateForceAddButtonState(abilityCount: Int, levelCount: Int) {
        // Find the force add button by searching through the view hierarchy
        val forceAddButton = findButtonByTag("forceAddButton")
        forceAddButton?.let { button ->
            if (abilityCount >= 100 && levelCount >= 100) {
                // All abilities and levels are present
                button.isEnabled = false
                button.text = "✅ All Abilities Already Added"
                button.setBackgroundColor(android.graphics.Color.rgb(158, 158, 158)) // Gray
                button.setTextColor(android.graphics.Color.rgb(97, 97, 97)) // Dark gray text
            } else {
                // Some abilities/levels are missing
                button.isEnabled = true
                button.text = "Force Add All Abilities (Overwrite)"
                button.setBackgroundColor(android.graphics.Color.rgb(255, 152, 0)) // Orange
                button.setTextColor(android.graphics.Color.WHITE)
            }
        }
    }
    
    private fun findButtonByTag(tag: String): android.widget.Button? {
        return view?.findViewWithTag<android.widget.Button>(tag)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        try {
            return inflater.inflate(R.layout.fragment_admin, container, false)
        } catch (e: Exception) {
            android.util.Log.e("AdminFragment", "Error inflating layout", e)
            // Return a simple TextView as fallback
            return android.widget.TextView(requireContext()).apply {
                text = "Error loading Admin Studio layout: ${e.message}"
                textSize = 16f
                setPadding(32, 32, 32, 32)
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.RED)
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            // Set up back button to navigate to notifications
            view.findViewById<Button>(R.id.btnBack)?.setOnClickListener {
                findNavController().navigate(R.id.navigation_notifications)
            }
            
            // Show a simple message first to test if the fragment loads
            val localContentView = view.findViewById<android.widget.FrameLayout>(R.id.adminContent)
            contentView = localContentView
            if (localContentView != null) {
                val testText = android.widget.TextView(requireContext()).apply {
                    text = "Admin Studio Loaded Successfully!\n\nClick 'Abilities' or 'Level Tasks' to continue."
                    textSize = 16f
                    setPadding(32, 32, 32, 32)
                    gravity = android.view.Gravity.CENTER
                }
                localContentView.addView(testText)
                
                // Test database connectivity
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = com.example.vampire_system.data.db.AppDatabase.get(requireContext())
                        val abilityCount = db.abilityDao().getAllOnce().size
                        val levelCount = db.levelDao().getAll().firstOrNull()?.size ?: 0
                        
                        withContext(Dispatchers.Main) {
                            testText.text = "Admin Studio Loaded Successfully!\n\nDatabase Status:\n• Abilities: $abilityCount\n• Levels: $levelCount\n\nClick 'Abilities' or 'Level Tasks' to continue."
                            // Update button state based on current data
                            updateForceAddButtonState(abilityCount, levelCount)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminFragment", "Database test failed", e)
                        withContext(Dispatchers.Main) {
                            testText.text = "Admin Studio Loaded!\n\nDatabase Error: ${e.message}\n\nClick 'Abilities' or 'Level Tasks' to continue."
                            testText.setTextColor(android.graphics.Color.rgb(255, 165, 0)) // Orange color
                        }
                    }
                }
            } else {
                android.util.Log.e("AdminFragment", "adminContent FrameLayout not found")
            }

            // Set up Abilities button
            val btnAbilities = view.findViewById<Button>(R.id.btnTabAbilities)
            android.util.Log.d("AdminFragment", "Abilities button found: ${btnAbilities != null}")
            
            btnAbilities?.setOnClickListener {
                android.util.Log.d("AdminFragment", "Abilities button clicked")
                try {
                    // Get local reference to avoid smart cast issues
                    val localContentView = contentView
                    if (localContentView != null) {
                        // Remove the test text first
                        localContentView.removeAllViews()
                        
                        android.util.Log.d("AdminFragment", "Loading AbilitiesFragment")
                        
                        // First, just show a simple message to test if the button works
                        val testText = android.widget.TextView(requireContext()).apply {
                            text = "Abilities Tab Selected!\n\nLoading abilities..."
                            textSize = 16f
                            setPadding(32, 32, 32, 32)
                            gravity = android.view.Gravity.CENTER
                            setTextColor(android.graphics.Color.BLUE)
                        }
                        localContentView.addView(testText)
                        
                        // Try to load the fragment after a short delay
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            try {
                                localContentView.removeAllViews()
                                val abilitiesFragment = AbilitiesFragment()
                                childFragmentManager.beginTransaction()
                                    .add(R.id.adminContent, abilitiesFragment, "abilities")
                                    .addToBackStack("abilities")
                                    .commit()
                                android.util.Log.d("AdminFragment", "AbilitiesFragment loaded successfully")
                            } catch (e: Exception) {
                                android.util.Log.e("AdminFragment", "Error loading AbilitiesFragment", e)
                                localContentView.removeAllViews()
                                val errorText = android.widget.TextView(requireContext()).apply {
                                    text = "Error loading Abilities:\n${e.message}"
                                    textSize = 14f
                                    setPadding(32, 32, 32, 32)
                                    gravity = android.view.Gravity.CENTER
                                    setTextColor(android.graphics.Color.RED)
                                }
                                localContentView.addView(errorText)
                            }
                        }, 1000) // 1 second delay
                    } else {
                        android.util.Log.e("AdminFragment", "contentView is null when trying to load Abilities")
                        android.widget.Toast.makeText(requireContext(), "Error: Content view not available", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    
                } catch (e: Exception) {
                    android.util.Log.e("AdminFragment", "Error switching to Abilities", e)
                    android.widget.Toast.makeText(requireContext(), "Error loading Abilities: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            
            // Set up Tasks button
            val btnTasks = view.findViewById<Button>(R.id.btnTabTasks)
            android.util.Log.d("AdminFragment", "Tasks button found: ${btnTasks != null}")
            
            btnTasks?.setOnClickListener {
                android.util.Log.d("AdminFragment", "Tasks button clicked")
                try {
                    // Get local reference to avoid smart cast issues
                    val localContentView = contentView
                    if (localContentView != null) {
                        // Remove the test text first
                        localContentView.removeAllViews()
                        
                        android.util.Log.d("AdminFragment", "Loading TasksFragment")
                        
                        // First, just show a simple message to test if the button works
                        val testText = android.widget.TextView(requireContext()).apply {
                            text = "Level Tasks Tab Selected!\n\nLoading tasks..."
                            textSize = 16f
                            setPadding(32, 32, 32, 32)
                            gravity = android.view.Gravity.CENTER
                            setTextColor(android.graphics.Color.GREEN)
                        }
                        localContentView.addView(testText)
                        
                        // Try to load the fragment after a short delay
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            try {
                                localContentView.removeAllViews()
                                
                                // Check if we're still attached to the activity
                                if (!isAdded || isDetached) {
                                    android.util.Log.w("AdminFragment", "Fragment not attached, skipping TasksFragment load")
                                    return@postDelayed
                                }
                                
                                val tasksFragment = TasksFragment()
                                
                                // Check if we're still valid
                                if (!isAdded || isDetached || activity == null) {
                                    android.util.Log.w("AdminFragment", "Fragment not valid, skipping TasksFragment load")
                                    return@postDelayed
                                }
                                
                                // Check if the childFragmentManager is valid
                                if (childFragmentManager.isDestroyed || childFragmentManager.isStateSaved) {
                                    android.util.Log.w("AdminFragment", "ChildFragmentManager not valid, skipping TasksFragment load")
                                    return@postDelayed
                                }
                                
                                // Use commitAllowingStateLoss to avoid state loss issues
                                try {
                                    childFragmentManager.beginTransaction()
                                        .replace(R.id.adminContent, tasksFragment, "tasks")
                                        .addToBackStack("tasks")
                                        .commitAllowingStateLoss()
                                        
                                    android.util.Log.d("AdminFragment", "TasksFragment loaded successfully")
                                } catch (transactionException: Exception) {
                                    android.util.Log.e("AdminFragment", "Fragment transaction failed", transactionException)
                                    throw transactionException
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("AdminFragment", "Error loading TasksFragment", e)
                                localContentView.removeAllViews()
                                val errorText = android.widget.TextView(requireContext()).apply {
                                    text = "Error loading Tasks:\n${e.message}\n\nPlease try again or check the logs."
                                    textSize = 14f
                                    setPadding(32, 32, 32, 32)
                                    gravity = android.view.Gravity.CENTER
                                    setTextColor(android.graphics.Color.RED)
                                }
                                localContentView.addView(errorText)
                                
                                // Add a retry button
                                val retryButton = android.widget.Button(requireContext()).apply {
                                    text = "Retry"
                                    setOnClickListener {
                                        // Remove error message and try again
                                        localContentView.removeAllViews()
                                        val retryText = android.widget.TextView(requireContext()).apply {
                                            text = "Retrying..."
                                            textSize = 16f
                                            setPadding(32, 32, 32, 32)
                                            gravity = android.view.Gravity.CENTER
                                            setTextColor(android.graphics.Color.BLUE)
                                        }
                                        localContentView.addView(retryText)
                                        
                                        // Try to load again after a short delay
                                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                            try {
                                                localContentView.removeAllViews()
                                                
                                                // Check if we're still valid
                                                if (!isAdded || isDetached || activity == null) {
                                                    android.util.Log.w("AdminFragment", "Fragment not valid during retry, skipping")
                                                    return@postDelayed
                                                }
                                                
                                                // Check if the childFragmentManager is valid
                                                if (childFragmentManager.isDestroyed || childFragmentManager.isStateSaved) {
                                                    android.util.Log.w("AdminFragment", "ChildFragmentManager not valid during retry, skipping")
                                                    return@postDelayed
                                                }
                                                
                                                val retryFragment = TasksFragment()
                                                childFragmentManager.beginTransaction()
                                                    .replace(R.id.adminContent, retryFragment, "tasks")
                                                    .addToBackStack("tasks")
                                                    .commitAllowingStateLoss()
                                            } catch (retryException: Exception) {
                                                android.util.Log.e("AdminFragment", "Retry failed", retryException)
                                                localContentView.removeAllViews()
                                                val finalErrorText = android.widget.TextView(requireContext()).apply {
                                                    text = "Retry failed:\n${retryException.message}"
                                                    textSize = 14f
                                                    setPadding(32, 32, 32, 32)
                                                    gravity = android.view.Gravity.CENTER
                                                    setTextColor(android.graphics.Color.RED)
                                                }
                                                localContentView.addView(finalErrorText)
                                            }
                                        }, 500)
                                    }
                                }
                                localContentView.addView(retryButton)
                            }
                        }, 1000) // 1 second delay
                    } else {
                        android.util.Log.e("AdminFragment", "contentView is null when trying to load Tasks")
                        android.widget.Toast.makeText(requireContext(), "Error: Content view not available", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    
                } catch (e: Exception) {
                    android.util.Log.e("AdminFragment", "Error switching to Tasks", e)
                    android.widget.Toast.makeText(requireContext(), "Error loading Tasks: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            
            // Add button to force add comprehensive abilities even if database exists
            val forceAddButton = android.widget.Button(requireContext()).apply {
                text = "Force Add All Abilities (Overwrite)"
                textSize = 14f
                setPadding(20, 10, 20, 10)
                setBackgroundColor(android.graphics.Color.rgb(255, 152, 0)) // Orange
                setTextColor(android.graphics.Color.WHITE)
                tag = "forceAddButton" // Add tag for identification
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 16
                }
                

                
                // Check if all abilities are already present and disable button accordingly
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val db = com.example.vampire_system.data.db.AppDatabase.get(requireContext())
                        val abilityCount = db.abilityDao().getAllOnce().size
                        val levelCount = db.levelDao().getAll().firstOrNull()?.size ?: 0
                        
                        withContext(Dispatchers.Main) {
                            // Use the button reference safely
                            this@apply.apply {
                                if (abilityCount >= 100 && levelCount >= 100) {
                                    // All abilities and levels are present
                                    isEnabled = false
                                    text = "✅ All Abilities Already Added"
                                    setBackgroundColor(android.graphics.Color.rgb(158, 158, 158)) // Gray
                                    setTextColor(android.graphics.Color.rgb(97, 97, 97)) // Dark gray text
                                } else {
                                    // Some abilities/levels are missing
                                    isEnabled = true
                                    text = "Force Add All Abilities (Overwrite)"
                                    setBackgroundColor(android.graphics.Color.rgb(255, 152, 0)) // Orange
                                    setTextColor(android.graphics.Color.WHITE)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminFragment", "Error checking ability count", e)
                        // Keep button enabled if we can't check
                        withContext(Dispatchers.Main) {
                            this@apply.isEnabled = true
                        }
                    }
                }
                
                setOnClickListener {
                    try {
                        android.widget.Toast.makeText(requireContext(), "Force adding all abilities...", android.widget.Toast.LENGTH_SHORT).show()
                        
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val db = com.example.vampire_system.data.db.AppDatabase.get(requireContext())
                                // Use the force seed method that clears everything first
                                com.example.vampire_system.data.db.PlanSeederComprehensive.forceSeed(db)
                                
                                withContext(Dispatchers.Main) {
                                    android.widget.Toast.makeText(requireContext(), "✅ All abilities force-added successfully!", android.widget.Toast.LENGTH_LONG).show()
                                    loadInitialContent()
                                    
                                    // Update button state to show completion
                                    // The button will be updated by loadInitialContent() which calls updateForceAddButtonState
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("AdminFragment", "Error force adding abilities", e)
                                withContext(Dispatchers.Main) {
                                    android.widget.Toast.makeText(requireContext(), "Error force adding abilities: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminFragment", "Error in force add button", e)
                        android.widget.Toast.makeText(requireContext(), "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            // Add the button to the main view
            val mainLayout = view.findViewById<android.widget.FrameLayout>(R.id.adminContent)
            if (mainLayout != null) {
                mainLayout.addView(forceAddButton)
            }
        } catch (e: Exception) {
            // Log error and show toast
            android.util.Log.e("AdminFragment", "Error in onViewCreated", e)
            android.widget.Toast.makeText(requireContext(), "Error loading Admin Studio: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            
            // Show error in the content area if available
            val localContentView = contentView
            if (localContentView != null) {
                localContentView.removeAllViews()
                val errorText = android.widget.TextView(requireContext()).apply {
                    text = "Admin Studio Error:\n${e.message}"
                    textSize = 14f
                    setPadding(32, 32, 32, 32)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(android.graphics.Color.RED)
                }
                localContentView.addView(errorText)
            }
        }
    }
}


