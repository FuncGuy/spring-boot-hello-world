package com.example.helloworld.config;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public RemoteCacheManager getRemoteCacheManager() {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.transactionTimeout(1, TimeUnit.MINUTES);

        configurationBuilder
        .addServer()
        .host("d")
        .port(11222)
        .security()
        .ssl()
        .sniHostName("infinispan")
        .trustStoreFileName("./cacerts 2")
        .trustStorePassword("changeit".toCharArray())
        .authentication()
        .username("d")
        .password("d/kCKlH5daHZlAqw==")
         // Hot Rod clients use intelligence mechanisms to efficiently send requests to Infinispan Server clusters
        .clientIntelligence(ClientIntelligence.BASIC);
        return new RemoteCacheManager(configurationBuilder.build());
    }

}
