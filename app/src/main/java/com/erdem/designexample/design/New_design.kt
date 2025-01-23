package com.erdem.designexample.design

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.erdem.designexample.R
import com.erdem.designexample.adapter.ViewPagerAdapter
import com.erdem.designexample.databinding.FragmentNewDesignBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class new_design : Fragment() {

    private var _binding: FragmentNewDesignBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewDesignBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.viewPager.adapter = ViewPagerAdapter(requireActivity())
        TabLayoutMediator(binding.tabLayout, binding.viewPager) {tab, position ->
            when(position) {
                0 -> tab.text = "DEVLET"
                1 -> tab.text = "OZEL"
            }
        }.attach()

        // İlk açıldığında seçilen tab'ın rengini değiştirmek
        val firstTab = binding.tabLayout.getTabAt(0)
        firstTab?.view?.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.selected_tab_color)
        )
        //ikinci tabı defalut renk olarak ayarla
        val lasTab = binding.tabLayout.getTabAt(1)
        lasTab?.view?.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.default_tab_color)
        )


        // Tab seçimi dinleyicisi
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.view.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.selected_tab_color)
                )
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.view.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.default_tab_color)
                )
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Tekrar seçilen tab için özel bir işlem yapmak istiyorsanız buraya yazabilirsiniz
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}