package com.erdem.designexample.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R

class BahceAdapter(val dataSet: ArrayList<String>) : RecyclerView.Adapter<BahceAdapter.ViewHolder>() {

    private var lastSelectPosition: Int = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val card: CardView

        init {
            textView = view.findViewById(R.id.BahceRecyclerViewTextView)
            card = view.findViewById(R.id.BahceRecyclerViewCardView)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_card, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = dataSet[position]

        // Seçili elemanı kontrol edip arka plan rengini değiştir
        if (position == lastSelectPosition) {
            viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.spinnerColor))
            Log.e("bilgi", "Yeni seçim değişildi")
        } else {
            viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.white))
            Log.e("bilgi", "Eski seçim değişildi")
        }

        viewHolder.card.setOnClickListener {
            val previousPosition = lastSelectPosition
            lastSelectPosition = viewHolder.adapterPosition
            Log.e("bilgi", dataSet[position])
            // Önceki seçili elemanı güncelle
            notifyItemChanged(previousPosition)
            // Yeni seçili elemanı güncelle
            notifyItemChanged(lastSelectPosition)
        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size


}