package com.erdem.designexample.design

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FilterModelBottomSheet2Binding
import com.erdem.designexample.viewModels.filtreViewModel
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

// TODO: Customize parameter argument names
//const val ARG_ITEM_COUNT = "item_count"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    filterListDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class filterListDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FilterModelBottomSheet2Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FilterModelBottomSheet2Binding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.findViewById<RecyclerView>(R.id.list)?.layoutManager =
            LinearLayoutManager(context)

        val helper = DatabaseHelper(requireContext())

        val firma = DatabaseOperations().readCompany(helper)
        firma.add("Tüm Firmalar")

        firma.forEach { topic ->
            val chip = LayoutInflater.from(requireContext()).inflate(R.layout.chip, binding.firmaChipGroup, false) as Chip

            chip.id = View.generateViewId()
            chip.text = topic
            chip.isCheckable = true
            chip.isClickable = true

            chip.setOnClickListener {
                if(chip.text == "Tüm Firmalar") {
                    for(i in 0 until binding.tarihChipGroup.childCount) {

                        val tmp = binding.firmaChipGroup.getChildAt(i)
                        if(tmp is Chip && tmp.isChecked && tmp.text != "Tüm Firmalar") {
                            tmp.isChecked = false
                        }

                    }
                }

                if(chip.text != "Tüm Firmalar") {
                    for(i in 0 until binding.firmaChipGroup.childCount) {

                        val tmp = binding.firmaChipGroup.getChildAt(i)
                        if(tmp is Chip && tmp.isChecked && tmp.text == "Tüm Firmalar") {
                            tmp.isChecked = false
                        }

                    }
                }
            }

            binding.firmaChipGroup.addView(chip)
        }

        val tarih = DatabaseOperations().readYear(helper)
        tarih.add("Tüm Yıllar")

        tarih.forEach { topic ->
            val chip = LayoutInflater.from(requireContext()).inflate(R.layout.chip, binding.firmaChipGroup, false) as Chip

            chip.id = View.generateViewId()
            chip.text = topic
            chip.isCheckable = true
            chip.isClickable = true
            chip.setOnClickListener {
                if(chip.text == "Tüm Yıllar") {
                    for(i in 0 until binding.tarihChipGroup.childCount) {

                        val tmp = binding.tarihChipGroup.getChildAt(i)
                        if(tmp is Chip && tmp.isChecked && tmp.text != "Tüm Yıllar") {
                           tmp.isChecked = false
                        }

                    }
                }

                if(chip.text != "Tüm Yıllar") {
                    for(i in 0 until binding.tarihChipGroup.childCount) {

                        val tmp = binding.tarihChipGroup.getChildAt(i)
                        if(tmp is Chip && tmp.isChecked && tmp.text == "Tüm Yıllar") {
                            tmp.isChecked = false
                        }

                    }
                }

                //viewmodele seçilen tarihleri aktarıyor.
                var times = ArrayList<String>()
                for(i in 0 until binding.tarihChipGroup.childCount) {

                    val tmp = binding.tarihChipGroup.getChildAt(i)
                    if(tmp is Chip && tmp.isChecked) {
                        times.add(tmp.text.toString())
                    }

                }

                val viewModel: filtreViewModel by activityViewModels()
                viewModel.saveTimes(times)
            }
            binding.tarihChipGroup.addView(chip)
        }

    }

    companion object {
        const val TAG = "ModalBottomSheet"
        // TODO: Customize parameters
        fun newInstance(itemCount: Int): filterListDialogFragment =
            filterListDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT, itemCount)
                }
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getSelectedChip(flexboxLayout: FlexboxLayout): List<String> {

        val selectedChips = mutableListOf<String>()

        for(i in 0 until flexboxLayout.childCount) {
            val child = flexboxLayout.getChildAt(i)
            if(child is Chip && child.isChecked) {
                selectedChips.add(child.text.toString())
            }
        }
        return selectedChips
    }

}