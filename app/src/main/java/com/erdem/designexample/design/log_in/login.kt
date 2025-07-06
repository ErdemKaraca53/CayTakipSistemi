package com.erdem.designexample.design.log_in

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.erdem.designexample.R
import com.erdem.designexample.database.FirebaseSyncHelper
import com.erdem.designexample.databinding.FragmentLoginBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        /**
        Navigation bar sign-up ve login sayfalarında gerekli değil.
        Bu yüzden sign-up ve login sayfalarından geçiş yapılana kadar kaldırılıyor.
         **/
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
        bottomNavigationView.visibility = View.GONE

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
    Kullanıcı giriş işlemi burada yapılıyor.
    email ve şifre bilgileri kontrol ediliyor.
    email ve şifre boş değilse ve email doğru formatta ise @loginuser fonksiyonu çağrılır
     **/
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

    /**
     * Kullanıcının giriş yapmasını sağlar. Bilgiler doğruysa veritabanı Firebase ile senkronize edilir.
     *
     * Giriş başarılı olduğunda
     * - Kullanınıcıya bir toast mesajı verilir.
     * - Sqlite veritabanı Firebase veritabanı ile senkronize olur
     * - Kullanıcı ana ekrana yönlendirilir.
     *
     * Giriş başarısız olursa.
     * - Kullanıcıya giriş işleminin başarısız olduğu ile ilgili toast mesaj gönderilir.
     *
     * @param email kullanıcı emaili
     * @param password  kullanıcı şifresi
     */
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Giriş başarılı!", Toast.LENGTH_SHORT).show()

                    val syncHelper = FirebaseSyncHelper(requireContext())

                    // ✅ Firestore senkronizasyonunu arka planda çalıştır
                    CoroutineScope(Dispatchers.IO).launch {
                        syncHelper.syncFirestoreToSQLite()
                    }

                    // Ana ekrana yönlendirme
                    val navController = findNavController()
                    navController.navigate(R.id.action_login_to_new_design)
                } else {
                    Toast.makeText(requireContext(), "Giriş başarısız! Bilgilerinizi kontrol edin.", Toast.LENGTH_SHORT).show()
                }
            }
    }


    /**
     * Parametre olarak alınan stringin email formatında olup olmadığını kontrol eder.
     *
     * @param email Kulllanıcı email bilgisi.
     *
     * @return E-posta geçerli formatta ise 'true', yanlış  formatta ise 'false' döner
     */

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Girilen şifrenin boyutunu kontrol eder.
     *
     * @param password Kullanıcı şifre bilgisi.
     *
     * @return Şifre boyutu 6'dan büyükse 'true', 6'dan  küçükse false döner.
     *
     */

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}
