package org.smartboot.http.graal.conf;

import java.util.List;

public class Config {
    private String host;
    private int port;
    private List<Route> routes;
    private List<BackendProxy> servers;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public List<BackendProxy> getServers() {
        return servers;
    }

    public void setServers(List<BackendProxy> servers) {
        this.servers = servers;
    }
}
