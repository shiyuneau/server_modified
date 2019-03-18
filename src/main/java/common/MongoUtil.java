package common;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author shiyu
 * @Description
 * @create 2019-03-14 10:17
 */
public class MongoUtil {

    private static Logger logger = LoggerFactory.getLogger(MongoUtil.class);

    public static void main(String args[]) {
        MongoUtil action = new MongoUtil();
        action.conectWithoutPassword();
    }

    /**
     * 返回 指定IP 对应的mongoclient客户端
     *
     * @param ip
     * @return
     */
    public static MongoClient mongoClientGet(String ip) {
        // 使用 MongoCliendOptions.Builder 来设置 验证的 时间，默认为30s ， 此处设置为5s
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.serverSelectionTimeout(5000);
        MongoClientOptions options = builder.build();
        MongoClient client = new MongoClient(ip, options);
        return client;
    }

    /**
     * 获取 ip 下 mongo 所有得数据库名称
     *
     * @param ip
     * @param mongoClient
     * @return
     */
    public static List<String> mongoDatabaseList(String ip, MongoClient mongoClient) {
        Map<Integer, List<String>> map = new HashMap<>();
        List<String> databaseNameList = null;
        try {
            mongoClient.listDatabaseNames().first();
            Iterable<String> databaseList = mongoClient.listDatabaseNames();
            databaseNameList = new ArrayList<>();
            for (String str : databaseList) {
                databaseNameList.add(str);
            }
        } catch (MongoCommandException e) {
//            e.printStackTrace();
            logger.error(ip + " 连接错误，需要密码登录");
        }
        return databaseNameList;
    }

    /**
     * 获取 databaseName 数据库 下 得 所有 集合得名称
     * @param mongoClient
     * @param databaseName
     * @return
     */
    public static List<String> mongoCollectionNameList(MongoClient mongoClient, String databaseName) {
        List<String> collectionNameList = null;
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
        if (mongoDatabase.listCollectionNames().first() != null) {
            Iterable<String> collectionList = mongoDatabase.listCollectionNames();
            collectionNameList = new ArrayList<>();
            for (String name : collectionList) {
                collectionNameList.add(name);
            }
        }
        return collectionNameList;
    }

    /**
     * 获取具体得 database下 collectionName得属性名称
     * @param mongoClient
     * @param databaseName
     * @param collectionName
     * @return
     */
    public static StringBuilder documentInfo(MongoClient mongoClient , String databaseName , String collectionName){
        StringBuilder stringBuilder = new StringBuilder();
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        FindIterable<Document> documents = collection.find();
        Document document = documents.first();
        documentLoop(stringBuilder, document, null);
        return stringBuilder;
    }

    /**
     * 对 document 进行便利，取得每一个字段 及子字段
     * 拼成 string 返回
     *
     * @param stringBuilder
     * @param document
     * @param parentDoc
     */
    private static void documentLoop(StringBuilder stringBuilder, Document document, String parentDoc) {
        if (null == document) {
            return;
        }
        Set<Map.Entry<String, Object>> set = document.entrySet();
        for (Map.Entry<String, Object> entry : set) {
            Object value = entry.getValue();
            if (null == parentDoc) {
                stringBuilder.append(entry.getKey() + "\t");
            } else {
                stringBuilder.append(parentDoc + "[" + entry.getKey() + "]" + "\t");
            }
            if (value instanceof Document) {
                Document doc = (Document) value;
                documentLoop(stringBuilder, doc, entry.getKey());
            }

        }
    }







    public void conectWithoutPassword() {
        try {
            // 连接到 mongodb 服务
            MongoClient mongoClient = new MongoClient("1.32.196.130", 27017);
            System.out.println(mongoClient.getAddress());
            Iterable<String> databaseList = mongoClient.listDatabaseNames();
            System.out.println("数据库名称");
            for (String str : databaseList) {
                System.out.println("数据库" + str + "对应的集合");
                // 连接到数据库
                MongoDatabase mongoDatabase = mongoClient.getDatabase(str);
                Iterable<String> collectionList = mongoDatabase.listCollectionNames();
                for (String name : collectionList) {
                    System.out.println("集合名称 :" + name);
                    MongoCollection<Document> collection = mongoDatabase.getCollection(name);
                    FindIterable<Document> documents = collection.find();
                    Document document = documents.first();
                    Set<Map.Entry<String, Object>> set = document.entrySet();
                    for (Map.Entry<String, Object> entry : set) {
                        System.out.println(entry.getKey() + ":" + entry.getValue());
                    }
                }


                System.out.println("-------------------------------------------------");

            }

            System.out.println("Connect to database successfully");

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void connectWithPasswod() {
        try {
            //连接到MongoDB服务 如果是远程连接可以替换“localhost”为服务器所在IP地址
            //ServerAddress()两个参数分别为 服务器地址 和 端口
            ServerAddress serverAddress = new ServerAddress("localhost", 27017);
            List<ServerAddress> addrs = new ArrayList<ServerAddress>();
            addrs.add(serverAddress);

            //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
            MongoCredential credential = MongoCredential.createScramSha1Credential("username", "databaseName", "password".toCharArray());
            List<MongoCredential> credentials = new ArrayList<MongoCredential>();
            credentials.add(credential);

            //通过连接认证获取MongoDB连接
            MongoClient mongoClient = new MongoClient(addrs, credentials);

            //连接到数据库
            MongoDatabase mongoDatabase = mongoClient.getDatabase("databaseName");
            System.out.println("Connect to database successfully");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
