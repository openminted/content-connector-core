package eu.openminted.content.connector.core;

import eu.openminted.content.connector.ContentConnector;
import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.SearchResult;
import eu.openminted.registry.core.domain.Facet;
import eu.openminted.registry.core.domain.Value;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @author lucasanastasiou
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {COREConnectorConfiguration.class})
public class COREConnectorTest {

    @Autowired
    private ContentConnector contentConnector;

    private Query query;

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
    public void searchFacets() {
        query.getFacets().add("rightsstmtname");
        query.getFacets().add("documentlanguage");
        query.getFacets().add("documenttype");
        query.getFacets().add("publicationtype");
        query.getFacets().add("publicationyear");

        SearchResult searchResult = contentConnector.search(query);

        // Assert that result is not null and also that the resulting facets are not null
        // and the resulting publication exists and it is equal to the required number
        assertNotEquals(null, searchResult);
        assertNotEquals(null, searchResult.getFacets());
        assertNotEquals(null, searchResult.getPublications());
        assertEquals(query.getTo(), searchResult.getPublications().size());

        // Assert that the number of facets returned is equal to the number of facets required
        assertEquals(searchResult.getFacets().size(), query.getFacets().size());

        int totalResults = 0;
        for (Facet facet : searchResult.getFacets()) {
            // For each facet the sum of the values returned should be equal to the total number of hits
            for (Value value : facet.getValues()) {
                totalResults += value.getCount();
            }
            assertEquals(searchResult.getTotalHits(), totalResults);
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
        assertNotEquals(null, searchResult);
        assertNotEquals(null, searchResult.getPublications());
        assertNotEquals(0, searchResult.getPublications().size());
    }

    /**
     * Test of downloadFullText method, of class COREConnector.
     */
    @Test
    @Ignore
    public void testDownloadFullText() {
        System.out.println("downloadFullText");
        String string = "oai:real-j.mtak.hu:6339";
        InputStream result = contentConnector.downloadFullText(string);

        assertNotEquals(null, result);

//      In order to download the pdf itself, uncomment the following lines

//        FileOutputStream fileOutputStream = null;
//        try {
//            fileOutputStream = new FileOutputStream(new File("downloaded.pdf"));
//            IOUtils.copy(result, fileOutputStream);
//            fileOutputStream.close();
//            result.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Test of fetchMetadata method, of class COREConnector.
     */
    @Test
    @Ignore
    public void testFetchMetadata() {
        System.out.println("fetchMetadata");
        InputStream result = contentConnector.fetchMetadata(query);
        assertTrue(result != null);

//      In order to download the pdf itself, uncomment the following lines

//        FileOutputStream fileOutputStream = new FileOutputStream(new File("metadata.xml"));
//        IOUtils.copy(result, fileOutputStream);
//        fileOutputStream.close();
//        result.close();
    }

    /**
     * Test of getSourceName method, of class COREConnector.
     */
    @Test
    @Ignore
    public void testGetSourceName() {
        System.out.println("getSourceName");
        String expResult = "CORE";
        String result = contentConnector.getSourceName();
        assertEquals(expResult, result);

        expResult = "";
        assertFalse(expResult.equals(result));
    }

    /**
     * Test that a valid query response
     */
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
        assertNotEquals(null, searchResult.getPublications());

        for (Facet facet : searchResult.getFacets()) {
            int totalHits = 0;
            for (Value value : facet.getValues()) {
                totalHits += value.getCount();
            }
            assertEquals(searchResult.getTotalHits(), totalHits);
        }
    }
}
