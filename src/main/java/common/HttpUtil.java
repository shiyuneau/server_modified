package common;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

/**
 * @author shiyu
 * @Description
 * @create 2019-03-15 12:29
 */
public class HttpUtil {

    static final CloseableHttpClient client = HttpClients.createDefault();

    public static boolean ipTelnetFlag(String site) {
        boolean telnetFlag = true;
        HttpGet get = new HttpGet(site);
        // 设置连接的 超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000).build();
        get.setConfig(requestConfig);
        CloseableHttpResponse httpResponse = null;
        int statusCode=0;
        try {
            httpResponse = client.execute(get);
            if(httpResponse != null) {
                statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    telnetFlag = false;
                }
            }
        } catch (IOException e) {
            telnetFlag = false;
        }
        return telnetFlag;
    }

}
