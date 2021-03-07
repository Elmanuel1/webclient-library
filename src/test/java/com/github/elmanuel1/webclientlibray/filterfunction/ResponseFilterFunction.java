package com.github.elmanuel1.webclientlibray.filterfunction;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.Map;

public class ResponseFilterFunction {
    public static ExchangeFilterFunction interceptResponse (String body, String url, Map headers, String method) {
        return (filteredRequest, next) -> {
            String filteredUrl = filteredRequest.url().toString();

            Map filteredHeaders = filteredRequest.headers().toSingleValueMap();
            String filteredMethod = filteredRequest.method().toString();
            for(Object header :  headers.entrySet()) {
                Map.Entry entry = (Map.Entry) header;
                Object key = entry.getKey();
                Assertions.assertEquals(headers.get(key), filteredHeaders.get(key));
            }


            Assertions.assertEquals(method, filteredMethod);
            Assertions.assertEquals(url, filteredUrl);
            ClientResponse response = ClientResponse.create(HttpStatus.OK)
                    .body("Successfully processed").build();
            return next.exchange(filteredRequest)
                    .flatMap(clientResponse -> Mono.just(response))
                    .onErrorResume(err -> Mono.just(response));
        };
    }
}
