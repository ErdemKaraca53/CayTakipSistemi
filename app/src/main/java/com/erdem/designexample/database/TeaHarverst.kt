package com.erdem.designexample.database

data class TeaHarverst(
    var year: Int,
    var month: Int,
    var day: Int,
    var season: Int,
    var gardenName: String,
    var weight_kg: Float,
    var SatisYeri: String,
    var SatisFiyati: Float,
    var VadeTarihi: String
) {
}