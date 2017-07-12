package eu.openminted.content.connector.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import eu.openminted.content.connector.Query;
import eu.openminted.registry.core.domain.Facet;
import eu.openminted.registry.core.domain.Value;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import uk.ac.core.elasticsearch.entities.ElasticSearchArticleMetadata;

/**
 * @author lucasanastasiou
 */
public class ElasticsearchConverter {

    public static List<String> DEFAULT_FACETS = Arrays.asList(new String[]{"authors", "journals", "licence", "publicationYear", "documentLanguage", "publicationType"});

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

    public static String constructElasticsearchQueryFromOmtdQuery(Query query) {
        int from = query.getFrom();
        int to = query.getTo();
        String keyword = query.getKeyword();
        String queryComponent = "";
        // Parameters
        Map<String, List<String>> params = query.getParams();
        String paramsString = "";
        if (params != null) {

            paramsString +=
                    "         \"bool\": {\n" +
                    "              \"must\": [\n";

            for (String key : params.keySet()) {
                String paramKey = key;
                if (key.equalsIgnoreCase("documentLanguage")) {
                    paramKey = "language.name";
                }
                if (key.equalsIgnoreCase("publicationYear")) {
                    paramKey = "year";
                }
                if (key.equalsIgnoreCase("publicationType")) {
                    paramKey = "documentType";
                }
                if (key.equalsIgnoreCase("licence")) {
                    paramKey = "licence";
                    continue;
                }

                if (params.get(key).size() > 0) {
                    for (String value : params.get(key)) {
                        paramsString +=
                                "                           {\n" +
                                        "                                \"term\": { \"" + paramKey + "\": \"" + value + "\" }\n" +
                                        "                           },\n";
                    }
                }
            }
            //remove trailing comma
            paramsString = paramsString.replaceAll(",\n$", "");
            paramsString +=
                    "\n          ]" +
                    "\n    }" +
                    "\n";
        }


        if (keyword == null || keyword.isEmpty() || keyword.equals("*")) {
            if (params.size() > 0 && !paramsString.isEmpty()) {
                queryComponent = paramsString;
            } else {
                queryComponent =
                        "    \"match_all\" : { }\n";
            }
        } else {
            String escapedKeyword = QueryParserUtil.escape(keyword);

            if (params.size() > 0 && !paramsString.isEmpty()) {
                /*
                In case there are both a keyword and parameters defined by user,
                we use the following query schema:

                "bool": {
                      "must": [
                        {
                          "query_string": {
                            "query": "field:text"
                          }
                        },
                        {
                            "bool": {
                                "must": [
                                    {
                                        "match":
                                        {
                                            "field": "text"
                                        }
                                    },
                                    {
                                        "match":
                                        {
                                            "field": "text"
                                        }
                                    }
                               ]
                           }
                        }
                      ]
                 }
                 */
                queryComponent =
                        "          \"bool\": {\n"
                        + "               \"must\": [\n"
                        + "                {\n"
                        + "                   \"query_string\": {\n"
                        + "                         \"query\": \""+ escapedKeyword +"\"\n"
                        + "                     }\n"
                        + "                },\n"
                        + "                {\n" +
                          "                 " + paramsString + "\n" +
                          "                }]\n" +
                          "         }\n";

            } else {
                queryComponent = "        \"query_string\": {\n"
                        + "           \"query\": \"" + escapedKeyword + "\"\n"
                        + "        }\n";
            }
        }

        //facets
        List<String> facets = query.getFacets();

        if (facets == null || facets.isEmpty()) {
            query.setFacets(DEFAULT_FACETS);
        }

        String facetString = "";
        // for each facet creates a line like:
        // \"yearFacet\" : { \"terms\" : {\"field\":\"year\"}}
        for (String facet : facets) {
            //special case for some fields (from the multi-field choose the non-analyzed version)
            String facetField = facet;
            if (facet.equalsIgnoreCase("documentLanguage")) {
                facetField = "language.name";
            }
            if (facet.equalsIgnoreCase("publicationYear")) {
                facetField = "year";
            }
            if (facet.equalsIgnoreCase("publicationType")) {
                facetField = "documentType";
            }
            if (facet.equalsIgnoreCase("licence")) {
                facetField = "licence";
            }
            if (facet.equals("authors")) {
                facetField = "authors.raw";
            }
            if (facet.equals("journals")) {
                facetField = "journals.title.raw";
            }
            facetString += "\"" + facet + "Facet\" : { \"terms\" : {\"field\" : \"" + facetField + "\"} },";
        }
        //remove trailing comma
        facetString = facetString.replaceAll(",$", "");

        String esQuery = "{\n"
                + "    \"query\": {\n"
                + queryComponent
                + "     },\n"
                + "    \"facets\" : {\n"
                + facetString
                + "    },"
                + "      \"filter\":{\n"
                + "        \"bool\":{\n"
                + "            \"must\":[\n"
                + "                     { \"exists\" : { \"field\" : \"fullText\" } }\n"
                + "                    ,{ \"term\": { \"deleted\":\"ALLOWED\" } }\n"
                + "            ]\n"
                + "        }\n"
                + "    },    \n"
                + "    \"_source\": {\n"
                + "        \"exclude\": [ \"fullText\" ]\n"
                + "    },"
                + "    \"from\":" + from + ",\n"
                + "    \"size\":" + (to - from) + "\n"
                + "}";


        System.out.println(esQuery);
        return esQuery;
    }

