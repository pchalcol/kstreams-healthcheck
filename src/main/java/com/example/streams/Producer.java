package com.example.streams;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KeyValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * First, create the topics.
 * <code>
 *     <ul>
 *     <li>./kafka-topics --create --topic events --zookeeper localhost:2181 --partitions 4 --replication-factor 1</li>
 *     <li>./kafka-topics --create --topic library --zookeeper localhost:2181 --partitions 4 --replication-factor 1 </li>
 *     </ul>
 * </code>
 */
public class Producer {

    public static void main(String[] args) throws Exception {

        @SuppressWarnings("unchecked")
        final List<KeyValue<Long, String>> titles = Arrays.asList(
                KeyValue.pair(1L, "Fresh Fruit For Rotting Vegetables"),
                KeyValue.pair(2L,"We Are the League"),
                KeyValue.pair(3L,"Live In A Dive"),
                KeyValue.pair(4L,"PSI"),
                KeyValue.pair(5L,"Totally Exploited"),
                KeyValue.pair(6L,"The Audacity Of Hype"),
                KeyValue.pair(7L, "Licensed to Ill"),
                KeyValue.pair(8L,"De La Soul Is Dead"),
                KeyValue.pair(9L,"Straight Outta Compton"),
                KeyValue.pair(10L,"Fear Of A Black Planet"),
                KeyValue.pair(11L,"Curtain Call - The Hits"),
                KeyValue.pair(12L,"The Calling"));

        titles.forEach(System.out::println);

        Serde<Long> longSerde = Serdes.Long();
        Serde<String> stringSerde = Serdes.String();

        List<ProducerRecord<Long, String>> records = titles.stream()
                .map(title -> new ProducerRecord<>("library", title.key, title.value))
                .collect(Collectors.toList());

        final KProducer<Long, String> kProducer = new KProducer<>();
        kProducer.produce(records, longSerde.serializer(), stringSerde.serializer());

        final Random random = new Random();

        while (true) {
            final KeyValue<Long, String> playedSong = titles.get(random.nextInt(titles.size()));
            List<ProducerRecord<Long, String>> lst = new ArrayList<>();
            lst.add(new ProducerRecord<>("events", playedSong.key, playedSong.value));

            kProducer.produce(lst, longSerde.serializer(), stringSerde.serializer());

            Thread.sleep(100L);
        }

    }

    private static class KProducer<Key, Value> {

        private final Properties props = Configurations.producerConfig();

        /**
         *
         * @param records
         * @param keySerializer
         * @param valueSerializer
         */
        void produce(Iterable<ProducerRecord<Key, Value>> records,
                     Serializer<Key> keySerializer,
                     Serializer<Value> valueSerializer) {

            final KafkaProducer<Key, Value> producer = new KafkaProducer<>(props, keySerializer, valueSerializer);

            try {
                records.forEach(elem -> {
                    System.out.println(String.format("%s - sending element %s",
                            Thread.currentThread().getName(),
                            elem.toString()));
                    producer.send(elem, (metadata, exception) -> {
                        if (null != exception)
                            System.out.println(String.format("Exception: %s", exception.getMessage()));
                    });
                });
            } finally {
                producer.flush();
                producer.close();
            }
        }
    }
}

/*
class Healthcheck {
// TODO
    private ExecutorService executor =
            new ThreadPoolExecutor(1,
                    1,
                    1000,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());

    void start() throws Exception {
        executor.submit(() -> {
            while (true) {

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}*/
