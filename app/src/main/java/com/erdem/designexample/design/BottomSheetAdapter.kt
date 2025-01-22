package com.erdem.designexample.design

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R

class bottomSheetAdapter(private val dataSet: ArrayList<String>) : RecyclerView.Adapter<bottomSheetAdapter.ViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class ViewHolder(view: View, listener: onItemClickListener) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.bottomSheetDiolagTextview)

            view.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.bottom_sheet_diolag, viewGroup, false)

        return ViewHolder(view,mListener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}