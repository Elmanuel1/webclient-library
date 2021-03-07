package com.github.elmanuel1.webclientlibrary.clientconnectors;

import com.github.elmanuel1.webclientlibrary.exceptions.InvalidRequestException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;

import javax.net.ssl.SSLException;

public class ClientConnector {
    private ConnectionProp connectionProp;

    public ClientConnector() {
        this.connectionProp = new ConnectionProp();
    }

    public ClientConnector(ConnectionProp connectionProp) throws InvalidRequestException {
        if (connectionProp == null) {
            throw new InvalidRequestException("Null connection prop passed.");
        }

        this.connectionProp = connectionProp;
    }

    public ReactorClientHttpConnector getReactorClientHttpConnector() throws SSLException {
        SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        HttpClient httpClient = HttpClient.create();
        if (!connectionProp.shouldVerifySSLCertificate()) {
            httpClient = httpClient.secure((t) -> t.sslContext(sslContext));
        }

        httpClient = httpClient.tcpConfiguration((client) -> {
            client = client.doOnConnected((connection) -> connection.addHandlerLast(new ReadTimeoutHandler(connectionProp.getReadTimeoutSecs()))
                    .addHandlerLast(new WriteTimeoutHandler(connectionProp.getWriteTimeoutSecs())));
            if (connectionProp.isProxyEnabled()) {
                client = client.proxy((proxyOptions) -> {
                    ProxyProvider.Builder builder = proxyOptions.type(ProxyProvider.Proxy.HTTP)
                            .host(connectionProp.getProxyIp())
                            .port(connectionProp.getProxyPort());
                    String username = connectionProp.getProxyUsername();
                    if (username != null && !username.isEmpty()) {
                        builder.username(username);
                    }

                    String password = connectionProp.getproxyPassword();
                    if (password != null && !password.isEmpty()) {
                        builder.password(u -> password);
                    }
                });
            }
            return client;
        });
        return new ReactorClientHttpConnector(httpClient);
    }
}
