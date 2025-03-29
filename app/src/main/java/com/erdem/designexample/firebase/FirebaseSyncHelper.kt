package com.erdem.designexample.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

class FirebaseSyncHelper(private val context: Context) {
    private val dbHelper = DatabaseHelper(context) // SQLite veritabanı erişimi
    private val firebaseDB = FirebaseFirestore.getInstance() // Firestore bağlantısı
    private val userId = FirebaseAuth.getInstance().currentUser?.uid // Kullanıcı kimliği
    private val sharedPreferences = context.getSharedPreferences("offline_deletes", Context.MODE_PRIVATE) // Silme işlemleri için yerel kayıt
    private val gson = Gson() // JSON işlemleri için Gson kütüphanesi

    /**
     * 📌 **SQLite Verilerini Firestore'a Senkronize Et**
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

        // ✅ 1. Çay Bahçelerini Firestore’a Yedekle
        val gardenCursor: Cursor = db.rawQuery("SELECT * FROM TeaGardens WHERE isSynced = 0", null)
        while (gardenCursor.moveToNext()) {
            val id = gardenCursor.getInt(gardenCursor.getColumnIndexOrThrow("id"))
            val gardenName = gardenCursor.getString(gardenCursor.getColumnIndexOrThrow("gardenName"))

            val gardenData = hashMapOf("gardenName" to gardenName)

            firebaseDB.collection("Users").document(userId!!)
                .collection("TeaGardens").document(id.toString())
                .set(gardenData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("FirebaseSync", "✅ Bahçe Firestore'a yüklendi: $gardenName")

                    val updateQuery = "UPDATE TeaGardens SET isSynced = 1 WHERE id = ?"
                    db.execSQL(updateQuery, arrayOf(id))
                }
                .addOnFailureListener {
                    Log.e("FirebaseSync", "❌ Bahçe yüklenemedi: $gardenName", it)
                }
        }
        gardenCursor.close()

        // ✅ 2. Hasat Verilerini Firestore’a Yedekle
        val harvestCursor: Cursor = db.rawQuery("SELECT * FROM TeaHarverst WHERE isSynced = 0", null)
        while (harvestCursor.moveToNext()) {
            val id = harvestCursor.getInt(harvestCursor.getColumnIndexOrThrow("id"))
            val year = harvestCursor.getInt(harvestCursor.getColumnIndexOrThrow("year"))
            val month = harvestCursor.getInt(harvestCursor.getColumnIndexOrThrow("month"))
            val day = harvestCursor.getInt(harvestCursor.getColumnIndexOrThrow("day"))
            val season = harvestCursor.getInt(harvestCursor.getColumnIndexOrThrow("season"))
            val gardenId = harvestCursor.getInt(harvestCursor.getColumnIndexOrThrow("garden_id"))
            val weightKg = harvestCursor.getDouble(harvestCursor.getColumnIndexOrThrow("weight_kg"))
            val company = harvestCursor.getString(harvestCursor.getColumnIndexOrThrow("company"))
            val price = harvestCursor.getDouble(harvestCursor.getColumnIndexOrThrow("price"))
            val paymentDate = harvestCursor.getString(harvestCursor.getColumnIndexOrThrow("paymentDate"))

            val harvestData = hashMapOf(
                "year" to year,
                "month" to month,
                "day" to day,
                "season" to season,
                "garden_id" to gardenId,
                "weight_kg" to weightKg,
                "company" to company,
                "price" to price,
                "paymentDate" to paymentDate
            )

            firebaseDB.collection("Users").document(userId!!)
                .collection("TeaHarvest").document(id.toString())
                .set(harvestData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("FirebaseSync", "✅ Hasat Firestore'a yüklendi: ID=$id, $company - $weightKg kg")

                    val updateQuery = "UPDATE TeaHarverst SET isSynced = 1 WHERE id = ?"
                    db.execSQL(updateQuery, arrayOf(id))
                }
                .addOnFailureListener {
                    Log.e("FirebaseSync", "❌ Hasat yüklenemedi: ID=$id", it)
                }
        }
        harvestCursor.close()
    }

    /**
     * 📌 **Firestore’dan Belge Sil**
     * - Eğer internet varsa hemen siler.
     * - Eğer internet yoksa, `addPendingDelete()` ile işlemi kaydeder.
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
     * 📌 **İnternet Yokken Bekleyen Silme İşlemlerini Kaydet**
     * - SharedPreferences içinde JSON olarak saklar.
     */
    private fun addPendingDelete(collection: String, documentId: String) {
        val pendingDeletes = getPendingDeletes().toMutableList()
        pendingDeletes.add(Pair(collection, documentId))
        sharedPreferences.edit().putString("pending_deletes", gson.toJson(pendingDeletes)).apply()
    }

