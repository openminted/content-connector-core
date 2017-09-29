package eu.openminted.content.connector.core.mappings;

import eu.openminted.content.connector.utils.faceting.OMTDFacetEnum;

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
        OMTD_TO_ES_PARAMETER_NAMES.put(OMTDFacetEnum.DOCUMENT_LANG.value(), "language.code");
        OMTD_TO_ES_PARAMETER_NAMES.put(OMTDFacetEnum.PUBLICATION_YEAR.value(), "year");
        OMTD_TO_ES_PARAMETER_NAMES.put(OMTDFacetEnum.PUBLICATION_TYPE.value(), "documentType");
//        omtdToEsParameterNames.put("","");

    }

    public static final Map<String, String> OMTD_TO_ES_FACETS_NAMES;

    static {
        OMTD_TO_ES_FACETS_NAMES = new HashMap<>();
        OMTD_TO_ES_FACETS_NAMES.put(OMTDFacetEnum.DOCUMENT_LANG.value(), "language.code");
        OMTD_TO_ES_FACETS_NAMES.put(OMTDFacetEnum.PUBLICATION_YEAR.value(), "year");
        OMTD_TO_ES_FACETS_NAMES.put(OMTDFacetEnum.RIGHTS_STMT_NAME.value(), "licence");
        OMTD_TO_ES_FACETS_NAMES.put(OMTDFacetEnum.PUBLICATION_TYPE.value(), "documentType");
        OMTD_TO_ES_FACETS_NAMES.put(OMTDFacetEnum.PUBLISHER.value(), "publisher");
//        OMTD_TO_ES_FACETS_NAMES.put("keyword", "?");
//        OMTD_TO_ES_FACETS_NAMES.put("source", "?");
        //PUBLICATION_TYPE, PUBLICATION_YEAR, PUBLISHER, RIGHTS_STMT_NAME, RIGHTS, DOCUMENT_LANG, DOCUMENT_TYPE, KEYWORD, SOURCE 
    }

}
