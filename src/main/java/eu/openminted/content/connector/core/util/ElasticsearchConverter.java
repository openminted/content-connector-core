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
import java.util.List;
import java.util.Map;
import java.util.Random;
import uk.ac.core.elasticsearch.entities.ElasticSearchArticleMetadata;

/**
 *
 * @author lucasanastasiou
 */
public class ElasticsearchConverter {

    public static String constructElasticsearchScanAndScrollQueryFromOmtdQuery(Query query) {
        String keyword = query.getKeyword();
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
        String escapedKeyword = org.apache.lucene.queryparser.flexible.standard.QueryParserUtil.escape(keyword);

        List<String> facets = query.getFacets();
        Map<String, List<String>> params = query.getParams();

        String facetString = "";
        // for each facet creates a line like:
        // \"yearFacet\" : { \"terms\" : {\"field\":\"year\"}}
        for (String facet : facets) {
            //special case for some fields (from the multi-field choose the non-analyzed version)
            String facetField = facet;
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
                + "        \"query_string\": {\n"
                + "           \"query\": \"" + escapedKeyword + "\"\n"
                + "        }\n"
                + "    },\n"
                + "    \"facets\" : {\n"
                + facetString
                + "    },\n"
//                + "    \"fields\": [\n"
//                + "       \"title\",\"description\"\n"
//                + "    ],\n"
                + "    \"from\":" + from + ",\n"
                + "    \"size\":" + (to - from) + "\n"
                + "}";
        return esQuery;
    }

    public static List<Facet> getOmtdFacetsFromSearchResult(SearchResult searchResult, List<String> queryFacets) {
        List<eu.openminted.registry.domain.Facet> omtdFacets = new ArrayList<>();

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

        }
        return omtdFacets;
    }

    public static void main(String[] args) {
        Query omtdQuery = new Query();
        omtdQuery.setFrom(0);
        omtdQuery.setTo(10);
        omtdQuery.setKeyword("semantic web");
        List<String> qFacets = new ArrayList<>();
        qFacets.add("authors");
        qFacets.add("year");
        omtdQuery.setFacets(qFacets);

        String s = constructElasticsearchQueryFromOmtdQuery(omtdQuery);
        System.out.println("s = " + s);
    }

    public static List<String> getPublicationsFromSearchResultAsString(SearchResult searchResult) {
        List<Hit<ElasticSearchArticleMetadata, Void>> hits = searchResult.getHits(ElasticSearchArticleMetadata.class);
        List<Hit<Map,Void>> mapHits = searchResult.getHits(Map.class);
        List<String> publications = new ArrayList<>();
        for (io.searchbox.core.SearchResult.Hit<ElasticSearchArticleMetadata, Void> hit : hits) {
            if (hit!=null && hit.source != null) {
                publications.add(hit.source.toString());
            }
        }
        return publications;
    }

    public static List<ElasticSearchArticleMetadata> getPublicationsFromSearchResult(SearchResult searchResult) {
        List<io.searchbox.core.SearchResult.Hit<ElasticSearchArticleMetadata, Void>> hits = searchResult.getHits(ElasticSearchArticleMetadata.class);
        List<ElasticSearchArticleMetadata> publications = new ArrayList<>();
        for (io.searchbox.core.SearchResult.Hit<ElasticSearchArticleMetadata, Void> hit : hits) {
            publications.add(hit.source);
        }
        return publications;
    }

    public static List<ElasticSearchArticleMetadata> getPublicationsFromResultJsonArray(JsonArray hits) {
        java.util.Random random = new Random();
        List<ElasticSearchArticleMetadata> results = new ArrayList<>();
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
