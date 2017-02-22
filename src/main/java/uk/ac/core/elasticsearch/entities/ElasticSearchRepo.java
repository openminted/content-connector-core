package uk.ac.core.elasticsearch.entities;

//import org.springframework.data.elasticsearch.annotations.Document;
/**
 *
 * @author la4227 <lucas.anastasiou@open.ac.uk>
 */
//@Document(indexName = "repositories", type = "repository", useServerConfiguration = true)
public class ElasticSearchRepo {

    boolean Disabled;
    long OpenDoarId;
    long crawlingLimit;
    boolean excludeSameDomainPolicy;
    String harvestLevel;
    long id;
    String name;
    String physicalName;

    ElasticSearchRepositoryLocation repositoryLocation;

    String selectedAlgorithm;
    String source;
    String uri;

    public boolean isDisabled() {
        return Disabled;
    }

    public void setDisabled(boolean Disabled) {
        this.Disabled = Disabled;
    }

    public long getOpenDoarId() {
        return OpenDoarId;
    }

    public void setOpenDoarId(long OpenDoarId) {
        this.OpenDoarId = OpenDoarId;
    }

    public long getCrawlingLimit() {
        return crawlingLimit;
    }

    public void setCrawlingLimit(long crawlingLimit) {
        this.crawlingLimit = crawlingLimit;
    }

    public boolean isExcludeSameDomainPolicy() {
        return excludeSameDomainPolicy;
    }

    public void setExcludeSameDomainPolicy(boolean excludeSameDomainPolicy) {
        this.excludeSameDomainPolicy = excludeSameDomainPolicy;
    }

    public String getHarvestLevel() {
        return harvestLevel;
    }

    public void setHarvestLevel(String harvestLevel) {
        this.harvestLevel = harvestLevel;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhysicalName() {
        return physicalName;
    }

    public void setPhysicalName(String physicalName) {
        this.physicalName = physicalName;
    }

    public ElasticSearchRepositoryLocation getRepositoryLocation() {
        return repositoryLocation;
    }

    public void setRepositoryLocation(ElasticSearchRepositoryLocation repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
    }

    public String getSelectedAlgorithm() {
        return selectedAlgorithm;
    }

    public void setSelectedAlgorithm(String selectedAlgorithm) {
        this.selectedAlgorithm = selectedAlgorithm;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
    
    
    
}
