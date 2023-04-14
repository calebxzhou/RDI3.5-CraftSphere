package calebxzhou.rdi

import calebxzhou.libertorch.util.NetUt
import calebxzhou.rdi.mixin.AMcServer
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Lifecycle
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.commands.Commands
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.RegistryOps
import net.minecraft.server.*
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.dedicated.DedicatedServerProperties
import net.minecraft.server.dedicated.DedicatedServerSettings
import net.minecraft.server.level.progress.LoggerChunkProgressListener
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.repository.FolderRepositorySource
import net.minecraft.server.packs.repository.PackRepository
import net.minecraft.server.packs.repository.PackSource
import net.minecraft.server.packs.repository.ServerPacksSource
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.datafix.DataFixers
import net.minecraft.world.Difficulty
import net.minecraft.world.level.DataPackConfig
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import net.minecraft.world.level.LevelSettings
import net.minecraft.world.level.storage.LevelResource
import net.minecraft.world.level.storage.LevelStorageSource
import net.minecraft.world.level.storage.PrimaryLevelData
import net.minecraft.world.level.storage.WorldData
import java.net.Proxy
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.Path

/**
 * Created  on 2023-04-13,22:21.
 */
object RdiLogicServer {

    @JvmStatic
    fun <S : MinecraftServer> spin(threadFunction: (thread: Thread) -> S): S {
        val atomicReference = AtomicReference<S>()
        val thread =
            Thread({ ((atomicReference.get() as MinecraftServer) as AMcServer).invokeRunServer() }, "RDI-LogicServer")
        thread.uncaughtExceptionHandler =
            Thread.UncaughtExceptionHandler { threadx, throwable ->
                logger.error(
                    "Uncaught exception in server thread",
                    throwable
                )
            }
        if (Runtime.getRuntime().availableProcessors() > 4) {
            thread.priority = 8
        }

        val minecraftServer = threadFunction.invoke(thread)
        atomicReference.set(minecraftServer)
        thread.start()
        return minecraftServer
    }

    @JvmStatic
    fun start(thread: Thread): MinecraftServer {
        RdiCore.logger.info("启动逻辑服务器")
        val props = Properties()
        props["level-type"]="minecraft:normal"
        val levelStorageSource = LevelStorageSource.createDefault(Path("fake"))
        val levelStorageAccess = levelStorageSource.createAccess("fake")
        val packRepository = PackRepository(
            PackType.SERVER_DATA,
            ServerPacksSource(),
            FolderRepositorySource(
                levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(),
                PackSource.WORLD
            )
        )
        val dataPackConfig =
            Objects.requireNonNullElse(levelStorageAccess.dataPacks, DataPackConfig.DEFAULT) as DataPackConfig
        val dedicatedServerSettings = DedicatedServerSettings(Path("fake_server.prop"))
        val packConfig = WorldLoader.PackConfig(packRepository, dataPackConfig, false)
        val initConfig = WorldLoader.InitConfig(
            packConfig,
            Commands.CommandSelection.DEDICATED,
            4
        )
        val worldStem: WorldStem = Util.blockUntilDone { applyExecutor: Executor? ->
            WorldStem.load(
                initConfig,
                { resourceManager: ResourceManager?, dataPackSettings: DataPackConfig? ->
                    val writable = RegistryAccess.builtinCopy()
                    val dynamicOps =
                        RegistryOps.createAndLoad(NbtOps.INSTANCE, writable, resourceManager)
                    val worldDatax =
                        levelStorageAccess.getDataTag(dynamicOps, dataPackSettings, writable.allElementsLifecycle())
                    if (worldDatax != null) {
                        return@load Pair.of(
                            worldDatax,
                            writable.freeze()
                        )
                    } else {
                        val dProps = DedicatedServerProperties(props)
                        val levelSettings = LevelSettings(
                            "rdi-logic-level",
                            GameType.CREATIVE,
                            false,
                            Difficulty.HARD,
                            true,
                            GameRules(),
                            dataPackSettings
                        )
                        val worldGenSettings = dProps.getWorldGenSettings(writable)
                        val var13x = PrimaryLevelData(
                            levelSettings,
                            worldGenSettings,
                            Lifecycle.stable()
                        )
                        return@load Pair.of<WorldData, RegistryAccess.Frozen>(var13x, writable.freeze())
                    }
                },
                Util.backgroundExecutor(),
                applyExecutor
            )
        }
            .get() as WorldStem

        val services =
            Services.create(YggdrasilAuthenticationService(Proxy.NO_PROXY), Minecraft.getInstance().gameDirectory)
        val dedicatedServerx = DedicatedServer(
            thread,
            levelStorageAccess,
            packRepository,
            worldStem,
            dedicatedServerSettings,
            DataFixers.getDataFixer(),
            services
        ) { radius: Int -> LoggerChunkProgressListener(radius) }
        dedicatedServerx.singleplayerProfile = null
        dedicatedServerx.port = NetUt.getAvailablePort()
        dedicatedServerx.isDemo = false
        return dedicatedServerx
    }
}
