package eu.openminted.content.connector.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.SearchResult;
import eu.openminted.content.connector.core.util.ElasticsearchConverter;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import io.searchbox.core.SearchScroll;
import io.searchbox.params.Parameters;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.core.elasticsearch.entities.ElasticSearchArticleMetadata;

/**
 *
 * @author lucasanastasiou
 */
@Service
public class CORESearchService {

    @Autowired
    JestClient jestClient;

    public SearchResult query(Query query) {
        SearchResult omtdSearchResult = new SearchResult();

        String elasticSearchQueryString = ElasticsearchConverter.constructElasticsearchQueryFromOmtdQuery(query);

        Search search = new Search.Builder(elasticSearchQueryString)
                .addIndex("articles")
                .addType("article")
                .build();
        
        io.searchbox.core.SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException ex) {
            System.out.println("ex = " + ex);
            ex.printStackTrace();
        }

        omtdSearchResult.setFrom(query.getFrom());
        omtdSearchResult.setTo(query.getTo());
        
        omtdSearchResult.setPublications(ElasticsearchConverter.getPublicationsFromSearchResultAsString(searchResult));
        omtdSearchResult.setFacets(ElasticsearchConverter.getOmtdFacetsFromSearchResult(searchResult, query.getFacets()));
        omtdSearchResult.setTotalHits(searchResult.getTotal());

        return omtdSearchResult;
    }

    public InputStream fetchBigResultSet(Query omtdQuery) throws IOException {
       QueryBuilder queryBuilder = QueryBuilders
                .queryStringQuery(omtdQuery.getKeyword());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
//        searchSourceBuilder.fields(fields);
        searchSourceBuilder.fetchSource(null, "fullText");//do not fetch fulltext - will overwhelm the response

        
        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex("articles")
                .addType("article")
                .setParameter(Parameters.SIZE, 25)//each scroll can fetch up to 15*25=375 results (15 the number of shards in core cluster)
                .setParameter(Parameters.SCROLL, "5m") // 5 minutes should be enough to digest these               
                .build();

        JestResult result = jestClient.execute(search);

        List<ElasticSearchArticleMetadata> publicationResults = new ArrayList<>();
        
        String newScrollId = "";
        newScrollId = result.getJsonObject().get("_scroll_id").getAsString();
        String scrollId = "";
        JsonArray hits = null;
        do {
            scrollId = newScrollId;
            SearchScroll scroll = new SearchScroll.Builder(scrollId, "5m")
                    .setParameter(Parameters.SIZE, 25).build();
            result = jestClient.execute(scroll);
            hits = result.getJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");
            
            publicationResults.addAll(ElasticsearchConverter.getPublicationsFromResultJsonArray(hits));
            
            newScrollId = result.getJsonObject().getAsJsonPrimitive("_scroll_id").getAsString();
        
        }while(hits!=null && hits.size()>0);
         
        return convertListToStream(publicationResults);     
       
    }

    InputStream fetchByIdentifier(String identifier) throws IOException {

        String elasticSearchQueryString = ElasticsearchConverter.constructFetchByIdentifierElasticsearchQuery(identifier);
        
        Search search = new Search.Builder(elasticSearchQueryString)
                .addIndex("articles")
                .addType("article")
                .build();

        io.searchbox.core.SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException ex) {

        }
        List<ElasticSearchArticleMetadata> publications = ElasticsearchConverter.getPublicationsFromSearchResult(searchResult);
        
        return convertListToStream(publications);
    }

    private InputStream convertListToStream(List<ElasticSearchArticleMetadata> list) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (ElasticSearchArticleMetadata esam : list) {
            Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .create();
            String serialisedEsam = gson.toJson(esam, ElasticSearchArticleMetadata.class);
            baos.write(serialisedEsam.getBytes());
        }

        byte[] bytes = baos.toByteArray();

        return new ByteArrayInputStream(bytes);
    }

    
}
