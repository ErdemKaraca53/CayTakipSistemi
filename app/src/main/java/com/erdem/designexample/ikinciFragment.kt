package com.erdem.designexample

import android.R
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.erdem.designexample.databinding.FragmentIkiniciBinding


class ikinciFragment : Fragment() {

    private var _binding: FragmentIkiniciBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIkiniciBinding.inflate(inflater, container, false)
        val view = binding.root

        val tarla = TeaGardens("")
        val hasat = TeaHarverst(0,0,0,0,"",0)
        val dbHelper = DatabaseHelper(requireContext())
        /*val dbHelper = DatabaseHelper(requireContext())
        val gardens = DatabaseOperations().readGardens(dbHelper)

        val adapter = ArrayAdapter<String>(requireContext(), R.id.tarlaEditText,)

        dbHelper.close()*/
        /*
            TARİH GİRDİSİ ALINIYOR

            hasat değişkeninin tarih bilgisi burada düzenleniyor

         */
        binding.devletButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if(binding.cardView.getVisibility() == View.GONE) {

                    TransitionManager.beginDelayedTransition(binding.cardView, AutoTransition())
                    binding.cardView.visibility = View.VISIBLE
                }else {
                    binding.cardView.visibility = View.VISIBLE;
                }
                Toast.makeText(requireContext(), "DEVLET", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ozelButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if(binding.cardView.visibility == View.VISIBLE) {
                    //Kapanma sırasında animasyon ekliyor !!!
                    TransitionManager.beginDelayedTransition(binding.cardView, AutoTransition())
                    binding.cardView.visibility = View.GONE
                }else {
                    binding.cardView.visibility = View.VISIBLE;
                }
                Toast.makeText(requireContext(), "ÖZEL", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tarlaEditText.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Toast.makeText(requireContext(), "asd", Toast.LENGTH_SHORT).show()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Toast.makeText(requireContext(), s.toString(), Toast.LENGTH_SHORT).show()
                //val dbHelper = DatabaseHelper(requireContext())
                val tarlalar = DatabaseOperations().readGardens(dbHelper, s.toString()).toTypedArray()
                val adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item,tarlalar )
                binding.tarlaEditText.threshold = 1
                binding.tarlaEditText.setAdapter(adapter)
                Log.e("BAHCE", DatabaseOperations().readGardens(dbHelper, s.toString()).toString())
            }

            override fun afterTextChanged(s: Editable?) {
                //Toast.makeText(requireContext(), "asd", Toast.LENGTH_SHORT).show()
            }

        })


        binding.tarihEditText.setOnClickListener {

            val calendar = Calendar.getInstance()
            val yil = calendar.get(Calendar.YEAR)
            val ay = calendar.get(Calendar.MONTH)
            val gun = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(),DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

                val tarih = "$dayOfMonth/${month+1}/$year"
                binding.tarihEditText.text = tarih

                hasat.day = dayOfMonth
                hasat.month = month+1
                hasat.year = year

                //Toast.makeText(context, binding.textView6.text, Toast.LENGTH_SHORT).show()

            },yil, ay, gun)

            datePickerDialog.setTitle("Tarih seçiniz")
            datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "AYARLA", datePickerDialog)
            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "İPTAL", datePickerDialog)

            datePickerDialog.show()
            binding.tarihEditText.setTextColor(Color.GRAY)
        }



        binding.kaydetButton.setOnClickListener {

            val editTexts = listOf(binding.tarlaEditText, binding.tarihEditText, binding.kgEditText, binding.surumEditText)
            var allFieldsFilled = true

            if(binding.tarihEditText.text.toString().trim() == "Tarih seçiniz") {
                allFieldsFilled = false
                binding.tarihEditText.setTextColor(Color.RED)
            }


            for (editText in editTexts) {
                if (editText.text.toString().trim().isEmpty()) {
                    // Eğer alan boşsa, text rengini kırmızı yap ve allFieldsFilled'ı false olarak ayarla
                    editText.setHintTextColor(Color.RED)
                    allFieldsFilled = false
                } else {
                    // Alan doluysa varsayılan renk ayarını yap
                    editText.setHintTextColor(Color.GRAY) // Varsayılan renge göre düzenleyin
                }
            }

            if (!allFieldsFilled) {
                Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tarla.gardenName = binding.tarlaEditText.text.toString().trim()
            hasat.season = binding.surumEditText.text.toString().toInt()
            hasat.weight_kg = binding.kgEditText.text.toString().toInt()


            Toast.makeText(context, "Kaydedildi", Toast.LENGTH_SHORT).show()
            DatabaseOperations().add(dbHelper,tarla,hasat,requireContext())
            clearTextFields(view)
            binding.surumEditText.text = ""

        }



        binding.surumEditText.setOnClickListener {

            // Klavyeyi kapat
            it.hideKeyboard()

            // Klavyenin kapanmasını bekleyerek PopupMenu'yu göstermek için bir gecikme ekleyin
            binding.surumEditText.postDelayed({
                binding.surumEditText.inputType = InputType.TYPE_NULL
                binding.surumEditText.isFocusable = false

                val popupMenu = PopupMenu(requireContext(), binding.surumEditText)
                popupMenu.menu.add("1")
                popupMenu.menu.add("2")
                popupMenu.menu.add("3")
                popupMenu.menu.add("4")

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    binding.surumEditText.setText(menuItem.title) // Seçilen değeri EditText'e yaz
                    true
                }

                popupMenu.show()
            }, 100) // 100ms gecikme ile klavyenin kapanması için zaman tanıyoruz
        }

        binding.silButton.setOnClickListener {
            /*val helper = DatabaseHelper(requireContext())
            DatabaseOperations().deleteData(helper,binding.tarlaEditText.text.toString())*/
        }

        /*
            NEDEN YAPTIĞIMI UNUTTUM ???
         */
        /*binding.kgEditText.addTextChangedListener {
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    TODO("Not yet implemented")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    TODO("Not yet implemented")
                }

                override fun afterTextChanged(s: Editable?) {
                    binding.kaydetButton.isEnabled = true
                }

            }
        }*/


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}

fun clearTextFields(view: View) {
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            clearTextFields(view.getChildAt(i))
        }
    } else if (view is EditText) {
        view.text.clear()
    }
}

fun View.hideKeyboard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, 0)
}

