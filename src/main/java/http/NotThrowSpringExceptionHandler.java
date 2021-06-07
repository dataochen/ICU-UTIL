package http;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

/**
 * 重写spring默认的请求异常处理类类 org.springframework.web.client.DefaultResponseErrorHandler
 * 无需抛出异常
 * @author dataochen
 * @Description
 * @date: 2020/10/13 14:49
 */
public class NotThrowSpringExceptionHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        int rawStatusCode = clientHttpResponse.getRawStatusCode();
        HttpStatus[] var3 = HttpStatus.values();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            HttpStatus statusCode = var3[var5];
            if(statusCode.value() == rawStatusCode) {
                return this.hasError(statusCode);
            }
        }

        return this.hasError(rawStatusCode);
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
//        什么都不处理
    }
    protected boolean hasError(HttpStatus statusCode) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }
    protected boolean hasError(int unknownStatusCode) {
        int seriesCode = unknownStatusCode / 100;
        return seriesCode == Series.CLIENT_ERROR.value() || seriesCode == Series.SERVER_ERROR.value();
    }

}
