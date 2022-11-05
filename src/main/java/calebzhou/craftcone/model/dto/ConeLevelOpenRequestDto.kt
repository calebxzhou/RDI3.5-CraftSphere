package calebzhou.craftcone.model.dto

/**
 * Created  on 2022-11-05,10:08.
 */
@kotlinx.serialization.Serializable
data class ConeLevelOpenRequestDto(val player: ConePlayerDto, val levelName:String)
