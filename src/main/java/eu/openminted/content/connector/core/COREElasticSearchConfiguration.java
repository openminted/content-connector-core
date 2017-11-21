package eu.openminted.content.connector.core;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author lucasanastasiou
 */
@Configuration
@ComponentScan("eu.openminted.content")
public class COREElasticSearchConfiguration {

    @Value("${es.rest.endpoint:'localhost:9200'}")
    String endpoint;

    @Bean
    JestClient client() {

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(endpoint)
                .readTimeout(60000)
                .multiThreaded(true)
                .build());
        JestClient jestClient = factory.getObject();
        return jestClient;
    }
}
