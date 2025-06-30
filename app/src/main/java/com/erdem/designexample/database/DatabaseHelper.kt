package com.erdem.designexample.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "db", null, 27) { // 🔹 Versiyon 24'ten 25'e çıkarıldı!

    // onCreate: Veritabanı üzerindeki tabloların tanımlandığı yer
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE TeaGardens (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
                "gardenName TEXT NOT NULL UNIQUE, " +
                "isSynced INTEGER DEFAULT 0 )")  // 🔹 Firestore ile senkronizasyon durumu için yeni sütun

        db?.execSQL("CREATE TABLE TeaHarverst (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
                "year INTEGER NOT NULL, " +
                "month INTEGER NOT NULL, " +
                "day INTEGER NOT NULL, " +
                "season INTEGER NOT NULL, " +
                "garden_id INTEGER NOT NULL, " +
                "weight_kg REAL NOT NULL, " +
                "company TEXT  NOT NULL, " +
                "price REAL NOT NULL, " +
                "paymentDate TEXT NOT NULL , " +
                "isSynced INTEGER DEFAULT 0, " +  // 🔹 Firestore ile senkronizasyon durumu için yeni sütun
                "FOREIGN KEY (garden_id) REFERENCES TeaGardens(id))")
    }

    // Veritabanında bir güncelleme yapıldığında çalışır (Önceki verileri koruyarak güncelleriz)
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 25) { // 🔹 Sadece eski versiyonlardan güncelleniyorsa tabloyu değiştirelim
            db?.execSQL("ALTER TABLE TeaGardens ADD COLUMN isSynced INTEGER DEFAULT 0")
            db?.execSQL("ALTER TABLE TeaHarverst ADD COLUMN isSynced INTEGER DEFAULT 0")
        }
    }
}
