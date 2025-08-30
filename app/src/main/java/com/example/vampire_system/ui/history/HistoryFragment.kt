package com.example.vampire_system.ui.history

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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

        view.findViewById<Button>(R.id.btnAll)?.setOnClickListener { filter = null; bindPager() }
        view.findViewById<Button>(R.id.btnAwards)?.setOnClickListener { filter = LedgerType.AWARD; bindPager() }
        view.findViewById<Button>(R.id.btnBonus)?.setOnClickListener { filter = LedgerType.BONUS; bindPager() }
        view.findViewById<Button>(R.id.btnPenalty)?.setOnClickListener { filter = LedgerType.PENALTY; bindPager() }

        bindPager()
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


