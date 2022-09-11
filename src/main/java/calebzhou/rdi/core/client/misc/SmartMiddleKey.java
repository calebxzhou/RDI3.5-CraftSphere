package calebzhou.rdi.core.client.misc;

import calebzhou.rdi.core.client.RdiCore;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class SmartMiddleKey {
    //鼠标中键·智能选择挖掘工具
    public static void handle(@Nullable HitResult hitResult, @Nullable LocalPlayer player, @Nullable ClientLevel level){
        if(hitResult==null)
            return;
        if(hitResult.getType()!= HitResult.Type.BLOCK)
            return;
        BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.isAir())
            return;
        Inventory inventory = player.getInventory();
        if (inventory.isEmpty()) {
            if(RdiCore.debug)
                RdiCore.LOGGER.debug("背包为空，无法找到挖掘工具");
            return;
        }

        //先把所有符合要求的工具找出来，放进list里
        ReferenceList<ItemStack> correctItemList = new ReferenceArrayList<>();
        //多线程查找每一个符合的
        inventory.items.parallelStream().forEach(itemStack -> {
            Item item = itemStack.getItem();
            if(item.isCorrectToolForDrops(blockState)){
                if(RdiCore.debug)
                    RdiCore.LOGGER.info("找到了{}符合挖掘",item.toString());
                correctItemList.add(itemStack);
            }
        });
        //如果什么都没找到，就什么都不做
        if(correctItemList.isEmpty()){
            if(RdiCore.debug)
                RdiCore.LOGGER.info("没有找到合适的工具来挖掘{}",blockState.getBlock().toString());
            return;
        }
        //如果只有一个，就直接选择
        if(correctItemList.size()==1){
            ItemStack correctStack = correctItemList.get(0);
            int slotMatchingItem = inventory.findSlotMatchingItem(correctStack);
            if(RdiCore.debug)
                RdiCore.LOGGER.info("找到了 只有一个 {}",correctStack.toString());
            inventory.selected= slotMatchingItem;
            return;
        }

        //如果有两个or以上，就开始分析这些工具，耐久高的、NBT多的优先
        //找出NBT多的（附魔多的）、
        ItemStack mostNbt = correctItemList.stream()
                .max(Comparator.comparing(itemStack ->{
                            if(itemStack==null)
                                return 0;
                            if(itemStack.getTag()==null)
                                return 0;

                            return  itemStack.getTag().size();
                        }
                ))
                .orElse(null);
        if(mostNbt!=null){
            inventory.selected= inventory.findSlotMatchingItem(mostNbt);
            return;
        }
        //找出耐久高的
        ItemStack mostDurability = correctItemList.stream()
                .max(Comparator.comparing(ItemStack::getDamageValue))
                .get();
        inventory.selected=inventory.findSlotMatchingItem(mostDurability);


//TODO
        /*for (int i = 0; i < 36; i++) {

            ItemStack item = inventory.getItem(i);
            if(item.getItem().isCorrectToolForDrops(blockState)){
                inventory.selected=i;
                ExampleMod.LOGGER.info("挖掘工具快捷键");
                break;
            }

        }*/


    }
}
