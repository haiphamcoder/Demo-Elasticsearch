package org.example;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;

public class SearchAfterExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200),
                        new HttpHost("localhost", 9201)
                )
        );

        SearchRequest searchRequest = new SearchRequest("article");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("title", "Getting Started"));
        searchSourceBuilder.sort("title.keyword", SortOrder.ASC);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        System.out.println(searchHits.length);
        for (SearchHit hit : searchHits) {
            System.out.println(hit.getSourceAsString());
        }
        System.out.println();

        Object[] sortValues;
        if (searchHits.length > 0) {
            sortValues = searchHits[searchHits.length - 1].getSortValues();
        } else {
            sortValues = new Object[]{};
        }
        for (Object sortValue : sortValues) {
            System.out.println(sortValue);
        }

        Thread.sleep(10000);
        searchSourceBuilder.searchAfter(sortValues);
        searchRequest.source(searchSourceBuilder);
        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        searchHits = searchResponse.getHits().getHits();
        if (searchHits.length > 0) {
            sortValues = searchHits[searchHits.length - 1].getSortValues();
        } else {
            sortValues = new Object[]{};
        }
        for (Object sortValue : sortValues) {
            System.out.println(sortValue);
        }

        client.close();
    }
}
