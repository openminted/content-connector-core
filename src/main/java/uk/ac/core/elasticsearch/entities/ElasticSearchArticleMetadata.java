package uk.ac.core.elasticsearch.entities;

import java.util.List;


/**
 *
 * @author la4227 <lucas.anastasiou@open.ac.uk>
 */
//@Document(indexName = "#{@indexName}", type = "article", useServerConfiguration = true)
public class ElasticSearchArticleMetadata {
    
//    @Id
    private String id;

    private List<String> authors;

    private List<ElasticSearchCitation> citations;

    private List<String> contributors;

    private String datePublished;

    private String deleted;

    private String description;

    private String fullText;

    private String fullTextIdentifier;

    private List<String> identifiers;//doi,..,core-id,..

    private List<ElasticSearchJournal> journals;

    private ElasticSearchLanguage language;
    //it's a unique ID to identify documents with duplicates, all documents with the same duplicateId can be considered duplicate
    private String duplicateId;

    private String publisher;

    private String rawRecordXml;

    private List<String> relations;

    private List<ElasticSearchRepo> repositories;

    private ElasticSearchRepositoryDocument repositoryDocument;

    private List<ElasticSearchSimilarDocument> similarities;

    private List<String> subjects;

    private String title;

    private List<String> topics;

    private List<String> types;
    
    private List<String> urls;

    private Integer year;
    
    private String doi;

    private String oai;
    
    private String downloadUrl;
    
    private String pdfHashValue;
    
    private String documentType;
    
    private Double documentTypeConfidence;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<ElasticSearchCitation> getCitations() {
        return citations;
    }

    public void setCitations(List<ElasticSearchCitation> citations) {
        this.citations = citations;
    }

    public List<String> getContributors() {
        return contributors;
    }

    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }

    public String getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers;
    }

    public List<ElasticSearchJournal> getJournals() {
        return journals;
    }

    public void setJournals(List<ElasticSearchJournal> journals) {
        this.journals = journals;
    }

    public ElasticSearchLanguage getLanguage() {
        return language;
    }

    public void setLanguage(ElasticSearchLanguage language) {
        this.language = language;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getRawRecordXml() {
        return rawRecordXml;
    }

    public void setRawRecordXml(String rawRecordXml) {
        this.rawRecordXml = rawRecordXml;
    }

    public List<String> getRelations() {
        return relations;
    }

    public void setRelations(List<String> relations) {
        this.relations = relations;
    }

    public List<ElasticSearchRepo> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<ElasticSearchRepo> repositories) {
        this.repositories = repositories;
    }

    public ElasticSearchRepositoryDocument getRepositoryDocument() {
        return repositoryDocument;
    }

    public void setRepositoryDocument(ElasticSearchRepositoryDocument repositoryDocument) {
        this.repositoryDocument = repositoryDocument;
    }

    /**
     * gets the URL of the original fulltext PDF
     * @return
     */
    public String getFullTextIdentifier() {
        return fullTextIdentifier;
    }

    /**
     * Sets the URL to the original fulltext PDF
     * @param fullTextIdentifier
     */
    public void setFullTextIdentifier(String fullTextIdentifier) {
        this.fullTextIdentifier = fullTextIdentifier;
    }

    public String getDuplicateId() {
        return duplicateId;
    }

    public void setDuplicateId(String duplicateId) {
        this.duplicateId = duplicateId;
    }

    public List<ElasticSearchSimilarDocument> getSimilarities() {
        return similarities;
    }

    public void setSimilarities(List<ElasticSearchSimilarDocument> similarities) {
        this.similarities = similarities;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }   
    
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
    
    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getOai() {
        return oai;
    }

    public void setOai(String oai) {
        this.oai = oai;
    }    

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getPdfHashValue() {
        return pdfHashValue;
    }

    public void setPdfHashValue(String pdfHashValue) {
        this.pdfHashValue = pdfHashValue;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Double getDocumentTypeConfidence() {
        return documentTypeConfidence;
    }

    public void setDocumentTypeConfidence(Double documentTypeConfidence) {
        this.documentTypeConfidence = documentTypeConfidence;
    }

    
    @Override
    public String toString() {
        return "ElasticSearchArticleMetadata{" + "id=" + id + ", authors=" + authors + ", citations=" + citations + ", contributors=" + contributors + ", datePublished=" + datePublished + ", deleted=" + deleted + ", description=" + description + ", fullText=" + fullText + ", fullTextIdentifier=" + fullTextIdentifier + ", identifiers=" + identifiers + ", journals=" + journals + ", language=" + language + ", duplicateId=" + duplicateId + ", publisher=" + publisher + ", rawRecordXml=" + rawRecordXml + ", relations=" + relations + ", repositories=" + repositories + ", repositoryDocument=" + repositoryDocument + ", similarities=" + similarities + ", subjects=" + subjects + ", title=" + title + ", topics=" + topics + ", types=" + types + ", urls=" + urls + ", year=" + year + ", doi=" + doi + ", oai=" + oai + '}';
    }
    
    
}