    public static List<Facet> getOmtdFacetsFromSearchResult(SearchResult searchResult, List<String> queryFacets) {
        List<Facet> omtdFacets = new ArrayList<>();

        try {
            JsonObject jsonObject = searchResult.getJsonObject();
            JsonObject facetsJsonObject = jsonObject.getAsJsonObject("facets");

            for (String f : queryFacets) {
                Facet omtdFacet = new Facet();
                omtdFacet.setLabel(f);
                omtdFacet.setField(f);

                JsonObject fJObj = facetsJsonObject.getAsJsonObject(f + "Facet");
                JsonArray terms = fJObj.getAsJsonArray("terms");

                List<Value> omtdFacetValues = new ArrayList<>();
                for (int i = 0; i < terms.size(); i++) {
                    JsonObject fTermElement = terms.get(i).getAsJsonObject();
                    String term = fTermElement.get("term").getAsString();
                    int count = fTermElement.get("count").getAsInt();
                    Value omtdValue = new Value();
                    omtdValue.setValue(term);
                    omtdValue.setCount(count);
                    omtdFacetValues.add(omtdValue);
                }

                omtdFacet.setValues(omtdFacetValues);
                omtdFacets.add(omtdFacet);
            }
            Facet omtdFacet = new Facet();
            List<Value> omtdFacetValues = new ArrayList<>();

            omtdFacet.setField("documentType");
            omtdFacet.setLabel("Document Type");

            String term = "fullText";
            int count = searchResult.getTotal();
            Value omtdValue = new Value();
            omtdValue.setValue(term);
            omtdValue.setCount(count);
            omtdFacetValues.add(omtdValue);
            omtdFacet.setValues(omtdFacetValues);
            omtdFacets.add(omtdFacet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return omtdFacets;
    }

    public static List<String> getPublicationsFromSearchResultAsString(SearchResult searchResult) {
        List<String> publications = new ArrayList<>();
        try {
            List<Hit<ElasticSearchArticleMetadata, Void>> hits = searchResult.getHits(ElasticSearchArticleMetadata.class);
            List<Hit<Map, Void>> mapHits = searchResult.getHits(Map.class);
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

    public static List<ElasticSearchArticleMetadata> getPublicationsFromSearchResult(SearchResult searchResult) {
        List<ElasticSearchArticleMetadata> publications = new ArrayList<>();
        try {
            List<io.searchbox.core.SearchResult.Hit<ElasticSearchArticleMetadata, Void>> hits = searchResult.getHits(ElasticSearchArticleMetadata.class);
            for (io.searchbox.core.SearchResult.Hit<ElasticSearchArticleMetadata, Void> hit : hits) {
                publications.add(hit.source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publications;
    }

    public static List<ElasticSearchArticleMetadata> getPublicationsFromResultJsonArray(JsonArray hits) {
        java.util.Random random = new Random();
        List<ElasticSearchArticleMetadata> results = new ArrayList<>();
        try {
            for (int i = 0; i < hits.size(); i++) {
                JsonElement obj = hits.get(i).getAsJsonObject().get("_source");
                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .create();
                //aaaaaa cannot do gson deserailisation - shall be done manually
                try {
                    ElasticSearchArticleMetadata esam = gson.fromJson(obj, ElasticSearchArticleMetadata.class);
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

    public static String constructFetchByIdentifierElasticsearchQuery(String identifier) {
        String esQuery = "{\n"
                + "    \"query\": {\n"
                + "        \"term\": {\n"
                + "           \"identifiers\": {\n"
                + "              \"value\": \"" + identifier + "\"\n"
                + "           }\n"
                + "        }\n"
                + "    }\n"
                + "}";
        return esQuery;
    }

}
