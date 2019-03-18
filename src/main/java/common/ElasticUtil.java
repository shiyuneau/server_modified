package common;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

/**
 * @author shiyu
 * @Description
 * @create 2019-03-15 14:04
 */
public class ElasticUtil {
    static TransportClient client;

    public static void main(String[] args) throws Exception {
        // 设置集群名称
        Settings settings = Settings.builder().put("cluster.name", "my-es").build();
        // 创建client
        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("39.105.9.196"), 9300));

        indexList("http://39.105.9.196:9200/");

        ActionFuture<IndicesStatsResponse> isr = client.admin().indices().stats(new IndicesStatsRequest().all());
        Set<String> set = isr.actionGet().getIndices().keySet();

        set.forEach(line -> {
            System.out.println(line);
            String httpSite = "http://39.105.9.196:9200/" + line;
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet get = new HttpGet(httpSite);
            CloseableHttpResponse httpResponse = null;
            try {
                httpResponse = client.execute(get);
                HttpEntity entity = httpResponse.getEntity();
                String content = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);//关闭内容流
                JSONObject o1 = JSONObject.parseObject(content);
                JSONObject o2 = o1.getJSONObject(line);
                JSONObject o3 = o2.getJSONObject("mappings");
                String key = o3.entrySet().iterator().next().getKey();
                JSONObject o4 = o3.getJSONObject(key);
                JSONObject o5 = o4.getJSONObject("properties");
                for (Map.Entry<String, Object> entry : o5.entrySet()) {
                    System.out.println(entry.getKey() + "\t" + entry.getValue());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        client.close();
    }

    /**
     * 获取 集群得名称
     * @param site
     * @return
     */
    public static String clusterNameData(String site) {
        String clusterName = null;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(site);
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = client.execute(get);
            HttpEntity entity = httpResponse.getEntity();
            String content = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);//关闭内容流
            JSONObject o1 = JSONObject.parseObject(content);
            clusterName = o1.getString("cluster_name");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clusterName;
    }

    /**
     * 获取 client下所有得 index
     * @param client
     * @return
     */
    public static List<String> indexList(TransportClient client) {
        List<String> indexList = null;
        ActionFuture<IndicesStatsResponse> isr = client.admin().indices().stats(new IndicesStatsRequest().all());
        Set<String> set = isr.actionGet().getIndices().keySet();
        if (set.size() > 0) {
            indexList = new ArrayList<>();
            for (String index : set) {
                indexList.add(index);
            }
        }
        return indexList;
    }

    public static List<String> indexList(String httpSite) {
        httpSite += "*";
        List<String> indexList = null;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(httpSite);
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = client.execute(get);
            HttpEntity entity = httpResponse.getEntity();
            String content = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);//关闭内容流
            JSONObject o1 = JSONObject.parseObject(content);
            if (o1.entrySet().size() > 0) {
                indexList = new ArrayList<>();
                for (Map.Entry<String, Object> map : o1.entrySet()) {
                    indexList.add(map.getKey());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return indexList;
    }

    /**
     * 根据 indexSite 获取 index 所有得字段
     * @param indexSite
     * @param index
     * @return
     */
   public static StringBuilder indexFieldList(String indexSite , String index) {
        StringBuilder stringBuilder = new StringBuilder();
       CloseableHttpClient client = HttpClients.createDefault();
       HttpGet get = new HttpGet(indexSite);
       CloseableHttpResponse httpResponse = null;
       try {
           httpResponse = client.execute(get);
           HttpEntity entity = httpResponse.getEntity();
           String content = EntityUtils.toString(entity, "UTF-8");
           EntityUtils.consume(entity);//关闭内容流
           JSONObject o1 = JSONObject.parseObject(content);
           JSONObject o2 = o1.getJSONObject(index);
           JSONObject o3 = o2.getJSONObject("mappings");
           String key = o3.entrySet().iterator().next().getKey();
           JSONObject o4 = o3.getJSONObject(key);
           jsonObjectLoop(stringBuilder , o4 , null);
       } catch (IOException e) {
           e.printStackTrace();
       }
        return stringBuilder;
    }

    /**
     * 循环获取 properties 中得属性得值
     * @param stringBuilder
     * @param object
     * @param parentDoc
     */
    public static void jsonObjectLoop(StringBuilder stringBuilder,JSONObject object, String parentDoc ) {
        JSONObject o5 = object.getJSONObject("properties");
        for (Map.Entry<String, Object> entry : o5.entrySet()) {
            if (null == parentDoc) {
                stringBuilder.append(entry.getKey() + "\t");
            } else {
                stringBuilder.append(parentDoc + "[" + entry.getKey() + "]" + "\t");
            }
            String value = entry.getValue().toString();
            if (value.contains("\"properties\"")){
                jsonObjectLoop(stringBuilder,JSONObject.parseObject(value) ,entry.getKey());
            }
        }
    }


    public static String indexDataInfo(String url) {
        String content = null;
        List<String> indexList = null;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = client.execute(get);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);//关闭内容流
            JSONObject o1 = JSONObject.parseObject(content);
            JSONObject o2 = o1.getJSONObject("hits");
            JSONArray o3 = o2.getJSONArray("hits");
            content = o3.getString(0);
            JSONObject o4 = JSONObject.parseObject(content);
            content = o4.getString("_source");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }




    private static List<String> getIndexFieldList(String fieldName,
                                                  Map<String, Object> mapProperties) {
        List<String> fieldList = new ArrayList<String>();
        Map<String, Object> map = (Map<String, Object>) mapProperties
                .get("properties");
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (((Map<String, Object>) map.get(key)).containsKey("type")) {
                fieldList.add(fieldName + "" + key);
            } else {
                List<String> tempList = getIndexFieldList(fieldName + "" + key
                        + ".", (Map<String, Object>) map.get(key));
                fieldList.addAll(tempList);
            }
        }
        return fieldList;
    }


    /**
     * 组织json串, 方式1,直接拼接
     */
    public String createJson1() {
        String json = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        return json;
    }

    /**
     * 使用map创建json
     */
    public Map<String, Object> createJson2() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("user", "kimchy");
        json.put("postDate", new Date());
        json.put("message", "trying out elasticsearch");
        return json;
    }

    /**
     * 使用fastjson创建
     */
    public JSONObject createJson3() {
        JSONObject json = new JSONObject();
        json.put("user", "kimchy");
        json.put("postDate", new Date());
        json.put("message", "trying out elasticsearch");
        return json;
    }

    /**
     * 使用es的帮助类
     */
    public XContentBuilder createJson4() throws Exception {
        // 创建json对象, 其中一个创建json的方式
        XContentBuilder source = XContentFactory.jsonBuilder()
                .startObject()
                .field("user", "kimchy")
                .field("postDate", new Date())
                .field("message", "trying to out ElasticSearch")
                .endObject();
        return source;
    }


    public void test1() throws Exception {
        XContentBuilder source = createJson4();
        // 存json入索引中
        IndexResponse response = client.prepareIndex("domain-hbase", "domain-type", "1").setSource(source).get();
//        // 结果获取
        String index = response.getIndex();
        String type = response.getType();
        String id = response.getId();
        long version = response.getVersion();
        boolean created = response.isFragment();
        System.out.println(index + " : " + type + ": " + id + ": " + version + ": " + created);
    }
}
