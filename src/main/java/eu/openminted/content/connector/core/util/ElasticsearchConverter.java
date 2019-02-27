package eu.openminted.content.connector.core.util;

import com.google.gson.*;
import eu.openminted.content.connector.ContentConnector;
import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.core.mappings.OMTDtoESMapper;
import eu.openminted.content.connector.utils.faceting.OMTDFacetEnum;
import eu.openminted.content.connector.utils.language.LanguageUtils;
import eu.openminted.registry.core.domain.Facet;
import eu.openminted.registry.core.domain.Value;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.core.elasticsearch.entities.ElasticSearchArticleMetadata;

import java.util.*;

/**
 * Class responsible for querying CORE's elasticsearch and get results
 *
 * @author lucasanastasiou
 */
@Service
public class ElasticsearchConverter {

    private static Logger log = Logger.getLogger(ElasticsearchConverter.class);

    @Autowired
    private LanguageUtils languageUtils;

    public static List<String> DEFAULT_FACETS = Arrays.asList(
            OMTDFacetEnum.DOCUMENT_LANG.value(),
            OMTDFacetEnum.PUBLICATION_YEAR.value(),
            OMTDFacetEnum.PUBLICATION_TYPE.value()
    );

    public static String constructElasticsearchScanAndScrollQueryFromOmtdQuery(Query query) {
        String keyword = query.getKeyword();
        if (keyword == null) {
            keyword = "";
        }

        String escapedKeyword = org.apache.lucene.queryparser.flexible.standard.QueryParserUtil.escape(keyword);

        String esQuery = "{\n"
                + "        \"query_string\": {\n"
                + "           \"query\": \"" + keyword + "\"\n"
                + "        }\n"
                + "    }";
        return keyword;
    }

    /**
     * Constructs a query for elastic search
     *
     * @param query the openminted query
     * @return String with the query for elastic search
     */
    public String constructElasticsearchQueryFromOmtdQuery(Query query) {
        return constructElasticsearchQueryFromOmtdQuery(query, true);
    }

