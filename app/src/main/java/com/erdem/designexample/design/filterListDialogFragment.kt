package com.erdem.designexample.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.R
import com.erdem.designexample.databinding.FilterModelBottomSheet2Binding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import kotlin.random.Random

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


        val data = arrayListOf("ÇAYKUR", "LİPTON", "DOĞUŞ", "KARALİ", "KARACA ÇAY")

        data.forEach { topic ->
            val chip = LayoutInflater.from(requireContext()).inflate(R.layout.chip, binding.chipGroup, false) as Chip

            chip.id = View.generateViewId()
            chip.text = topic
            chip.isCheckable = true
            chip.isClickable = true
            //chip.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.purple)
            binding.chipGroup.addView(chip)
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
}