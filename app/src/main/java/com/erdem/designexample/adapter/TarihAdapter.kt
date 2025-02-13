package com.erdem.designexample.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R

class TarihAdapter(val dataSet: ArrayList<String>,
                    private val listener: RecyclerViewEvent) : RecyclerView.Adapter<TarihAdapter.ViewHolder>() {

    private var lastSelectPosition: Int = -1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.recyclerViewTextView)
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = bindingAdapterPosition

            if(position != RecyclerView.NO_POSITION) {
                listener.onItemClick(dataSet[position])
            }

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
            //viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.spinnerColor))
            Log.e("bilgi", "Yeni seçim değişildi")
        } else {
            //viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.white))
            Log.e("bilgi", "Eski seçim değişildi")
        }

        /*viewHolder.card.setOnClickListener {
            val previousPosition = lastSelectPosition
            lastSelectPosition = viewHolder.adapterPosition
            Log.e("bilgi", dataSet[position])
            // Önceki seçili elemanı güncelle
            notifyItemChanged(previousPosition)
            // Yeni seçili elemanı güncelle
            notifyItemChanged(lastSelectPosition)
        }*/
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    interface RecyclerViewEvent {
        fun onItemClick(data: String)
    }

}