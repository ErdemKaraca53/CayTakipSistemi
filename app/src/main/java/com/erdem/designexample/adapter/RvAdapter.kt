package com.erdem.designexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.database.TeaHarverst

class RvAdapter(private val dataSet: ArrayList<TeaHarverst>) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val bahce: TextView
        val surum: TextView
        val yıl: TextView
        val kg: TextView

        init {
            // Define click listener for the ViewHolder's View
            bahce = view.findViewById(R.id.gardenTextview)
            surum = view.findViewById(R.id.seasonTextview)
            yıl = view.findViewById(R.id.yearTextview)
            kg = view.findViewById(R.id.weightTextview)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bahce.text = dataSet[position].gardenName
        holder.yıl.text = dataSet[position].year.toString()
        holder.surum.text = "${dataSet[position].season.toString()}. sürüm"
        holder.kg.text = "Ağırlık: ${dataSet[position].weight_kg.toString()} kg"
    }

}