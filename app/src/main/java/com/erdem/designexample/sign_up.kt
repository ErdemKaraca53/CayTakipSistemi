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
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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
        /**
        Navigation bar sign-up ve login sayfalarında gerekli değil.
        Bu yüzden sign-up ve login sayfalarından geçiş yapılana kadar kaldırılıyor.
         **/
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
        bottomNavigationView.visibility = View.GONE
        return binding.root
    }

    /**
     * Fragment'in görünür hale geldiğinde çalıştırılacak işlemleri gerçekleştirir.
     *
     * - Eğer giriş yapmış bir kullanıcı varsa, `reload()` fonksiyonu çağrılır.
     * - SQLite verileri Firestore'a yedeklenir (senkronizasyon işlemi başlatılır).
     * - Veri senkronizasyon işlemi `CoroutineScope(Dispatchers.IO)` kullanılarak arka planda çalıştırılır.
     */
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
            val syncHelper = FirebaseSyncHelper(requireContext())
            // ✅ Coroutine ile Firestore'dan SQLite'a veri çekme işlemini arka planda çalıştır
            CoroutineScope(Dispatchers.IO).launch {
                syncHelper.backupSQLiteToFirestore()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    /**
     * Kullanıcının kayıt olmasını sağlamak için e-posta ve şifre girişlerini kontrol eder.
     *
     * - Kullanıcı kayıt butonuna bastığında, e-posta ve şifre girişleri alınır.
     * - Eğer e-posta adresi geçerli bir formatta değilse hata mesajı gösterilir.
     * - Eğer şifre en az 6 karakter değilse hata mesajı gösterilir.
     * - Bilgiler geçerliyse `createAccount()` fonksiyonu çağrılarak kayıt işlemi başlatılır.
     * - Kullanıcı "Giriş Yap" bağlantısına tıkladığında, giriş ekranına yönlendirilir.
     *
     * @param view Fragment'e ait View nesnesi.
     * @param savedInstanceState Önceki durumu saklayan Bundle nesnesi.
     */

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

    /**
     * Kullanıcıyı belirtilen e-posta ve şifre ile kayıt eder.
     *
     * - Firebase Authentication kullanılarak yeni bir kullanıcı oluşturulur.
     * - Eğer kayıt başarılı olursa:
     *   - `sendEmailVerification()` çağrılarak e-posta doğrulama işlemi başlatılır.
     *   - `updateUI(user)` çağrılarak kullanıcı arayüzü güncellenir.
     * - Eğer kayıt başarısız olursa:
     *   - Kullanıcıya hata mesajı gösterilir.
     *   - `updateUI(null)` çağrılarak kullanıcı arayüzü güncellenir.
     *
     * @param email Kullanıcının e-posta adresi.
     * @param password Kullanıcının şifresi.
     */

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    sendEmailVerification()
                    updateUI(user)
                } else {
                    // Hata kontrolü
                    try {
                        throw task.exception ?: Exception("Bilinmeyen hata!")
                    } catch (e: FirebaseAuthUserCollisionException) {
                        // E-posta zaten kullanılıyorsa kullanıcıya mesaj göster
                        Toast.makeText(requireContext(), "Bu e-posta zaten kayıtlı!", Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        // Şifre zayıfsa kullanıcıya mesaj göster
                        Toast.makeText(requireContext(), "Şifre çok zayıf! Daha güçlü bir şifre deneyin.", Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        // Geçersiz e-posta adresi formatı
                        Toast.makeText(requireContext(), "Geçersiz e-posta adresi!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Diğer hatalar
                        Log.w(TAG, "createUserWithEmail:failure", e)
                        Toast.makeText(requireContext(), "Kayıt işlemi başarısız!", Toast.LENGTH_SHORT).show()
                    }

                    updateUI(null)
                }
            }
    }


    /**
     * Kullanıcının e-posta adresine doğrulama bağlantısı gönderir.
     *
     * - Firebase Authentication ile doğrulama e-postası gönderilir.
     * - Kullanıcının doğrulamayı tamamlaması gerekir, aksi takdirde giriş yapamaz.
     * - Eğer doğrulama e-postası başarıyla gönderilirse, kullanıcıya bilgi mesajı gösterilir.
     * - Eğer işlem başarısız olursa, hata mesajı gösterilir ve `task.exception` loglanır.
     */
    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Doğrulama e-postası gönderildi.", Toast.LENGTH_SHORT).show()
                } else {
                    // Hata kontrolü yapalım
                    val errorMessage = task.exception?.localizedMessage ?: "Bilinmeyen hata!"
                    Log.w(TAG, "E-posta doğrulama hatası: $errorMessage")

                    if (task.exception is FirebaseTooManyRequestsException) {
                        Toast.makeText(
                            requireContext(),
                            "Çok fazla doğrulama isteği gönderdiniz! Lütfen daha sonra tekrar deneyin.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(requireContext(), "E-posta gönderme başarısız!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


    /**
     * Verilen e-posta adresinin geçerli olup olmadığını kontrol eder.
     *
     * Firebase'in `Patterns.EMAIL_ADDRESS` desenini kullanarak e-posta adresinin
     * standart formatta olup olmadığını doğrular.
     *
     * @param email Kullanıcının e-posta adresi.
     * @return Eğer geçerli bir e-posta formatındaysa `true`, aksi halde `false` döner.
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Kullanıcının girdiği şifrenin geçerli olup olmadığını kontrol eder.
     *
     * Şifre geçerlilik kriteri:
     * - En az 6 karakter uzunluğunda olmalıdır.
     *
     * @param password Kullanıcının şifresi.
     * @return Şifre en az 6 karakter içeriyorsa `true`, aksi halde `false` döner.
     */
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Kullanıcının giriş yapma durumuna göre arayüzü günceller.
     *
     * - Eğer `user` nesnesi `null` değilse, kullanıcı başarılı şekilde giriş yapmıştır.
     * - Kullanıcıya hoş geldiniz mesajı gösterilir.
     * - Ana sayfaya yönlendirme işlemi gerçekleştirilir.
     *
     * @param user Firebase Authentication ile giriş yapmış kullanıcı.
     */
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(requireContext(), "Hoş geldiniz: ${user.email}", Toast.LENGTH_SHORT).show()

            // Kullanıcıyı ana ekrana yönlendir
            val navController = findNavController()
            navController.navigate(R.id.action_signUp2_to_new_design) // HomeFragment'in id'si
        }
    }

    /**
     * Uygulama ekranını yenileyerek ana sayfaya yönlendirir.
     *
     * - Kullanıcıya "Sayfa yenilendi!" mesajı gösterilir.
     * - Ana sayfa olarak belirlenen fragment'a yönlendirme yapılır.
     */
    private fun reload() {
        Toast.makeText(requireContext(), "Sayfa yenilendi!", Toast.LENGTH_SHORT).show()
        val navController = findNavController()
        navController.navigate(R.id.action_signUp2_to_new_design) // HomeFragment'in id'si
    }


    companion object {
        private const val TAG = "EmailPassword"
    }
}
