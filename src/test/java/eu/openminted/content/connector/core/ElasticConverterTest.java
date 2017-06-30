package eu.openminted.content.connector.core;

import eu.openminted.content.connector.Query;
import eu.openminted.content.connector.core.util.ElasticsearchConverter;
import static eu.openminted.content.connector.core.util.ElasticsearchConverter.DEFAULT_FACETS;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author lucasanastasiou
 */
public class ElasticConverterTest {

    @Test
//    @Ignore
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
        String constructedEmptyQueryString = ElasticsearchConverter.constructElasticsearchQueryFromOmtdQuery(omtdQuery);

        omtdQuery.setKeyword("random");
        String constructedRandomQueryString = ElasticsearchConverter.constructElasticsearchQueryFromOmtdQuery(omtdQuery);

        System.out.println("cleaned empty = " + replaceEmptyAndNewLines(constructedEmptyQueryString));
        System.out.println("cleaned random = " + replaceEmptyAndNewLines(constructedRandomQueryString));
        
        Assert.assertEquals(replaceEmptyAndNewLines(expectedEmptyQueryString), 
                replaceEmptyAndNewLines(constructedEmptyQueryString));
        Assert.assertEquals(replaceEmptyAndNewLines(expectedRandomQueryString), 
                replaceEmptyAndNewLines(constructedRandomQueryString));
    }
    
    private String replaceEmptyAndNewLines(String s){
        return s.replaceAll(" ", "").replaceAll("\\n", "");
    }

}
