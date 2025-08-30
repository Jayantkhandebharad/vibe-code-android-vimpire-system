package com.example.vampire_system.ui.search

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vampire_system.R
import com.example.vampire_system.data.db.*
import com.example.vampire_system.domain.search.SearchIndexer
import com.example.vampire_system.domain.search.SearchPagingSource
import com.example.vampire_system.features.search.SearchPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class SearchFragment : Fragment() {
    private lateinit var sv: SearchView
    private lateinit var rvPins: RecyclerView
    private lateinit var spKind: Spinner
    private lateinit var btnPickAbilities: Button
    private lateinit var tvAbilitiesPreview: TextView
    private lateinit var rsXp: com.google.android.material.slider.RangeSlider
    private lateinit var tvXpLabel: TextView
    private lateinit var cbAllWords: CheckBox
    private lateinit var btnFrom: Button
    private lateinit var btnTo: Button
    private lateinit var cbDone: CheckBox
    private lateinit var cbEvidence: CheckBox
    private lateinit var list: RecyclerView
    private lateinit var adapter: SearchAdapter
    private lateinit var pins: PinAdapter

    private val selectedAbilities = linkedSetOf<String>() // preserves order
    private var minXp: Int? = null
    private var maxXp: Int? = null
    private var tz: String = "Asia/Kolkata"

    private var q: String = ""
    private var kind: SearchKind? = null
    private var from: String? = null
    private var to: String? = null
    private var doneOnly = false
    private var hasEvidence = false

    private lateinit var suggestPopup: ListPopupWindow
    private var suggestions: List<String> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sv = view.findViewById(R.id.searchView)
        rvPins = view.findViewById(R.id.rvPins)
        spKind = view.findViewById(R.id.spKind)
        btnPickAbilities = view.findViewById(R.id.btnPickAbilities)
        tvAbilitiesPreview = view.findViewById(R.id.tvAbilitiesPreview)
        rsXp = view.findViewById(R.id.rsXp)
        tvXpLabel = view.findViewById(R.id.tvXpLabel)
        cbAllWords = view.findViewById(R.id.cbAllWords)
        btnFrom = view.findViewById(R.id.btnFrom)
        btnTo = view.findViewById(R.id.btnTo)
        cbDone = view.findViewById(R.id.cbDone)
        cbEvidence = view.findViewById(R.id.cbEvidence)
        list = view.findViewById(R.id.rv)

        // pins row
        rvPins.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        pins = PinAdapter(
            onClick = { applyPin(it) },
            onLong = { editOrDeletePin(it) }
        )
        rvPins.adapter = pins
        loadPins()

        // results
        list.layoutManager = LinearLayoutManager(requireContext())
        adapter = SearchAdapter(onClick = { openResult(it) })
        list.adapter = adapter

        setupFilters()
        setupNewControls()
        setupSuggestions()
        bindPager()
        bindButtons(view)

        // Load timezone setting in background
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val tzDb = AppDatabase.get(requireContext()).settingsDao().get()?.timezone
            if (!tzDb.isNullOrBlank()) tz = tzDb
        }
    }

    private fun setupFilters() {
        // Kind
        val kinds = arrayOf("All", "Notes", "Links", "Files", "Quests", "Tasks", "Abilities")
        spKind.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, kinds)
        spKind.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                kind = when (pos) {
                    1 -> SearchKind.NOTE
                    2 -> SearchKind.LINK
                    3 -> SearchKind.FILE
                    4 -> SearchKind.QUEST
                    5 -> SearchKind.TASK
                    6 -> SearchKind.ABILITY
                    else -> null
                }; bindPager()
            }
        }

        // Abilities - removed, now handled by multi-picker

        // toggles
        cbDone.setOnCheckedChangeListener { _, b -> doneOnly = b; bindPager() }
        cbEvidence.setOnCheckedChangeListener { _, b -> hasEvidence = b; bindPager() }

        // date pickers
        val pick: (Boolean) -> Unit = { isFrom ->
            val now = LocalDate.now()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val date = LocalDate.of(y, m+1, d).toString()
                if (isFrom) { from = date; btnFrom.text = "From: $date" } else { to = date; btnTo.text = "To: $date" }
                bindPager()
            }, now.year, now.month.value-1, now.dayOfMonth).show()
        }
        btnFrom.setOnClickListener { pick(true) }
        btnTo.setOnClickListener { pick(false) }
    }

    private fun setupNewControls() {
        // XP slider
        rsXp.addOnChangeListener { slider, _, _ ->
            val v = slider.values
            minXp = v[0].toInt()
            maxXp = v[1].toInt()
            tvXpLabel.text = "${minXp ?: 0}–${maxXp ?: 0} XP"
            bindPager()
        }

        // Multi-ability picker dialog
        btnPickAbilities.setOnClickListener { openAbilityMultiPicker() }

        // Date chips
        requireView().findViewById<Button>(R.id.chThisWeek).setOnClickListener { applyRange(RangePreset.THIS_WEEK) }
        requireView().findViewById<Button>(R.id.chLast7).setOnClickListener { applyRange(RangePreset.LAST_7) }
        requireView().findViewById<Button>(R.id.chLast30).setOnClickListener { applyRange(RangePreset.LAST_30) }
        requireView().findViewById<Button>(R.id.chClearDates).setOnClickListener { 
            from=null; to=null; btnFrom.text="From"; btnTo.text="To"; bindPager() 
        }
    }

    private enum class RangePreset { THIS_WEEK, LAST_7, LAST_30 }
    private fun applyRange(p: RangePreset) {
        val zone = java.time.ZoneId.of(tz)
        val today = java.time.LocalDate.now(zone)
        when (p) {
            RangePreset.LAST_7 -> { from = today.minusDays(6).toString(); to = today.toString() }
            RangePreset.LAST_30 -> { from = today.minusDays(29).toString(); to = today.toString() }
            RangePreset.THIS_WEEK -> {
                val dow = today.dayOfWeek.value // 1=Mon..7
                val monday = today.minusDays((dow-1).toLong())
                from = monday.toString(); to = today.toString()
            }
        }
        btnFrom.text = "From: $from"; btnTo.text = "To: $to"
        bindPager()
    }

    private fun openAbilityMultiPicker() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.get(requireContext())
            val all = db.abilityDao().getAllOnce().map { it.id }.sorted()
            launch(Dispatchers.Main) {
                val checked = BooleanArray(all.size) { i -> selectedAbilities.contains(all[i]) }
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Select abilities")
                    .setMultiChoiceItems(all.toTypedArray(), checked) { _, which, isChecked ->
                        if (isChecked) selectedAbilities.add(all[which]) else selectedAbilities.remove(all[which])
                    }
                    .setPositiveButton("Apply") { _, _ ->
                        btnPickAbilities.text = "Abilities (${selectedAbilities.size})"
                        tvAbilitiesPreview.text = selectedAbilities.joinToString(", ").take(80)
                        bindPager()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun ftsQuery(): String {
        val raw = q.trim()
        if (raw.isEmpty()) return raw
        if (cbAllWords.isChecked) {
            val tokens = raw.split(Regex("\\s+")).filter { it.isNotBlank() }.map {
                // quote tokens with special chars
                if (it.any { ch -> !ch.isLetterOrDigit() }) "\"$it\"" else it
            }
            return tokens.joinToString(" AND ")
        }
        return raw
    }

    private fun setupSuggestions() {
        suggestPopup = ListPopupWindow(requireContext())
        suggestPopup.anchorView = sv
        suggestPopup.setOnItemClickListener { _, _, pos, _ ->
            val s = suggestions[pos]; sv.setQuery(s, true)
        }

        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(txt: String?): Boolean {
                q = txt?.trim().orEmpty()
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) { SearchPrefs.pushRecent(requireContext(), q) }
                bindPager(); suggestPopup.dismiss(); return true
            }
            override fun onQueryTextChange(txt: String?): Boolean {
                q = txt?.trim().orEmpty()
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) { buildSuggestions(q) }
                bindPager(); return true
            }
        })
    }

    private suspend fun buildSuggestions(prefix: String) {
        val ctx = requireContext()
        val db = AppDatabase.get(ctx)
        val recents = SearchPrefs.getRecents(ctx).filter { it.startsWith(prefix, ignoreCase = true) }
        val abilities = db.abilityDao().getAllOnce().map { it.name }.filter { it.contains(prefix, true) }
        val tasks = (1..100).flatMap { db.levelTaskDao().forLevelOnce(it) }.map { it.spec }.filter { it.contains(prefix, true) }
        val top = (recents + abilities + tasks).distinct().take(8)
        suggestions = top
        lifecycleScope.launch(Dispatchers.Main) {
            val adapter = ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, top)
            suggestPopup.setAdapter(adapter)
            if (top.isEmpty()) suggestPopup.dismiss() else if (!suggestPopup.isShowing) suggestPopup.show()
        }
    }

    private fun bindButtons(root: View) {
        root.findViewById<Button>(R.id.btnRebuild).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                SearchIndexer(AppDatabase.get(requireContext())).rebuild()
                launch(Dispatchers.Main) { Toast.makeText(requireContext(), "Index rebuilt", Toast.LENGTH_SHORT).show(); bindPager() }
            }
        }
        root.findViewById<Button>(R.id.btnSavePin).setOnClickListener { savePinDialog() }
    }

    private fun bindPager() {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.get(requireContext())
            val abilities = selectedAbilities.toList()
            val flow = Pager(PagingConfig(pageSize = 40, prefetchDistance = 20, enablePlaceholders = false)) {
                SearchPagingSource(
                    db = db,
                    query = ftsQuery(),
                    kind = kind,
                    abilities = abilities,
                    from = from,
                    to = to,
                    doneOnly = doneOnly,
                    hasEvidence = hasEvidence,
                    minXp = minXp,
                    maxXp = maxXp
                )
            }.flow
            flow.collectLatest { data -> adapter.submitData(data) }
        }
    }

    // ----- Pins -----
    private fun loadPins() = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
        val items = AppDatabase.get(requireContext()).savedSearchDao().all()
        if (items.isEmpty()) {
            val dao = AppDatabase.get(requireContext()).savedSearchDao()
            val seed = listOf(
                SavedSearchEntity(UUID.randomUUID().toString(), "All Notes", null, SearchKind.NOTE, null, null, null, false, false, 0),
                SavedSearchEntity(UUID.randomUUID().toString(), "Done w/ Evidence", null, SearchKind.QUEST, null, null, null, true, true, 1),
                SavedSearchEntity(UUID.randomUUID().toString(), "Tasks", null, SearchKind.TASK, null, null, null, false, false, 2)
            )
            dao.upsert(seed[0]); dao.upsert(seed[1]); dao.upsert(seed[2])
            val newItems = dao.all()
            lifecycleScope.launch(Dispatchers.Main) { pins.submit(newItems) }
        } else {
            lifecycleScope.launch(Dispatchers.Main) { pins.submit(items) }
        }
    }

    private fun savePinDialog() {
        val input = EditText(requireContext()); input.hint = "Name this pin"
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Save current filters as a Pin")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text?.toString()?.ifBlank { "Saved" } ?: "Saved"
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppDatabase.get(requireContext()).savedSearchDao()
                    val order = (dao.all().maxOfOrNull { it.orderIdx } ?: -1) + 1
                    dao.upsert(
                        SavedSearchEntity(
                            id = UUID.randomUUID().toString(),
                            title = name, query = q.ifBlank { null },
                            kind = kind, ability = if (selectedAbilities.isEmpty()) null else selectedAbilities.joinToString(","), from = from, to = to,
                            doneOnly = doneOnly, hasEvidence = hasEvidence,
                            orderIdx = order
                        )
                    )
                    loadPins()
                }
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun applyPin(p: SavedSearchEntity) {
        q = p.query ?: ""; sv.setQuery(q, false)
        kind = p.kind; setSpinnerKind(p.kind)
        selectedAbilities.clear()
        p.ability?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let { selectedAbilities.addAll(it) }
        btnPickAbilities.text = "Abilities (${selectedAbilities.size})"
        tvAbilitiesPreview.text = selectedAbilities.joinToString(", ").take(80)
        from = p.from; to = p.to
        btnFrom.text = if (from != null) "From: $from" else "From"
        btnTo.text = if (to != null) "To: $to" else "To"
        doneOnly = p.doneOnly; cbDone.isChecked = doneOnly
        hasEvidence = p.hasEvidence; cbEvidence.isChecked = hasEvidence
        bindPager()
    }
    private fun editOrDeletePin(p: SavedSearchEntity) {
        val actions = arrayOf("Rename", "Delete")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(p.title)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> { // rename
                        val input = EditText(requireContext()); input.setText(p.title)
                        android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Rename Pin")
                            .setView(input)
                            .setPositiveButton("Save") { _, _ ->
                                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                    val dao = AppDatabase.get(requireContext()).savedSearchDao()
                                    dao.upsert(p.copy(title = input.text.toString()))
                                    loadPins()
                                }
                            }.setNegativeButton("Cancel", null).show()
                    }
                    1 -> { // delete
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            AppDatabase.get(requireContext()).savedSearchDao().delete(p.id)
                            loadPins()
                        }
                    }
                }
            }.show()
    }

    private fun setSpinnerKind(k: SearchKind?) {
        val pos = when (k) { null -> 0; SearchKind.NOTE->1; SearchKind.LINK->2; SearchKind.FILE->3; SearchKind.QUEST->4; SearchKind.TASK->5; SearchKind.ABILITY->6 }
        spKind.setSelection(pos)
    }



    // open results (same as Phase 14)
    private fun openResult(row: SearchIndexEntity) {
        when {
            row.questInstanceId != null -> {
                val b = Bundle().apply { putString("qi", row.questInstanceId) }
                findNavController().navigate(R.id.navigation_gallery, b)
            }
            row.kind == SearchKind.ABILITY -> {
                findNavController().navigate(R.id.navigation_admin)
                Toast.makeText(requireContext(), "Admin → Abilities", Toast.LENGTH_SHORT).show()
            }
            row.kind == SearchKind.TASK -> {
                findNavController().navigate(R.id.navigation_admin)
                Toast.makeText(requireContext(), "Admin → Level Tasks", Toast.LENGTH_SHORT).show()
            }
            else -> {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle(row.title).setMessage(row.snippet ?: "").setPositiveButton("OK", null).show()
            }
        }
    }
}

