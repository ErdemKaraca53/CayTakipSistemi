package com.erdem.designexample.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

class FirebaseSyncHelper(private val context: Context) {
    private val dbHelper = DatabaseHelper(context) // SQLite veritabanı erişimi
    private val firebaseDB = FirebaseFirestore.getInstance() // Firestore bağlantısı
    private val userId = FirebaseAuth.getInstance().currentUser?.uid // Kullanıcı kimliği
    private val sharedPreferences = context.getSharedPreferences("offline_deletes", Context.MODE_PRIVATE) // Silme işlemleri için yerel kayıt
    private val gson = Gson() // JSON işlemleri için Gson kütüphanesi

    /**
     * SQLite verilerini Firestore'a yedekler.
     *
     * - Eğer internet yoksa veya kullanıcı giriş yapmamışsa işlem iptal edilir.
     * - `isSynced = 0` olan veriler toplanır ve Firestore'a batch (toplu işlem) olarak kaydedilir.
     * - Başarıyla yedeklenen verilerin `isSynced` değeri SQLite üzerinde güncellenir.
     *
     * **İçerdiği İşlemler:**
     *  1️⃣ Çay Bahçeleri (TeaGardens) → Firestore'a yedeklenir.
     *  2️⃣ Hasat Verileri (TeaHarvest) → Firestore'a yedeklenir.
     */
    suspend fun backupSQLiteToFirestore() {
        if (!isInternetAvailable()) {
            Log.e("FirebaseSync", "📡 İnternet yok, yedekleme iptal edildi.")
            return
        }

        if (userId == null) {
            Log.e("FirebaseSync", "❌ Kullanıcı giriş yapmamış!")
            return
        }

        val db: SQLiteDatabase = dbHelper.writableDatabase

        // ✅ 1. Çay Bahçelerini Yedekleme
        syncTableToFirestore(
            tableName = "TeaGardens",
            collectionName = "TeaGardens",
            mapping = { cursor ->
                hashMapOf("gardenName" to cursor.getString(cursor.getColumnIndexOrThrow("gardenName")))
            }
        )

        // ✅ 2. Hasat Verilerini Yedekleme
        syncTableToFirestore(
            tableName = "TeaHarverst",
            collectionName = "TeaHarvest",
            mapping = { cursor ->
                hashMapOf(
                    "year" to cursor.getInt(cursor.getColumnIndexOrThrow("year")),
                    "month" to cursor.getInt(cursor.getColumnIndexOrThrow("month")),
                    "day" to cursor.getInt(cursor.getColumnIndexOrThrow("day")),
                    "season" to cursor.getInt(cursor.getColumnIndexOrThrow("season")),
                    "garden_id" to cursor.getInt(cursor.getColumnIndexOrThrow("garden_id")),
                    "weight_kg" to cursor.getDouble(cursor.getColumnIndexOrThrow("weight_kg")),
                    "company" to cursor.getString(cursor.getColumnIndexOrThrow("company")),
                    "price" to cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    "paymentDate" to cursor.getString(cursor.getColumnIndexOrThrow("paymentDate"))
                )
            }
        )
    }

    /**
     * Genel amaçlı SQLite → Firestore senkronizasyon fonksiyonu.
     *
     * - `tableName` içindeki **isSynced = 0** olan verileri alır.
     * - Firestore'daki `collectionName` altına ekler.
     * - İşlem başarılıysa SQLite'daki `isSynced` değerini **1** olarak günceller.
     *
     * @param tableName SQLite içindeki tablo adı
     * @param collectionName Firestore koleksiyon adı
     * @param mapping SQLite'dan okunan Cursor'u HashMap'e dönüştüren lambda fonksiyonu
     */
    private suspend fun syncTableToFirestore(
        tableName: String,
        collectionName: String,
        mapping: (Cursor) -> HashMap<String, Any?>
    ) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $tableName WHERE isSynced = 0", null)

        if (!cursor.moveToFirst()) {
            cursor.close()
            Log.d("FirebaseSync", "✅ $tableName tablosunda senkronize edilecek veri yok.")
            return
        }

        val batch = firebaseDB.batch()

        try {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val data = mapping(cursor)

                val docRef = firebaseDB.collection("Users").document(userId!!)
                    .collection(collectionName).document(id.toString())

                batch.set(docRef, data, SetOptions.merge())

                // SQLite'daki `isSynced` durumunu güncelle
                val updateQuery = "UPDATE $tableName SET isSynced = 1 WHERE id = ?"
                db.execSQL(updateQuery, arrayOf(id))

            } while (cursor.moveToNext())

            batch.commit().await() // 🔥 Firestore'a verileri tek seferde gönder

