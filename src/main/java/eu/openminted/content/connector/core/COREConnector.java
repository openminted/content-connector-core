package eu.openminted.content.connector.core;

import eu.openminted.content.connector.ContentConnector;
import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.SearchResult;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of the ContentConnector interface for the CORE
 *
 * @author lucasanastasiou
 */
@Component
public class COREConnector implements ContentConnector {

    public COREConnector(CORESearchService cORESearchService) {
        this.cORESearchService = cORESearchService;
    }    
    
    @Autowired
    private CORESearchService cORESearchService;

    

    /**
     * Search method for browsing metadata
     *
     * @param query the query as inserted in content connector service
     *
     * @return SearchResult with metadata and facets
     */
    @Override
    public SearchResult search(Query query) throws IOException {
        return cORESearchService.query(query);
    }

    /**
     * Method for downloading fullText linked documents (pdf, xml etc)
     *
     * @param string the ID of the metadata
     * @return the document in the form of InputStream
     */
    @Override
    public InputStream downloadFullText(String string) {
        try {
            return cORESearchService.fetchByIdentifier(string);
        } catch (Exception ex) {
            Logger.getLogger(COREConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Method for downloading metadata where the query's criteria are applicable
     *
     * @param query the query as inserted in content connector service
     * @return The metadata in the form of InputStream
     */
    @Override
    public InputStream fetchMetadata(Query query) {
        try {
            return cORESearchService.fetchBigResultSet(query);
        } catch (IOException ex){
            Logger.getLogger(COREConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Method that returns the name of the connector
     *
     * @return fixed String CORE
     */
    @Override
    public String getSourceName() {
        return "CORE";
    }

}
