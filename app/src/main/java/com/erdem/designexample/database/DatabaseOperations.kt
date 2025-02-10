package com.erdem.designexample.database

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.erdem.designexample.dataClass.BahceRapor
import com.erdem.designexample.dataClass.SurumRapor
import com.erdem.designexample.dataClass.YılRapor

class DatabaseOperations {

    fun add(helper: DatabaseHelper, garden: TeaGardens, harverst: TeaHarverst, context: Context){

        val db = helper.writableDatabase

        val harverstValues = ContentValues()
        val gardenValues = ContentValues()

        gardenValues.put("gardenName", garden.gardenName)
        harverstValues.put("year", harverst.year)
        harverstValues.put("month", harverst.month)
        harverstValues.put("day", harverst.day)
        harverstValues.put("season", harverst.season)
        harverstValues.put("weight_kg", harverst.weight_kg)

        //SONRADAN EKLENDİ KONTROL EDİLMELİ
        harverstValues.put("company", harverst.SatisYeri)
        harverstValues.put("price", harverst.SatisFiyati)
        harverstValues.put("paymentDate", harverst.VadeTarihi.toString())
    //
        //Eğer daha önce bu bahçe ismi kullanılmamış ise kayıt yapar
        if (isGardenExists(helper,garden.gardenName) == -1) {

            db.insertOrThrow("TeaGardens", null, gardenValues)
            val garden_id = isGardenExists(helper,garden.gardenName)
            harverstValues.put("garden_id", garden_id)
            db.insertOrThrow("TeaHarverst", null, harverstValues)

        } else {
            if (!checkSeasonAndYear(helper, harverst.year, harverst.season, isGardenExists(helper, garden.gardenName), harverst.VadeTarihi)) {
                harverstValues.put("garden_id", isGardenExists(helper,garden.gardenName))
                db.insertOrThrow("TeaHarverst", null, harverstValues)
            } else {
                Log.e("hata", harverst.VadeTarihi)
                val db = helper.writableDatabase
                val updateQuery = """
                                UPDATE TeaHarverst
                                SET weight_kg = weight_kg + ?
                                WHERE year = ? AND season = ? AND garden_id = ? AND paymentDate = ?
                            """
                val statement = db.compileStatement(updateQuery)

                // Parametreleri bağla
                statement.bindDouble(1, harverst.weight_kg.toDouble()) // İlk "?" -> weight_kg
                statement.bindString(2, harverst.year.toString()) // İkinci "?" -> year
                statement.bindString(3, harverst.season.toString()) // Üçüncü "?" -> season
                statement.bindLong(4, isGardenExists(helper, garden.gardenName).toLong()) // Dördüncü "?" -> garden_id
                statement.bindString(5, harverst.VadeTarihi) // Beşinci "?" -> paymentDate

                // Sorguyu çalıştır
                statement.execute()
            }
        }

        //db.execSQL("DELETE FROM TeaGardens")
        //db.close()
    }

    fun readData(helper: DatabaseHelper, year: Int, season: Int) : ArrayList<TeaHarverst> {

        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM TeaHarverst JOIN TeaGardens ON TeaHarverst.garden_id = TeaGardens.id " +
                                    "WHERE year = ? AND season =?",
            arrayOf(year.toString(), season.toString())
        )

        val harverst = ArrayList<TeaHarverst>()

