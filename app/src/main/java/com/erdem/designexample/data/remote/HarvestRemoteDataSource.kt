package com.erdem.designexample.data.remote

import android.util.Log
import com.erdem.designexample.data.db.entity.HarvestEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HarvestRemote"

/**
 * Hasat kayıtlarının bulut (Firestore) tarafı. Her kullanıcının verisi kendi uid'i altında
 * tutulur: `users/{uid}/harvests/{harvestId}`. Doküman id'si Room'daki [HarvestEntity.id]
 * ile aynıdır; böylece geri yüklemede REPLACE ile birebir eşlenir.
 *
 * Not: Firestore çevrimdışı kalıcılığı varsayılan açıktır — internet yokken yazımlar kuyruğa
 * alınır, bağlantı gelince otomatik gönderilir. Bu yüzden yazımlarda hata yutulur (best-effort).
 */
@Singleton
class HarvestRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val uid: String? get() = auth.currentUser?.uid

    /** Oturum açık kullanıcının hasat koleksiyonu; oturum yoksa null. */
    private fun harvestsCollection() = uid?.let { id ->
        firestore.collection("users").document(id).collection("harvests")
    }

    /**
     * Tek kaydı buluta yazar/günceller. Oturum yoksa atlar.
     * **Fire-and-forget:** Task beklenmez — çevrimdışıyken yazım yerel kuyruğa alınır ve
     * bağlantı gelince otomatik gönderilir. (`.await()` çevrimdışıyken sunucu onayına kadar
     * askıda kalacağı için bilinçli olarak kullanılmıyor; kayıt akışı kilitlenmesin diye.)
     */
    fun upsert(entity: HarvestEntity) {
        val col = harvestsCollection()
        if (col == null) {
            Log.w(TAG, "upsert ATLANDI — oturum yok (uid null)")
            return
        }
        Log.d(TAG, "upsert deneniyor → users/$uid/harvests/${entity.id}")
        col.document(entity.id.toString()).set(entity.toMap())
            .addOnSuccessListener { Log.d(TAG, "✅ Buluta yazıldı: harvest ${entity.id}") }
            .addOnFailureListener { e -> Log.e(TAG, "❌ Bulut yazımı BAŞARISIZ: harvest ${entity.id}", e) }
    }

    /** Kaydı buluttan siler (fire-and-forget; çevrimdışı kuyruğa alınır). */
    fun delete(id: Long) {
        val col = harvestsCollection()
        if (col == null) {
            Log.w(TAG, "delete ATLANDI — oturum yok (uid null)")
            return
        }
        col.document(id.toString()).delete()
            .addOnSuccessListener { Log.d(TAG, "✅ Buluttan silindi: harvest $id") }
            .addOnFailureListener { e -> Log.e(TAG, "❌ Bulut silme BAŞARISIZ: harvest $id", e) }
    }

    /**
     * Tüm bulut kayıtlarını indirir.
     * Başarılı (boş olsa bile) → [Result.success]; oturum yok/internet yok/hata → [Result.failure].
     * Çağıran taraf bu ayrımı geri yüklemeyi tekrar deneyip denememek için kullanır.
     */
    suspend fun fetchAll(): Result<List<HarvestEntity>> {
        val col = harvestsCollection() ?: run {
            Log.w(TAG, "fetchAll ATLANDI — oturum yok (uid null)")
            return Result.failure(IllegalStateException("Oturum yok"))
        }
        return runCatching {
            col.get().await().documents.mapNotNull { it.toHarvestEntity() }
        }.onSuccess { Log.d(TAG, "⬇️ Buluttan indirildi: ${it.size} kayıt") }
            .onFailure { Log.e(TAG, "❌ Buluttan indirme BAŞARISIZ", it) }
    }
}

// --- Eşleme yardımcıları ----------------------------------------------------------------------

private fun HarvestEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "gardenName" to gardenName,
    "harvestDateEpoch" to harvestDateEpoch,
    "season" to season,
    "weightKg" to weightKg.toDouble(),
    "pricePerKg" to pricePerKg.toDouble(),
    "source" to source,
    "companyName" to companyName,
    "dueDateEpoch" to dueDateEpoch,
    "createdAt" to createdAt
)

private fun DocumentSnapshot.toHarvestEntity(): HarvestEntity? = runCatching {
    HarvestEntity(
        id               = getLong("id") ?: return null,
        gardenName       = getString("gardenName") ?: "",
        harvestDateEpoch = getLong("harvestDateEpoch") ?: return null,
        season           = (getLong("season") ?: 0L).toInt(),
        weightKg         = (getDouble("weightKg") ?: 0.0).toFloat(),
        pricePerKg       = (getDouble("pricePerKg") ?: 0.0).toFloat(),
        source           = getString("source") ?: "OZEL",
        companyName      = getString("companyName") ?: "",
        dueDateEpoch     = getLong("dueDateEpoch") ?: 0L,
        createdAt        = getLong("createdAt") ?: System.currentTimeMillis()
    )
}.getOrNull()
