package eu.openminted.content.connector.core;

import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.SearchResult;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.openminted.registry.domain.Facet;
import eu.openminted.registry.domain.Value;
import io.searchbox.client.JestClient;
import org.apache.lucene.util.automaton.LimitedFiniteStringsIterator;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 *
 * @author lucasanastasiou
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {COREElasticSearchConfiguration.class})
public class COREConnectorTest {

    @Autowired
    COREConnector cOREConnector;

    public COREConnectorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of search method, of class COREConnector.
     */
    @Test
    @Ignore
    public void testSearch() {
        System.out.println("search");
        Query query = null;
        COREConnector instance = new COREConnector();
        SearchResult expResult = null;
        SearchResult result = instance.search(query);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of downloadFullText method, of class COREConnector.
     */
    @Test
    @Ignore
    public void testDownloadFullText() {
        System.out.println("downloadFullText");
        String string = "";
        COREConnector instance = new COREConnector();
        InputStream expResult = null;
        InputStream result = instance.downloadFullText(string);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fetchMetadata method, of class COREConnector.
     */
    @Test
    @Ignore
    public void testFetchMetadata() {
        System.out.println("fetchMetadata");
        Query query = null;
        COREConnector instance = new COREConnector();
        InputStream expResult = null;
        InputStream result = instance.fetchMetadata(query);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSourceName method, of class COREConnector.
     */
    @Test
    @Ignore
    public void testGetSourceName() {
        System.out.println("getSourceName");
        COREConnector instance = new COREConnector();
        String expResult = "";
        String result = instance.getSourceName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testGetParameters() {
        Query query = new Query("", new HashMap<>(), new ArrayList<>(), 0, 1);
        query.getParams().put("licence", new ArrayList<>());
        query.getParams().get("licence").add("Open Access");
        query.getParams().put("publicationYear", new ArrayList<>());
        query.getParams().get("publicationYear").add("2010");

        query.getFacets().add("Licence");
        query.getFacets().add("DocumentLanguage");
        query.getFacets().add("PublicationType");
        query.getFacets().add("publicationYear");

        query.setKeyword("*");
        SearchResult searchResult = cOREConnector.search(query);

        if (searchResult.getPublications() != null) {
            for (String metadataRecord : searchResult.getPublications()) {
                System.out.println(metadataRecord);
            }

            for (Facet facet : searchResult.getFacets()) {
                System.out.println("facet:{" + facet.getLabel() + "[");
                for (Value value : facet.getValues()) {
                    System.out.println("\t{" + value.getValue() + ":" + value.getCount() + "}");
                }
                System.out.println("]}");
            }
            System.out.println("reading " + searchResult.getPublications().size() +
                    " publications from " + searchResult.getFrom() +
                    " to " + searchResult.getTo() +
                    " out of " + searchResult.getTotalHits() + " total hits.");
        } else {
            System.out.println("Could not find any result with these parameters or keyword");
        }
    }
}
