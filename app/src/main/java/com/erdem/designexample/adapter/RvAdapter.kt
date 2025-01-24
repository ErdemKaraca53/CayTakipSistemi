package com.erdem.designexample.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.rapor

class RvAdapter(private val dataSet: ArrayList<rapor>, val context: Context) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {

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
        init {
            // Define click listener for the ViewHolder's View
            yıl = view.findViewById(R.id.YılTextView)
            ToplamKg = view.findViewById(R.id.ToplamKgTextView)
            ToplamGelir = view.findViewById(R.id.ToplamGelirTextView)
            card = view.findViewById(R.id.RecyclerviewCard)
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
        holder.yıl.text = dataSet[position].year.toString()
        holder.ToplamKg.text = dataSet[position].total_weight.toString() + " KG"
        holder.ToplamGelir.text = "${dataSet[position].total_revenue} TL"

        //sürüm sayfasına geçiş yapılacak.
        holder.card.setOnClickListener {
            //Navigation.findNavController(it).navigate(R.id.bottomSheetFragment)
        }

    }

}