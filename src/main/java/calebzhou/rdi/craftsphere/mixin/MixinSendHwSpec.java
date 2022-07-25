package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.NetworkPackets;
import calebzhou.rdi.craftsphere.util.NetworkUtils;
import com.google.gson.Gson;
import lombok.Data;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

//发送硬件参数
@Mixin(ClientPacketListener.class)
public class MixinSendHwSpec {
    @Inject(method = "handleAddPlayer(Lnet/minecraft/network/protocol/game/ClientboundAddPlayerPacket;)V",
    at = @At("TAIL"))
    private void send(ClientboundAddPlayerPacket clientboundAddPlayerPacket, CallbackInfo ci){
        String info = new Gson().toJson(HwSpec.getSystemSpec());
        NetworkUtils.sendPacketToServer(NetworkPackets.HW_SPEC,info);
    }
}
@Data
class HwSpec implements Serializable {
    String os;
    String mem;
    String cpu;
    String gpu;
    String mods;

    public static HwSpec getSystemSpec(){
        HwSpec spec = new HwSpec();
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();

        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        String os = String.format("%s (%s,%s)",osName,osArch,osVersion);
        spec.setOs(os);


        CentralProcessor cpuinfo = hal.getProcessor();
        CentralProcessor.ProcessorIdentifier cpuid = cpuinfo.getProcessorIdentifier();
        String cpuName = cpuid.getName();
        int cpuCores =  cpuinfo.getPhysicalProcessorCount();
        int cpuThreads = cpuinfo.getLogicalProcessorCount();
        double cpuFreq = cpuid.getVendorFreq() / 1.0E9f;
        String cpu = String.format("%s(%sC/%sT)@%sGHz",cpuName,cpuCores,cpuThreads,cpuFreq);
        spec.setCpu(cpu);

        StringBuilder gpu = new StringBuilder();
        for (GraphicsCard gpuinfo : hal.getGraphicsCards()) {
            String gpuName = gpuinfo.getName();
            String gpuVram = String.format("%.2f", (float) gpuinfo.getVRam() / 1024*1024*1024f) ;
            gpu.append(String.format("%s (%sGB);", gpuName, gpuVram));
        }
        spec.setGpu(gpu.toString());

        StringBuilder mem = new StringBuilder();
        for (PhysicalMemory meminfo : hal.getMemory().getPhysicalMemory()) {
            String memSize = String.format("%.2f", (float) meminfo.getCapacity() / 1024*1024*1024f);
            String memType = meminfo.getMemoryType();
            String memSpd = String.valueOf((int) (meminfo.getClockSpeed() / 1.0E6f));
            mem.append(String.format("%sGB %s %s;", memSize,memType, memSpd));
        }
        spec.setMem(mem.toString());

        ArrayList<ModContainer> topLevelMods = new ArrayList<>();
        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            if (container.getContainingMod().isEmpty()) {
                topLevelMods.add(container);
            }
        }

        StringBuilder mods = new StringBuilder();
        topLevelMods.sort(Comparator.comparing(mod -> mod.getMetadata().getId()));
        for (ModContainer mod : topLevelMods) {
            ModMetadata metadata = mod.getMetadata();
            if(metadata.getId().startsWith("fabric-"))
                continue;
            mods.append(String.format("%s(%s);", metadata.getName(), metadata.getId()));
        }
        spec.setMods(mods.toString());

        return spec;
    }
}