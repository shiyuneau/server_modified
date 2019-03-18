package IPClassifi;

import com.mongodb.MongoClient;
import common.HttpUtil;
import common.MongoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author shiyu
 * @Description
 * @create 2019-03-15 9:40
 */
public class MongoIPValidate {

    private static Logger logger = LoggerFactory.getLogger(MongoIPValidate.class);

    public static void main(String[] args) {



        // 先验证 ip 无密码的状态下是否能够 连接成功
        String ip = "101.200.197.252";//"221.122.108.57";//
        boolean telnetFlag = HttpUtil.ipTelnetFlag("http://"+ip+":27017");
        System.out.println(telnetFlag);
        if (telnetFlag) {
            MongoClient mongoClient = MongoUtil.mongoClientGet(ip);
            List<String> databaseNameList = MongoUtil.mongoDatabaseList(ip,mongoClient);
            if (null==databaseNameList) {
                logger.info("登录需要密码");
            } else{
                // 获取 数据库下 所有得 表名
                databaseNameList.forEach(databaseName -> {
                    List<String> collectionList = MongoUtil.mongoCollectionNameList(mongoClient,databaseName);
                    if (null==collectionList) {
                        logger.info(databaseName + "下 没有集合");
                    } else {
                        collectionList.forEach(collectionName -> {
                            logger.info("集合名称 ：" +collectionName);
                            StringBuilder stringBuilder = MongoUtil.documentInfo(mongoClient,databaseName,collectionName);
                            logger.info(stringBuilder.toString());
                            logger.info("================================");
                        });
                    }

                });
            }
        } else {
            logger.error(ip + " 不可连接");
        }
//        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
//        builder.serverSelectionTimeout(5000);
//        MongoClientOptions options = builder.build();
//        MongoClient mongoClient = new MongoClient(ip,options);
//        int processContinue = 0;
//        try {
//            System.out.println(mongoClient.getAddress());
//            processContinue++;
//        } catch (Exception e) {
//            logger.error(ip + " 不可连接");
//        }
//        switch (processContinue) {
//            case 1:
//                List<String> databaseNameList = MongoUtil.mongoDatabaseList(ip,mongoClient);
//                if (null==databaseNameList) {
//                    System.out.println("登录需要密码");
//                } else{
//                    databaseNameList.forEach(line -> System.out.println(line));
//                }
//                break;
//            case 0:
//                logger.error(ip+"错误");
//                break;
//            default:
//                break;
//        }


    }
}
