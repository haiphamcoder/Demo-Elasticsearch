package org.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.List;

public class SearchAfterExample {
    static String serverUrl = "http://localhost:9200";
    static String indexName = "article";
    static String fieldSearch = "title";
    static String searchText = "Getting Started";

    public static void main(String[] args) throws IOException, InterruptedException {
        RestClient httpClient = RestClient.builder(HttpHost.create(serverUrl)).build();

        ElasticsearchTransport transport = new RestClientTransport(
                httpClient,
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
                        ).sort(so -> so
                                .field(FieldSort.of(f -> f
                                        .field("title.keyword")
                                        .order(SortOrder.Asc))))
                        .size(3)
                ,
                Article.class
        );

        List<Hit<Article>> hits = response.hits().hits();
        for (Hit<Article> hit : hits) {
            Article article = hit.source();
            assert article != null;
            System.out.println("Found article: " + article.getTitle() + " - " + article.getContent());
        }

        Thread.sleep(10000);

        final List<Hit<Article>> finalHits = hits;
        response = esClient.search(s -> s
                        .index(indexName)
                        .query(q -> q
                                .match(m -> m
                                        .field(fieldSearch)
                                        .query(searchText)
                                )
                        ).sort(so -> so
                                .field(FieldSort.of(f -> f
                                        .field("title.keyword")
                                        .order(SortOrder.Asc))))
                        .size(3)
                        .searchAfter(finalHits.get(2).sort())
                ,
                Article.class
        );

        hits = response.hits().hits();
        for (Hit<Article> hit : hits) {
            Article article = hit.source();
            assert article != null;
            System.out.println("Found article: " + article.getTitle() + " - " + article.getContent());
        }

        httpClient.close();
    }
}
