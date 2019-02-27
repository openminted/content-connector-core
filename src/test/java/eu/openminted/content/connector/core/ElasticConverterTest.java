package eu.openminted.content.connector.core;

import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.core.util.ElasticsearchConverter;
import static eu.openminted.content.connector.core.util.ElasticsearchConverter.DEFAULT_FACETS;
import eu.openminted.content.connector.utils.faceting.OMTDFacetEnum;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author lucasanastasiou
 */
//@Ignore
public class ElasticConverterTest {

    @Test
    @Ignore
    public void testConstructElasticsearchQueryFromOmtdQuery() {
        String expectedEmptyQueryString = "{\n"
                + "    \"query\": {\n"
                + "        \"match_all\" : { }\n"
                + "     },    \n"
                + "     \"facets\" : {\n"
                + "        \"authorsFacet\" : {\n"
                + "            \"terms\" : {\n"
                + "                \"field\" : \"authors.raw\"\n"
                + "                \n"
                + "            } \n"
                + "            \n"
                + "        },\n"
                + "        \"journalsFacet\" : { \n"
                + "            \"terms\" : {\n"
                + "                \"field\" : \"journals.title.raw\"\n"
                + "                \n"
                + "            } \n"
                + "            \n"
                + "        },\n"
                + "        \"publicationYearFacet\" : { \n"
                + "            \"terms\" : {\n"
                + "                \"field\" : \"year\"\n"
                + "                \n"
                + "            } \n"
                + "            \n"
                + "        },\n"
                + "        \"documentLanguageFacet\" : { \n"
                + "            \"terms\" : {\n"
                + "                \"field\" : \"language.name\"\n"
                + "                \n"
                + "            } \n"
                + "            \n"
                + "        },\n"
                + "        \"publicationTypeFacet\" : { \n"
                + "            \"terms\" : {\n"
                + "                \"field\" : \"documentType\"\n"
                + "                \n"
                + "            } \n"
                + "            \n"
                + "        }    \n"
                + "        \n"
                + "     },\n"
                + "     \"filter\":{\n"
                + "        \"bool\":{\n"
                + "            \"must\":[\n"
                + "                {   \"exists\" : {\n"
                + "                        \"field\" : \"fullText\"\n"
                + "                    }\n"
                + "                },{\n"
                + "                    \"term\":{\n"
                + "                        \"deleted\":\"ALLOWED\"\n"
                + "                }\n"
                + "            }]\n"
                + "        }\n"
                + "    },    \n"
                + "    \"_source\": {\n"
                + "        \"exclude\": [ \"fullText\" ]\n"
                + "    },    \"from\":0,\n"
                + "    \"size\":10\n"
                + "}";

        String expectedRandomQueryString = "{\n"
                + "    \"query\": {\n"
                + "        \"query_string\": {\n"
                + "           \"query\": \"random\"\n"
                + "        }\n"
                + "     },    \n"
                + "     \"facets\" : {\n"
                + "        \"authorsFacet\" : {\n"
                + "            \"terms\" : {\n"
                + "                \"field\" : \"authors.raw\"\n"
                + "                \n"
                + "            } \n"
                + "            \n"
                + "        },\n"
                + "        \"journalsFacet\" : { \n"
                + "            \"terms\" : {\n"
                + "                \"field\" : \"journals.title.raw\"\n"
                + "                \n"
                + "            } \n"
                + "            \n"
                + "        },\n"
                + "        \"publicationYearFacet\" : { \n"
                + "            \"terms\" : {\n"
                + "                \"field\" : \"year\"\n"
                + "                \n"
                + "            } \n"
                + "            \n"
                + "        },\n"
                + "        \"documentLanguageFacet\" : { \n"
                + "            \"terms\" : {\n"
                + "                \"field\" : \"language.name\"\n"
                + "                \n"
                + "            } \n"
                + "            \n"
                + "        },\n"
                + "        \"publicationTypeFacet\" : { \n"
                + "            \"terms\" : {\n"
                + "                \"field\" : \"documentType\"\n"
                + "                \n"
                + "            } \n"
                + "            \n"
                + "        }    \n"
                + "        \n"
                + "     },\n"
                + "     \"filter\":{\n"
                + "        \"bool\":{\n"
                + "            \"must\":[\n"
                + "                {   \"exists\" : {\n"
                + "                        \"field\" : \"fullText\"\n"
                + "                    }\n"
                + "                },{\n"
                + "                    \"term\":{\n"
                + "                        \"deleted\":\"ALLOWED\"\n"
                + "                }\n"
                + "            }]\n"
                + "        }\n"
                + "    },    \n"
                + "    \"_source\": {\n"
                + "        \"exclude\": [ \"fullText\" ]\n"
                + "    },    \"from\":0,\n"
                + "    \"size\":10\n"
                + "}";

        Query omtdQuery = new Query();
        omtdQuery.setFrom(0);
        omtdQuery.setTo(10);
        omtdQuery.setKeyword("");
        omtdQuery.setFacets(DEFAULT_FACETS);
        ElasticsearchConverter elasticsearchConverter = new ElasticsearchConverter();
        String constructedEmptyQueryString = elasticsearchConverter.constructElasticsearchQueryFromOmtdQuery(omtdQuery);

        omtdQuery.setKeyword("random");
        String constructedRandomQueryString = elasticsearchConverter.constructElasticsearchQueryFromOmtdQuery(omtdQuery);

        System.out.println("cleaned empty = " + replaceEmptyAndNewLines(constructedEmptyQueryString));
        System.out.println("cleaned random = " + replaceEmptyAndNewLines(constructedRandomQueryString));

        Assert.assertEquals(replaceEmptyAndNewLines(expectedEmptyQueryString),
                replaceEmptyAndNewLines(constructedEmptyQueryString));
        Assert.assertEquals(replaceEmptyAndNewLines(expectedRandomQueryString),
                replaceEmptyAndNewLines(constructedRandomQueryString));
    }

