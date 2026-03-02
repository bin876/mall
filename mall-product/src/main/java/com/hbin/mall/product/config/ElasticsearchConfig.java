// ElasticsearchConfig.java
package com.hbin.mall.product.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Override
    public ClientConfiguration clientConfiguration() {
        try {
            SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial((X509Certificate[] chain, String authType) -> true)
                .build();

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "+GNGCsB667UzepZMdeus")
            );

            return ClientConfiguration.builder()
                .connectedTo("192.168.72.130:9200")
                .usingSsl(sslContext)
                .withBasicAuth("elastic", "+GNGCsB667UzepZMdeus")
                .build();
                
        } catch (Exception e) {
            throw new RuntimeException("创建 Elasticsearch 配置失败", e);
        }
    }
}