package com.erdem.designexample.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.dataClass.paymentData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


class KayitCardAdapter (val dataSet: ArrayList<paymentData>) :
    RecyclerView.Adapter<KayitCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kayıtTarih: TextView
        val kayıtSurgun: TextView
        val kayıtFiyat: TextView
        val KayıtMiktar: TextView

        init {
            // Define click listener for the ViewHolder's View
            kayıtTarih = view.findViewById(R.id.kayit_tarih)
            kayıtSurgun = view.findViewById(R.id.kayit_surgun)
            kayıtFiyat = view.findViewById(R.id.kayit_fiyat)
            KayıtMiktar = view.findViewById(R.id.kayit_kg)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_kayit_card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {



    }

    override fun getItemCount() = dataSet.size

    fun updateData(newList: ArrayList<paymentData>) {
        dataSet.clear()
        dataSet.addAll(newList)
        notifyDataSetChanged()
    }
}