package com.erdem.designexample

import java.util.Date

data class TeaHarverst(
    var year: Int,
    var month: Int,
    var day: Int,
    var season: Int,
    var gardenName: String,
    var weight_kg: Int,
    var SatisYeri: String,
    var SatisFiyati: Float,
    var VadeTarihi: String
) {
}