package eu.openminted.content.connector.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import eu.openminted.content.connector.Query;
import eu.openminted.registry.domain.Facet;
import eu.openminted.registry.domain.Value;
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
        if (keyword == null || keyword.isEmpty() || keyword.equals("*")) {
            queryComponent = 
                     "    \"match_all\" : { }\n";
        } else {

            String escapedKeyword = QueryParserUtil.escape(keyword);
            queryComponent = "        \"query_string\": {\n"
                    + "           \"query\": \"" + escapedKeyword + "\"\n"
                    + "        }\n";
        }
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

        // Parameters
        Map<String, List<String>> params = query.getParams();
        String paramsString = "";
        if (params != null) {
            for (String key : params.keySet()) {
                if (key.equalsIgnoreCase("documentLanguage")) {
                    paramsString += ",{\n" +
                            "\"term\" : {\n" +
                            "   \"$key\" : \"$value\"" +
                            "   }\n" +
                            "  }\n";
                    paramsString.replace("$key", "language.name");
                    paramsString.replace("value", params.get(key).get(0));
                }
            }
        }

        String esQuery = "{\n"
                + "    \"query\": {\n"
                + queryComponent
                + "     },"
                + "    \"facets\" : {\n"
                + facetString
                + "    },"
                + "      \"filter\":{\n"
                + "        \"bool\":{\n"
                + "            \"must\":[\n"
                + "                {   \"exists\" : {\n"
                + "                        \"field\" : \"fullText\"\n"
                + "                    }\n"
                + "                },{\n"
                + "                    \"term\":{\n"
                + "                        \"deleted\":\"ALLOWED\"\n"
                + "                     }\n"
                + "                 }"
                + paramsString +
                            "]\n"
                + "        }\n"
                + "    },    \n"
                + "    \"_source\": {\n"
                + "        \"exclude\": [ \"fullText\" ]\n"
                + "    },"
                + "    \"from\":" + from + ",\n"
                + "    \"size\":" + (to - from) + "\n"
                + "}";
        return esQuery;
    }

    public static List<Facet> getOmtdFacetsFromSearchResult(SearchResult searchResult, List<String> queryFacets) {
        List<eu.openminted.registry.domain.Facet> omtdFacets = new ArrayList<>();

        try {
            JsonObject jsonObject = searchResult.getJsonObject();
            JsonObject facetsJsonObject = jsonObject.getAsJsonObject("facets");

            for (String f : queryFacets) {
                eu.openminted.registry.domain.Facet omtdFacet = new eu.openminted.registry.domain.Facet();
                omtdFacet.setLabel(f + "Facet");
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
