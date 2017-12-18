package com.example.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;

public class Streams {
    public static void main(String[] args) throws Exception {
        final Properties streamConfig = Configurations.StreamConfig();


        // TODO Serdes -> ClassCastExceptions

        // builder
        //final KStreamBuilder builder = new KStreamBuilder();
        final StreamsBuilder builder = new StreamsBuilder();

        // play events
        final KStream<Long, String> kStream =
                builder.stream("events", Consumed.with(Serdes.Long(), Serdes.String()));
        // titles with ids
        final KTable<Long, String> table =
                builder.table("library", Consumed.with(Serdes.Long(), Serdes.String()), Materialized.as("titles"));

        kStream.map((k, v) -> {
            System.out.println(String.format("%s - k=%d, v=%s", Thread.currentThread().getName(), k, v));
            return KeyValue.pair(k, v);
        }).leftJoin(table, (k, v) -> v.toLowerCase(), Joined.with(Serdes.Long(), Serdes.String(), Serdes.String()))
                .groupBy((k, v) -> v, Serialized.with(Serdes.String(), Serdes.String()))
                .count(Materialized.as("events-counts"));

        // topology
        Topology topology = builder.build();

        // stream start
        final KafkaStreams streams = new KafkaStreams(topology, streamConfig);

        streams.start();
        System.out.println("Stream started");

        // server
        Server server = new Server(streams);
        server.start();

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
                streams.close();
                System.out.println("Stream stopped");
            } catch (Exception e) {
                // ignored
            }
        }));
    }
}
