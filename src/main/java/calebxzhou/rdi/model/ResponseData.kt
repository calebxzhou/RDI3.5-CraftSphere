package calebxzhou.rdi.model

import calebxzhou.libertorch.SerDesJson
import com.google.gson.reflect.TypeToken
import kotlin.reflect.KClass

/**
 * Created by calebzhou on 2022-10-04,18:42.
 */
data class ResponseData<T>(val stat:Int, val msg:String,var data:T?){
    fun isSuccess():Boolean{
        return stat>0
    }
    companion object{

        fun <T : Any> fromString(json:String): ResponseData<T> {
            return fromString(json,null)
        }
        fun <T : Any> fromString(json:String, dataClass: KClass<T>): ResponseData<T> {
            return fromString(json, dataClass.java)
        }
        fun <T : Any> fromString(json:String, dataClass: Class<T>?): ResponseData<T> {
            return SerDesJson.gson.fromJson(json,
                if(dataClass==null)
                    ResponseData::class.java
                else
                    TypeToken.getParameterized(ResponseData::class.java,dataClass).type)
        }
    }
}
