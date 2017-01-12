package eu.openminted.content.connector.core;

import eu.openminted.content.connector.ContentConnector;
import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.SearchResult;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author lucasanastasiou
 */
public class COREConnector implements ContentConnector {

    @Autowired
    CORESearchService cORESearchService;
    
    @Override
    public SearchResult search(Query query) {
        return cORESearchService.query(query);
    }

    @Override
    public InputStream downloadFullText(String string) {
        try {        
            return cORESearchService.fetchByIdentifier(string);
        } catch (IOException ex) {
            Logger.getLogger(COREConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public InputStream fetchMetadata(Query query) {
        try {
            return cORESearchService.fetchBigResultSet(query);
        } catch (IOException ex){
            Logger.getLogger(COREConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String getSourceName() {
        return "CORE";
    }

}
