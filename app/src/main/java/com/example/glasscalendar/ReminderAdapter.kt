package com.example.glasscalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

data class ReminderItem(
    val id: Long,
    val title: String,
    val description: String,
    val timeLabel: String
)

class ReminderAdapter(
    private var items: List<ReminderItem>,
    private val backgroundColor: Int
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView as CardView
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textDescription: TextView = itemView.findViewById(R.id.textDescription)
        val textTime: TextView = itemView.findViewById(R.id.textTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val item = items[position]
        holder.textTitle.text = item.title
        holder.textDescription.text = item.description
        holder.textTime.text = item.timeLabel

        holder.card.setCardBackgroundColor(backgroundColor)
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ReminderItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