        while(cursor.moveToNext()) {

            val year = cursor.getInt(cursor.getColumnIndexOrThrow("year"))
            val month = cursor.getInt(cursor.getColumnIndexOrThrow("month"))
            val day = cursor.getInt(cursor.getColumnIndexOrThrow("day"))
            val season = cursor.getInt(cursor.getColumnIndexOrThrow("season"))
            val gardenName = cursor.getString(cursor.getColumnIndexOrThrow("gardenName"))
            val weight_kg = cursor.getFloat(cursor.getColumnIndexOrThrow("weight_kg"))

            /*
                SATIŞ YERİ, SATIŞ FİYAT - VADE TARİHİ DEAFULT DEĞERLER KOYULDU ŞİMDİLİK.
                DÜZELTİLECEK !!!!!!!!!!!!!!
             */
            val tmp = TeaHarverst(year,month,day,season,gardenName, weight_kg, "",0.0f,"null")
            harverst.add(tmp)

        }
        cursor.close()
        return harverst
    }

    fun GetInfoYear(databaseHelper: DatabaseHelper) : ArrayList<YılRapor>{

        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT year, sum(weight_kg) as total_weight, sum(weight_kg * price) as total_revenue" +
                                        " FROM TeaHarverst GROUP BY year", null)

        val yılRaporList = ArrayList<YılRapor>()

        while (cursor.moveToNext()) {

            val year = cursor.getInt(cursor.getColumnIndexOrThrow("year"))
            val total_weight = cursor.getFloat(cursor.getColumnIndexOrThrow("total_weight"))
            val total_revenue = cursor.getFloat(cursor.getColumnIndexOrThrow("total_revenue"))

            val YılRapor = YılRapor(year, total_weight, total_revenue)

            yılRaporList.add(YılRapor)
        }
        cursor.close()
        return yılRaporList
    }

    fun GetInfoSeason(databaseHelper: DatabaseHelper, year: Int) : ArrayList<SurumRapor>{

        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT season, sum(weight_kg) as total_weight, sum(weight_kg * price) as total_revenue" +
                " FROM TeaHarverst WHERE year = ? GROUP BY season", arrayOf(year.toString())
        )

        val SurumRaporList = ArrayList<SurumRapor>()

        while (cursor.moveToNext()) {

            val surum = cursor.getInt(cursor.getColumnIndexOrThrow("season"))
            val total_weight = cursor.getFloat(cursor.getColumnIndexOrThrow("total_weight"))
            val total_revenue = cursor.getFloat(cursor.getColumnIndexOrThrow("total_revenue"))

            val SurumRapor = SurumRapor(surum, total_weight, total_revenue, year)

            SurumRaporList.add(SurumRapor)
        }
        //Recyclerview son elemanı bottom app bar ile çakışıyordu.
        //Bunu engellemek için fazladan bir elaman koyuldu listeye.
        //Recyclerview içerisinde son eleman pasif hale getiriliyor. bu sayede çakışma olmuyor görüntüler arasında
        val tmp = SurumRapor(0,0.0f,0.0f,0)
        SurumRaporList.add(tmp)

        cursor.close()
        return SurumRaporList
    }

    fun GetInfoGarden(databaseHelper: DatabaseHelper, year: Int, season: Int) : ArrayList<BahceRapor>{

        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT gardenName, sum(weight_kg) as total_weight, sum(weight_kg * price) as total_revenue" +
                " FROM TeaHarverst JOIN TeaGardens ON TeaHarverst.garden_id = TeaGardens.id  " +
                "WHERE year = ? AND season = ? GROUP BY gardenName", arrayOf(year.toString(), season.toString())
        )

        val BahceRaporList = ArrayList<BahceRapor>()

        while (cursor.moveToNext()) {

            val bahce = cursor.getString(cursor.getColumnIndexOrThrow("gardenName"))
            val total_weight = cursor.getFloat(cursor.getColumnIndexOrThrow("total_weight"))
            val total_revenue = cursor.getFloat(cursor.getColumnIndexOrThrow("total_revenue"))

            val BahceRapor = BahceRapor(bahce, total_weight, total_revenue)

            BahceRaporList.add(BahceRapor)
        }

        val tmp = BahceRapor("",0.0f, 0.0f)
        BahceRaporList.add(tmp)
        cursor.close()
        return BahceRaporList
    }


    //Eğer girilen bahçe ismi daha önce kullanılmış ise id değeri döner
    //Eğer bahçe ismi daha önce kullanılmamış ise -1 değerini döner
    fun isGardenExists(helper: DatabaseHelper, gardenName: String) : Int {

        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT id FROM TeaGardens WHERE gardenName = ?", arrayOf(gardenName))

        var tmp:Int = -1

        while (cursor.moveToNext()) {
            tmp = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }

        cursor.close()
        return tmp
    }

    //TRUE --> Verilen yıl ve sezon içinde bahçenin kaydı var
    //FALSE  --> Verilen yıl ve sezon içinde bahçenin kaydı yok

    fun checkSeasonAndYear(helper: DatabaseHelper, year: Int, season:Int, garden_id:Int, paymentDate:String) :Boolean{

        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT id FROM TeaHarverst " +
                                "WHERE season = ? AND year = ? AND garden_id = ? AND paymentDate = ?",
                                arrayOf(season.toString(), year.toString(), garden_id.toString(), paymentDate))

        cursor.use {
            // Eğer sonuç bulunursa true döndür, aksi takdirde false
            return if (cursor.moveToNext()) {
                cursor.close()
                db.close()
                true
            } else {
                cursor.close()
                false
            }
        }

    }

    fun deleteData(helper: DatabaseHelper, gardenName:String) {

        val db = helper.writableDatabase
        db.delete("TeaGardens", "gardenName=?", arrayOf(gardenName))
    }

    fun readYear(helper: DatabaseHelper) : ArrayList<String> {
        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT year FROM TeaHarverst ",null)

        val years = ArrayList<String>()

        while (cursor.moveToNext()) {
            val tmp = cursor.getString(cursor.getColumnIndexOrThrow("year"))

            if(!years.contains(tmp)) {
                years.add(tmp)
            }
        }

        cursor.close()
        return years
    }

    fun readSeason(helper: DatabaseHelper, year: Int) : ArrayList<String> {
        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT season FROM TeaHarverst WHERE year = ?", arrayOf(year.toString()))

        val seasons = ArrayList<String>()

        while (cursor.moveToNext()) {
            val tmp = cursor.getString(cursor.getColumnIndexOrThrow("season"))

            if(!seasons.contains(tmp)) {
                seasons.add(tmp)
            }
        }

        cursor.close()
        return seasons
    }

    fun readGardens(helper: DatabaseHelper, string: String) :ArrayList<String> {

        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT gardenName FROM TeaGardens WHERE gardenName LIKE '%$string%' ", null)

        var gardens = ArrayList<String>()

        while (cursor.moveToNext()) {
            val tmp = cursor.getString(cursor.getColumnIndexOrThrow("gardenName"))

            gardens.add(tmp)

        }
        cursor.close()
        return gardens
    }

    fun readGardenName(helper: DatabaseHelper) :ArrayList<String> {

        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT gardenName FROM TeaGardens", null)

        var gardens = ArrayList<String>()

        while (cursor.moveToNext()) {
            val tmp = cursor.getString(cursor.getColumnIndexOrThrow("gardenName"))

            gardens.add(tmp)

        }
        cursor.close()
        return gardens
    }

















}