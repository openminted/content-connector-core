package uk.ac.core.elasticsearch.entities;

import java.util.Date;

/**
 *
 * @author la4227 <lucas.anastasiou@open.ac.uk>
 */
public class ElasticSearchRepositoryDocument {

    private Integer pdfStatus;
    private Integer textStatus;
    private long metadataUpdated;
    private Date timestamp;
    private Integer indexed;
    private String deletedStatus;
    private Long pdfSize;

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

    public long getMetadataUpdated() {
        return metadataUpdated;
    }

    public void setMetadataUpdated(long metadataUpdated) {
        this.metadataUpdated = metadataUpdated;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
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

}
