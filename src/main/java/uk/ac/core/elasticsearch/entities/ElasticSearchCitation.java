package uk.ac.core.elasticsearch.entities;

import java.util.List;

/**
 *
 * @author la4227 <lucas.anastasiou@open.ac.uk>
 */
public class ElasticSearchCitation {

    private Integer id;
    
    private String title;
    
    private List<String> authors;
    
    private String date;
    
    private String doi;
    
    private String raw;
    
    private List<Integer> cites;

    public ElasticSearchCitation() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public List<Integer> getCites() {
        return cites;
    }

    public void setCites(List<Integer> cites) {
        this.cites = cites;
    }

}
