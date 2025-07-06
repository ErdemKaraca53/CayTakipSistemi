package com.erdem.designexample.design

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.databinding.FragmentKayitSayfasiBinding


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [kayit_ekranFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class kayit_ekranFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentKayitSayfasiBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKayitSayfasiBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val helper = DatabaseHelper(requireContext())
        val dataset = DatabaseOperations().readYear(helper)

        Log.e("years", dataset.toString())


        binding.KayitFiltreFAB.setOnClickListener {
            val modalBottomSheet = KayitFilterDialogFragment()
            modalBottomSheet.show(parentFragmentManager, KayitFilterDialogFragment.TAG)
        }

        helper.close()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment kayit_ekranFragment.
         */

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            kayit_ekranFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}