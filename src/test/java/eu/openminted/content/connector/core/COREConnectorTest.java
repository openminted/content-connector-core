package eu.openminted.content.connector.core;

import eu.openminted.content.connector.ContentConnector;
import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.SearchResult;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import eu.openminted.registry.core.domain.Facet;
import eu.openminted.registry.core.domain.Value;
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
@ContextConfiguration(classes = {COREConnectorConfiguration.class})
public class COREConnectorTest {

    @Autowired
    ContentConnector contentConnector;

    Query query;

    @Before
    public void setUp() {
        // The way this test is implemented it supposes all of the following parameters enabled.
        // To alter the query by a parameter or field or facet
        // feel free to comment or add anything

        query = new Query();
        query.setFrom(0);
        query.setTo(1);
        query.setKeyword("");
        query.setParams(new HashMap<>());
        query.getParams().put("sort", new ArrayList<>());
        query.getParams().get("sort").add("__indexrecordidentifier asc");
        query.getParams().put("licence", new ArrayList<>());
        query.getParams().get("licence").add("Open Access");

    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @After
    public void tearDown() {
        query = null;
    }


    @Test
    @Ignore
    public void searchFacets() throws Exception {
        query.getFacets().add("rightsstmtname");
        query.getFacets().add("documentlanguage");
        query.getFacets().add("documenttype");
        query.getFacets().add("publicationtype");
        query.getFacets().add("publicationyear");

        SearchResult searchResult = contentConnector.search(query);

        // Assert that result is not null and also that the resulting facets are not null
        // and the resulting publication exists and it is equal to the required number
        assert searchResult != null;
        assert searchResult.getFacets() != null;
        assert searchResult.getPublications() != null && searchResult.getPublications().size() == query.getTo();

        // Assert that the number of facets returned is equal to the number of facets required
        assert searchResult.getFacets().size() == query.getFacets().size();

        int totalResults = 0;
        for (Facet facet : searchResult.getFacets()) {

            // For each facet the sum of the values returned should be equal to the total number of hits
            for (Value value : facet.getValues()) {
                totalResults += value.getCount();
            }

            System.out.println("For facet " + facet.getField() + " totalResults: " + totalResults + " of " + searchResult.getTotalHits());
            assert totalResults == searchResult.getTotalHits();

            totalResults = 0;
        }
    }

    /**
     * Test of search method, of class COREConnector.
     */
    @Test
    @Ignore
    public void testSearch() {
        System.out.println("search");
        SearchResult searchResult = contentConnector.search(query);
        assert searchResult != null;

        System.out.println(searchResult.getPublications());

        /*
        COREConnector instance = new COREConnector();
        SearchResult expResult = null;
        SearchResult result = instance.search(query);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        */
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
        query.getParams().put("publicationyear", new ArrayList<>());
        query.getParams().put("documentlanguage", new ArrayList<>());
        query.getParams().put("publicationtype", new ArrayList<>());

        query.getParams().get("publicationyear").add("2010");
        query.getParams().get("documentlanguage").add("en");
        query.getParams().get("publicationtype").add("research");

        query.getFacets().add("rightsstmtname");
        query.getFacets().add("documentlanguage");
        query.getFacets().add("documenttype");
        query.getFacets().add("publicationtype");
        query.getFacets().add("publicationyear");

        query.setKeyword("digital");
        SearchResult searchResult = contentConnector.search(query);

        if (searchResult.getPublications() != null) {
            for (String metadataRecord : searchResult.getPublications()) {
                System.out.println(metadataRecord);
            }

            for (Facet facet : searchResult.getFacets()) {
                int totalHits = 0;
                System.out.println("facet:{" + facet.getLabel() + "[");
                for (Value value : facet.getValues()) {
                    System.out.println("\t{" + value.getValue() + ":" + value.getCount() + "}");
                    totalHits += value.getCount();
                }
                System.out.println("]}");
                assert totalHits == searchResult.getTotalHits();
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
