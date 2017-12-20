package com.example.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Properties;

public class Streams {
    public static void main(String[] args) throws Exception {
        final Properties streamConfig = Configurations.StreamConfig();

        // builder
        final StreamsBuilder builder = new StreamsBuilder();

        // play events
        final KStream<Long, String> kStream =
                builder.stream("events", Consumed.with(Serdes.Long(), Serdes.String()));
        // titles with ids
        final KTable<Long, String> table =
                builder.table("library", Materialized.<Long, String, KeyValueStore<Bytes, byte[]>>as("titles")
                        .withKeySerde(Serdes.Long())
                        .withValueSerde(Serdes.String()));

        KStream<Long, String> stream = kStream.map((k, v) -> {
            System.out.println(String.format("%s - k=%d, v=%s", Thread.currentThread().getName(), k, v));
            return KeyValue.pair(k, v);
        });

        KStream<Long, String> leftJoinedStream = stream.leftJoin(table,
                        (event, title) -> title.toLowerCase(),
                        Joined.with(Serdes.Long(), Serdes.String(), Serdes.String()));

         KGroupedStream<String, String> groupedStream = leftJoinedStream
                 .groupBy((k, v) -> { System.out.println(String.format("%s - groupBy: k=%d, v=%s", Thread.currentThread().getName(), k, v)); return v; },
                         Serialized.with(Serdes.String(), Serdes.String()));

        groupedStream.count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("events-counts")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.Long()));

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
