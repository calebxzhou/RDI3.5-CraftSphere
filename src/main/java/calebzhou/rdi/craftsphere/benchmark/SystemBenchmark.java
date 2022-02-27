package calebzhou.rdi.craftsphere.benchmark;

import calebzhou.rdi.craftsphere.util.ThreadPool;
import oshi.SystemInfo;

import java.util.concurrent.Executors;

public class SystemBenchmark extends Thread{
    public static void go(){

        //new SystemBenchmark().start();
    }

    public final SystemInfo osInfo;
    private final long maxMemory ;

    public SystemBenchmark() {
        osInfo = new SystemInfo();
        maxMemory = Runtime.getRuntime().maxMemory();
        }
    public void benchFlops(){
        FloatExchange fe = new FloatExchange(1.11f, 2.22f, 3.33f, 4.44f);
        fe.fa+=fe.fd;
        fe.fb-=fe.fc;
        fe.fc+=fe.fb;
        fe.fd-=fe.fa;
        fe.fa+=fe.fd;
        fe.fb-=fe.fc;
        fe.fc+=fe.fb;
        fe.fd-=fe.fa;
        fe.fa+=fe.fd;
        fe.fb-=fe.fc;
        fe.fc+=fe.fb;
        fe.fd-=fe.fa;
        fe.fa+=fe.fd;
        fe.fb-=fe.fc;
        fe.fc+=fe.fb;
        fe.fd-=fe.fa;
    }


    @Override
    public void run() {
        Executors.newFixedThreadPool(osInfo.getHardware().getProcessor().getLogicalProcessorCount()).execute(this::benchFlops);

    }

    private static class FloatExchange{
        public float fa,fb,fc,fd;

        public FloatExchange(float fa, float fb, float fc, float fd) {
            this.fa = fa;
            this.fb = fb;
            this.fc = fc;
            this.fd = fd;
        }
    }
}