private object SearchDiff : androidx.recyclerview.widget.DiffUtil.ItemCallback<SearchIndexEntity>() {
    override fun areItemsTheSame(a: SearchIndexEntity, b: SearchIndexEntity) = a.sid == b.sid
    override fun areContentsTheSame(a: SearchIndexEntity, b: SearchIndexEntity) = a == b
}
class SearchAdapter(private val onClick: (SearchIndexEntity)->Unit)
    : PagingDataAdapter<SearchIndexEntity, SearchVH>(SearchDiff) {
    override fun onCreateViewHolder(p: ViewGroup, v: Int): SearchVH {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_search_row, p, false)
        return SearchVH(view)
    }
    override fun onBindViewHolder(h: SearchVH, pos: Int) { getItem(pos)?.let { h.bind(it, onClick) } }
}
class SearchVH(v: View) : RecyclerView.ViewHolder(v) {
    private val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
    private val tvSnippet = v.findViewById<TextView>(R.id.tvSnippet)
    private val tvMeta = v.findViewById<TextView>(R.id.tvMeta)
    fun bind(s: SearchIndexEntity, onClick: (SearchIndexEntity)->Unit) {
        tvTitle.text = s.title
        tvSnippet.text = s.snippet ?: ""
        val meta = buildString {
            s.date?.let { append(it); append("  •  ") }
            append(s.kind.name)
            s.abilityId?.let { append("  •  "); append(it) }
        }
        tvMeta.text = meta
        itemView.setOnClickListener { onClick(s) }
    }
}
