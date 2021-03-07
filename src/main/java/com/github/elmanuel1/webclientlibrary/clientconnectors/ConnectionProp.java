package com.github.elmanuel1.webclientlibrary.clientconnectors;

import reactor.util.annotation.NonNull;

public class ConnectionProp {

    private boolean proxyEnabled = false;

    private String proxyIp;
    private int proxyPort = 0;
    private boolean verifySSLCertificate = true;
    private int connectionTimeoutSecs = 30;
    private int readTimeoutSecs = 30;
    private int writeTimeoutSecs = 30;
    private String proxyUsername;
    private String proxyPassword;

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public void useProxy(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    public String getProxyIp() {
        return proxyIp;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public boolean shouldVerifySSLCertificate() {
        return verifySSLCertificate;
    }

    public void verifySSLCertificate(boolean verifySSLCertificate) {
        this.verifySSLCertificate = verifySSLCertificate;
    }

    public int getConnectionTimeoutSecs() {
        return connectionTimeoutSecs;
    }

    public void setConnectionTimeoutSecs(int connectionTimeoutSecs) {
        this.connectionTimeoutSecs = connectionTimeoutSecs;
    }

    public int getReadTimeoutSecs() {
        return readTimeoutSecs;
    }

    public void setReadTimeoutSecs(int readTimeoutSecs) {
        this.readTimeoutSecs = readTimeoutSecs;
    }

    public int getWriteTimeoutSecs() {
        return writeTimeoutSecs;
    }

    public void setWriteTimeoutSecs(int writeTimeoutSecs) {
        this.writeTimeoutSecs = writeTimeoutSecs;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getproxyPassword() {
        return proxyPassword;
    }

    public void setproxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }
}