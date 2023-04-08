package calebxzhou.rdi.util

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

object PlayerUtils {
    fun getPlayerLookingAt(player: Player, isFluid: Boolean): HitResult? {
        var hitResult = Minecraft.getInstance().hitResult
        if (hitResult == null) hitResult = player.pick(256.0, 0.0f, isFluid)
        return hitResult
    }

    fun getPlayerLookingBlock(player: Player?, isFluid: Boolean): BlockPos? {


        /*HitResult playerLookingAt = getPlayerLookingAt(player, isFluid);
        if(playerLookingAt==null)
            return null;
        BlockHitResult rays=(BlockHitResult) playerLookingAt;*/
        val hitResult = Minecraft.getInstance().hitResult ?: return null
        return if (hitResult.type == HitResult.Type.BLOCK) {
            (hitResult as BlockHitResult).blockPos
        } else null
    }

    val playerLookingEntity: Entity?
        get() {
            val hitResult = Minecraft.getInstance().hitResult ?: return null
            return if (hitResult.type == HitResult.Type.ENTITY) {
                (Minecraft.getInstance().hitResult as EntityHitResult?)!!.entity
            } else null
        }

    fun displayClientMessage(player: Player?, msg: String?) {
        displayClientMessage(player, msg)
    }

    fun displayClientMessage(player: Player, msg: String?, onActionBar: Boolean) {
        displayClientMessage(player, Component.literal(msg), onActionBar)
    }

    fun displayClientMessage(player: Player, msg: Component?, onActionBar: Boolean) {
        player.displayClientMessage(msg, onActionBar)
    }
}
