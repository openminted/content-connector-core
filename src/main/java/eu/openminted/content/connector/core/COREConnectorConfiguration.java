package eu.openminted.content.connector.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author lucasanastasiou
 */
@Configuration
@ComponentScan("eu.openminted.content.connector")
//@PropertySource(value = {"classpath:application.properties"})
public class COREConnectorConfiguration {

    @Value("${content.limit:500}")
    public Integer CONTENT_LIMIT;
    
}
