package com.erdem.designexample

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(contex:Context) : SQLiteOpenHelper(contex, "db", null, 6) {


    //onCreate veritabanı üzerindeki tabloların tanımlandığı yer
    override fun onCreate(db: SQLiteDatabase?) {

        db?.execSQL("CREATE TABLE TeaGardens (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
                "gardenName TEXT NOT NULL UNIQUE )")

        db?.execSQL("CREATE TABLE TeaHarverst (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
                "year INTEGER NOT NULL, " +
                "month INTEGER NOT NULL, " +
                "day INTEGER NOT NULL, " +
                "season INTEGER NOT NULL, " +
                "garden_id INTEGER NOT NULL, " +
                "weight_kg INTEGER NOT NULL, " +
                "company TEXT  UNIQUE, " +
                "price REAL , " +
                "paymentDate TEXT , " +
                "FOREIGN KEY (garden_id) REFERENCES TeaGardens(id))")
    }

    //Veritabanında bir sorun olduğu zaman ne olacağı onUpgrade içerisine kodlanır
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS TeaGardens")
        db?.execSQL("DROP TABLE IF EXISTS TeaHarverst")
        onCreate(db)
    }
}