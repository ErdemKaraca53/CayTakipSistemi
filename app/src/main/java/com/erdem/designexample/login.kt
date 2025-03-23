package com.erdem.designexample

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.erdem.designexample.databinding.FragmentLoginBinding
import com.erdem.designexample.databinding.FragmentNewDesignBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
        bottomNavigationView.visibility = View.GONE

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            val email = binding.emailLogin.text.toString().trim()
            val password = binding.passwordLogin.text.toString().trim()

            if (!isValidEmail(email)) {
                Toast.makeText(requireContext(), "Geçerli bir e-posta giriniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                Toast.makeText(requireContext(), "Şifre en az 6 karakter olmalıdır!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)

        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Giriş başarılı!", Toast.LENGTH_SHORT).show()

                    val navController = findNavController()
                    navController.navigate(R.id.action_login_to_new_design) // HomeFragment'in id'si
                } else {
                    Toast.makeText(requireContext(), "Giriş başarısız! Bilgilerinizi kontrol edin.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}
