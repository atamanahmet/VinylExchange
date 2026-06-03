package com.atamanahmet.vinylexchange.infrastructure.search.config;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;

import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchClientConfig {

        @Value("${opensearch.host}")
        private String host;

        @Value("${opensearch.port}")
        private int port;

        @Value("${opensearch.username}")
        private String username;

        @Value("${opensearch.password}")
        private String password;

        @Bean
        public RestHighLevelClient openSearchClient() throws Exception {

                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

                credentialsProvider.setCredentials(AuthScope.ANY,
                                new UsernamePasswordCredentials(username, password));

                SSLContext sslContext = SSLContextBuilder.create()
                                .loadTrustMaterial(null, (chains, authType) -> true)
                                .build();

                RestClientBuilder builder = RestClient
                        .builder(new HttpHost(host, port, "https"))
                        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider)
                                .setSSLContext(sslContext)

                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE));

                return new RestHighLevelClient(builder);
        }
}
