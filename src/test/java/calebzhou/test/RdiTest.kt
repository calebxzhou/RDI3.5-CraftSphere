package calebzhou.test

import calebzhou.rdi.core.client.RdiCore
import calebzhou.rdi.core.client.misc.HwSpec
import org.junit.jupiter.api.Test

/**
 * Created by calebzhou on 2022-10-12,22:22.
 */
class RdiTest {
    @Test
    fun testHwSpec(){
        println(HwSpec.currentHwSpec)
    }
    @Test
    fun testGeoWeather(){
        RdiCore().loadGeoLocationAndWeather()
    }

}
