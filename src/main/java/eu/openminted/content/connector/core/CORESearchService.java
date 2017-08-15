package eu.openminted.content.connector.core;

import com.google.gson.JsonArray;
import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.SearchResult;
import eu.openminted.content.connector.core.mappings.COREtoOMTDMapper;
import eu.openminted.content.connector.core.util.ElasticsearchConverter;
import eu.openminted.registry.domain.DocumentMetadataRecord;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import io.searchbox.core.SearchScroll;
import io.searchbox.params.Parameters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.core.elasticsearch.entities.ElasticSearchArticleMetadata;

/**
 * @author lucasanastasiou
 */
@Service
public class CORESearchService {

    @Autowired
    private JestClient jestClient;

    @Autowired
    private COREConnectorConfiguration cOREConnectorConfiguration;
    private Integer contentLimit;

    @PostConstruct
    private void init() {
        this.contentLimit = cOREConnectorConfiguration.CONTENT_LIMIT;
    }

    Logger logger = Logger.getLogger(CORESearchService.class.getName());

    public SearchResult query(Query query) {
        SearchResult omtdSearchResult = new SearchResult();

        String elasticSearchQueryString = ElasticsearchConverter.constructElasticsearchQueryFromOmtdQuery(query);

        Search search = new Search.Builder(elasticSearchQueryString)
                .addIndex("articles_2017_08_03")
                .addType("article")
                .build();

        io.searchbox.core.SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);

            omtdSearchResult.setFrom(query.getFrom());
            omtdSearchResult.setTo(query.getTo());

            omtdSearchResult.setPublications(ElasticsearchConverter.getPublicationsFromSearchResultAsString(searchResult));
            omtdSearchResult.setFacets(ElasticsearchConverter.getOmtdFacetsFromSearchResult(searchResult, query.getFacets()));

            omtdSearchResult.setTotalHits(searchResult.getTotal());
        } catch (IOException ex) {
            System.out.println("ex = " + ex);
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("ex = " + ex);
            ex.printStackTrace();
        }
        return omtdSearchResult;
    }

    public InputStream fetchBigResultSet(Query omtdQuery) throws IOException {
        List<ElasticSearchArticleMetadata> publicationResults = new ArrayList<>();
        try {
            omtdQuery.setFrom(1);
            omtdQuery.setTo(25);

            String elasticSearchQueryString = ElasticsearchConverter.constructElasticsearchQueryFromOmtdQuery(omtdQuery);

            Search search = new Search.Builder(elasticSearchQueryString)
                    .addIndex("articles_2017_08_03")
                    .addType("article")
                    .setParameter(Parameters.SIZE, 25)//each scroll can fetch up to 15*25=375 results (15 the number of shards in core cluster)
                    .setParameter(Parameters.SCROLL, "5m") // 5 minutes should be enough to digest these
                    .build();

            JestResult result = jestClient.execute(search);

            String newScrollId = "";
            newScrollId = result.getJsonObject().get("_scroll_id").getAsString();
            String scrollId = "";
            JsonArray hits = null;

            int publicationsResultSize = publicationResults.size();
            do {
                scrollId = newScrollId;
                SearchScroll scroll = new SearchScroll.Builder(scrollId, "5m")
                        .setParameter(Parameters.SIZE, 25).build();

                hits = result.getJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");
                publicationResults.addAll(ElasticsearchConverter.getPublicationsFromResultJsonArray(hits));

                result = jestClient.execute(scroll);

                newScrollId = result.getJsonObject().getAsJsonPrimitive("_scroll_id").getAsString();
                publicationsResultSize = publicationResults.size();

            } while (hits != null && hits.size() > 0 && publicationsResultSize < this.contentLimit);
        } catch (Exception ex) {
            logger.log(Level.ALL, "Exception while fetching big result set:" + ex.getMessage());
        }

        return convertListToStream(convertToOMTDSchema(publicationResults));

    }

    InputStream fetchByIdentifier(String identifier) throws IOException {

        String elasticSearchQueryString = ElasticsearchConverter.constructFetchByIdentifierElasticsearchQuery(identifier);

        Search search = new Search.Builder(elasticSearchQueryString)
                .addIndex("articles_2017_08_03")
                .addType("article")
                .build();

        io.searchbox.core.SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException ex) {
            Logger.getLogger(CORESearchService.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<ElasticSearchArticleMetadata> publications = ElasticsearchConverter.getPublicationsFromSearchResult(searchResult);
        if (publications == null || publications.isEmpty()) {
            logger.log(Level.INFO, null, "No article in CORE found with identifier " + identifier);
            return null;
        }
        String articlePdfUrl = "";

        for (ElasticSearchArticleMetadata esam : publications) {
            String coreID = esam.getId();

            // find the first non-empty identifier
            if (coreID != null && !coreID.isEmpty()) {
                String dlURL = "https://core.ac.uk/download/pdf/" + coreID + ".pdf";
                articlePdfUrl = dlURL;
                break;
            }
        }

        if (!articlePdfUrl.isEmpty()) {
            InputStream in = null;
            try {
                in = new URL(articlePdfUrl).openStream();
                return in;
            } catch (IOException iOException) {
                Logger.getLogger(CORESearchService.class.getName()).log(Level.SEVERE, null, iOException);
            } finally {
//                IOUtils.closeQuietly(in);
            }
        }
        // in case the block above failed to return the stream
        return null;
    }

    private List<DocumentMetadataRecord> convertToOMTDSchema(List<ElasticSearchArticleMetadata> publicationResults) {
        // convert to OMTD share schema
        List<DocumentMetadataRecord> omtdRecords = new ArrayList<>();
        for (ElasticSearchArticleMetadata esam : publicationResults) {
            DocumentMetadataRecord omtdRecord = COREtoOMTDMapper.convertCOREtoOMTD(esam);
            omtdRecords.add(omtdRecord);
        }
        return omtdRecords;
    }

    private InputStream convertListToStream(List<DocumentMetadataRecord> list) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DocumentMetadataRecord.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            baos.write("<OMTDPublications>".getBytes());
            for (DocumentMetadataRecord omtdRecord : list) {
                // convert to XML string and write it to the stream
                StringWriter sw = new StringWriter();
                jaxbMarshaller.marshal(omtdRecord, sw);
                String xmlString = sw.toString();

                System.out.println("XML STRING DOCUMENTMETADATARECORD\n\n" + xmlString);
                baos.write(xmlString.getBytes());
            }
            baos.write("</OMTDPublications>".getBytes());

            byte[] bytes = baos.toByteArray();

            return new ByteArrayInputStream(bytes);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Stream was interrupted", ioe);
        } catch (JAXBException je) {
            logger.log(Level.ALL, "Cannot serialise to XML", je);
        } finally {
            baos.close();
        }
        return null;
    }

}
