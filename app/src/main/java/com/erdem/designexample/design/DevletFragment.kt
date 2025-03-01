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
import com.erdem.designexample.R
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.database.TeaGardens
import com.erdem.designexample.database.TeaHarverst
import com.erdem.designexample.databinding.FragmentDevletBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class DevletFragment : Fragment() {

    private var _binding: FragmentDevletBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDevletBinding.inflate(inflater, container, false)
        val view = binding.root

        val dbHelper = DatabaseHelper(requireContext())

        val calendar = Calendar.getInstance()
        val gun = calendar.get(Calendar.DAY_OF_MONTH)
        val yil = calendar.get(Calendar.YEAR)
        val ay = calendar.get(Calendar.MONTH)

        val tarla = TeaGardens("")
        val hasat = TeaHarverst(yil,0,0,0,"",0.0f, "DEVLET", 0.0f, "")

        var tarih = "$gun/${(ay+1) % 12}/$yil"
        binding.TarihEditText.text = tarih
        var VadeTarih = "31/${(ay+2) % 12}/$yil"
        binding.VadeTarihiEdit.text = VadeTarih

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
                    var VadeTarih = "31/${(month+2) % 12}/$yil"
                    Log.e("Tarih1", VadeTarih)
                    binding.VadeTarihiEdit.text = VadeTarih
                //Toast.makeText(context, binding.textView6.text, Toast.LENGTH_SHORT).show()

            },yil, ay, gun)

            datePickerDialog.setTitle("Tarih seçiniz")
            datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "AYARLA", datePickerDialog)
            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "İPTAL", datePickerDialog)

            datePickerDialog.show()
            binding.TarihEditText.setTextColor(Color.BLACK)
        }

        binding.KaydetButton.setOnClickListener {

            val editTexts = listOf(binding.TarlaEditText,
                binding.TarlaEditText,
                binding.KgEditText,
                binding.SurumEditText,
                binding.SatisYeriEditText,
                binding.FiyatEdit,
                binding.VadeTarihiEdit
            )
            var allFieldsFilled = true

            if(binding.TarihTextView.text.toString().trim() == "Tarih seçiniz") {
                allFieldsFilled = false
                binding.TarihTextView.setTextColor(Color.RED)
            }
            Log.e("tarih", binding.VadeTarihiTextView.text.toString().trim())
            if(binding.VadeTarihiTextView.text.toString().trim() == "Tarih seçiniz") {
                allFieldsFilled = false
                binding.VadeTarihiTextView.setTextColor(Color.RED)
            }


            for (editText in editTexts) {
                if (editText.text.toString().trim().isEmpty()) {
                    // Eğer alan boşsa, text rengini kırmızı yap ve allFieldsFilled'ı false olarak ayarla
                    editText.setHintTextColor(Color.RED)
                    if(editText.id != binding.SatisYeriEditText.id) {
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

            var text = "Yıl: ${hasat.year}\n" +
                    "Bahçe: ${tarla.gardenName}\n" +
                    "Sürüm: ${hasat.season}\n" +
                    "Kg: ${hasat.weight_kg.toInt()}\n" +
                    "Satış Yeri: ${hasat.SatisYeri}\n" +
                    "Satış Fiyatı: ${hasat.SatisFiyati}\n" +
                    "Vade Tarihi: ${hasat.VadeTarihi}"
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Son kayıt kontrolü")
                .setMessage(text)
                .setNegativeButton("Reddet") { dialog, which ->
                    // Respond to negative button press
                    Toast.makeText(requireContext(), "Reddedildi", Toast.LENGTH_SHORT).show()
                }
                .setPositiveButton("Kabul et") { dialog, which ->
                    // Respond to positive button press


                    Toast.makeText(context, "Kaydedildi", Toast.LENGTH_SHORT).show()
                    DatabaseOperations().add(dbHelper,tarla,hasat,requireContext())
                    clearTextFields(view)
                    binding.SurumEditText.text = ""
                    Toast.makeText(requireContext(), "Kabul edildi", Toast.LENGTH_SHORT).show()
                }
                .show()

        }


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}