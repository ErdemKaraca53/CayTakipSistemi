package com.erdem.designexample.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.erdem.designexample.dataClass.PieChartData
import com.erdem.designexample.dataClass.paymentData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DatabaseOperations {

    fun add(helper: DatabaseHelper, garden: TeaGardens, harverst: TeaHarverst, context: Context){

        val db = helper.writableDatabase

        val harverstValues = ContentValues()
        val gardenValues = ContentValues()
        Log.e("vadeTarihi","vade tarihi: " + harverst.VadeTarihi.toString())
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
            if (!checkSeasonAndYear(helper, harverst.year, harverst.season, isGardenExists(helper, garden.gardenName), harverst.VadeTarihi.toString())) {
                harverstValues.put("garden_id", isGardenExists(helper,garden.gardenName))
                db.insertOrThrow("TeaHarverst", null, harverstValues)
            } else {
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
                statement.bindString(5, harverst.VadeTarihi.toString()) // Beşinci "?" -> paymentDate

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

    fun readYear(helper: DatabaseHelper) :ArrayList<String> {

        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT year FROM TeaHarverst", null)

        var years = ArrayList<String>()

        while (cursor.moveToNext()) {
            val tmp = cursor.getString(cursor.getColumnIndexOrThrow("year"))

            years.add(tmp)

        }
        cursor.close()
        return years
    }


    fun readCompany(helper: DatabaseHelper) :ArrayList<String> {

        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT company FROM TeaHarverst", null)

        var years = ArrayList<String>()

        while (cursor.moveToNext()) {
            val tmp = cursor.getString(cursor.getColumnIndexOrThrow("company"))

            years.add(tmp)

        }
        cursor.close()
        return years
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

    fun getPaymentData(helper: DatabaseHelper, time: ArrayList<String>, company: ArrayList<String>) : ArrayList<paymentData> {

        val db = helper.readableDatabase
        val cursor: Cursor

        val timeStr = List(time.size) { "?" }.joinToString(",")
        val companyStr = List(company.size) { "?" }.joinToString(",")

        Log.e("shareeed", company.toString())
        Log.e("shareeed", time.toString())
        if( (time.isEmpty() && company.isEmpty())) {

            cursor = db.rawQuery("SELECT paymentDate, sum(weight_kg * price) as totalPayment, " +
                    "sum(weight_kg) as total_kg, company, weight_kg, price " +
                    "FROM TeaHarverst GROUP BY paymentDate, price, company", null)
            Log.e("shareeed", "girdim1")
        }else if(time.isEmpty() && company.isNotEmpty()){
            cursor = db.rawQuery("SELECT paymentDate, sum(weight_kg * price) as totalPayment, " +
                    "sum(weight_kg) as total_kg, company, weight_kg, price " +
                    "FROM TeaHarverst WHERE company IN ($companyStr) GROUP BY paymentDate, price, company", company.toTypedArray())
            Log.e("shareeed", "time boş, company dolu")
        }
        else if(company.isEmpty() && time.isNotEmpty()){
            cursor = db.rawQuery("SELECT paymentDate, sum(weight_kg * price) as totalPayment, " +
                    "sum(weight_kg) as total_kg, company, weight_kg, price " +
                    "FROM TeaHarverst WHERE year IN ($timeStr) GROUP BY paymentDate, price, company", time.toTypedArray())
            Log.e("shareeed", "time dolu, company boş")
        } else {
            //Log.e("shareeed", "girdim2")
            val selectionArgs = (time + company).toTypedArray()
            cursor = db.rawQuery("SELECT paymentDate, sum(weight_kg * price) as totalPayment, " +
                    "sum(weight_kg) as total_kg, company, weight_kg, price " +
                    "FROM TeaHarverst " +
                    "WHERE year IN ($timeStr) AND company IN ($companyStr) " +
                    "GROUP BY paymentDate, price, company", selectionArgs)
        }

        val paymenDataSet = ArrayList<paymentData>()

        while (cursor.moveToNext()) {

            val paymentDate = cursor.getString(cursor.getColumnIndexOrThrow("paymentDate"))
            val money = cursor.getFloat(cursor.getColumnIndexOrThrow("totalPayment"))
            val company = cursor.getString(cursor.getColumnIndexOrThrow("company"))
            val kg = cursor.getFloat(cursor.getColumnIndexOrThrow("total_kg"))
            val fiyat = cursor.getFloat(cursor.getColumnIndexOrThrow("price"))

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")  // Formatı belirle
            val parsedDate = LocalDate.parse(paymentDate, formatter)  // String -> LocalDate
            println("Parsed LocalDate: $parsedDate")  // Çıktı: 2025-03-16


            Log.e("vadeTarihi", parsedDate.toString())

            val tmp = paymentData(parsedDate, money, company, kg, fiyat)
            paymenDataSet.add(tmp)
        }
        cursor.close()
        return paymenDataSet
    }


}