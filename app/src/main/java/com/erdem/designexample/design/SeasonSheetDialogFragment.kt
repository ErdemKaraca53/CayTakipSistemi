package com.erdem.designexample.design

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.R
import com.erdem.designexample.databinding.FragmentSeasonSheetDialogBinding
import com.erdem.designexample.adapter.seasonSheetDialogAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class seasonSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSeasonSheetDialogBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSeasonSheetDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        val bundle: seasonSheetDialogFragmentArgs by navArgs()
        val year = bundle.year

        val helper = DatabaseHelper(requireContext())
        var dataset = DatabaseOperations().readSeason(helper, year)

        val customAdapter = seasonSheetDialogAdapter(dataset)

        val recyclerView: RecyclerView = binding.seasonRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = customAdapter

        Log.e("HATA", "${dataset.size}")

        customAdapter.setOnClickListener(object : seasonSheetDialogAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val gecis = seasonSheetDialogFragmentDirections.actionSeasonSheetDialogFragmentToBirinciFragment(year, dataset[position].toInt())
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
                    .setPopUpTo(R.id.seasonSheetDialogFragment, true)  // bottomSheetFragment'i backstack'ten kaldır
                    .build()
                navController.navigate(gecis, navOptions)

                // Geçiş sırasında navOptions'u ekliyoruz

            }

        })

        helper.close()


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}