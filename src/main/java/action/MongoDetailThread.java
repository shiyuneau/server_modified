package action;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import common.MongoUtil;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author shiyu
 * @Description
 * @create 2019-03-14 13:24
 */
public class MongoDetailThread implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(MongoDetailThread.class);

    private String ip;
    private static FileWriter errorFileWriter;
    private static FileWriter successFileWriter;

    public MongoDetailThread(String ip , FileWriter successFileWriter , FileWriter errorFileWriter) {
        this.ip = ip;
        MongoDetailThread.successFileWriter = successFileWriter;
        MongoDetailThread.errorFileWriter = errorFileWriter;
    }

    @Override
    public void run() {
        //先判断这个IP 能不能 连接成功
        MongoClient mongoClient = null;
            mongoClient = MongoUtil.mongoClientGet(ip);

        try {
            logger.info(mongoClient.getAddress()+"");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(ip +"连接失败，写入错误文件");
            try {
                errorFileWriter.write(ip+"\r\n");
                errorFileWriter.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Thread.currentThread().stop();
        }
        // 如果能够连接成功，那么获取数据库的信息
        Iterable<String> databaseList = mongoClient.listDatabaseNames();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (String str : databaseList) {
//            logger.info("数据库名称:" + str);
                MongoDatabase mongoDatabase = mongoClient.getDatabase(str);
                Iterable<String> collectionList = mongoDatabase.listCollectionNames();
                if (((MongoIterable<String>) collectionList).first() == null) {
                    try {
                        successFileWriter.write(ip + "\n" + str + "\n");
                        successFileWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                for (String name : collectionList) {
//                logger.info("集合名称:" + name);
                    MongoCollection<Document> collection = mongoDatabase.getCollection(name);
                    FindIterable<Document> documents = collection.find();
                    Document document = documents.first();
                    documentLoop(stringBuilder, document, null);

//               logger.info("************************************************");
//               logger.info(stringBuilder.toString());
                    try {
                        successFileWriter.write("====================================" + "\n" + ip + "\n" + str + "\n" + name + "\n" + stringBuilder.toString() + "\n");
                        successFileWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

//               logger.info("************************************************");
                    stringBuilder.delete(0, stringBuilder.length());
                }
//            logger.info("-------------------------------------------------");
            }
        }catch (Exception e) {
            e.printStackTrace();
            logger.info(ip +"连接失败，写入错误文件");
            try {
                errorFileWriter.write(ip+"\r\n");
                errorFileWriter.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Thread.currentThread().stop();
        }
    }

    public void documentLoop(StringBuilder stringBuilder ,Document document , String parentDoc) {
        if (null == document) {
            return;
        }
        Set<Map.Entry<String,Object>> set = document.entrySet();
        for (Map.Entry<String,Object> entry : set) {
            Object value = entry.getValue();
            if (null == parentDoc) {
                stringBuilder.append(entry.getKey() + "\t");
//                logger.info(entry.getKey());
            } else {
//                logger.info(parentDoc + "[ " +entry.getKey() + "]");
                stringBuilder.append(parentDoc + "[" +entry.getKey() + "]" + "\t");
            }
            if (value instanceof Document) {
                Document doc = (Document)value;
                documentLoop(stringBuilder,doc,entry.getKey());
            }

        }
    }
}
