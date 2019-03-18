package IPClassifi;

import common.ElasticUtil;
import common.HttpUtil;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author shiyu
 * @Description
 * @create 2019-03-15 16:30
 */
public class ElasticSearchIPValidate {

    private static Logger logger = LoggerFactory.getLogger(ElasticSearchIPValidate.class);

    public static void main(String[] args) {
        String ip = "116.62.24.156";//"106.75.35.87";
        String site = "http://" + ip + ":9200/";
        boolean telnetFlag = HttpUtil.ipTelnetFlag(site);
        try {

            String url = "http://106.75.35.87:9200/blog/_search?size=1";
            String content = ElasticUtil.indexDataInfo(url);
            System.out.println(content);

//            if (telnetFlag) {
//                // 获取所有得 index
//                // 设置集群名称
//                logger.info(ip + " es 服务连接成功");
//                String clusterName = ElasticUtil.clusterNameData(site);
////                logger.info("cluser name = " + clusterName );
//                Settings settings = Settings.builder().put("cluster.name", clusterName).build();
//                // 创建client
//                TransportClient client = new PreBuiltTransportClient(settings)
//                        .addTransportAddress(new TransportAddress(InetAddress.getByName(ip), 9300));
//                List<String> indexList = ElasticUtil.indexList(site);//ElasticUtil.indexList(client);
//                if (null != indexList) {
//
//                    indexList.forEach(indexName -> {
//                        logger.info(indexName);
//                        StringBuilder stringBuilder = ElasticUtil.indexFieldList(site+"/"+indexName ,indexName);
//                        logger.info(stringBuilder.toString());
//                        logger.info("========================================");
//                    });
//                } else {
//                    logger.info("该ip下没有索引");
//                }
//            } else {
//                logger.error(ip + "连接不同");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
