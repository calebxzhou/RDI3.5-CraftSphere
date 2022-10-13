package calebzhou.rdi.core.client.model

/**
 * Created by calebzhou on 2022-10-03,21:09.
 */

/**
 *  地理天气
 */
//经度纬度
data class GeoLocation(val latitude:Double,val longitude:Double)
//RDI地址位置
data class RdiGeoLocation(val nation: String, val province: String, val city: String,
                     val district: String, val isp: String, val location: GeoLocation
)
