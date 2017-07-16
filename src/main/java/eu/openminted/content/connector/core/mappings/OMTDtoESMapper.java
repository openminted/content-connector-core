package eu.openminted.content.connector.core.mappings;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lucasanastasiou
 */
public class OMTDtoESMapper {

    public static final Map<String, String> OMTD_TO_ES_PARAMETER_NAMES;

    static {
        OMTD_TO_ES_PARAMETER_NAMES = new HashMap<>();
        OMTD_TO_ES_PARAMETER_NAMES.put("documentLanguage", "language.name");
        OMTD_TO_ES_PARAMETER_NAMES.put("publicationYear", "year");
        OMTD_TO_ES_PARAMETER_NAMES.put("publicationType", "documentType");
        OMTD_TO_ES_PARAMETER_NAMES.put("licence", "licence");
//        omtdToEsParameterNames.put("","");

    }

    public static final Map<String, String> OMTD_TO_ES_FACETS_NAMES;

    static {
        OMTD_TO_ES_FACETS_NAMES = new HashMap<>();
        OMTD_TO_ES_FACETS_NAMES.put("documentlanguage", "language.name");
        OMTD_TO_ES_FACETS_NAMES.put("publicationyear", "year");
        OMTD_TO_ES_FACETS_NAMES.put("rightsstmtname", "licence");
        OMTD_TO_ES_FACETS_NAMES.put("authors", "authors.raw");
        OMTD_TO_ES_FACETS_NAMES.put("journals", "journals.title.raw");
        OMTD_TO_ES_FACETS_NAMES.put("publicationtype", "documentType");
        OMTD_TO_ES_FACETS_NAMES.put("publisher", "publisher");
        OMTD_TO_ES_FACETS_NAMES.put("rights", "licence");
        OMTD_TO_ES_FACETS_NAMES.put("documenttype", "documentType");
//        OMTD_TO_ES_FACETS_NAMES.put("keyword", "?");
//        OMTD_TO_ES_FACETS_NAMES.put("source", "?");
        //PUBLICATION_TYPE, PUBLICATION_YEAR, PUBLISHER, RIGHTS_STMT_NAME, RIGHTS, DOCUMENT_LANG, DOCUMENT_TYPE, KEYWORD, SOURCE 
    }

}
