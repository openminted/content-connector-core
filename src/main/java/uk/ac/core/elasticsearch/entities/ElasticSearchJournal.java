package uk.ac.core.elasticsearch.entities;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author la4227 <lucas.anastasiou@open.ac.uk>
 */
public class ElasticSearchJournal {

    private String title;
    
    private List<String> identifiers;

    public ElasticSearchJournal() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers;
    }
    
    public void addIdentifier(String id){
        if (this.identifiers==null){
            this.identifiers=new ArrayList<>();
        }
        if (!this.identifiers.contains(id)){
            this.identifiers.add(id);
        }
    }

}
