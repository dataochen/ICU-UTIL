package http;

import com.alibaba.fastjson.JSONObject;
import common.SslUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

/**
 * @author dataochen
 * @Description
 * @date: 2020/11/26 9:47
 */
public class HttpClient {
    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

    private CloseableHttpClient client;

    @Value("${http.client.timeout}")
    private String CLIENT_TIMEOUT;

    /**
     * 1.初始化httpClient
     *
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    @PostConstruct
    private void init() throws NoSuchAlgorithmException, KeyManagementException, CertificateException, KeyStoreException, IOException, URISyntaxException, UnrecoverableKeyException {

        SSLContext sslcontext = SslUtils.createIgnoreVerifySSL();
        //设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(
                        sslcontext, new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"},
                        null,
                        new DefaultHostnameVerifier(null)))
                .build();
        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = (final HttpResponse response, final HttpContext context) -> {
            Args.notNull(response, "HTTP response");
            final HeaderElementIterator it = new BasicHeaderElementIterator(
                    response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                final HeaderElement he = it.nextElement();
                final String param = he.getName();
                final String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch (final NumberFormatException ignore) {
                    }
                }
            }
            //            keep alive 3秒 客户端维护这个连接最多3秒的有效期 在获取环节超过3秒就会关闭此连接org.apache.http.pool.AbstractConnPool.getPoolEntryBlocking() entry.isExpired(System.currentTimeMillis())
            return 3 * 1000;
        };
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connManager.setDefaultMaxPerRoute(10);
        connManager.setMaxTotal(100);
        //获取连接后 再次校验是否空闲超时org.apache.http.pool.AbstractConnPool.getPoolEntryBlocking() entry.getUpdated() + this.validateAfterInactivity <= System.currentTimeMillis()
        connManager.setValidateAfterInactivity(3000);
        //evictIdleConnections 超时之前 定期回收空闲连接 并发setMaxConnPerRoute=10 最多setMaxConnTotal=100个;注意，evictIdleConnections会在启动时线程sleep一个maxIdle时间
        //创建自定义的httpclient对象
        client = HttpClients.custom()
                //                注意：HttpClients的setDefaultMaxPerRoute和setMaxTotal不会覆盖connManager的值
                .setConnectionManager(connManager)
                .setConnectionManagerShared(false).evictIdleConnections(3000, TimeUnit.MILLISECONDS)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                //                visa接口幂等 允许重试 注释掉disableAutomaticRetries 默认重试3次 会从连接池中获取 不会直接创建新的连接
                .disableAutomaticRetries()
                .build();


    }

    /**
     * 公共的请求网络IO方法
     *
     * @param httpMethod
     * @return
     */
    public <T> T networkCall(String requestBody, HttpMethod httpMethod, String url, Class<T> tClass) {
        try {
            HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(client);
            //设置超时时间
            httpComponentsClientHttpRequestFactory.setConnectTimeout(Integer.valueOf(CLIENT_TIMEOUT));
            httpComponentsClientHttpRequestFactory.setReadTimeout(Integer.valueOf(CLIENT_TIMEOUT));
            httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(Integer.valueOf(CLIENT_TIMEOUT));
            RestTemplate restTemplate = new RestTemplate(httpComponentsClientHttpRequestFactory);
            restTemplate.setErrorHandler(new NotThrowSpringExceptionHandler());
            // 设置header
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> objectHttpEntity = null;
            switch (httpMethod) {
                case POST:
                    objectHttpEntity = new HttpEntity<>(requestBody, httpHeaders);
                    break;
                case PUT:
                    objectHttpEntity = new HttpEntity<>(requestBody, httpHeaders);
                    break;
                case GET:
                    objectHttpEntity = new HttpEntity<>(null, httpHeaders);
                    //                    nothing
                    break;
                default:
                    break;
            }

            log.info("#######invoke  api start,,url={},requestBody={}",
                    url, requestBody);
            ResponseEntity<String> exchange = restTemplate.exchange(url, httpMethod, objectHttpEntity, String.class);
            log.info("#######invoke api end,exchange={}", JSONObject.toJSONString(exchange));
            return convertResCommonObject(exchange, tClass);
        } catch (Exception e) {
            log.error("invoke  api Exception,e={}", e);
            throw e;
        }

    }

    /**
     * 公共反参转换方法
     * Object
     *
     * @param stringResponseEntity
     * @return
     */
    private <T> T convertResCommonObject(ResponseEntity<String> stringResponseEntity, Class<T> tClass) {
        if (null == stringResponseEntity) {
            log.error("netWork http response not get normal response.");
            throw new IllegalStateException("http invoke exception");
        }
        String body = stringResponseEntity.getBody();
        log.debug("parse body={}", body);
        //        此网关所有接口都用反参
        if (null == body) {
            log.error("http response body is null");
            return null;
        }
        HttpStatus statusCode = stringResponseEntity.getStatusCode();
//        if (statusCode.is5xxServerError()) {
//            log.error("http status is {}", JSONObject.toJSONString(statusCode));
//            throw new ChannelException(ErrCodeEnum.CHANNEL_ERR_NETWORK_EXCEPTION);
//        }
        try {
            if (tClass.equals(String.class)) {
                return (T) body;
            }
            T jsonObject = JSONObject.parseObject(body, tClass);
            return jsonObject;
        } catch (Exception e) {
            log.error("body parse not json. body is ={}", body);
            return null;
        }

    }


}
