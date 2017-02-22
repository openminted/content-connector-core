package eu.openminted.content.connector.core.app;

import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.SearchResult;
import eu.openminted.content.connector.core.CORESearchService;
import io.searchbox.client.JestClient;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author lucasanastasiou
 */
@SpringBootApplication
@ComponentScan(basePackages = {"eu.openminted.content.connector.core"})
public class TestApp implements CommandLineRunner {

    @Autowired
    CORESearchService cORESearchService;

    @Autowired
    JestClient jestClient;
    public static void main(String args[]) {
        SpringApplication.run(TestApp.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {

        Query omtdQuery = new Query();
        omtdQuery.setFrom(0);
        omtdQuery.setTo(10);
        omtdQuery.setKeyword("avandi lucass gogogo");
        List<String> qFacets = new ArrayList<>();
        qFacets.add("authors");
        qFacets.add("year");
        omtdQuery.setFacets(qFacets);
        
        System.out.println("jestClient = " + jestClient.toString());
        
        SearchResult reuslt = cORESearchService.query(omtdQuery);
        System.out.println("reuslt = " + reuslt);
    }
}
