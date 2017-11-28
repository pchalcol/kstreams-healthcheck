package com.example.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Properties;

public class Streams {
    public static void main(String[] args) throws Exception {
        final Properties streamConfig = Configurations.StreamConfig();

        // builder
        final KStreamBuilder builder = new KStreamBuilder();

        // topology
        final KStream<Long, String> kStream = builder.stream(Serdes.Long(), Serdes.String(), "events"); // play events
        final KTable<Long, String> table = builder.table(Serdes.Long(), Serdes.String(), "library", "titles"); // titles with ids

        kStream.map((k, v) -> {
            System.out.println(String.format("%s - k=%d, v=%s", Thread.currentThread().getName(), k, v));
            return KeyValue.pair(k, v);
        }).leftJoin(table, (k, v) -> v.toLowerCase(), Serdes.Long(), Serdes.String())
                .groupBy((k, v) -> v, Serdes.String(), Serdes.String())
                .count("events-counts");

        // stream start
        final KafkaStreams streams = new KafkaStreams(builder, streamConfig);

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
