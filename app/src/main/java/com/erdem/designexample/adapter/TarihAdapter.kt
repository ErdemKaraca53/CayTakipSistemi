package com.erdem.designexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.adapter.BahceAdapter.RecyclerViewEvent
import com.erdem.designexample.design.ItemType


class TarihAdapter(val dataSet: ArrayList<String>,
                    private val listener: RecyclerViewEvent) : RecyclerView.Adapter<TarihAdapter.ViewHolder>() {

    private var lastSelectPosition: Int = -1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageView : ImageView

        init {
            textView = view.findViewById(R.id.recyclerViewTextView)
            imageView = view.findViewById(R.id.recyclerViewImageView)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int, ): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fragment_item_list_dialog_list_dialog_item, viewGroup, false)

        return ViewHolder(view)
    }
    //
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = dataSet[position]
        // Seçili elemanı kontrol edip arka plan rengini değiştir
        if (position == lastSelectPosition) {
            viewHolder.textView.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.spinnerColor))
            viewHolder.imageView.setImageDrawable(ContextCompat.getDrawable(viewHolder.imageView.context, R.drawable.baseline_check_24))
        } else {
            viewHolder.textView.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.white))
            viewHolder.imageView.setImageDrawable(null)
        }

        viewHolder.textView.setOnClickListener {
            val previousPosition = lastSelectPosition
            lastSelectPosition = viewHolder.bindingAdapterPosition
            // Önceki seçili elemanı güncelle
            notifyItemChanged(previousPosition)
            // Yeni seçili elemanı güncelle
            notifyItemChanged(lastSelectPosition)
            listener.onItemClick(dataSet[position], ItemType.TARIH)
        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}