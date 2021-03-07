package com.github.elmanuel1.webclientlibrary.webclient;


import com.github.elmanuel1.webclientlibrary.exceptions.InvalidRequestException;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebClientHttpRequest {
 
    private ReactorClientHttpConnector clientHttpConnector;
    private HttpHeaders headers;
    private MediaType contentType;
    private MediaType accepts;
    private Object body;
    private RequestType requestType;
    private String url;
    private boolean isFormData = false;
    private final List<ExchangeFilterFunction> exchangeFilterFunctions = new ArrayList<>();
    Map<String, Object> requestResponseMap = new HashMap<>();
    private Encoder<?> encoder;
    private Decoder<?> decoder;
    private boolean useProxy = false;


    public enum RequestType {
        GET, PUT, POST
    }

    public WebClientHttpRequest() {

    }

 
    HttpHeaders getHeaders() {
        return headers;
    }

    MediaType getContentType() {
        return contentType;
    }


    MediaType getAccepts() {
        return accepts;
    }

    BodyInserter getBody() {
        if (this.isFormData) {
            return BodyInserters.fromFormData((MultiValueMap<String, String>)this.body);
        }
        return BodyInserters.fromObject(this.body);
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public String getUrl() {
        return url;
    }


    public WebClientHttpRequest url(String url) {
        this.url = url;
        return this;
    }

    public WebClientHttpRequest logObject(Map<String, Object> requestResponseMap) {
        this.requestResponseMap = requestResponseMap;
        return this;
    }

    public WebClientHttpRequest headers(HttpHeaders headers) throws InvalidRequestException {
        if (headers == null) {
            throw new InvalidRequestException("You cannot set headers whose value is null");
        }
        this.headers = headers;
        return this;
    }

    public WebClientHttpRequest iSFormData(boolean isFormData) {
        this.isFormData = isFormData;
        return this;
    }

    public WebClientHttpRequest contentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }


    public WebClientHttpRequest accepts(MediaType accepts) {
        this.accepts = accepts;
        return this;
    }

    public WebClientHttpRequest body(Object body) {
        this.body = body;
        return this;
    }

    public WebClientHttpRequest get() {
        this.requestType = RequestType.GET;
        return this;
    }

    public WebClientHttpRequest post() {
        this.requestType = RequestType.POST;
        return this;
    }

    public WebClientHttpRequest put() {
        this.requestType = RequestType.PUT;
        return this;
    }

    public WebClientHttpRequest setRequestEncoder(Encoder<?> encoder) throws InvalidRequestException {
        if (encoder == null) {
            throw new InvalidRequestException("Null encoder passed. Please set a request encoder");
        }
        this.encoder = encoder;
        return this;

    }

    public WebClientHttpRequest setResponseDecoder(Decoder<?> decoder) throws InvalidRequestException {
        if (decoder == null) {
            throw new InvalidRequestException("Null decoder passed. Please set a response decoder");
        }
        this.decoder = decoder;
        return this;
    }


    public WebClientHttpRequest setClientConnector(ReactorClientHttpConnector clientConnector) throws InvalidRequestException {
        if (clientConnector == null) {
            throw new InvalidRequestException("client connector added cannot be null");
        }
        this.clientHttpConnector = clientConnector;
        return this;
    }

    public WebClientHttpRequest addFilter(ExchangeFilterFunction exchangeFilterFunction) throws InvalidRequestException {
        if (exchangeFilterFunction == null) {
            throw new InvalidRequestException("filter added cannot have a null value");

        }
        this.exchangeFilterFunctions.add(exchangeFilterFunction);
        return this;
    }

    public RestWebClient build() throws InvalidRequestException {
        if (this.requestType == null) {
            throw new InvalidRequestException("Specify http verb when using the webclient..");
        }

        if (this.url == null  || this.url.isEmpty()) {
            throw new InvalidRequestException( "Specify operation type or url when using the webclient..");
        }


        ExchangeStrategies.Builder strategiesBuilder = ExchangeStrategies.builder();
        if (encoder != null) {
            strategiesBuilder = strategiesBuilder.codecs(clientCodecConfigurer ->
                    clientCodecConfigurer.defaultCodecs().jackson2JsonEncoder(encoder));
        }

        if (decoder != null) {
            strategiesBuilder = strategiesBuilder.codecs(clientCodecConfigurer ->
                    clientCodecConfigurer.defaultCodecs().jackson2JsonDecoder(decoder));
        }

        WebClient.Builder webClientBuilder = WebClient.builder()
                .exchangeStrategies(strategiesBuilder.build());
        if (this.clientHttpConnector != null) {
            webClientBuilder = webClientBuilder.clientConnector(this.clientHttpConnector);
        }

        for (ExchangeFilterFunction func: exchangeFilterFunctions) {
            webClientBuilder = webClientBuilder.filter(func);
        }


        return new RestWebClient(webClientBuilder.build(), this);
    }
} 