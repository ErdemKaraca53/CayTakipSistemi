package com.erdem.designexample.adapter

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


class OdemelerCardAdapter (val dataSet: ArrayList<paymentData>) :
    RecyclerView.Adapter<OdemelerCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val CompanyName: TextView
        val OdemeDurumu: TextView
        val OdemeMiktari: TextView
        val OdemeTarihi: TextView
        val KalanTarih: TextView
        val Kg: TextView
        val Fiyat: TextView
        val btnDetay: TextView = view.findViewById(R.id.btnDetay)
        val statusBadge: View = view.findViewById(R.id.status_badge) // Badge container
        val statusDot: View = itemView.findViewById(R.id.statusDot)
        init {
            // Define click listener for the ViewHolder's View
            CompanyName = view.findViewById(R.id.CompanyName)
            OdemeDurumu = view.findViewById(R.id.OdemeDurumu)
            OdemeMiktari = view.findViewById(R.id.OdemeMiktari)
            OdemeTarihi = view.findViewById(R.id.OdemeTarihi)
            KalanTarih = view.findViewById(R.id.kalanTarih)
            Kg = view.findViewById(R.id.KgMiktari)
            Fiyat = view.findViewById(R.id.SatisFiyati)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.OdemeMiktari.text = dataSet[position].money.toInt().toString() + " TL"
        viewHolder.CompanyName.text = dataSet[position].company

        val kgText = "${dataSet[position].Kg} Kg"
        val fiyatText = "${dataSet[position].satisFiyati} TL"

        viewHolder.Kg.text = kgText
        viewHolder.Fiyat.text = fiyatText
        val today = LocalDate.now()
        val vadeTarihi = dataSet[position].paymentDate

        val formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy")
        val formattedString: String? = vadeTarihi.format(formatter)

        val kalanGun =  ChronoUnit.DAYS.between(today, vadeTarihi)

        val kalanTarih = "$kalanGun gün kaldı"

        if (kalanGun > 0) {
            val context = viewHolder.itemView.context
            viewHolder.KalanTarih.text = kalanTarih
            viewHolder.statusBadge.setBackgroundResource(R.drawable.status_badge_pending_bg)
            viewHolder.statusDot.setBackgroundResource(R.drawable.status_dot_pending)
            viewHolder.OdemeDurumu.text = "Ödeme tarihi gelmedi"
            viewHolder.KalanTarih.text = kalanTarih
            viewHolder.KalanTarih.visibility = View.VISIBLE
            viewHolder.OdemeDurumu.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
        } else {
            val context = viewHolder.itemView.context
            viewHolder.OdemeTarihi.text = "$formattedString"
            viewHolder.OdemeTarihi.setTextColor(ContextCompat.getColor(context, R.color.purple_500))
            viewHolder.statusBadge.setBackgroundResource(R.drawable.status_badge_success_bg)
            viewHolder.statusDot.setBackgroundResource(R.drawable.status_dot_success)
            viewHolder.OdemeDurumu.text = "Odeme yapıldı"
            //Burada yaptığımızda üsttede yapmamız gerekir
            viewHolder.KalanTarih.visibility = View.GONE
            viewHolder.OdemeDurumu.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
        }

    }

    override fun getItemCount() = dataSet.size
}