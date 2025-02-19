package com.erdem.designexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.design.ItemType

class BahceAdapter(private val dataSet: ArrayList<String>,
                   private val listener: RecyclerViewEvent) : RecyclerView.Adapter<BahceAdapter.ViewHolder>() {


    private var lastSelection: Int = -1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val textView: TextView
        val imageView: ImageView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.recyclerViewTextView)
            imageView = view.findViewById(R.id.recyclerViewImageView)

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fragment_item_list_dialog_list_dialog_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = dataSet[position]

        if(position == lastSelection) {
            viewHolder.textView.setBackgroundColor(ContextCompat.getColor(viewHolder.textView.context,R.color.spinnerColor))
            viewHolder.imageView.setImageDrawable(ContextCompat.getDrawable(viewHolder.imageView.context, R.drawable.baseline_check_24))
        } else {
            viewHolder.textView.setBackgroundColor(ContextCompat.getColor(viewHolder.textView.context,R.color.white))
            viewHolder.imageView.setImageDrawable(null)
        }

        viewHolder.textView.setOnClickListener {
            val previousSelection = lastSelection
            lastSelection = viewHolder.bindingAdapterPosition
            notifyItemChanged(previousSelection)
            notifyItemChanged(lastSelection)
            listener.onItemClick(dataSet[position], ItemType.BAHCE)
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    interface RecyclerViewEvent {
        fun onItemClick(data: String, type: ItemType)
    }

}


