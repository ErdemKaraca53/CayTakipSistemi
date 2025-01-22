package com.erdem.designexample

import android.content.Context
import android.content.res.Configuration
import android.graphics.Insets
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.erdem.designexample.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        NavigationUI.setupWithNavController(binding.bottomNavigationBar,navHostFragment.navController)

        //binding.bottomNavigationBar.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_SELECTED

        /*binding.bottomNavigationBar.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.birinciFragment -> {
      
                    true
                }
                R.id.ikinciFragment -> {
                    true
                }
                else -> false
            }
        }*/


    }

    override fun attachBaseContext(newBase: Context?) {
        val newOverride = Configuration(newBase?.resources?.configuration)
        newOverride.fontScale = 1.0f
        applyOverrideConfiguration(newOverride)
        super.attachBaseContext(newBase)
    }
}