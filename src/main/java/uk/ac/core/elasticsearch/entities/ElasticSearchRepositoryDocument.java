package uk.ac.core.elasticsearch.entities;


/**
 *
 * @author la4227 <lucas.anastasiou@open.ac.uk>
 */
public class ElasticSearchRepositoryDocument {

    private Integer pdfStatus;
    private Integer textStatus;
    private Long metadataUpdated;
    private Long timestamp;
    private Integer indexed;
    private String deletedStatus;
    private Long pdfSize;
    private Boolean tdmOnly;

    public Integer getPdfStatus() {
        return pdfStatus;
    }

    public void setPdfStatus(Integer pdfStatus) {
        this.pdfStatus = pdfStatus;
    }

    public Integer getTextStatus() {
        return textStatus;
    }

    public void setTextStatus(Integer textStatus) {
        this.textStatus = textStatus;
    }

    public Long getMetadataUpdated() {
        return metadataUpdated;
    }

    public void setMetadataUpdated(Long metadataUpdated) {
        this.metadataUpdated = metadataUpdated;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getIndexed() {
        return indexed;
    }

    public void setIndexed(Integer indexed) {
        this.indexed = indexed;
    }

    public String getDeletedStatus() {
        return deletedStatus;
    }

    public void setDeletedStatus(String deletedStatus) {
        this.deletedStatus = deletedStatus;
    }

    public Long getPdfSize() {
        return pdfSize;
    }

    public void setPdfSize(Long pdfSize) {
        this.pdfSize = pdfSize;
    }

    public Boolean isTdmOnly() {
        return tdmOnly;
    }

    public void setTdmOnly(Boolean tdmOnly) {
        this.tdmOnly = tdmOnly;
    }

}
