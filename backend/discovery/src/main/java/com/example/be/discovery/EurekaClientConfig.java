package com.example.be.discovery;

import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EurekaClientConfig {

    @Bean
    public AbstractDiscoveryClientOptionalArgs<?> discoveryClientOptionalArgs() {
        return new AbstractDiscoveryClientOptionalArgs<Object>() {};
    }
}