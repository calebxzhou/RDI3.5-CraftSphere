package calebxzhou.rdi.misc

import calebxzhou.rdi.NetTx
import calebxzhou.rdi.logger
import calebxzhou.rdi.util.PlayerUtils
import calebxzhou.libertorch.util.ThreadPool
import calebxzhou.rdi.consts.NetPacks
import com.google.gson.Gson
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.BoneMealItem
import net.minecraft.world.level.block.SaplingBlock
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents
import org.quiltmc.qsl.networking.api.PacketSender
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents

//事件注册
class EventRegister {

    //多长tick检测一次是否挂机 3秒
    private val checkAfkTickTime = 20 * 3

    //两次检测之间的tick
    private var checkAfkInterval = 0

    //总共挂机了多长时间
    private var totalAfkTicks = 0

    //如果达到了挂机时间（5分钟），告诉服务器已经挂机
    val ticksOnAfk = 20 * 5 * 60

    init {
        logger.info("正在注册事件")
        //初始化按键事件
        KeyBinds.init()
        //进入服务器发送硬件数据
        ClientPlayConnectionEvents.JOIN.register(::onJoinServer)
        //客户端世界tick事件
        ClientTickEvents.END.register(::onClientWorldTick)
        ClientPlayConnectionEvents.DISCONNECT.register(::onDisconnectServer)
    }


    private var danceTreeCurrentScore = 0
    private fun onClientWorldTick(minecraft: Minecraft) {
        val player = Minecraft.getInstance().player?:return
        val level = minecraft.level?:return
        //afkDetect(player)
        //danceTree(player, level)
        animalSex(player)
        ClientMobSpawner.tick(level)
        KeyBinds.handleKeyActions(level)
    }

    private var sexTickAmount = 0
    var needStandUp = false

    //动物快速繁殖
    private fun animalSex(player: LocalPlayer) {

        //玩家下蹲
        if (player.isCrouching) {
            //获取所面对的生物
            val lookingEntity = PlayerUtils.playerLookingEntity ?: return
            val entityType = lookingEntity.type
            if (!sexableEntityType.contains(entityType)) return
            if (sexTickAmount % 5 == 0) needStandUp = true
            if (!needStandUp) ++sexTickAmount
            PlayerUtils.displayClientMessage(
                player,
                String.format("动物繁殖进度 %d/%d", sexTickAmount, sexTickAmountNeedToAdult),
                true
            )
            if (sexTickAmount >= sexTickAmountNeedToAdult) {
                val entityStringUUID = lookingEntity.stringUUID
                NetTx.send(NetPacks.ANIMAL_SEX, entityStringUUID)
                sexTickAmount = 0
            }
        } else if (needStandUp) {
            ++sexTickAmount
            needStandUp = false
        }
    }

    //建立服务器连接
    private fun onJoinServer(listener: ClientPacketListener, sender: PacketSender, minecraft: Minecraft) {
        ThreadPool.run {
            val info = Gson().newBuilder().setPrettyPrinting().create().toJson(HwSpec.currentHwSpec)
            NetTx.send(NetPacks.HW_SPEC, info)
        }
    }

    //断开服务器连接
    private fun onDisconnectServer(listener: ClientPacketListener, minecraft: Minecraft) {
        //清零挂机时间和跳舞树积分
        danceTreeCurrentScore = 0
        totalAfkTicks = 0
    }

    //跳舞树
    fun danceTree(player: LocalPlayer, world: ClientLevel?) {
        if (world!!.dimension() !== ClientLevel.OVERWORLD) return
        val onPos = player.onPos
        val nearestSapling = BlockPos.betweenClosedStream(
            onPos.offset(-5, -2, -5),
            onPos.offset(5, 2, 5)
        )
            .filter { blockPos: BlockPos? -> world!!.getBlockState(blockPos).block is SaplingBlock }
            .findFirst()
            .map { obj: BlockPos -> obj.immutable() }
        if (nearestSapling.isEmpty) return
        var scoreToAdd = 0
        //500分长一棵树
        val requireScore = 500
        //跑步 1tick+3
        scoreToAdd = if (player.isSprinting) {
            3
        } else if (!player.isOnGround) {
            1
        } else  //只走路，不加分
            return
        if (scoreToAdd > 0) {
            danceTreeCurrentScore += scoreToAdd
            player.displayClientMessage(
                Component.literal("树苗生长进度${danceTreeCurrentScore*5}/${requireScore*5}"),
                true
            )
            val finalScoreToAdd = scoreToAdd
            BoneMealItem.addGrowthParticles(world, nearestSapling.get(), finalScoreToAdd * 5)
        }
        if (danceTreeCurrentScore > requireScore) {
            NetTx.send(
                NetPacks.DANCE_TREE_GROW,
                nearestSapling.get().toShortString().replace(" ", "")
            )
            danceTreeCurrentScore = 0
        }
    }

    //检测挂机
    fun afkDetect(player: LocalPlayer) {
        val pos1 = player.onPos
        //如果没达到检测挂机的时间 就先不检测
        if (checkAfkInterval < checkAfkTickTime) {
            ++checkAfkInterval
            return
        }

        //达到检测挂机时间---
        val pos2 = player.onPos
        //如果3秒之内没有动 就累计挂机tick
        if (pos2.compareTo(pos1) == 0) {
            totalAfkTicks += checkAfkInterval
            checkAfkInterval = 0
        }

        //累计挂机tick达到了规定时间 开始向服务器发送挂机时长
        if (totalAfkTicks >= ticksOnAfk) {
            NetTx.send(NetPacks.AFK_DETECT, totalAfkTicks)
            //如果玩家动了 就清除挂机时间
            if (pos2.compareTo(pos1) > 0) {
                totalAfkTicks = 0
                NetTx.send(NetPacks.AFK_DETECT, 0)
            }
        }
    }

    companion object {
        //繁殖成功所需要的tick数
        private const val sexTickAmountNeedToAdult = 200
        private val sexableEntityType: MutableList<EntityType<*>> = ObjectArrayList()

        init {
            sexableEntityType.add(EntityType.PIG)
            sexableEntityType.add(EntityType.COW)
            sexableEntityType.add(EntityType.SHEEP)
            sexableEntityType.add(EntityType.CHICKEN)
            sexableEntityType.add(EntityType.VILLAGER)
        }
    }
}
