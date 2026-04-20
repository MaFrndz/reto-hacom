package com.hacom.app_hacom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Value("${apiPort}")
    private int apiPort;

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        factory.setPort(apiPort);
    }
}

