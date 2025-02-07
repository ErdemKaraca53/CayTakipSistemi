import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.erdem.designexample.R

class spinnerAdapter(private val context: Context, private val items: ArrayList<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return items.size // Listedeki toplam öğe sayısını döndür
    }

    override fun getItem(position: Int): Any {
        return items[position] // Belirtilen pozisyondaki öğeyi döndür
    }

    override fun getItemId(position: Int): Long {
        return position.toLong() // Pozisyonu öğe kimliği olarak kullan
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_item, parent, false)

        // Mevcut öğeyi al
        val currentItem = getItem(position) as String

        // XML'deki TextView bileşenlerini bağla
        val textViewItemName = view.findViewById<TextView>(R.id.spinnerTextView)
       // val textViewItemDescription = view.findViewById<TextView>(R.id.text_view_item_description)

        // Mevcut öğenin verilerini TextView bileşenlerine ata
        textViewItemName.text = currentItem
        //textViewItemDescription.text = currentItem.itemDescription

        return view
    }
}
