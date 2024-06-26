package gg.rimumu.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.google.gson.Gson;
import gg.rimumu.common.util.ElasticSearchUtil;
import gg.rimumu.common.util.ApplicationDataUtil;
import gg.rimumu.common.util.FileUtil;
import gg.rimumu.dto.Item;
import gg.rimumu.exception.RimumuException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static gg.rimumu.common.util.FileUtil.readJsonFile;


@Service
public class ElasticSearchService {
    private final ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
    private ElasticsearchClient client = elasticSearchUtil.getClient();

    private static final String MAPPING_ITEM_INDEX_JSON = "/elasticsearch/item_set.json";
    private static final String INSERT_ITEM_FILE_PATH = "/datadragon/item.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchService.class);
    private final Gson gson = new Gson();
    private int MAX_RETRIES = 1;
    private String INDEX;

    public String initIndexName() {
        return INDEX = "item" + ApplicationDataUtil.DD_VERSION;
    }

    public String createIndexAndInsertDoc() {
        createIndex();
        return indexData();
    }

    public String createIndex() {
        initIndexName();
        int retryCount = 0;

        while (retryCount <= MAX_RETRIES) {
            try {
                client.indices()
                        .create(new CreateIndexRequest.Builder()
                                .index(INDEX)
                                .withJson(FileUtil.readJsonFileToReader(MAPPING_ITEM_INDEX_JSON))
                                .build())
                        .acknowledged();

                return "index name : " + INDEX;

            } catch (Exception e) {
                LOGGER.error("!! ElasticSearch createIndex error");
                LOGGER.error(e.getMessage());

                if (retryCount < MAX_RETRIES) {
                    LOGGER.info("Retrying to create index...");
                    deleteIndex(INDEX);
                    retryCount++;
                } else {
                    LOGGER.error("Maximum retry attempts reached.");
                    e.printStackTrace();
                    new RimumuException();
                    break;
                }
            }
        }
        return "create index failed";
    }


    public boolean deleteIndex(String indexName) {
        try {
            LOGGER.info("delete index");
            return client.indices()
                    .delete(new DeleteIndexRequest.Builder().index(indexName).build())
                    .acknowledged();
        } catch (Exception e) {
            LOGGER.error("delete index failed");
            LOGGER.error(e.getMessage());
            return false;
        }
    }

    public String indexData() {
        return indexData(initIndexName());
    }

    public String indexData(String indexName) {
        try {
            Map<String, Object> jsonData = gson.fromJson(readJsonFile(INSERT_ITEM_FILE_PATH).get("data"), Map.class);

            for (Map.Entry<String, Object> entry : jsonData.entrySet()) {
                String documentId = entry.getKey(); // 아이템 넘버 "1001"
                Map<String, Object> itemData = (Map<String, Object>) entry.getValue();

                // 각 하위 문서를 Elasticsearch에 색인화
                IndexRequest<Map<String, Object>> request = IndexRequest.of(builder -> builder
                        .index(indexName)
                        .id(documentId)
                        .document(itemData));

                IndexResponse response = client.index(request);

                return response.index();
            }

        } catch (Exception e) {
            LOGGER.error("insert document failed");
            LOGGER.error(e.getMessage());
        }
        // 클라이언트 종료 (필요한 경우)
        // client.close();
        return "insert document failed";
    }

    public Item get(int num) {
        LOGGER.info("item key : {}", num);
        Item result = new Item();

        TermQuery termQuery2 = new TermQuery.Builder()
                .field("_id")
                .value(num)
                .build();

        BoolQuery boolQuery = new BoolQuery.Builder()
                .must(termQuery2._toQuery())
                .build();

        SearchRequest request = new SearchRequest.Builder()
                .index(INDEX)
                .query(boolQuery._toQuery())
                .build();
        LOGGER.info("request : {}", request.query());

        try {

            SearchResponse response = client.search(request, Map.class);
            List<Hit<Map<String, Object>>> hits = response.hits().hits();

            if (ObjectUtils.isEmpty(hits) || 1 > hits.size()) {
                LOGGER.error("item response error!! hits size : {}", hits.size());
                return result;
            }

            Map<String, Object> hit = hits.get(0).source();
            result.setNum(num);
            result.setName((String) hit.get("name"));
            result.setDescription((String) hit.get("description"));
            Map<String, Object> goldField = (Map<String, Object>) hit.get("gold");
            result.setGold(((Number) goldField.get("total")).intValue());

        } catch (IOException e) {
            LOGGER.error("Item search error!");
            LOGGER.error(e.getMessage());
        }

        return result;
    }

}
