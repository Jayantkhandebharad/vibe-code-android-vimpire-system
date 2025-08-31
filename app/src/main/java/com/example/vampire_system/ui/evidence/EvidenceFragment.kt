package com.example.vampire_system.ui.evidence

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.core.content.FileProvider
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.EvidenceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EvidenceFragment : Fragment() {
    
    private lateinit var adapter: EvidenceAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var dateFilter: EditText
    private lateinit var taskFilter: EditText
    private lateinit var btnFilter: Button
    private lateinit var btnClear: Button
    private lateinit var btnBack: Button
    private lateinit var tvCount: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_evidence, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        recyclerView = view.findViewById(R.id.evidenceList)
        dateFilter = view.findViewById(R.id.dateFilter)
        taskFilter = view.findViewById(R.id.taskFilter)
        btnFilter = view.findViewById(R.id.btnFilter)
        btnClear = view.findViewById(R.id.btnClear)
        btnBack = view.findViewById(R.id.btnBack)
        tvCount = view.findViewById(R.id.tvCount)
        
        // Set up RecyclerView
        adapter = EvidenceAdapter { evidence ->
            // Handle evidence item click
            openEvidence(evidence)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        // Set up button listeners
        btnBack.setOnClickListener {
            // Navigate back to history tab
            requireActivity().onBackPressed()
        }
        
        btnFilter.setOnClickListener {
            val date = dateFilter.text.toString().takeIf { it.isNotBlank() }
            val task = taskFilter.text.toString().takeIf { it.isNotBlank() }
            loadEvidence(date, task)
        }
        
        btnClear.setOnClickListener {
            dateFilter.setText("")
            taskFilter.setText("")
            loadEvidence(null, null)
        }
        
        // Load initial evidence
        loadEvidence(null, null)
    }
    
    private fun loadEvidence(dateFilter: String?, taskFilter: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.get(requireContext())
            val evidence = mutableListOf<EvidenceEntity>()
            
            db.openHelper.readableDatabase.query(
                "SELECT * FROM evidence ORDER BY createdAt DESC",
                emptyArray()
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val evidenceEntity = EvidenceEntity(
                        id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        questInstanceId = cursor.getString(cursor.getColumnIndexOrThrow("questInstanceId")),
                        kind = com.example.vampire_system.data.model.EvidenceKind.valueOf(
                            cursor.getString(cursor.getColumnIndexOrThrow("kind"))
                        ),
                        uriOrText = cursor.getString(cursor.getColumnIndexOrThrow("uriOrText")),
                        meta = emptyMap(), // We'll use empty map for now
                        sha256 = cursor.getString(cursor.getColumnIndexOrThrow("sha256")),
                        createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt"))
                    )
                    evidence.add(evidenceEntity)
                }
            }
            
            val filtered = evidence.filter { evidence ->
                val matchesDate = dateFilter == null || evidence.createdAt.toString().contains(dateFilter)
                val matchesTask = taskFilter == null || evidence.questInstanceId.contains(taskFilter)
                matchesDate && matchesTask
            }
            
            withContext(Dispatchers.Main) {
                adapter.submitList(filtered)
                tvCount.text = "Showing ${filtered.size} of ${evidence.size} evidence items"
            }
        }
    }
    
    private fun openEvidence(evidence: EvidenceEntity) {
        // Handle different types of evidence
        when (evidence.kind) {
            com.example.vampire_system.data.model.EvidenceKind.PHOTO -> {
                // Open photo viewer
                openPhotoEvidence(evidence)
            }
            com.example.vampire_system.data.model.EvidenceKind.VIDEO -> {
                // Open video player
                openVideoEvidence(evidence)
            }
            com.example.vampire_system.data.model.EvidenceKind.NOTE -> {
                // Show note content
                showNoteEvidence(evidence)
            }
            else -> {
                // Show general evidence details
                showEvidenceDetails(evidence)
            }
        }
    }
    
    private fun openPhotoEvidence(evidence: EvidenceEntity) {
        try {
            val file = java.io.File(evidence.uriOrText)
            android.util.Log.d("EvidenceFragment", "Attempting to open photo: ${file.path}")
            android.util.Log.d("EvidenceFragment", "File exists: ${file.exists()}")
            android.util.Log.d("EvidenceFragment", "File size: ${if (file.exists()) file.length() else "N/A"}")
            
            if (file.exists()) {
                try {
                    // Use FileProvider to share internal files securely
                    val authority = "${requireContext().packageName}.fileprovider"
                    android.util.Log.d("EvidenceFragment", "Using authority: $authority")
                    
                    val uri = FileProvider.getUriForFile(requireContext(), authority, file)
                    android.util.Log.d("EvidenceFragment", "Generated URI: $uri")
                    
                                         val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                     intent.setDataAndType(uri, "image/*")
                     intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                     
                     android.util.Log.d("EvidenceFragment", "Intent: $intent")
                     
                     // Use chooser to let user select which app to use
                     val chooser = android.content.Intent.createChooser(intent, "Open Photo With")
                     android.util.Log.d("EvidenceFragment", "Using chooser intent")
                     
                     try {
                         startActivity(chooser)
                         android.util.Log.d("EvidenceFragment", "Chooser activity started successfully")
                     } catch (e: Exception) {
                         android.util.Log.e("EvidenceFragment", "Error starting chooser: ${e.message}")
                         // Fallback to generic method
                         tryOpenGeneric(evidence)
                     }
                } catch (e: Exception) {
                    android.util.Log.e("EvidenceFragment", "FileProvider error: ${e.message}")
                    android.util.Log.e("EvidenceFragment", "Stack trace: ${e.stackTraceToString()}")
                    // Fallback to generic method
                    tryOpenGeneric(evidence)
                }
            } else {
                android.widget.Toast.makeText(requireContext(), "File not found: ${file.path}", android.widget.Toast.LENGTH_SHORT).show()
                android.util.Log.e("EvidenceFragment", "File not found: ${file.path}")
                showEvidenceDetails(evidence)
            }
        } catch (e: Exception) {
            android.util.Log.e("EvidenceFragment", "Error opening photo: ${e.message}")
            android.util.Log.e("EvidenceFragment", "Stack trace: ${e.stackTraceToString()}")
            showEvidenceDetails(evidence)
        }
    }
    
    private fun openVideoEvidence(evidence: EvidenceEntity) {
        try {
            val file = java.io.File(evidence.uriOrText)
            if (file.exists()) {
                // Use FileProvider to share internal files securely
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )
                
                                 val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                 intent.setDataAndType(uri, "video/*")
                 intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                 
                 // Use chooser to let user select which app to use
                 val chooser = android.content.Intent.createChooser(intent, "Open Video With")
                 
                 try {
                     startActivity(chooser)
                 } catch (e: Exception) {
                     android.util.Log.e("EvidenceFragment", "Error starting video chooser: ${e.message}")
                     showEvidenceDetails(evidence)
                 }
            } else {
                android.widget.Toast.makeText(requireContext(), "File not found: ${file.path}", android.widget.Toast.LENGTH_SHORT).show()
                showEvidenceDetails(evidence)
            }
        } catch (e: Exception) {
            android.util.Log.e("EvidenceFragment", "Error opening video: ${e.message}")
            showEvidenceDetails(evidence)
        }
    }
    
    private fun showNoteEvidence(evidence: EvidenceEntity) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Note Evidence")
            .setMessage(evidence.uriOrText)
            .setPositiveButton("Close", null)
            .show()
    }
    
    private fun showEvidenceDetails(evidence: EvidenceEntity) {
        val details = """
            Evidence ID: ${evidence.id}
            Type: ${evidence.kind}
            Quest: ${evidence.questInstanceId}
            Created: ${java.time.Instant.ofEpochMilli(evidence.createdAt)
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}
            SHA256: ${evidence.sha256}
            ${if (evidence.uriOrText.isNotBlank()) "Content: ${evidence.uriOrText}" else ""}
            
            DEBUG INFO:
            URI Format: ${evidence.uriOrText}
            URI Type: ${if (evidence.uriOrText.startsWith("content://")) "Content URI" else if (evidence.uriOrText.startsWith("file://")) "File URI" else if (evidence.uriOrText.startsWith("/")) "Absolute Path" else "Other"}
        """.trimIndent()
        
        val builder = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Evidence Details")
            .setMessage(details)
            .setPositiveButton("Close", null)
            .setNeutralButton("Try Open") { _, _ ->
                // Try to open with generic intent
                tryOpenGeneric(evidence)
            }
            .setNegativeButton("Test FileProvider") { _, _ ->
                // Test FileProvider specifically
                testFileProvider(evidence)
            }
        
        // Add "Open Photo" button for photo evidence
        if (evidence.kind == com.example.vampire_system.data.model.EvidenceKind.PHOTO) {
            builder.setNeutralButton("Open Photo") { _, _ ->
                openPhotoEvidence(evidence)
            }
        }
        
        builder.show()
    }
    
    private fun testFileProvider(evidence: EvidenceEntity) {
        try {
            val file = java.io.File(evidence.uriOrText)
            if (file.exists()) {
                val authority = "${requireContext().packageName}.fileprovider"
                android.util.Log.d("EvidenceFragment", "Testing FileProvider with authority: $authority")
                android.util.Log.d("EvidenceFragment", "File path: ${file.path}")
                
                val uri = FileProvider.getUriForFile(requireContext(), authority, file)
                android.util.Log.d("EvidenceFragment", "Generated URI: $uri")
                
                // Show the URI in a dialog for debugging
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("FileProvider Test")
                    .setMessage("""
                        File exists: ${file.exists()}
                        File size: ${file.length()} bytes
                        Authority: $authority
                        Generated URI: $uri
                        File path: ${file.path}
                    """.trimIndent())
                    .setPositiveButton("OK", null)
                    .show()
                
            } else {
                android.widget.Toast.makeText(requireContext(), "File not found for testing", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("EvidenceFragment", "FileProvider test error: ${e.message}")
            android.widget.Toast.makeText(requireContext(), "FileProvider test failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun tryOpenGeneric(evidence: EvidenceEntity) {
        try {
            val uri = when {
                evidence.uriOrText.startsWith("content://") -> android.net.Uri.parse(evidence.uriOrText)
                evidence.uriOrText.startsWith("file://") -> android.net.Uri.parse(evidence.uriOrText)
                evidence.uriOrText.startsWith("/") -> android.net.Uri.fromFile(java.io.File(evidence.uriOrText))
                else -> android.net.Uri.parse(evidence.uriOrText)
            }
            
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.setData(uri)
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                android.widget.Toast.makeText(requireContext(), "No app can handle this URI", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

// Evidence Adapter
class EvidenceAdapter(
    private val onItemClick: (EvidenceEntity) -> Unit
) : ListAdapter<EvidenceEntity, EvidenceAdapter.EvidenceViewHolder>(EvidenceDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvidenceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evidence_list, parent, false)
        return EvidenceViewHolder(view, onItemClick)
    }
    
    override fun onBindViewHolder(holder: EvidenceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class EvidenceViewHolder(
        itemView: View,
        private val onItemClick: (EvidenceEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle = itemView.findViewById<TextView>(R.id.tvEvidenceTitle)
        private val tvSub = itemView.findViewById<TextView>(R.id.tvEvidenceSub)
        private val tvDate = itemView.findViewById<TextView>(R.id.tvEvidenceDate)
        
        fun bind(evidence: EvidenceEntity) {
            tvTitle.text = evidence.kind.name
            tvSub.text = "Quest: ${evidence.questInstanceId}"
            val date = java.time.Instant.ofEpochMilli(evidence.createdAt)
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
            tvDate.text = "Created: $date"
            
            // Set click listener
            itemView.setOnClickListener {
                onItemClick(evidence)
            }
        }
    }
}

class EvidenceDiffCallback : DiffUtil.ItemCallback<EvidenceEntity>() {
    override fun areItemsTheSame(oldItem: EvidenceEntity, newItem: EvidenceEntity) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: EvidenceEntity, newItem: EvidenceEntity) = oldItem == newItem
}
