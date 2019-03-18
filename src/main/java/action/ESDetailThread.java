package action;

import common.ElasticUtil;
import common.HttpUtil;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author shiyu
 * @Description
 * @create 2019-03-18 13:42
 */
public class ESDetailThread implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ESDetailThread.class);

    private String ip;
    private static FileWriter errorFileWriter;
    private static FileWriter successFileWriter;

    public ESDetailThread(String ip , FileWriter successFileWriter , FileWriter errorFileWriter) {
        this.ip = ip;
        ESDetailThread.successFileWriter = successFileWriter;
        ESDetailThread.errorFileWriter = errorFileWriter;
    }

    @Override
    public void run() {
        // 先判断IP能否连接成功
        String site = "http://" + ip + ":9200/";
        boolean telnetFlag = HttpUtil.ipTelnetFlag(site);
        try {
            if (telnetFlag) {
                // 获取所有得 index
                // 设置集群名称
                logger.info(ip + " es 服务连接成功");
                String clusterName = ElasticUtil.clusterNameData(site);
//                logger.info("cluser name = " + clusterName );
                List<String> indexList = ElasticUtil.indexList(site);//ElasticUtil.indexList(client);
                if (null != indexList) {

                    indexList.forEach(indexName -> {
                        StringBuilder stringBuilder = ElasticUtil.indexFieldList(site+"/"+indexName ,indexName);
                        // 将IP index  field 所有的信息写入 文件中
                        try {
                            successFileWriter.write(ip + "\n" + indexName + "\n" + stringBuilder.toString() + "\n");
                            successFileWriter.write("===================================================" + "\n");
                            successFileWriter.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    // 如果没有索引 ， 那么 也输入到 错误文件中
                    logger.info("该ip下没有索引");
                    errorFileWriter.write(ip+"\t" + "无数据" + "\n");
                    errorFileWriter.flush();
                }
            } else {
                logger.error(ip + "连接不同");
                // 将不能连接成功的 IP 写入失败的 文件
                errorFileWriter.write(ip+"\n");
                errorFileWriter.flush();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
