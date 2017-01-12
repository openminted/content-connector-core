package uk.ac.core.elasticsearch.entities;

//import org.springframework.data.elasticsearch.annotations.Field;
//import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 *
 * @author la4227 <lucas.anastasiou@open.ac.uk>
 */
public class ElasticSearchLanguage {

//    @Field(type = FieldType.String)
    private String code;
//    @Field(type = FieldType.Long)
    private Integer id;
//    @Field(type = FieldType.String)
    private String name;

    public ElasticSearchLanguage() {

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
