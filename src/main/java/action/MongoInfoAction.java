package action;

import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author shiyu
 * @Description 获取 指定IP 中的所有的 库、表、字段信息
 * @create 2019-03-14 13:23
 */
public class MongoInfoAction {

    static Logger logger = LoggerFactory.getLogger(MongoInfoAction.class);

    public static void main(String[] args) throws IOException {
        //
        LogManager.getLogger("org.mongodb.driver.cluster").setLevel(org.apache.log4j.Level.OFF);
        LogManager.getLogger("org.mongodb").setLevel(org.apache.log4j.Level.OFF);
        String iplistPath = "/data/mongoinfo/iplist.txt";
        String successFilePath = "/data/mongoinfo/success_v3.txt";
        String errorFilePath = "/data/mongoinfo/error_v3.txt";
        if (args.length >0 && args[0] != null) {
            iplistPath = args[0];
            successFilePath = args[1];
            errorFilePath = args[2];
        }


        String[] ipList = new String[]{"1.83.125.99","1.180.57.114","101.200.197.252","1.32.196.130","101.91.220.104","101.254.166.207"};
        FileWriter successFileWriter = new FileWriter(successFilePath);
        FileWriter errorFileWriter = new FileWriter(errorFilePath);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(50, 50,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        Files.lines(Paths.get(iplistPath)).forEach(ip -> {
            executor.execute(new MongoDetailThread(ip,successFileWriter,errorFileWriter));
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
//        for (String ip : ipList) {
//            new Thread(new MongoDetailThread(ip,successFileWriter,errorFileWriter) ).start();
//        }
//        MongoDetailThread runThread = new MongoDetailThread(ip,successFileWriter,errorFileWriter);
//        new Thread(runThread).start();
    }
}