    /**
     * 📌 **Bekleyen Silme İşlemlerini Al**
     * - SharedPreferences içindeki JSON verisini listeye çevirir.
     */
    private fun getPendingDeletes(): List<Pair<String, String>> {
        val json = sharedPreferences.getString("pending_deletes", null) ?: return emptyList()
        val type = object : com.google.gson.reflect.TypeToken<List<Pair<String, String>>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    /**
     * 📌 **İnternet Gelince Bekleyen Silme İşlemlerini Çalıştır**
     * - Daha önce kaydedilen silme işlemlerini Firestore’a uygular.
     */
    suspend fun processPendingDeletes() {
        if (!isInternetAvailable()) return
        if (userId == null) return

        val pendingDeletes = getPendingDeletes()
        for ((collection, documentId) in pendingDeletes) {
            firebaseDB.collection("Users").document(userId)
                .collection(collection).document(documentId)
                .delete()
                .addOnSuccessListener { Log.d("FirebaseSync", "✅ Bekleyen silme tamamlandı: $documentId") }
                .addOnFailureListener { e -> Log.e("FirebaseSync", "❌ Bekleyen silme başarısız: ${e.message}") }
        }

        // ✅ Tüm silme işlemleri bittikten sonra JSON verisini temizle
        sharedPreferences.edit { remove("pending_deletes") }
    }

    /**
     * 🌐 **İnternet Bağlantısını Kontrol Et**
     * @return **true** -> İnternet var, **false** -> İnternet yok.
     */
    private suspend fun isInternetAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // 1️⃣ Önce cihazın bir ağa bağlı olup olmadığını kontrol et
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return@withContext false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@withContext false
                if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return@withContext false
                }
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo == null || !activeNetworkInfo.isConnected) {
                    return@withContext false
                }
            }

            // 2️⃣ Şimdi Google’a bir HTTP isteği atarak gerçek internet var mı kontrol et
            try {
                val url = URL("https://www.google.com")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 1500
                urlConnection.readTimeout = 1500
                urlConnection.connect()
                return@withContext (urlConnection.responseCode == 200)
            } catch (e: Exception) {
                return@withContext false
            }
        }
    }




    suspend fun syncFirestoreToSQLite() {
        if (!isInternetAvailable()) {
            Log.e("FirebaseSync", "📡 İnternet yok, Firestore senkronizasyonu iptal edildi.")
            return
        }

        if (userId == null) {
            Log.e("FirebaseSync", "❌ Kullanıcı giriş yapmamış!")
            return
        }

        val db = dbHelper.writableDatabase

        // ✅ 1. Çay Bahçelerini Firebase'den Al ve SQLite'a Kaydet
        firebaseDB.collection("Users").document(userId!!)
            .collection("TeaGardens")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.id.toInt()
                    val gardenName = document.getString("gardenName") ?: continue

                    // SQLite'ta bu bahçe var mı kontrol et
                    val cursor = db.rawQuery("SELECT id FROM TeaGardens WHERE id = ?", arrayOf(id.toString()))
                    val exists = cursor.moveToFirst()
                    cursor.close()

                    if (exists) {
                        // Güncelle
                        val updateQuery = "UPDATE TeaGardens SET gardenName = ?, isSynced = 1 WHERE id = ?"
                        db.execSQL(updateQuery, arrayOf(gardenName, id))
                        Log.d("FirebaseSync", "🔄 Güncellendi: $gardenName")
                    } else {
                        // Yeni ekle
                        val insertQuery = "INSERT INTO TeaGardens (id, gardenName, isSynced) VALUES (?, ?, 1)"
                        db.execSQL(insertQuery, arrayOf(id, gardenName))
                        Log.d("FirebaseSync", "✅ Eklendi: $gardenName")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseSync", "❌ Firestore'dan Çay Bahçeleri alınamadı!", e)
            }

        // ✅ 2. Hasat Verilerini Firebase'den Al ve SQLite'a Kaydet
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

                    // SQLite'ta bu hasat var mı kontrol et
                    val cursor = db.rawQuery("SELECT id FROM TeaHarverst WHERE id = ?", arrayOf(id.toString()))
                    val exists = cursor.moveToFirst()
                    cursor.close()

                    if (exists) {
                        // Güncelle
                        val updateQuery = """
                        UPDATE TeaHarverst 
                        SET year = ?, month = ?, day = ?, season = ?, garden_id = ?, weight_kg = ?, 
                            company = ?, price = ?, paymentDate = ?, isSynced = 1 
                        WHERE id = ?
                    """
                        db.execSQL(updateQuery, arrayOf(year, month, day, season, gardenId, weightKg, company, price, paymentDate, id))
                        Log.d("FirebaseSync", "🔄 Güncellendi: ID=$id, $company - $weightKg kg")
                    } else {
                        // Yeni ekle
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
