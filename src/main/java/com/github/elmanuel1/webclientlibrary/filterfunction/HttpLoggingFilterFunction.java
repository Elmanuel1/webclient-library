package com.github.elmanuel1.webclientlibrary.filterfunction;
import com.github.elmanuel1.webclientlibrary.utils.DateUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.util.Date;
import java.util.Map;

public class HttpLoggingFilterFunction {

    public static ExchangeFilterFunction log(Object requestBody, Map<String, Object> requestResponseMap) {
        return (filteredRequest, next) -> {
            if (requestBody != null) {
                requestResponseMap.put("requestBody", requestBody);
            }
            requestResponseMap.put("method", filteredRequest.method().toString());
            requestResponseMap.put("host", filteredRequest.url().getHost());
            requestResponseMap.put("path", filteredRequest.url().getPath());
            requestResponseMap.put("query", filteredRequest.url().getQuery());
            requestResponseMap.put("headers", filteredRequest.headers().toSingleValueMap());
            requestResponseMap.put("logType", "OUTBOUND");
            requestResponseMap.put("requestTime", DateUtil.convertDateToString(new Date()));
            try {
                requestResponseMap.put("sysName", InetAddress.getLocalHost().getHostName());
                requestResponseMap.put("sysAddress", InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                requestResponseMap.put("hostName", "unknown");
                requestResponseMap.put("sysAddress", "unknown");
            }

            Long startTime = System.currentTimeMillis();
            return next.exchange(filteredRequest)
                    .flatMap(clientResponse -> {
                        requestResponseMap.put("isError",  false);
                        requestResponseMap.put("statusCode", clientResponse.statusCode().toString());
                        requestResponseMap.put("responseTime", DateUtil.convertDateToString(new Date()));
                        requestResponseMap.put("responseHeaders", clientResponse.headers().asHttpHeaders().toSingleValueMap());

                        Flux<DataBuffer> body = (clientResponse.body(BodyExtractors.toDataBuffers()));
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        return Mono.just(ClientResponse
                                .from(clientResponse)
                                .body(body.doOnNext(dataBuffer -> {
                                    try{
                                        Channels.newChannel(byteArrayOutputStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                                    } catch (Exception var18) {
                                        requestResponseMap.put("responseLogError", String.format("could not add log to the output stream for logging purpose %s", var18.getMessage()));
                                    }

                                }).doOnComplete(()-> {
                                    try {
                                        requestResponseMap.put("response", IOUtils.toString(byteArrayOutputStream.toByteArray(), "UTF-8"));
                                        byteArrayOutputStream.close();
                                    } catch (Throwable e) {
                                        requestResponseMap.put("responseLogError2", String.format("could not log due to error %s" , e.getMessage()));
                                    }
                                })).build());
                    }).doOnError((error) ->{
                        requestResponseMap.put("isError",  true);
                        requestResponseMap.put("error",  String.format("%s|%s", error.getMessage(), error.toString()));
                    }).doOnSuccessOrError((suc, err) -> {
                        Long endTime = System.currentTimeMillis();
                        requestResponseMap.put("elapsedTime", (endTime - startTime));
                    });
        };
    }
}