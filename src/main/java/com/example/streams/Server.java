package com.example.streams;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.StreamsMetadata;

import spark.Spark;

import java.util.*;
import java.util.stream.Collectors;

/**
 * With httpie<br>
 * http get http://localhost:4567/metadata<br>
 * http get http://localhost:4567/host-info<br>
 * http get http://localhost:4567/metadata/:store<br>
 * http get http://localhost:4567/titles/counts<br>
 * http get http://localhost:4567/titles<br>
 * http get http://localhost:4567/titles/:title/counts<br>
 */
public class Server {

    private final Service service;
    private StandardResponse success = new StandardResponse(StatusResponse.SUCCESS);
    private StandardResponse error = new StandardResponse(StatusResponse.ERROR);

    public Server(KafkaStreams streams) {
        service = new Service(streams);
    }

    /**
     * Starts the http server and declares service.
     */
    void start() throws Exception {
        Spark.get("/metadata", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS,
                    new Gson().toJsonTree(service.allMetadata())));
        });

        Spark.get("/metadata/:store", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS,
                    new Gson().toJsonTree(service.allMetadataForStore(request.params(":store")))));
        });

        Spark.get("/host-info", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS,
                    new Gson().toJsonTree(service.hostInfo())));
        });

        Spark.get("/titles/counts", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS,
                    new Gson().toJsonTree(service.allTitlesAndCounts())));
        });

        Spark.get("/titles", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS,
                    new Gson().toJsonTree(service.allTitles())));
        });

        Spark.get("/titles/:title/counts", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS,
                    new Gson().toJsonTree(service.countForTitle(request.params(":title")))));
        });

        Spark.get("/healthcheck", (request, response) -> {
            String healthcheck = service.healthcheck();
            boolean ok = "OK".equals(healthcheck);

	    StandardResponse status = ok ? success : error;

	    if (!ok) response.status(503);
	    response.type("application/json");

            return new Gson().toJson(status);
        });
    }

    /**
     *
     */
    void stop() {
        Spark.stop();
    }

    /**
     * Endpoints.
     */
    private class Service {

        private final KafkaStreams streams;

        Service(KafkaStreams streams) {
            this.streams = streams;
        }

        /**
         *
         * @return
         */
        Collection<StreamsMetadata> allMetadata() {
            return streams.allMetadata();
        }

        /**
         *
         * @param store
         * @return
         */
        Collection<StreamsMetadata> allMetadataForStore(String store) {
            return streams.allMetadataForStore(store);
        }

        /**
         *
         * @return
         */
        List<KeyValue<Long, String>> allTitles() {
            return allElements("titles");
        }

        /**
         *
         * @return
         */
        List<KeyValue<String, Long>> allTitlesAndCounts() {
            return allElements("events-counts");
        }

        /**
         *
         * @param title
         * @return
         */
        Long countForTitle(String title) {

            try {
                ReadOnlyKeyValueStore<String, Long> kv =
                        streams.store("events-counts", QueryableStoreTypes.<String, Long>keyValueStore());
                return kv.get(title);

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return 0L;
            }
        }

        /**
         *
         * @return
         */
        Collection<HostInfo> hostInfo() {
            return allMetadata().stream().map(StreamsMetadata::hostInfo).collect(Collectors.toList());
        }

        /**
         *
         * @return
         */
        String healthcheck() {
            Collection<StreamsMetadata> stores = streams.allMetadata();
            long storescount = stores.stream()
                    .filter(meta -> meta.host().contains("localhost") && meta.port() == 4567)
                    .count();

            State state = streams.state();

            System.out.println(String.format("Application State: (%d, %s)", storescount, state.toString()));

            // KO if current node is down or if is in 'not running' state
            if (storescount == 0 || !state.isRunning()) return "KO";
            return "OK";
        }

        /**
         *
         * @param store
         * @return
         */
       private <K, V> List<KeyValue<K, V>> allElements(String store) {

            List<KeyValue<K, V>> list = new ArrayList<>();

            try {
                ReadOnlyKeyValueStore<K, V> kv =
                        streams.store(store, QueryableStoreTypes.<K, V>keyValueStore());

                Iterator<KeyValue<K, V>> it = kv.all();
                it.forEachRemaining(list::add);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            return list;
        }
    }

    /**
     *
     */
    private class StandardResponse {
        private StatusResponse status;
        private String message;
        private JsonElement data;

        StandardResponse(StatusResponse status) {
            this.status = status;
        }

        StandardResponse(StatusResponse status, JsonElement data) {
            this.status = status;
            this.data = data;
        }
    }

    /**
     *
     */
    private enum StatusResponse {
        SUCCESS("Success"), ERROR("Error");

        final private String status;

        StatusResponse(String status) {
            this.status = status;
        }

    }
}
