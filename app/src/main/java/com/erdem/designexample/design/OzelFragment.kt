package com.erdem.designexample.design

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.database.TeaGardens
import com.erdem.designexample.database.TeaHarverst
import com.erdem.designexample.databinding.FragmentOzelBinding


class OzelFragment : Fragment() {

    private var _binding: FragmentOzelBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOzelBinding.inflate(inflater, container, false)
        val view = binding.root


        val tarla = TeaGardens("")
        val hasat = TeaHarverst(0,0,0,0,"",0.0f, "DEVLET", 0.0f, "")
        val dbHelper = DatabaseHelper(requireContext())

        binding.TarlaEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Toast.makeText(requireContext(), "asd", Toast.LENGTH_SHORT).show()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Toast.makeText(requireContext(), s.toString(), Toast.LENGTH_SHORT).show()
                //val dbHelper = DatabaseHelper(requireContext())
                val tarlalar = DatabaseOperations().readGardens(dbHelper, s.toString()).toTypedArray()
                val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item,tarlalar )
                binding.TarlaEditText.threshold = 1
                binding.TarlaEditText.setAdapter(adapter)
                Log.e("BAHCE", DatabaseOperations().readGardens(dbHelper, s.toString()).toString())
            }

            override fun afterTextChanged(s: Editable?) {
                //Toast.makeText(requireContext(), "asd", Toast.LENGTH_SHORT).show()
            }

        })

        binding.SurumEditText.setOnClickListener {

            // Klavyeyi kapat
            //it.hideKeyboard()

            // Klavyenin kapanmasını bekleyerek PopupMenu'yu göstermek için bir gecikme ekleyin
            binding.SurumEditText.postDelayed({
                binding.SurumEditText.inputType = InputType.TYPE_NULL
                binding.SurumEditText.isFocusable = false

                val popupMenu = PopupMenu(requireContext(), binding.SurumEditText)
                popupMenu.menu.add("1")
                popupMenu.menu.add("2")
                popupMenu.menu.add("3")
                popupMenu.menu.add("4")

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    binding.SurumEditText.setText(menuItem.title) // Seçilen değeri EditText'e yaz
                    true
                }

                popupMenu.show()
            }, 100) // 100ms gecikme ile klavyenin kapanması için zaman tanıyoruz
        }

        binding.VadeTarihiEdit.setOnClickListener {
            val calendar = Calendar.getInstance()
            val yil = calendar.get(Calendar.YEAR)
            val ay = calendar.get(Calendar.MONTH)
            val gun = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(),
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

                    val tarih = "$dayOfMonth/0${month+1}/$year"
                    binding.VadeTarihiEdit.text = tarih

                    hasat.day = dayOfMonth
                    hasat.month = month+1
                    hasat.year = year

                    //Toast.makeText(context, binding.textView6.text, Toast.LENGTH_SHORT).show()

                },yil, ay, gun)

            datePickerDialog.setTitle("Tarih seçiniz")
            datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "AYARLA", datePickerDialog)
            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "İPTAL", datePickerDialog)

            datePickerDialog.show()
            binding.VadeTarihiEdit.setTextColor(Color.BLACK)
        }

        binding.TarihEditText.setOnClickListener {

            val calendar = Calendar.getInstance()
            val yil = calendar.get(Calendar.YEAR)
            val ay = calendar.get(Calendar.MONTH)
            val gun = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(),
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

                    val tarih = "$dayOfMonth/0${month+1}/$year"
                    binding.TarihEditText.text = tarih

                    hasat.day = dayOfMonth
                    hasat.month = month+1
                    hasat.year = year

                    //Toast.makeText(context, binding.textView6.text, Toast.LENGTH_SHORT).show()

                },yil, ay, gun)

            datePickerDialog.setTitle("Tarih seçiniz")
            datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "AYARLA", datePickerDialog)
            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "İPTAL", datePickerDialog)

            datePickerDialog.show()
            binding.TarihEditText.setTextColor(Color.BLACK)
        }

        binding.KaydetOzelButton.setOnClickListener {

            val editTexts = listOf(binding.TarlaEditText,
                binding.TarlaEditText,
                binding.KgEditText,
                binding.SurumEditText,
                binding.OzelSatisYeriEditText,
                binding.FiyatEdit,
                binding.VadeTarihiEdit
            )
            var allFieldsFilled = true

            if(binding.TarihEditText.text.toString().trim() == "Tarih seçiniz") {
                allFieldsFilled = false
                binding.TarihEditText.setTextColor(Color.RED)
            }
            Log.e("tarih", binding.VadeTarihiEdit.text.toString().trim())
            if(binding.VadeTarihiEdit.text.toString().trim() == "Tarih seçiniz") {
                allFieldsFilled = false
                binding.VadeTarihiEdit.setTextColor(Color.RED)
            }


            for (editText in editTexts) {
                if (editText.text.toString().trim().isEmpty()) {
                    // Eğer alan boşsa, text rengini kırmızı yap ve allFieldsFilled'ı false olarak ayarla
                    editText.setHintTextColor(Color.RED)
                    if(editText.id != binding.OzelSatisYeriEditText.id) {
                        allFieldsFilled = false
                    }

                } else {
                    // Alan doluysa varsayılan renk ayarını yap
                    editText.setHintTextColor(Color.GRAY) // Varsayılan renge göre düzenleyin
                }
            }

            if (!allFieldsFilled) {
                Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tarla.gardenName = binding.TarlaEditText.text.toString().trim()
            hasat.season = binding.SurumEditText.text.toString().toInt()
            hasat.weight_kg = binding.KgEditText.text.toString().toFloat()

            hasat.VadeTarihi = binding.VadeTarihiEdit.text.toString()
            hasat.SatisFiyati = binding.FiyatEdit.text.toString().toFloat()
            hasat.SatisYeri = binding.OzelSatisYeriEditText.text.toString().toString()

            Toast.makeText(context, "Kaydedildi", Toast.LENGTH_SHORT).show()
            DatabaseOperations().add(dbHelper,tarla,hasat,requireContext())
            clearTextFields(view)
            binding.SurumEditText.text = ""

        }



        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