    @Test
    public void testQueryWithParametersAndAggregations() {
        Query q1 = new Query();
        q1.setFrom(0);
        q1.setTo(100);
        q1.setKeyword("cryptic mongolia");
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        List<String> yearsParams = Arrays.asList("1999", "2000");
        params.put(OMTDFacetEnum.PUBLICATION_YEAR.value(), yearsParams);
        List<String> typeParams = Arrays.asList("thesis", "research");
        params.put("documentType", typeParams);
        q1.setParams(params);
        q1.setFacets(DEFAULT_FACETS);

        ElasticsearchConverter elasticsearchConverter = new ElasticsearchConverter();
        String s1 = elasticsearchConverter.constructElasticsearchQueryFromOmtdQuery(q1);
        System.out.println("s1 = " + s1);
    }

    @Test
    public void testDefaultQueryWithoutFacets() {
        Query q1 = new Query();
        q1.setFrom(0);
        q1.setKeyword("");
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        q1.setParams(params);

        ElasticsearchConverter elasticsearchConverter = new ElasticsearchConverter();
        String s1 = elasticsearchConverter.constructElasticsearchQueryFromOmtdQuery(q1);
        System.out.println("s1 = " + s1);

    }

    @Test
    public void testDefaultWithAggsQuery() {
        Query query = new Query();
        query.setFrom(0);
        query.setKeyword("");
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        query.setParams(params);//empty params
        query.setFacets(DEFAULT_FACETS);

        ElasticsearchConverter elasticsearchConverter = new ElasticsearchConverter();
        String constructedQuery = elasticsearchConverter.constructElasticsearchQueryFromOmtdQuery(query);
        System.out.println("Query := " + constructedQuery);

    }

    private String replaceEmptyAndNewLines(String s) {
        return s.replaceAll(" ", "").replaceAll("\\n", "");
    }

}
