package com.example.vampire_system.features.evidence

import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.EvidenceEntity
import com.example.vampire_system.data.model.EvidenceKind
import com.example.vampire_system.util.Share
import com.example.vampire_system.util.Thumbs
import kotlinx.coroutines.*
import java.io.File
import coil.load

class GalleryFragment : Fragment() {
    private val io = CoroutineScope(Dispatchers.IO)
    private lateinit var qi: String
    private lateinit var adapter: EviAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qi = requireArguments().getString("qi") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set up back button
        view.findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        
        val grid = view.findViewById<RecyclerView>(R.id.grid)
        grid.setHasFixedSize(true)
        grid.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = EviAdapter(
            onOpen = { open(it) },
            onShare = { share(it) },
            thumb = { thumbFor(it) }
        )
        grid.adapter = adapter
        load()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the evidence list when returning to this fragment
        load()
    }

    private fun load() = io.launch {
        val db = AppDatabase.get(requireContext())
        val items = db.evidenceDao().forQuestOnce(qi)
        withContext(Dispatchers.Main) { adapter.submit(items) }
    }

    private fun thumbFor(e: EvidenceEntity): Bitmap? = null // Coil will handle loading

    private fun open(e: EvidenceEntity) {
        when (e.kind) {
            EvidenceKind.PHOTO -> Share.viewFile(requireContext(), File(e.uriOrText), "image/*")
            EvidenceKind.VIDEO -> Share.viewFile(requireContext(), File(e.uriOrText), "video/*")
            EvidenceKind.AUDIO -> Share.viewFile(requireContext(), File(e.uriOrText), "audio/*")
            EvidenceKind.FILE  -> Share.viewFile(requireContext(), File(e.uriOrText), "*/*")
            EvidenceKind.LINK  -> startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(e.uriOrText)))
            EvidenceKind.NOTE, EvidenceKind.TIMER, EvidenceKind.CHECKLIST -> {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle(e.kind.name)
                    .setMessage(
                        when (e.kind) {
                            EvidenceKind.NOTE -> e.uriOrText
                            EvidenceKind.TIMER -> "Timer: ${e.uriOrText} min"
                            EvidenceKind.CHECKLIST -> e.uriOrText.ifBlank { "Checklist" }
                            else -> e.uriOrText
                        }
                    )
                    .setPositiveButton("OK", null).show()
            }
            // Handle all other evidence types as general content
            else -> {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle(e.kind.name.lowercase().replaceFirstChar { it.uppercase() })
                    .setMessage(e.uriOrText)
                    .setPositiveButton("OK", null).show()
            }
        }
    }

    private fun share(e: EvidenceEntity) {
        when (e.kind) {
            EvidenceKind.PHOTO -> Share.shareFile(requireContext(), File(e.uriOrText), "image/*")
            EvidenceKind.VIDEO -> Share.shareFile(requireContext(), File(e.uriOrText), "video/*")
            EvidenceKind.AUDIO -> Share.shareFile(requireContext(), File(e.uriOrText), "audio/*")
            EvidenceKind.FILE  -> Share.shareFile(requireContext(), File(e.uriOrText), "*/*")
            EvidenceKind.LINK  -> {
                val i = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, e.uriOrText)
                }
                startActivity(android.content.Intent.createChooser(i, "Share link"))
            }
            EvidenceKind.NOTE, EvidenceKind.TIMER, EvidenceKind.CHECKLIST -> {
                val text = when (e.kind) {
                    EvidenceKind.NOTE -> e.uriOrText
                    EvidenceKind.TIMER -> "Timer: ${e.uriOrText} min"
                    EvidenceKind.CHECKLIST -> e.uriOrText.ifBlank { "Checklist" }
                    else -> e.uriOrText
                }
                val i = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, text)
                }
                startActivity(android.content.Intent.createChooser(i, "Share"))
            }
            // Handle all other evidence types as text content
            else -> {
                val i = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, e.uriOrText)
                }
                startActivity(android.content.Intent.createChooser(i, "Share ${e.kind.name.lowercase()}"))
            }
        }
    }
}

class EviAdapter(
    private val onOpen: (EvidenceEntity) -> Unit,
    private val onShare: (EvidenceEntity) -> Unit,
    private val thumb: (EvidenceEntity) -> Bitmap?
) : RecyclerView.Adapter<EviVH>() {
    private val items = mutableListOf<EvidenceEntity>()
    fun submit(list: List<EvidenceEntity>) { items.clear(); items.addAll(list); notifyDataSetChanged() }
    override fun onCreateViewHolder(p: ViewGroup, v: Int): EviVH {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_evidence_grid, p, false)
        return EviVH(view)
    }
    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: EviVH, pos: Int) = h.bind(items[pos], onOpen, onShare, thumb)
}

class EviVH(v: View) : RecyclerView.ViewHolder(v) {
    private val img = v.findViewById<ImageView>(R.id.img)
    private val caption = v.findViewById<TextView>(R.id.caption)
    private val btnOpen = v.findViewById<Button>(R.id.btnOpen)
    private val btnShare = v.findViewById<Button>(R.id.btnShare)
    fun bind(e: EvidenceEntity, onOpen: (EvidenceEntity)->Unit, onShare: (EvidenceEntity)->Unit, thumb: (EvidenceEntity)->Bitmap?) {
        caption.text = when (e.kind) {
            EvidenceKind.PHOTO -> "Photo"
            EvidenceKind.VIDEO -> "Video"
            EvidenceKind.AUDIO -> "Audio"
            EvidenceKind.FILE  -> "File"
            EvidenceKind.LINK  -> "Link"
            EvidenceKind.NOTE  -> "Note"
            EvidenceKind.TIMER -> "Timer"
            EvidenceKind.CHECKLIST -> "Checklist"
            // Handle all other evidence types with a generic label
            else -> e.kind.name.lowercase().replaceFirstChar { it.uppercase() }
        }
        
        when (e.kind) {
            EvidenceKind.PHOTO, EvidenceKind.FILE -> {
                img.load(java.io.File(e.uriOrText)) {
                    size(coil.size.Size.ORIGINAL)
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_foreground)
                    error(R.drawable.ic_launcher_foreground)
                }
            }
            EvidenceKind.VIDEO -> {
                img.load(java.io.File(e.uriOrText)) {
                    decoderFactory(coil.decode.VideoFrameDecoder.Factory())
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_foreground)
                }
            }
            // Handle all other evidence types with default icon
            else -> img.setImageResource(R.drawable.ic_launcher_foreground)
        }
        
        btnOpen.setOnClickListener { onOpen(e) }
        btnShare.setOnClickListener { onShare(e) }
    }
}


