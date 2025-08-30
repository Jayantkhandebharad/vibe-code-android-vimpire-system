package com.example.vampire_system.ui.search

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vampire_system.R
import com.example.vampire_system.data.db.SavedSearchEntity

class PinAdapter(
    private val onClick: (SavedSearchEntity) -> Unit,
    private val onLong: (SavedSearchEntity) -> Unit
) : RecyclerView.Adapter<PinVH>() {
    private val items = mutableListOf<SavedSearchEntity>()
    fun submit(list: List<SavedSearchEntity>) { items.clear(); items.addAll(list); notifyDataSetChanged() }
    override fun getItemId(pos: Int) = items[pos].id.hashCode().toLong()
    init { setHasStableIds(true) }
    override fun onCreateViewHolder(p: ViewGroup, v: Int): PinVH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_pin_chip, p, false)
        return PinVH(v)
    }
    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: PinVH, pos: Int) = h.bind(items[pos], onClick, onLong)
}

class PinVH(v: View) : RecyclerView.ViewHolder(v) {
    private val txt = v.findViewById<TextView>(R.id.txt)
    fun bind(item: SavedSearchEntity, onClick: (SavedSearchEntity)->Unit, onLong: (SavedSearchEntity)->Unit) {
        txt.text = item.title
        itemView.setOnClickListener { onClick(item) }
        itemView.setOnLongClickListener { onLong(item); true }
    }
}
