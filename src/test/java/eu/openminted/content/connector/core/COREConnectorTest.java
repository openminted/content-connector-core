package eu.openminted.content.connector.core;

import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.SearchResult;
import java.io.InputStream;

import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 * @author lucasanastasiou
 */
public class COREConnectorTest {
    
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
    
}
