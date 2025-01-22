package com.erdem.designexample.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.R
import com.erdem.designexample.databinding.FragmentBottomSheet2Binding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
class bottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheet2Binding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?):
            View? {
        _binding = FragmentBottomSheet2Binding.inflate(inflater, container, false)
        val view = binding.root

        val helper = DatabaseHelper(requireContext())
        var dataset = DatabaseOperations().readYear(helper)

        val customAdapter = bottomSheetAdapter(dataset)

        val recyclerView: RecyclerView = binding.bottomSheetDioalogRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = customAdapter

        customAdapter.setOnClickListener(object : bottomSheetAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {

                val gecis = bottomSheetFragmentDirections.actionBottomSheetFragmentToSeasonSheetDialogFragment(dataset[position].toInt())
                val navController = Navigation.findNavController(requireActivity(),
                    R.id.navHostFragment
                )

                /*val tmp = ArrayList<String>(dataset) // Dataset'i tmp'ye kopyala
                val newData = DatabaseOperations().readSeason(helper)
                dataset.clear()
                dataset.addAll(newData)
                dataset.removeAll(tmp)
                customAdapter.notifyDataSetChanged()*/

                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.bottomSheetFragment, true)  // bottomSheetFragment'i backstack'ten kaldır
                    .build()
                navController.navigate(gecis, navOptions)

                // Geçiş sırasında navOptions'u ekliyoruz

            }

        })

        helper.close()
        return view
    }

}