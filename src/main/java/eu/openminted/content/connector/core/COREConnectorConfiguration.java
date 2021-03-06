package eu.openminted.content.connector.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author lucasanastasiou
 */
@Configuration
@ComponentScan(basePackages = {"eu.openminted.content.connector.core"})
@PropertySource(value = {"classpath:application.properties"})
public class COREConnectorConfiguration {

    @Value("${content.limit:250}")
    public Integer CONTENT_LIMIT;
    
}
