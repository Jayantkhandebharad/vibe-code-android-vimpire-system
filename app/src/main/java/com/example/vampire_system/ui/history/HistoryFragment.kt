package com.example.vampire_system.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataAdapter
import androidx.paging.cachedIn
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.LedgerType
import com.example.vampire_system.data.db.XpLedgerEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController


class HistoryFragment : Fragment() {
    private var list: RecyclerView? = null
    private lateinit var adapter: LedgerPagingAdapter
    private var filter: LedgerType? = null
    private var flowJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list = view.findViewById(R.id.ledgerList)
        if (list == null) {
            Toast.makeText(requireContext(), "ledgerList not found in layout", Toast.LENGTH_SHORT).show()
            return
        }

        adapter = LedgerPagingAdapter()
        list!!.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }

        // Set up filter button
        view.findViewById<Button>(R.id.btnFilter)?.setOnClickListener { showFilterMenu(it) }
        
        // Set up 3-dot menu button
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener { showPopupMenu(it) }

        // Set initial filter button text
        updateFilterButtonText()

        bindPager()
    }

    private fun showFilterMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_filter, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.filter_all -> {
                    filter = null
                    updateFilterButtonText()
                    bindPager()
                    true
                }
                R.id.filter_awards -> {
                    filter = LedgerType.AWARD
                    updateFilterButtonText()
                    bindPager()
                    true
                }
                R.id.filter_bonus -> {
                    filter = LedgerType.BONUS
                    updateFilterButtonText()
                    bindPager()
                    true
                }
                R.id.filter_penalty -> {
                    filter = LedgerType.PENALTY
                    updateFilterButtonText()
                    bindPager()
                    true
                }
                R.id.filter_adjustment -> {
                    filter = LedgerType.ADJUSTMENT
                    updateFilterButtonText()
                    bindPager()
                    true
                }
                R.id.filter_level_up -> {
                    filter = LedgerType.LEVEL_UP
                    updateFilterButtonText()
                    bindPager()
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun updateFilterButtonText() {
        val filterButton = view?.findViewById<Button>(R.id.btnFilter)
        filterButton?.text = when (filter) {
            null -> "Filter: All"
            LedgerType.AWARD -> "Filter: Awards"
            LedgerType.BONUS -> "Filter: Bonus"
            LedgerType.PENALTY -> "Filter: Penalty"
            LedgerType.ADJUSTMENT -> "Filter: Adjustment"
            LedgerType.LEVEL_UP -> "Filter: Level Up"
        }
    }
    
    private fun showPopupMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_history, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_evidence -> {
                    showEvidenceView()
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun showEvidenceView() {
        // Navigate to evidence fragment
        findNavController().navigate(R.id.navigation_evidence)
    }

    private fun bindPager() {
        flowJob?.cancel()
        flowJob = viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.get(requireContext())
            val pagingSourceFactory: () -> androidx.paging.PagingSource<Int, XpLedgerEntity> = when (val f = filter) {
                null -> { { db.xpLedgerDao().pagingAll() } }
                else -> { { db.xpLedgerDao().pagingByType(f) } }
            }
            Pager<Int, XpLedgerEntity>(
                PagingConfig(pageSize = 50, enablePlaceholders = false, prefetchDistance = 25),
                pagingSourceFactory = pagingSourceFactory
            ).flow
                .cachedIn(viewLifecycleOwner.lifecycleScope)
                .collectLatest { data -> adapter.submitData(data) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        flowJob?.cancel()
        list = null
    }
}

private object LedgerDiff : DiffUtil.ItemCallback<XpLedgerEntity>() {
    override fun areItemsTheSame(a: XpLedgerEntity, b: XpLedgerEntity) = a.id == b.id
    override fun areContentsTheSame(a: XpLedgerEntity, b: XpLedgerEntity) = a == b
}

class LedgerPagingAdapter
    : PagingDataAdapter<XpLedgerEntity, LedgerVH>(LedgerDiff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_ledger, parent, false)
        return LedgerVH(v)
    }
    override fun onBindViewHolder(holder: LedgerVH, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
}

class LedgerVH(v: View) : RecyclerView.ViewHolder(v) {
    private val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
    private val tvSub = v.findViewById<TextView>(R.id.tvSub)
    fun bind(e: XpLedgerEntity) {
        val sign = if (e.deltaXp >= 0) "+" else ""
        val label = e.note ?: e.abilityId ?: e.type.name
        tvTitle.text = "$label   ${sign}${e.deltaXp} XP"
        tvSub.text = "${e.date} • ${e.type.name}"
    }
}