    public String constructElasticsearchQueryFromOmtdQuery(Query query, boolean withFromParameter) {
        int from = query.getFrom();
        int to = query.getTo();
        String keyword = query.getKeyword();
        String queryComponent = "";

        //
        // Query - either a keyword query or match all
        //
        queryComponent += "{\n"
                + "         \"bool\":{\n"
                + "            \"must\":[\n";
        if (keyword == null || keyword.isEmpty() || keyword.equals("*")) {
            String matchAllQueryComponent = "{\"match_all\": {}}";
            queryComponent += matchAllQueryComponent;
        } else {
            String escapedKeyword = QueryParserUtil.escape(keyword);
            String keywordQueryComponent = "{\n"
                    + "                       \"query_string\": {\n"
                    + "                             \"query\": \"" + escapedKeyword + "\"\n"
                    + "                         }\n"
                    + "                    }\n";
            queryComponent += keywordQueryComponent;
        }

        //
        // Parameters
        //
        Map<String, List<String>> params = query.getParams();

        // parameters in ES query are part of a boolean filter in a filtered query
        StringBuilder paramsString = new StringBuilder();
        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                String esParameterName = OMTDtoESMapper.OMTD_TO_ES_PARAMETER_NAMES.get(key);

                if (esParameterName == null || esParameterName.isEmpty()) {
                    // a non-existent parameter in the omtd<->ES map was given, skip
                    continue;
                }

                // each of the values of this parameter becomes a should clause (equivalent of OR in ES lingo)
                if (params.get(key).size() > 0) {
                    paramsString.append("{\n" + "\"bool\": {\n" + "     \"should\": [\n");
                    for (String value : params.get(key)) {

                        // convert language values to lowercase
                        if (key.equalsIgnoreCase(OMTDFacetEnum.DOCUMENT_LANG.value())) {

                            if (languageUtils.getLangNameToCode().containsKey(value)) {
                                value = languageUtils.getLangNameToCode().get(value);
                            } else {
                                value = value.toLowerCase();
                            }

                            paramsString.append("{\n"
                                    + "                \"nested\": {\n"
                                    + "                  \"path\": \"language\",\n"
                                    + "                  \"query\": {\n"
                                    + "                    \"term\": {\n"
                                    + "                      \"language.code\": {\n"
                                    + "                        \"value\": \"" + value + "\"\n"
                                    + "                      }\n"
                                    + "                    }\n"
                                    + "                  }\n"
                                    + "                }\n"
                                    + "              }");
                        } else {

                            paramsString.append("{\"term\": { \"").append(esParameterName).append("\": \"").append(value).append("\" }},\n");
                        }
                    }
                    //remove trailing comma
                    paramsString = new StringBuilder(paramsString.toString().replaceAll(",\n$", ""));
                    paramsString.append("]\n" + "}\n" + "},\n");
                }

            }
            //remove trailing comma
            paramsString = new StringBuilder(paramsString.toString().replaceAll(",\n$", ""));

        }

        //
        // Filter
        //
        // apart of default filters (only fulltext items and not deleted) add
        // the parameters as a filter
        //
        // Elasticsearch 6 has deprecated filtered queries, instead include filters
        // in boolean queries
        //
        String filterQueryComponent = ","
                + "                    { \"exists\" : { \"field\" : \"fullText\" } },\n"
                + "                    { \"term\": { \"deleted\":\"ALLOWED\" } }\n";
        if (paramsString.length() > 0) {
            filterQueryComponent += "," + paramsString;
        }
        queryComponent += filterQueryComponent;
        queryComponent += "]";//end of must 
        queryComponent += "}";//end of bool
        queryComponent += "}";//end of query

        //        
        // Facets are deprecated in ES6- instead replaced with aggregations
        //
        List<String> facets = query.getFacets();
        StringBuilder aggsComponent = new StringBuilder();

        if (facets != null && !facets.isEmpty()) {
            aggsComponent.append(" \"aggs\" : {");
            // for each aggregation (facet) creates a line like:
            //"document_type_aggs": {"terms": {"field": "documentType"}}
            // BUT in the case of nested object use a nested aggregation
            for (String facet : facets) {
                String facetField = facet;
                String knownFacet = OMTDtoESMapper.OMTD_TO_ES_FACETS_NAMES.get(facet);
                if (knownFacet != null && !knownFacet.isEmpty()) {
                    facetField = knownFacet;
                    if (facetField.contains(".")) {
                        String[] splits = facetField.split("\\.");
                        String facetPathPrefix = facetField.split("\\.")[0];
                        String facetNestedObject = facetField.split("\\.")[1];
                        aggsComponent.append("\"" + facetField + "_aggs\": {");
                        aggsComponent.append("    \"nested\": {");
                        aggsComponent.append("        \"path\": \"" + facetPathPrefix + "\"");
                        aggsComponent.append("    },");
                        aggsComponent.append("    \"aggs\": {");
                        aggsComponent.append("        \"langs_aggs_inner\": {");
                        aggsComponent.append("            \"terms\": {");
                        aggsComponent.append("                \"field\": \"" + facetField + "\",");
                        aggsComponent.append("                \"size\": 10");
                        aggsComponent.append("            }");
                        aggsComponent.append("        }");
                        aggsComponent.append("    }");
                        aggsComponent.append("},");
                    } else {
                        aggsComponent.append("\"" + facetField + "_aggs\": {");
                        aggsComponent.append("    \"terms\": {");
                        aggsComponent.append("        \"field\": \"" + facetField + "\"");
                        aggsComponent.append("    }");
                        aggsComponent.append("},");
                    }
                }

            }
            //remove trailing comma
            aggsComponent = new StringBuilder(aggsComponent.toString().replaceAll(",$", ""));
            aggsComponent.append("},");
        }

        //-------------------------------------------------------
        //
        // Combine everything into a filtered query with facets
        //
        //-------------------------------------------------------
        String es_query = "{"
                + "    \"query\":" + queryComponent + ","
                + aggsComponent.toString()
                + "    \"_source\": {\n"
                + "        \"excludes\": [ \"fullText\" ]\n"
                + "    },";
        if (withFromParameter) {
            es_query += "    \"from\":" + from + ",\n";
        }
        es_query += "    \"size\":" + (to - from) + "\n"
                + "}";
        return es_query;
    }

    /**
     * Retrieves facets from elastic search's SearchResult as a list
     *
     * @param searchResult the result from the elastic search
     * @param queryFacets the list of facets that are declared in the query
     * @return a list with Facets matching those of the query
     */
    public static List<Facet> getOmtdFacetsFromSearchResult(SearchResult searchResult, List<String> queryFacets) {
        List<Facet> omtdFacets = new ArrayList<>();

        try {
            JsonObject jsonObject = searchResult.getJsonObject();
            JsonObject facetsJsonObject = jsonObject.getAsJsonObject("aggregations");

            for (String f : queryFacets) {
                Facet omtdFacet = new Facet();
                omtdFacet.setLabel(f);
                omtdFacet.setField(f);

                JsonObject fJObj = facetsJsonObject.getAsJsonObject(OMTDtoESMapper.OMTD_TO_ES_FACETS_NAMES.get(f) + "_aggs");

                if (f.equals("documentlanguage")) {
                    fJObj = fJObj.getAsJsonObject("langs_aggs_inner");
                }

                JsonArray terms = fJObj.getAsJsonArray("buckets");

                List<Value> omtdFacetValues = new ArrayList<>();
                for (int i = 0; i < terms.size(); i++) {
                    JsonObject fTermElement = terms.get(i).getAsJsonObject();
                    String term = fTermElement.get("key").getAsString();
                    int count = fTermElement.get("doc_count").getAsInt();
                    Value omtdValue = new Value();
                    omtdValue.setValue(term);
                    omtdValue.setLabel(term);
                    omtdValue.setCount(count);
                    omtdFacetValues.add(omtdValue);
                }

                omtdFacet.setValues(omtdFacetValues);
                omtdFacets.add(omtdFacet);
            }

            int count = searchResult.getTotal().intValue();

            setDocumentTypeFacetValue(omtdFacets, count);

            setRightsFacetValue(omtdFacets, count);

            setDocumentFacetValue(omtdFacets);

            setLanguageFacetValue(omtdFacets, count);

        } catch (Exception e) {
            log.error("Error getting omtd facets ", e);
        }
        return omtdFacets;
    }

    /**
     * Assisting method for setting documentType facet's value
     *
     * @param omtdFacets list of facets
     * @param count number of values found
     */
    private static void setDocumentTypeFacetValue(List<Facet> omtdFacets, int count) {
        // manually setting all document types as fulltext
        String term = "Fulltext";
        for (Facet f : omtdFacets) {
            if (f.getField().equalsIgnoreCase("documenttype")) {
                List<Value> documentTypeFacetValues = new ArrayList<>();
                Value rightsValue = new Value();
                rightsValue.setValue(term);
                rightsValue.setLabel(term);
                rightsValue.setCount(count);
                documentTypeFacetValues.add(rightsValue);
                f.setValues(documentTypeFacetValues);
            }
        }
    }

    /**
     * Assisting method for setting rightsstmtname facet's value
     *
     * @param omtdFacets list of facets
     * @param count number of values found
     */
    private static void setRightsFacetValue(List<Facet> omtdFacets, int count) {
        // manually setting all documents rights as open access
        for (Facet f : omtdFacets) {
            if (f.getField().equalsIgnoreCase("rights")) {
                List<Value> rightsFacetValues = new ArrayList<>();
                Value rightsValue = new Value();
                rightsValue.setValue("Open Access");
                rightsValue.setLabel("Open Access");
                rightsValue.setCount(count);
                rightsFacetValues.add(rightsValue);
                f.setValues(rightsFacetValues);
            }

        }
    }

    /**
     * Assisting method for getting publications as string from search results
     *
     * @param searchResult the search result from the elastic search
     * @return a list with publications as string
     */
    public static List<String> getPublicationsFromSearchResultAsString(SearchResult searchResult) {
        List<String> publications = new ArrayList<>();

        try {
            List<Hit<ElasticSearchArticleMetadata, Void>> hits = searchResult.getHits(ElasticSearchArticleMetadata.class
            );
            List<Hit<Map, Void>> mapHits = searchResult.getHits(Map.class
            );
            for (io.searchbox.core.SearchResult.Hit<ElasticSearchArticleMetadata, Void> hit : hits) {
                if (hit != null && hit.source != null) {
                    publications.add(hit.source.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publications;
    }

    /**
     * Method to get publications from search result as a list of
     * ElasticSearchArticleMetadata
     *
     * @param searchResult the search result from the elastic search
     * @return a list of publications as ElasticSearchArticleMetadata
     */
    public static List<ElasticSearchArticleMetadata> getPublicationsFromSearchResult(SearchResult searchResult) {
        List<ElasticSearchArticleMetadata> publications = new ArrayList<>();

        try {
            List<io.searchbox.core.SearchResult.Hit<ElasticSearchArticleMetadata, Void>> hits = searchResult.getHits(ElasticSearchArticleMetadata.class
            );
            for (io.searchbox.core.SearchResult.Hit<ElasticSearchArticleMetadata, Void> hit : hits) {
                publications.add(hit.source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publications;
    }

    /**
     * Method to get publications from result json array as a list of
     * ElasticSearchArticleMetadata
     *
     * @param hits the JsonArray with publications
     * @return a list of publications as ElasticSearchArticleMetadata
     */
    public static List<ElasticSearchArticleMetadata> getPublicationsFromResultJsonArray(JsonArray hits) {
        java.util.Random random = new Random();
        List<ElasticSearchArticleMetadata> results = new ArrayList<>();
        try {
            for (int i = 0; i < hits.size(); i++) {
                JsonElement obj = hits.get(i).getAsJsonObject().get("_source");
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

                try {
                    ElasticSearchArticleMetadata esam = gson.fromJson(obj, ElasticSearchArticleMetadata.class
                    );
                    results.add(esam);
                } catch (JsonSyntaxException j) {
                    System.out.println("json syntax exception " + j.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Method that constructs a fetch request for elastic search that fetches
     * documents by their identifier
     *
     * @param identifier the document's identifier
     * @return a request to be used in elastic search
     */
    public static String constructFetchByIdentifierElasticsearchQuery(String identifier) {
        //check if identifier is a number
        Integer id = null;
        try {
            id = Integer.parseInt(identifier);
        } catch (NumberFormatException nfe) {
            //not a number
        }
        String esQuery;
        if (id != null) {

            esQuery = "{\n"
                    + "    \"query\": {\n"
                    + "        \"bool\":{\n"
                    + "            \"should\": [\n"
                    + "               {\"term\": {\"identifiers\": {\"value\":\"" + identifier + "\" }}},\n"
                    + "               {\"term\": {\"id\": {\"value\":\"" + id.toString() + "\" }}}\n"
                    + "            ],"
                    + "            \"must\":[{\n"
                    + "                 \"exists\": {\n"
                    + "                     \"field\": \"fullText\"\n"
                    + "                 }\n"
                    + "             },\n"
                    + "             {\n"
                    + "                 \"term\": {\n"
                    + "                     \"deleted\": {\n"
                    + "                         \"value\": \"ALLOWED\"\n"
                    + "                     }\n"
                    + "                 }\n"
                    + "             }]\n"
                    + "        }\n"
                    + "    }\n"
                    + "}";

        } else {
            esQuery = "{\n"
                    + "    \"query\": {\n"
                    + "        \"bool\":{\n"
                    + "            \"should\": [\n"
                    + "               {\"term\": {\"identifiers\": {\"value\":\"" + identifier + "\" }}}\n"
                    + "            ],\n"
                    + "            \"must\":[{\n"
                    + "                 \"exists\": {\n"
                    + "                     \"field\": \"fullText\"\n"
                    + "                 }\n"
                    + "             },\n"
                    + "             {\n"
                    + "                 \"term\": {\n"
                    + "                     \"deleted\": {\n"
                    + "                         \"value\": \"ALLOWED\"\n"
                    + "                     }\n"
                    + "                 }\n"
                    + "             }]\n"
                    + "        }\n"
                    + "    }\n"
                    + "}";
        }
        return esQuery;
    }

    /**
     * Assisting method that sets document type facet
     *
     * @param omtdFacets a list of facets
     */
    private static void setDocumentFacetValue(List<Facet> omtdFacets) {
        // manually mapping CORE document types to OMTD document types        
        for (Facet f : omtdFacets) {
            if (f.getField().equalsIgnoreCase("publicationtype")) {

                List<Value> pubTypeValues = f.getValues();

                int researchArticleCount = 0;
                int thesisArticleCount = 0;
                int otherCount = 0;

                for (Value ptValue : pubTypeValues) {
                    if (ptValue.getValue().equalsIgnoreCase("Research")) {
                        researchArticleCount += ptValue.getCount();
                    } else if (ptValue.getValue().equalsIgnoreCase("Thesis")) {
                        thesisArticleCount += ptValue.getCount();
                    } else {
                        otherCount += ptValue.getCount();
                    }
                }

                Value resArticleValue = new Value();
//                resArticleValue.setValue(PublicationTypeEnum.RESEARCH_ARTICLE.value());
                resArticleValue.setValue("Research Article");
                resArticleValue.setLabel("Research Article");
                resArticleValue.setCount(researchArticleCount);

                Value thesisValue = new Value();
//                thesisValue.setValue(PublicationTypeEnum.THESIS.value());
                thesisValue.setValue("Thesis");
                thesisValue.setLabel("Thesis");
                thesisValue.setCount(thesisArticleCount);

                Value otherValue = new Value();
//                otherValue.setValue(PublicationTypeEnum.OTHER.value());
                otherValue.setValue("Other");
                otherValue.setLabel("Other");
                otherValue.setCount(otherCount);

                List<Value> newPubTypeValues = new ArrayList<>();
                newPubTypeValues.add(resArticleValue);
                newPubTypeValues.add(thesisValue);
                newPubTypeValues.add(otherValue);
                f.setValues(newPubTypeValues);
            }
        }

    }

    /**
     * Assisting method that sets document language facet
     *
     * @param omtdFacets a list of facets
     * @param count the number of values found
     */
    private static void setLanguageFacetValue(List<Facet> omtdFacets, int count) {
        // manually setting undetermined language as :
        // undetermined = total - sum(known_languages)
        LanguageUtils languageUtils = new LanguageUtils();
        for (Facet f : omtdFacets) {
            if (f.getField().equalsIgnoreCase("documentlanguage")) {
                List<Value> langFacetValues = f.getValues();

                int langCount = 0;
                for (Value langValue : langFacetValues) {
                    if (languageUtils.getLangCodeToName().containsKey(langValue.getValue())) {
                        langValue.setLabel(languageUtils.getLangCodeToName().get(langValue.getValue()));
                        langCount += langValue.getCount();
                    }
                }

                Value langValue = new Value();
                langValue.setLabel("Undetermined");
                langValue.setValue("und");
                langValue.setCount(count - langCount);
                langFacetValues.add(langValue);
                f.setValues(langFacetValues);
            }

        }
    }

}
