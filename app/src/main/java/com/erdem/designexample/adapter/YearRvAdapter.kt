package com.erdem.designexample.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.YılRapor

class YearRvAdapter(private val dataSet: ArrayList<YılRapor>) : RecyclerView.Adapter<YearRvAdapter.ViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val yıl: TextView
        val ToplamKg: TextView
        val ToplamGelir: TextView
        val card : CardView
        val DetayButton : Button
        init {
            // Define click listener for the ViewHolder's View
            yıl = view.findViewById(R.id.YılTextView)
            ToplamKg = view.findViewById(R.id.YılToplamKgTextView)
            ToplamGelir = view.findViewById(R.id.YılToplamGelirTextView)
            card = view.findViewById(R.id.CardView)
            DetayButton = view.findViewById(R.id.YılDetayButton)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.year_card_view_design, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.yıl.text = dataSet[position].year.toString()
        holder.ToplamKg.text = dataSet[position].total_weight.toString() + " KG"
        holder.ToplamGelir.text = "${dataSet[position].total_revenue} TL"

        //sürüm sayfasına geçiş yapılacak.
        holder.card.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("yıl", dataSet[position].year)
            Navigation.findNavController(it).navigate(R.id.action_birinciFragment_to_surumFragment, bundle)
        }

        holder.DetayButton.setOnClickListener{
            val bundle = Bundle()
            bundle.putInt("yıl", dataSet[position].year)
            Navigation.findNavController(it).navigate(R.id.action_birinciFragment_to_surumFragment, bundle)
        }

    }

}