            Log.d("FirebaseSync", "✅ $tableName başarıyla Firestore'a yedeklendi.")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "❌ $tableName yedekleme başarısız!", e)
        } finally {
            cursor.close()
        }
    }



    /**
     * Firestore'dan belirli bir belgeyi siler.
     *
     * - Eğer internet bağlantısı yoksa, silme işlemi beklemeye alınır (`addPendingDelete()` çağrılır).
     * - Eğer kullanıcı giriş yapmamışsa, işlem iptal edilir.
     * - Silme işlemi başarılı olursa log kaydı oluşturulur.
     * - Silme başarısız olursa hata mesajı loglanır ve gerekirse bekleyen işlemlere eklenir.
     *
     * @param collection Silinecek belgenin bulunduğu Firestore koleksiyonu.
     * @param documentId Silinecek belgenin ID'si.
     */
    suspend fun deleteFromFirestore(collection: String, documentId: String) {
        if (!isInternetAvailable()) {
            Log.e("FirebaseSync", "📡 İnternet yok, silme işlemi beklemeye alındı.")
            addPendingDelete(collection, documentId) // Silme işlemini beklet
            return
        }

        if (userId == null) {
            Log.e("FirebaseSync", "❌ Kullanıcı giriş yapmamış!")
            return
        }

        firebaseDB.collection("Users").document(userId)
            .collection(collection).document(documentId)
            .delete()
            .addOnSuccessListener { Log.d("FirebaseSync", "✅ Silindi: $collection/$documentId") }
            .addOnFailureListener { e -> Log.e("FirebaseSync", "❌ Silme başarısız: ${e.message}") }
    }

    /**
     * Silinmesi gereken ancak şu an gerçekleştirilemeyen bir Firestore belgesini bekleyen silme listesine ekler.
     *
     * - Eğer internet bağlantısı yoksa veya başka bir sebepten dolayı silme işlemi yapılamıyorsa,
     *   bu fonksiyon çağrılarak işlem daha sonra gerçekleştirilecek şekilde saklanır.
     * - Silme işlemi, `pending_deletes` adında bir JSON formatında `SharedPreferences` içinde tutulur.
     * - `getPendingDeletes()` fonksiyonu çağrılarak mevcut bekleyen silme işlemleri çekilir ve listeye yeni işlem eklenir.
     *
     * @param collection Silinmesi gereken belgenin bulunduğu Firestore koleksiyonu.
     * @param documentId Silinmesi gereken belgenin ID'si.
     */
    private fun addPendingDelete(collection: String, documentId: String) {
        // ✅ 1. Bekleyen silme işlemlerini SharedPreferences'tan al ve mutable listeye dönüştür
        val pendingDeletes = getPendingDeletes().toMutableList()

        // ✅ 2. Yeni silme işlemini listeye ekle
        pendingDeletes.add(Pair(collection, documentId))

        // ✅ 3. Güncellenmiş listeyi JSON formatında SharedPreferences'a kaydet
        sharedPreferences.edit().putString("pending_deletes", gson.toJson(pendingDeletes)).apply()
    }


    /**
     * 📌 **Bekleyen Silme İşlemlerini Al**
     *
     * - SharedPreferences içinde JSON formatında saklanan bekleyen silme işlemlerini alır.
     * - JSON stringini bir `List<Pair<String, String>>` listesine dönüştürür.
     * - Eğer bekleyen işlem yoksa boş bir liste döner.
     *
     * @return Bekleyen silme işlemlerinin listesi.
     */
    private fun getPendingDeletes(): List<Pair<String, String>> {
        // ✅ SharedPreferences'tan bekleyen silme işlemlerini JSON formatında al
        val json = sharedPreferences.getString("pending_deletes", null) ?: return emptyList()

        // ✅ JSON'u Kotlin List<Pair<String, String>> formatına çevir
        val type = object : com.google.gson.reflect.TypeToken<List<Pair<String, String>>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    /**
     * 📌 **İnternet Bağlantısı Gelince Bekleyen Silme İşlemlerini Çalıştır**
     *
     * - Daha önce internet olmadığı için yapılamayan silme işlemlerini tekrar dener.
     * - Firestore'da ilgili belgeleri silmeye çalışır.
     * - Silme işlemi başarılı olursa, işlem SharedPreferences'tan kaldırılır.
     */
    suspend fun processPendingDeletes() {
        // ✅ 1. İnternet olup olmadığını kontrol et
        if (!isInternetAvailable()) return

        // ✅ 2. Kullanıcının oturum açıp açmadığını kontrol et
        if (userId == null) return

        // ✅ 3. Bekleyen silme işlemlerini al
        val pendingDeletes = getPendingDeletes()

        // ✅ 4. Her bekleyen silme işlemini sırayla Firestore'da gerçekleştir
        for ((collection, documentId) in pendingDeletes) {
            firebaseDB.collection("Users").document(userId)
                .collection(collection).document(documentId)
                .delete()
                .addOnSuccessListener {
                    Log.d("FirebaseSync", "✅ Bekleyen silme tamamlandı: $documentId")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseSync", "❌ Bekleyen silme başarısız: ${e.message}")
                }
        }

        // ✅ 5. Tüm işlemler tamamlandıysa SharedPreferences içindeki bekleyen silme listesini temizle
        sharedPreferences.edit { remove("pending_deletes") }
    }

    /**
     * 🌐 **İnternet Bağlantısını Kontrol Et**
     *
     * - Cihazın bir ağa bağlı olup olmadığını kontrol eder.
     * - Gerçek bir internet bağlantısı olup olmadığını anlamak için Google’a HTTP isteği atar.
     *
     * @return **true** → İnternet var, **false** → İnternet yok.
     */

     fun isInternetAvailable(): Boolean {
        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    /**
     * 📌 **Firestore'daki Verileri SQLite'a Senkronize Et**
     *
     * - Eğer internet bağlantısı yoksa veya kullanıcı giriş yapmamışsa işlem iptal edilir.
     * - Firestore'daki "TeaGardens" ve "TeaHarvest" koleksiyonlarındaki veriler SQLite'a indirilir.
     * - Eğer veri zaten SQLite'ta varsa güncellenir, yoksa eklenir.
     */
    suspend fun syncFirestoreToSQLite() {
        // ✅ 1. İnternet bağlantısını kontrol et
        if (!isInternetAvailable()) {
            Log.e("FirebaseSync", "📡 İnternet yok, Firestore senkronizasyonu iptal edildi.")
            return
        }

        // ✅ 2. Kullanıcının giriş yapıp yapmadığını kontrol et
        if (userId == null) {
            Log.e("FirebaseSync", "❌ Kullanıcı giriş yapmamış!")
            return
        }

        val db = dbHelper.writableDatabase

        // ✅ 3. Çay Bahçelerini Firestore'dan Al ve SQLite'a Kaydet
        firebaseDB.collection("Users").document(userId!!)
            .collection("TeaGardens")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.id.toInt()
                    val gardenName = document.getString("gardenName") ?: continue

                    // ✅ 3.1 SQLite'ta bu bahçe var mı kontrol et
                    val cursor = db.rawQuery("SELECT id FROM TeaGardens WHERE id = ?", arrayOf(id.toString()))
                    val exists = cursor.moveToFirst()
                    cursor.close()

                    if (exists) {
                        // ✅ 3.2 Eğer bahçe SQLite'ta varsa güncelle
                        val updateQuery = "UPDATE TeaGardens SET gardenName = ?, isSynced = 1 WHERE id = ?"
                        db.execSQL(updateQuery, arrayOf(gardenName, id))
                        Log.d("FirebaseSync", "🔄 Güncellendi: $gardenName")
                    } else {
                        // ✅ 3.3 Eğer bahçe yoksa yeni olarak ekle
                        val insertQuery = "INSERT INTO TeaGardens (id, gardenName, isSynced) VALUES (?, ?, 1)"
                        db.execSQL(insertQuery, arrayOf(id, gardenName))
                        Log.d("FirebaseSync", "✅ Eklendi: $gardenName")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseSync", "❌ Firestore'dan Çay Bahçeleri alınamadı!", e)
            }

        // ✅ 4. Hasat Verilerini Firestore'dan Al ve SQLite'a Kaydet
        firebaseDB.collection("Users").document(userId!!)
            .collection("TeaHarvest")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.id.toInt()
                    val year = document.getLong("year")?.toInt() ?: continue
                    val month = document.getLong("month")?.toInt() ?: continue
                    val day = document.getLong("day")?.toInt() ?: continue
                    val season = document.getLong("season")?.toInt() ?: continue
                    val gardenId = document.getLong("garden_id")?.toInt() ?: continue
                    val weightKg = document.getDouble("weight_kg") ?: continue
                    val company = document.getString("company") ?: continue
                    val price = document.getDouble("price") ?: continue
                    val paymentDate = document.getString("paymentDate") ?: continue

                    // ✅ 4.1 SQLite'ta bu hasat var mı kontrol et
                    val cursor = db.rawQuery("SELECT id FROM TeaHarverst WHERE id = ?", arrayOf(id.toString()))
                    val exists = cursor.moveToFirst()
                    cursor.close()

                    if (exists) {
                        // ✅ 4.2 Eğer hasat SQLite'ta varsa güncelle
                        val updateQuery = """
                        UPDATE TeaHarverst 
                        SET year = ?, month = ?, day = ?, season = ?, garden_id = ?, weight_kg = ?, 
                            company = ?, price = ?, paymentDate = ?, isSynced = 1 
                        WHERE id = ?
                    """
                        db.execSQL(updateQuery, arrayOf(year, month, day, season, gardenId, weightKg, company, price, paymentDate, id))
                        Log.d("FirebaseSync", "🔄 Güncellendi: ID=$id, $company - $weightKg kg")
                    } else {
                        // ✅ 4.3 Eğer hasat yoksa yeni olarak ekle
                        val insertQuery = """
                        INSERT INTO TeaHarverst 
                        (id, year, month, day, season, garden_id, weight_kg, company, price, paymentDate, isSynced) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
                    """
                        db.execSQL(insertQuery, arrayOf(id, year, month, day, season, gardenId, weightKg, company, price, paymentDate))
                        Log.d("FirebaseSync", "✅ Eklendi: ID=$id, $company - $weightKg kg")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseSync", "❌ Firestore'dan Hasatlar alınamadı!", e)
            }
    }



}
