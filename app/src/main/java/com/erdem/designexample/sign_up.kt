package com.erdem.designexample

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.erdem.designexample.database.FirebaseSyncHelper
import com.erdem.designexample.databinding.FragmentSignUpBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SignUp : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
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
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
        bottomNavigationView.visibility = View.GONE


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
            val syncHelper = FirebaseSyncHelper(requireContext())

            // ✅ Coroutine ile Firestore'dan SQLite'a veri çekme işlemini arka planda çalıştır
            CoroutineScope(Dispatchers.IO).launch {
                syncHelper.syncFirestoreToSQLite()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (!isValidEmail(email)) {
                Toast.makeText(requireContext(), "Geçerli bir e-posta adresi giriniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                Toast.makeText(requireContext(), "Şifre en az 6 karakter olmalıdır!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createAccount(email, password)
        }

        binding.loginRedirect.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_signUp2_to_login) // HomeFragment'in id'si
        }

    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    sendEmailVerification()
                    updateUI(user)
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        requireContext(),
                        "Kayıt işlemi başarısız!",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Doğrulama e-postası gönderildi.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "E-posta gönderme başarısız!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(requireContext(), "Hoş geldiniz: ${user.email}", Toast.LENGTH_SHORT).show()
            // Ana sayfaya yönlendirme
            val navController = findNavController()
            navController.navigate(R.id.action_signUp2_to_new_design) // HomeFragment'in id'si
        }
    }

    private fun reload() {
        Toast.makeText(requireContext(), "Sayfa yenilendi!", Toast.LENGTH_SHORT).show()
        val navController = findNavController()
        navController.navigate(R.id.action_signUp2_to_new_design) // HomeFragment'in id'si
    }

    companion object {
        private const val TAG = "EmailPassword"
    }
}
