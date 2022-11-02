package calebzhou.rdi.core.client.screen

import calebzhou.rdi.core.client.RdiCore.Companion.rdiLevelSource
import calebzhou.rdi.core.client.RdiSharedConstants
import calebzhou.rdi.core.client.RdiSharedConstants.DEFAULT_LEVEL_NAME
import calebzhou.rdi.core.client.constant.DebugConst
import calebzhou.rdi.core.client.misc.MusicPlayer
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Lifecycle
import net.minecraft.Util
import net.minecraft.client.Game
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.commands.Commands
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.server.WorldLoader
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.repository.PackRepository
import net.minecraft.server.packs.repository.ServerPacksSource
import net.minecraft.world.Difficulty
import net.minecraft.world.level.DataPackConfig
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import net.minecraft.world.level.LevelSettings
import net.minecraft.world.level.levelgen.presets.WorldPresets
import net.minecraft.world.level.storage.LevelStorageSource
import net.minecraft.world.level.storage.PrimaryLevelData
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents
import org.quiltmc.qsl.networking.api.PacketSender
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents
import java.io.File
import java.util.*

/**
 * Created  on 2022-10-31,16:55.
 */
class IslandLoadScreen :Screen(Component.literal("载入空岛")){

    init{
        //进入海岛就启动本地LAN服务器

        ClientPlayConnectionEvents.JOIN.register(::onWorldLoaded)
        onInit()

    }

    private fun onWorldLoaded(listener: ClientPacketListener, sender: PacketSender, minecraft: Minecraft) {
        minecraft.singleplayerServer!!.publishServer(GameType.SURVIVAL, DebugConst.ALLOW_CHEAT_CMD,RdiSharedConstants.GAMEPLAY_PORT)
    }

    private fun onInit(){
        MusicPlayer.playOggAsync(File(RdiSharedConstants.RDI_SOUND_FOLDER, "connect.ogg"))
        val mc = Minecraft.getInstance()
        //岛屿存在 直接进入
        if(rdiLevelSource.levelExists(DEFAULT_LEVEL_NAME)){
            WorldOpenFlows(mc, rdiLevelSource).loadLevel(RdiTitleScreen(), DEFAULT_LEVEL_NAME)
            return
        }
        //岛屿不存在，创建一个
        val packRepository = PackRepository(PackType.SERVER_DATA, ServerPacksSource())
        val initConfig = createDefaultLoadConfig(packRepository)
        val dataPackLoader = WorldLoader.load(initConfig,
            { _,_ ->
                val frozen = RegistryAccess.builtinCopy().freeze()
                val worldGenSettings = WorldPresets.createNormalWorldFromPreset(frozen)
                Pair.of(worldGenSettings, frozen)
            },
            { resourceManager , resources , registryManager , generatorOptions  ->
                resourceManager.close()
                WorldCreationContext(generatorOptions, Lifecycle.stable(), registryManager, resources)
            }, Util.backgroundExecutor(), mc
        )
        mc.managedBlock { dataPackLoader.isDone }
        val worldGenSettingsComponent = WorldGenSettingsComponent(
            dataPackLoader.join(),
            Optional.of(WorldPresets.AMPLIFIED),
            OptionalLong.of(RdiSharedConstants.SEED)
        )

        val worldCreationContext = worldGenSettingsComponent.settings().withSettings{option -> option.withSeed(false,OptionalLong.of(RdiSharedConstants.SEED))}

        val worldData = PrimaryLevelData(createLevelSettings(),worldCreationContext.worldGenSettings,Lifecycle.stable())

        //开始创建世界了
        val levelStorageAccess = rdiLevelSource.createAccess(DEFAULT_LEVEL_NAME)
        WorldOpenFlows(mc,rdiLevelSource).createLevelFromExistingSettings(levelStorageAccess,worldCreationContext.dataPackResources,worldCreationContext.registryAccess,worldData)

    }
    private fun createDefaultLoadConfig(resourcePackManager: PackRepository): WorldLoader.InitConfig {
        return WorldLoader.InitConfig(
            WorldLoader. PackConfig(resourcePackManager, DataPackConfig.DEFAULT, false),
            Commands.CommandSelection.INTEGRATED,
            2)
    }
    private fun createLevelSettings(): LevelSettings {
        val gameRules = Util.make(GameRules()) { gameRules ->
            gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(true,null)
            gameRules.getRule(GameRules.RULE_DAYLIGHT).set(true,null)
            gameRules.getRule(GameRules.RULE_RANDOMTICKING).set(30,null)
        }
        return LevelSettings(
                DEFAULT_LEVEL_NAME,
                GameType.SURVIVAL,
                false,
                Difficulty.HARD,
                false,
                gameRules,
                DataPackConfig.DEFAULT)
    }

}
