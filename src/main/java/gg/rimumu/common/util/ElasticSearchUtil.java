package gg.rimumu.common.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchUtil {

    private String host = "localhost";

    private int port = 9200;

    private String scheme = "http";


    private volatile ElasticsearchClient client;

    public ElasticsearchClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = createClient();
                }
            }
        }
        return client;
    }

    private ElasticsearchClient createClient() {
        RestClient restClient = RestClient.builder(new HttpHost(host, port, scheme)).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

}
