package com.example.vampire_system.features.evidence

import android.app.Activity
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*

class EvidenceBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(questId: String) = EvidenceBottomSheet().apply {
            arguments = Bundle().apply { putString("qi", questId) }
        }
    }

    private val qi by lazy { requireArguments().getString("qi")!! }
    private val db by lazy { AppDatabase.get(requireContext()) }
    private val mgr by lazy { EvidenceManager(requireContext(), db) }
    private var pendingFile: java.io.File? = null
    private var recorder: MediaRecorder? = null

    private val photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            pendingFile?.let { file ->
                CoroutineScope(Dispatchers.IO).launch { mgr.savePhoto(qi, file) }
            }
        }
    }
    private val videoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            pendingFile?.let { file ->
                CoroutineScope(Dispatchers.IO).launch { mgr.saveVideo(qi, file) }
            }
        }
    }
    private val pickFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        CoroutineScope(Dispatchers.IO).launch { mgr.importFromUri(qi, uri) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.sheet_evidence, container, false)

        v.findViewById<View>(R.id.btnNote).setOnClickListener {
            val input = v.findViewById<EditText>(R.id.txtNote)
            val text = input.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch { mgr.addText(qi, text) }
                input.setText("")
                dismiss()
            } else Toast.makeText(requireContext(),"Write a note first",Toast.LENGTH_SHORT).show()
        }

        v.findViewById<View>(R.id.btnLink).setOnClickListener {
            val input = v.findViewById<EditText>(R.id.txtLink)
            val url = input.text?.toString()?.trim().orEmpty()
            if (url.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch { mgr.addLink(qi, url) }
                input.setText("")
                dismiss()
            } else Toast.makeText(requireContext(),"Paste a link",Toast.LENGTH_SHORT).show()
        }

        v.findViewById<View>(R.id.btnTimer5).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch { mgr.addTimer(qi, 5) }
            dismiss()
        }

        v.findViewById<View>(R.id.btnPhoto).setOnClickListener {
            val (file, uri) = mgr.preparePhotoCapture()
            pendingFile = file
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            photoLauncher.launch(intent)
        }

        v.findViewById<View>(R.id.btnVideo).setOnClickListener {
            val (file, uri) = mgr.prepareVideoCapture()
            pendingFile = file
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
                putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            videoLauncher.launch(intent)
        }

        v.findViewById<View>(R.id.btnPickFile).setOnClickListener {
            pickFile.launch(arrayOf("*/*"))
        }

        val recBtn = v.findViewById<ToggleButton>(R.id.btnAudio)
        recBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val file = mgr.newAudioFile()
                pendingFile = file
                try {
                    recorder = mgr.startAudioRecorder(file)
                    Toast.makeText(requireContext(),"Recording…",Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    recBtn.isChecked = false
                    Toast.makeText(requireContext(),"Mic error: ${e.message}",Toast.LENGTH_LONG).show()
                }
            } else {
                try {
                    recorder?.stop(); recorder?.release(); recorder = null
                    pendingFile?.let { f -> CoroutineScope(Dispatchers.IO).launch { mgr.saveAudio(qi, f) } }
                    dismiss()
                } catch (_: Exception) { }
            }
        }

        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try { recorder?.stop(); recorder?.release() } catch (_: Exception) {}
        recorder = null
    }
}


