package com.github.elmanuel1.webclientlibray;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.elmanuel1.webclientlibrary.clientconnectors.ClientConnector;
import com.github.elmanuel1.webclientlibrary.exceptions.InvalidRequestException;
import com.github.elmanuel1.webclientlibrary.filterfunction.HttpLoggingFilterFunction;
import com.github.elmanuel1.webclientlibrary.webclient.WebClientHttpRequest;
import com.github.elmanuel1.webclientlibray.filterfunction.ResponseFilterFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.net.ssl.SSLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@SpringBootTest
public class WebClientTest {
    private final Logger logger = LoggerFactory.getLogger(WebClientTest.class);

    @Test
    public void testNoUrlAdded() {
        InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> new WebClientHttpRequest()
                        .get()
                        .body("StringBody")
                        .build()
                        .exchange()
        );
        Assertions.assertEquals("Specify operation type or url when using the webclient..", exception.getMessage());
    }

    @Test
    public void testNoHTTPMethodAdded() {
        InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> new WebClientHttpRequest()
                        .body("StringBody")
                        .build()
                        .exchange()
        );

        Assertions.assertEquals("Specify http verb when using the webclient..", exception.getMessage());
    }

    @Test
    public void testNullBody() throws InvalidRequestException {
        new WebClientHttpRequest()
                .body(null)
                .post()
                .url("http:localhost:8080")
                .build()
                .exchange();
    }

    @Test
    public void testEmptyUrlAdded() {
        InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> new WebClientHttpRequest()
                        .body("StringBody")
                        .post()
                        .url("")
                        .build()
                        .exchange()
        );

        Assertions.assertEquals("Specify operation type or url when using the webclient..", exception.getMessage());
    }

    @Test
    public void testNullExchangeFilterFunctionAdded() {
        InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> new WebClientHttpRequest()
                        .body("StringBody")
                        .post()
                        .url("")
                        .addFilter(null)
                        .build()
                        .exchange()
        );

        Assertions.assertEquals("filter added cannot have a null value", exception.getMessage());
    }

    @Test
    public void testNullClientConnectorAdded() {
        InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> new WebClientHttpRequest()
                        .body("StringBody")
                        .post()
                        .url("http")
                        .setClientConnector(null)
                        .build()
                        .exchange()
        );

        Assertions.assertEquals("client connector added cannot be null", exception.getMessage());
    }

    @Test
    public void testNullHTTPHeadersAdded() {
        InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> new WebClientHttpRequest()
                        .body("StringBody")
                        .post()
                        .url("http")
                        .headers(null)
                        .build()
                        .exchange()
        );

        Assertions.assertEquals("You cannot set headers whose value is null", exception.getMessage());
    }

    @Test
    public void testNullRequestEncoderAdded() {
        InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> new WebClientHttpRequest()
                        .body("StringBody")
                        .post()
                        .url("http")
                        .setRequestEncoder(null)
                        .build()
                        .exchange()
        );

        Assertions.assertEquals("Null encoder passed. Please set a request encoder", exception.getMessage());
    }

    @Test
    public void testNullResponseDecoderAdded() {
        InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> new WebClientHttpRequest()
                        .body("StringBody")
                        .post()
                        .url("http")
                        .setResponseDecoder(null)
                        .build()
                        .exchange()
        );

        Assertions.assertEquals("Null decoder passed. Please set a response decoder", exception.getMessage());
    }

    @Test
    public void testNullConnectionProp() {
        String requestBody = "{\"name\": \"Oluwatoba Aribo\"}";
        String url = "http://localhost:8083/api/v1/test";
        HttpHeaders headers = new HttpHeaders();
        headers.put("test", Collections.singletonList("value"));
        headers.put("newKey", Collections.singletonList("newValue"));

        InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> new WebClientHttpRequest()
                        .body(requestBody)
                        .post()
                        .url(url)
                        .headers(headers)
                        .setClientConnector(new ClientConnector(null).getReactorClientHttpConnector())
                        //.setRequestEncoder(encoder)
                        //.setResponseDecoder(decoder)
                        .addFilter(ResponseFilterFunction.interceptResponse(requestBody, url, headers.toSingleValueMap(), WebClientHttpRequest.RequestType.POST.toString()))
                        .build()
                        .exchange());
        Assertions.assertEquals("Null connection prop passed.", exception.getMessage());
    }

    @Test
    public void testResponse() throws InvalidRequestException, SSLException {
        String requestBody = "{\"name\": \"Oluwatoba Aribo\"}";
        String url = "http://localhost:8083/api/v1/test";
        HttpHeaders headers = new HttpHeaders();
        headers.put("test", Collections.singletonList("value"));
        headers.put("newKey", Collections.singletonList("newValue"));

        Map<String, Object> map = new HashMap<>();
        Mono<String> resp = new WebClientHttpRequest()
                .body(requestBody)
                .post()
                .url(url)
                .headers(headers)

                .logObject(map)
                .setClientConnector(new ClientConnector().getReactorClientHttpConnector())
                //.setRequestEncoder(encoder)
                //.setResponseDecoder(decoder)
                .addFilter(HttpLoggingFilterFunction.log(requestBody, map))
                .addFilter(ResponseFilterFunction.interceptResponse(requestBody, url, headers.toSingleValueMap(), WebClientHttpRequest.RequestType.POST.toString()))
                .build()
                .exchange().flatMap(clientResponse -> clientResponse.bodyToMono(String.class)
                        .switchIfEmpty(Mono.error(new RuntimeException()))
                        .flatMap(payload-> Mono.just(payload)));
              StepVerifier.create(resp)
                      .expectNextMatches(r -> {
                          try {
                              logger.info(new ObjectMapper().writeValueAsString(map));
                          } catch (JsonProcessingException e) {
                              e.printStackTrace();
                          }
                          //TODO: Confirm it contains all the necessary fields it requires
                          return map.get("response").equals("Successfully processed");
                      }).verifyComplete();
    }
}
