package com.erdem.designexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.adapter.TarihAdapter.RecyclerViewEvent

class BahceAdapter(private val dataSet: ArrayList<String>,
                   private val listener: RecyclerViewEvent) : RecyclerView.Adapter<BahceAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),  View.OnClickListener {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.textView)
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = bindingAdapterPosition

            if(position != RecyclerView.NO_POSITION) {
                listener.onItemClick(dataSet[position])
            }

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

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    interface RecyclerViewEvent {
        fun onItemClick(data: String)
    }

}