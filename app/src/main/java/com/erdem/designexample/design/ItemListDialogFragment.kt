package com.erdem.designexample.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.erdem.designexample.R
import com.erdem.designexample.adapter.BahceAdapter
import com.erdem.designexample.adapter.RecyclerViewEvent
import com.erdem.designexample.adapter.TarihAdapter
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentItemListDialogListDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: Customize parameter argument names
//Fragment'ı başlatırken kaç adet öğe gösterileceği newInstance(itemCount: Int) metodu ile belirlenir.
const val ARG_ITEM_COUNT = "item_count"

enum class ItemType {
    BAHCE, TARIH
}


class ItemListDialogFragment : BottomSheetDialogFragment(), RecyclerViewEvent  {

    private var _binding: FragmentItemListDialogListDialogBinding? = null

    private val binding get() = _binding!!
    var tarih : String = ""
    var bahce : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentItemListDialogListDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onItemClick(data: String, type: ItemType) {
        when (type) {
            ItemType.TARIH -> tarih = data
            ItemType.BAHCE -> bahce = data
        }

        // Her iki değişken de doluysa işlemi yap
        if (bahce.isNotEmpty()) {
            val result = Bundle().apply {
                putString("bahce", bahce)
            }
            parentFragmentManager.setFragmentResult("requestKey", result)
            this.dismiss()
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val helper = DatabaseHelper(requireContext())
        //Bu iki satır kod sayesinde bottomSheet içerisindeki recyclerviewler kaydırılabiliyor.
        //Bu satırlar olmadan önce sadece bir tanesi kaydırılabiliyordu
        binding.BahceSecimRecyclerView.isNestedScrollingEnabled = false
        //binding.TarihSecimRecyclerView.isNestedScrollingEnabled = false
        val TarihDataSet = DatabaseOperations().readYear(helper)

        val TarihAdapter = TarihAdapter(TarihDataSet, this)
        //binding.TarihSecimRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        //binding.TarihSecimRecyclerView.adapter = TarihAdapter
        val BahceDataSet = DatabaseOperations().readGardenName(helper)
        
        val BahceAdapter = BahceAdapter(BahceDataSet, this)
        binding.BahceSecimRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.BahceSecimRecyclerView.adapter = BahceAdapter

        helper.close()
    }


    companion object {

        // TODO: Customize parameters
        const val TAG = "ModalBottomSheet"
        fun newInstance(itemCount: Int): ItemListDialogFragment =
            ItemListDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT2, itemCount)
                }
            }

    }

    override fun onStart() {
        super.onStart()

        //!! burada dialog nedir tam öğren !!
        dialog?.let {
            val bottomSheet = dialog?.findViewById<View>(R.id.TarihBahceList)
            bottomSheet?.layoutParams?.height = getWindowHeight()
        }
    }

    private fun getWindowHeight() = resources.displayMetrics.heightPixels

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}