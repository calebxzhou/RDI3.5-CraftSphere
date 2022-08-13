package calebzhou.rdi.craftsphere.misc;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class HwSpec implements Serializable {
    public String brand;
    public String os;
    public String board;
    public String mem;
    public String disk;
    public String cpu;
    public String gpu;
    public String mods;

    public static void main(String[] args) {
        HwSpec systemSpec = HwSpec.getSystemSpec();
        Gson gson = new Gson();
        Gson gson1 = gson.newBuilder().setPrettyPrinting().create();
        System.out.println(gson1.toJson(systemSpec));

    }
    public static HwSpec getSystemSpec() {
        HwSpec spec = new HwSpec();
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();

        ComputerSystem csys = hal.getComputerSystem();
        spec.brand = csys.getManufacturer()+":"+csys.getModel();
        Baseboard baseboard = csys.getBaseboard();
        spec.board = baseboard.getManufacturer()+":"+baseboard.getModel();

        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();


        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        spec.os= String.format("%s %s %s(%s,%s)",operatingSystem.getManufacturer(), operatingSystem.getFamily(),operatingSystem.getVersionInfo().toString(), osArch, osVersion);


        CentralProcessor cpuinfo = hal.getProcessor();
        CentralProcessor.ProcessorIdentifier cpuid = cpuinfo.getProcessorIdentifier();
        String cpuName = cpuid.getName().replace("  ","");
        int cpuCores = cpuinfo.getPhysicalProcessorCount();
        int cpuThreads = cpuinfo.getLogicalProcessorCount();
        double cpuFreq = cpuid.getVendorFreq() / 1.0E9f;
        double cpuMaxFreq = Arrays.stream(cpuinfo.getCurrentFreq()).max().getAsLong() / 1.0E9f;
        spec.cpu=(String.format("%s(%sC/%sT)@%.2f/%.2fGHz", cpuName, cpuCores, cpuThreads, cpuFreq,cpuMaxFreq));

        StringBuilder gpu = new StringBuilder();
        for (GraphicsCard gpuinfo : hal.getGraphicsCards()) {
            String gpuName = gpuinfo.getName();
            String gpuVram = String.format("%.2f", (float) gpuinfo.getVRam() / (1024 * 1024 * 1024f));
            gpu.append(String.format("%s (%sGB);", gpuName, gpuVram));
        }
        spec.gpu=(gpu.toString());

        StringBuilder mem = new StringBuilder();
        float memTotalSize=0;
        for (PhysicalMemory meminfo : hal.getMemory().getPhysicalMemory()) {
            float memSizef =(float) meminfo.getCapacity() / (1024 * 1024 * 1024f);
            memTotalSize+=memSizef;
            String memSize = String.format("%.2f", memSizef);
            String memType = meminfo.getMemoryType();
            String memSpd = String.valueOf((int) (meminfo.getClockSpeed() / 1.0E6f));
            mem.append(String.format("%sGB-%s-%s;", memSize, memType, memSpd));
        }
        mem.append(String.format("(∑%.2fGB)", memTotalSize));
        spec.mem=(mem.toString());

        StringBuilder disk = new StringBuilder();
        float diskTotalSize=0;
        for (HWDiskStore diskStore : hal.getDiskStores()) {
            float diskSizef =(float) diskStore.getSize() / (1024 * 1024 * 1024f);
            diskTotalSize+=diskSizef;
            disk.append(String.format("%s(%.2fGB);",diskStore.getModel().replace("(Standard disk drives)",""),diskSizef));
        }
        disk.append(String.format("(∑%.2fGB)", diskTotalSize));
        spec.disk=(disk.toString());


        ArrayList<ModContainer> topLevelMods = new ArrayList<>();
        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            if (container.getContainingMod().isEmpty()) {
                topLevelMods.add(container);
            }
        }

        StringBuilder mods = new StringBuilder();
        int modAmount =0;
        topLevelMods.sort(Comparator.comparing(mod -> mod.getMetadata().getId()));
        for (ModContainer mod : topLevelMods) {
            ModMetadata metadata = mod.getMetadata();
            if (metadata.getId().startsWith("fabric-"))
                continue;
            mods.append(String.format("%s(%s);", metadata.getName(), metadata.getId()));
            ++modAmount;
        }
        mods.append("(∑").append(modAmount).append(")");
        spec.mods=(mods.toString());

        return spec;
    }


}
