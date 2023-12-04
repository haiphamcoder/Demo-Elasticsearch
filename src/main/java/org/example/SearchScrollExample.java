package org.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.List;

public class SearchScrollExample {

    static String serverUrl = "http://localhost:9200";
    static String indexName = "article";

    static String fieldSearch = "title";
    static String searchText = "Getting Started";

    public static void main(String[] args) throws IOException {
        RestClient restClient = RestClient.builder(HttpHost.create(serverUrl)).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        ElasticsearchClient esClient = new ElasticsearchClient(transport);

        SearchResponse<Article> response = esClient.search(s -> s
                        .index(indexName)
                        .query(q -> q
                                .match(m -> m
                                        .field(fieldSearch)
                                        .query(searchText)
                                )
                        ).size(10).from(0)
                ,
                Article.class
        );

        TotalHits totalHits = response.hits().total();
        assert totalHits != null;
        boolean isExactResult = totalHits.relation() == TotalHitsRelation.Eq;
        if(isExactResult){
            System.out.println("There are " + totalHits.value() + " results");
        } else {
            System.out.println("There are more than " + totalHits.value() + " results");
        }

        List<Hit<Article>> hits = response.hits().hits();
        for(Hit<Article> hit : hits){
            Article article = hit.source();
            assert article != null;
            System.out.println("Found article: " + article.getTitle() + " - " + article.getContent());
        }

        restClient.close();
    }
}