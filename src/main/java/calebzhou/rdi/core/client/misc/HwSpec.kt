package calebzhou.rdi.core.client.misc

import calebzhou.rdi.core.client.RdiSharedConstants
import calebzhou.rdi.core.client.logger
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.QuiltLoader
import oshi.SystemInfo
import java.util.*
import kotlin.collections.ArrayList

data class HwSpec(val brand: String,
                  val os: String,
                  val board: String,
                  val mem: String,
                  val disk: String,
                  val cpu: String,
                  val gpu: String,
                  val mods: String,
                  val ver: String,){


    companion object {
        var currentHwSpec = loadSystemSpec()
        fun loadSystemSpec() :HwSpec{
            logger.info("载入硬件信息")
            val systemInfo = SystemInfo()
            val hal = systemInfo.hardware
            val csys = hal.computerSystem
            val brand = "${csys.manufacturer}:${csys.model}"

            val baseboard = csys.baseboard
            val board = "${baseboard.manufacturer}:${baseboard.model}"

            val osInfo = systemInfo.operatingSystem
            val osArch = System.getProperty("os.arch")
            val osVersion = System.getProperty("os.version")
            val os = "${osInfo.manufacturer} ${osInfo.family} ${osInfo.versionInfo}(${osArch},${osVersion})"

            val cpuinfo = hal.processor
            val cpuid = cpuinfo.processorIdentifier
            val cpuName = cpuid.name.replace("  ", "")
            val cpuCores = cpuinfo.physicalProcessorCount
            val cpuThreads = cpuinfo.logicalProcessorCount
            val cpuFreq = "%.2f".format((cpuid.vendorFreq / 1.0E9f).toDouble())
            val cpuMaxFreq = "%.2f".format((Arrays.stream(cpuinfo.currentFreq).max().asLong / 1.0E9f).toDouble())
            val cpu = "${cpuName}(${cpuCores}C/${cpuThreads}T)@${cpuFreq}/${cpuMaxFreq}GHz"

            val gpu = StringBuilder()
            for (gpuinfo in hal.graphicsCards) {
                val gpuVram = "%.2f".format(gpuinfo.vRam.toFloat() / (1024 * 1024 * 1024f))
                gpu.append("${gpuinfo.name} (${gpuVram}GB,${gpuinfo.vendor});")
            }

            val mem = StringBuilder()
            var memTotalSize = 0f
            for (meminfo in hal.memory.physicalMemory) {
                val memSizef = meminfo.capacity.toFloat() / (1024 * 1024 * 1024f)
                memTotalSize += memSizef
                val memSize = "%.2f".format(memSizef)
                val memType = meminfo.memoryType
                val memSpd = (meminfo.clockSpeed / 1.0E6f).toInt().toString()
                mem.append("${memSize}GB-${memType}-${memSpd};")
            }
            mem.append("(∑%.2fGB)".format(memTotalSize))

            val disk = StringBuilder()
            var diskTotalSize = 0f
            for (diskStore in hal.diskStores) {
                val diskSizef = diskStore.size.toFloat() / (1024 * 1024 * 1024f)
                diskTotalSize += diskSizef
                disk.append(
                        "%s(%.2fGB);".format(
                        diskStore.model,
                        diskSizef)
                )
            }
            disk.append(String.format("(∑%.2fGB)", diskTotalSize))


            val mods = StringBuilder()
            var modAmount = 0
            val topLevelMods: MutableList<ModContainer> = ArrayList()
            QuiltLoader.getAllMods().parallelStream().forEach { container->
                val metadata = container.metadata()
                if (!metadata.id().startsWith("quilt")){
                    mods.append("${metadata.name()}(${metadata.id()});")
                    ++modAmount
                    topLevelMods.add(container)
                }
            }
            mods.append("(∑").append(modAmount).append(")")
            logger.info("载入硬件信息完成")
            return HwSpec(
                brand, os, board, mem.toString(), disk.toString(), cpu, gpu.toString(),
                mods.toString(), RdiSharedConstants.CORE_VERSION
            )
        }
    }
}
