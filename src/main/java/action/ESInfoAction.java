package action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author shiyu
 * @Description
 * @create 2019-03-18 13:37
 */
public class ESInfoAction {

    static Logger logger = LoggerFactory.getLogger(ESInfoAction.class);

    public static void main(String[] args) throws Exception{
        // 首先验证这个IP 可不可用
        // 如果可用 获取IP 的所有 index
        // 根据每一个index获取 index 下的具体的属性

        String esIplistPath = "/data/esinfo/iplist.txt";
        String essuccessFilePath = "/data/esinfo/success_v3.txt";
        String eserrorFilePath = "/data/esinfo/error_v3.txt";
        if (args.length >0 && args[0] != null) {
            esIplistPath = args[0];
            essuccessFilePath = args[1];
            eserrorFilePath = args[2];
        }

        FileWriter successFileWriter = new FileWriter(essuccessFilePath);
        FileWriter errorFileWriter = new FileWriter(eserrorFilePath);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(50, 50,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        String[] ipList = new String[]{"116.62.24.156","111.230.210.127","120.26.99.173","103.26.3.234","112.74.12.5","39.105.21.57"};
//        Arrays.asList(ipList).forEach(ip -> {
//            executor.execute(new ESDetailThread(ip, successFileWriter, errorFileWriter));
//        });
        Files.lines(Paths.get(esIplistPath)).forEach(ip -> {
            executor.execute(new ESDetailThread(ip,successFileWriter,errorFileWriter));
        });
        executor.shutdown();
        boolean flag = true;
        while(flag) {
            if (executor.getActiveCount() ==0) {
                flag = false;
            } else {
                logger.info("thread num = " + executor.getQueue().size());
                logger.info("active count = " + executor.getActiveCount());
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        logger.info("结束");
    }

}
