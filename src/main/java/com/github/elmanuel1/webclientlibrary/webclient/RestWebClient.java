package com.github.elmanuel1.webclientlibrary.webclient;
 
import com.github.elmanuel1.webclientlibrary.exceptions.InvalidRequestException;
import org.springframework.http.HttpHeaders; 
import org.springframework.http.MediaType; 
import org.springframework.web.reactive.function.client.ClientResponse; 
import org.springframework.web.reactive.function.client.WebClient; 
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
 
 
public class RestWebClient { 
 
    private WebClient webClient;
    private WebClientHttpRequest request;

    public RestWebClient(WebClient webClient, WebClientHttpRequest request ) {
        this.webClient = webClient;
        this.request = request;
    }

    public Mono<ClientResponse> exchange() {

        MediaType accepts = this.request.getAccepts() != null ? request.getAccepts() : MediaType.APPLICATION_JSON;
        if (request.getRequestType().equals(WebClientHttpRequest.RequestType.GET)) {
            WebClient.RequestHeadersSpec<?> requestHeadersUriSpec = webClient.get()
            .uri(this.request.getUrl())
            .accept(accepts);
            return requestHeadersUriSpec.exchange();
        }
        WebClient.RequestBodyUriSpec requestBodyUriSpec ;
        switch (request.getRequestType()) {
            case PUT:
                requestBodyUriSpec = webClient.put();
                break;
            case POST:
                requestBodyUriSpec = webClient.post();
                break;
            default:
            return Mono.error(new InvalidRequestException("http verb is not supported by the webclient used"));
        }

        MediaType contentType = request.getContentType() != null ? request.getContentType() : MediaType.APPLICATION_JSON;
        WebClient.RequestHeadersSpec<WebClient.RequestBodySpec> r = requestBodyUriSpec
                .uri(this.request.getUrl())
                .contentType(contentType)
                .accept(accepts)
                .body(request.getBody());

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()){
            r = requestBodyUriSpec.headers(this.getHttpHeaders(request.getHeaders()));
        }
        return r.exchange();
    }
 
     private Consumer<HttpHeaders> getHttpHeaders(HttpHeaders headers) {
         return httpHeaders -> httpHeaders.addAll(headers);
     }
}