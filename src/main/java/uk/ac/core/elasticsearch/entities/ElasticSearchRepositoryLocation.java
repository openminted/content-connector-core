package uk.ac.core.elasticsearch.entities;

/**
 *
 * @author lucasanastasiou
 */
public class ElasticSearchRepositoryLocation {

    String countryCode;
    long id_repository;
    String latitude;
    String longitude;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public long getId_repository() {
        return id_repository;
    }

    public void setId_repository(long id_repository) {
        this.id_repository = id_repository;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    
    
}
