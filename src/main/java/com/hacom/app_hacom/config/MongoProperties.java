package com.hacom.app_hacom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mongodb")
public class MongoProperties {
    private String host;
    private Integer port;
    private String database;
    private String username;
    private String password;
    private String authDatabase;
    private boolean srv;

    // getters y setters
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAuthDatabase() { return authDatabase; }
    public void setAuthDatabase(String authDatabase) { this.authDatabase = authDatabase; }
    public boolean isSrv() { return srv; }
    public void setSrv(boolean srv) { this.srv = srv; }
}
