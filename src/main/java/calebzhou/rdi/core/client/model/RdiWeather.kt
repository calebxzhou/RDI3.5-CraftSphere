package calebzhou.rdi.core.client.model

/**
 * Created by calebzhou on 2022-10-03,20:39.
 */

//RDI天气
data class RdiWeather(
    var alert:String,
    var realTimeWeather: RdiRealTimeWeather,
    var dailyWeather: MutableList<RdiDailyWeather> = mutableListOf(),
)
//RDI天气 实时
data class RdiRealTimeWeather(
    var temp:Float=0f,
    var humi:Float=0f,
    var skycon:String="",
    var skyDesc:String="",
    var visi: Float=0f,
    var windSpd: Float=0f,
    var windDir: Float=0f,
    var pres: Float=0f,
    var aqi: Int=0,
    var aqiDesc: String="",
    var rainDesc: String="",
    var uv: String="",
    var feel: String="",
)
//RDI天气 全天
data class RdiDailyWeather(
    var skycon: String="",
    var tempMin:Float=0f,
    var tempMax:Float=0f,
    var visiMin:Float=0f,
    var visiMax:Float=0f,
    var sunRise:String="",
    var sunSet:String="",
    var preci:Float=0f,
    var humiMin:Float=0f,
    var humiMax:Float=0f,
    var uv:String="",
    var feel:String="",
    var cloth:String="",
    var coldRisk:String="",
)
