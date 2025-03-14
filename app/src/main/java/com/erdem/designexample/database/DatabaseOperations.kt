package com.erdem.designexample.database

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.erdem.designexample.dataClass.PieChartData
import com.erdem.designexample.dataClass.paymentData
import java.util.Calendar

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


    //Bunu düzenle
    fun getPieChartDataAllWithGardenName(databaseHelper: DatabaseHelper,gardenName: String) : ArrayList<PieChartData>{

        val db = databaseHelper.readableDatabase

        val cursor = db.rawQuery("SELECT year, season, sum(weight_kg) as total_weight, sum(weight_kg * price) as total_revenue" +
                " FROM TeaHarverst JOIN TeaGardens ON TeaHarverst.garden_id = TeaGardens.id  " +
                "AND gardenName = ? GROUP BY year, season", arrayOf(gardenName)
        )

        val PieChartDataList = ArrayList<PieChartData>()

        while (cursor.moveToNext()) {
            val year = cursor.getInt(cursor.getColumnIndexOrThrow("year"))
            val season = cursor.getInt(cursor.getColumnIndexOrThrow("season"))
            val total_weight = cursor.getFloat(cursor.getColumnIndexOrThrow("total_weight"))
            val total_revenue = cursor.getFloat(cursor.getColumnIndexOrThrow("total_revenue"))

            val PieChartData = PieChartData(year, season, total_weight, total_revenue)

            PieChartDataList.add(PieChartData)
        }

        cursor.close()
        return PieChartDataList
    }

    fun getPieChartDataAll(databaseHelper: DatabaseHelper) : ArrayList<PieChartData>{

        val db = databaseHelper.readableDatabase

        val cursor = db.rawQuery("SELECT year, season, sum(weight_kg) as total_weight, sum(weight_kg * price) as total_revenue" +
                " FROM TeaHarverst JOIN TeaGardens ON TeaHarverst.garden_id = TeaGardens.id  " +
                "GROUP BY year, season", arrayOf()
        )

        val PieChartDataList = ArrayList<PieChartData>()

        while (cursor.moveToNext()) {

            val year = cursor.getInt(cursor.getColumnIndexOrThrow("year"))
            val season = cursor.getInt(cursor.getColumnIndexOrThrow("season"))
            val total_weight = cursor.getFloat(cursor.getColumnIndexOrThrow("total_weight"))
            val total_revenue = cursor.getFloat(cursor.getColumnIndexOrThrow("total_revenue"))
            //Yıl bilgisine göre filtreleme yapıldığı içi
            val PieChartData = PieChartData(year,season, total_weight, total_revenue)

            PieChartDataList.add(PieChartData)
        }

        cursor.close()
        return PieChartDataList
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

    fun getPaymentData(helper: DatabaseHelper) : ArrayList<paymentData> {

        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT paymentDate, sum(weight_kg * price) as totalPayment, company " +
                "FROM TeaHarverst GROUP BY paymentDate", null)

        val paymenDataSet = ArrayList<paymentData>()

        while (cursor.moveToNext()) {

            val paymentDate = cursor.getString(cursor.getColumnIndexOrThrow("paymentDate"))
            val money = cursor.getFloat(cursor.getColumnIndexOrThrow("totalPayment"))
            val company = cursor.getString(cursor.getColumnIndexOrThrow("company"))

            val day = "${paymentDate[0]}${paymentDate[1]}"
            val month ="${paymentDate[4]}"
            val year = "${paymentDate.get(6)}${paymentDate[7]}${paymentDate[8]}${paymentDate[9]}"

            val date = Calendar.getInstance()
            Log.e("paymentDate", "$day/$month/$year")
            date.set(year.toInt(), month.toInt(), day.toInt())

            val tmp = paymentData(date, money, company)
            paymenDataSet.add(tmp)
        }
        cursor.close()
        return paymenDataSet
    }


}