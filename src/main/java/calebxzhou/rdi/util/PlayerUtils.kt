package calebzhou.rdi.core.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class PlayerUtils {
    public static HitResult getPlayerLookingAt(Player player, boolean isFluid){
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if(hitResult==null)
            hitResult=player.pick(256.0D,0.0f,isFluid);
        return hitResult;
    }
    public static BlockPos getPlayerLookingBlock(Player player, boolean isFluid){


        /*HitResult playerLookingAt = getPlayerLookingAt(player, isFluid);
        if(playerLookingAt==null)
            return null;
        BlockHitResult rays=(BlockHitResult) playerLookingAt;*/
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if(hitResult==null)
            return  null;
        if(hitResult.getType()==HitResult.Type.BLOCK) {
            return  ((BlockHitResult) hitResult).getBlockPos();
        }
        return null;
    }
    public static Entity getPlayerLookingEntity(){
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if(hitResult==null)
            return  null;
        if(hitResult.getType()==HitResult.Type.ENTITY) {
            return  ((EntityHitResult) Minecraft.getInstance().hitResult).getEntity();
        }
        return null;
    }
    public static void displayClientMessage(Player player,String msg){
        displayClientMessage(player, msg);
    }
    public static void displayClientMessage(Player player,String msg,boolean onActionBar){
        displayClientMessage(player,Component.literal(msg),onActionBar);
    }
    public static void displayClientMessage(Player player,Component msg,boolean onActionBar){
        player.displayClientMessage( msg,onActionBar);
    }

}
