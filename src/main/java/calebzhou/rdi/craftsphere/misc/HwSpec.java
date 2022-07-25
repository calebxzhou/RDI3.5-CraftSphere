package calebzhou.rdi.craftsphere.misc;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class HwSpec implements Serializable {
    String os;
    String mem;
    String cpu;
    String gpu;
    String mods;

    public static HwSpec getSystemSpec() {
        HwSpec spec = new HwSpec();
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();

        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        String os = String.format("%s (%s,%s)", osName, osArch, osVersion);
        spec.setOs(os);


        CentralProcessor cpuinfo = hal.getProcessor();
        CentralProcessor.ProcessorIdentifier cpuid = cpuinfo.getProcessorIdentifier();
        String cpuName = cpuid.getName();
        int cpuCores = cpuinfo.getPhysicalProcessorCount();
        int cpuThreads = cpuinfo.getLogicalProcessorCount();
        double cpuFreq = cpuid.getVendorFreq() / 1.0E9f;
        String cpu = String.format("%s(%sC/%sT)@%sGHz", cpuName, cpuCores, cpuThreads, cpuFreq);
        spec.setCpu(cpu);

        StringBuilder gpu = new StringBuilder();
        for (GraphicsCard gpuinfo : hal.getGraphicsCards()) {
            String gpuName = gpuinfo.getName();
            String gpuVram = String.format("%.2f", (float) gpuinfo.getVRam() / 1024 * 1024 * 1024f);
            gpu.append(String.format("%s (%sGB);", gpuName, gpuVram));
        }
        spec.setGpu(gpu.toString());

        StringBuilder mem = new StringBuilder();
        for (PhysicalMemory meminfo : hal.getMemory().getPhysicalMemory()) {
            String memSize = String.format("%.2f", (float) meminfo.getCapacity() / 1024 * 1024 * 1024f);
            String memType = meminfo.getMemoryType();
            String memSpd = String.valueOf((int) (meminfo.getClockSpeed() / 1.0E6f));
            mem.append(String.format("%sGB %s %s;", memSize, memType, memSpd));
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
            if (metadata.getId().startsWith("fabric-"))
                continue;
            mods.append(String.format("%s(%s);", metadata.getName(), metadata.getId()));
        }
        spec.setMods(mods.toString());

        return spec;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getMem() {
        return mem;
    }

    public void setMem(String mem) {
        this.mem = mem;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getGpu() {
        return gpu;
    }

    public void setGpu(String gpu) {
        this.gpu = gpu;
    }

    public String getMods() {
        return mods;
    }

    public void setMods(String mods) {
        this.mods = mods;
    }
}
