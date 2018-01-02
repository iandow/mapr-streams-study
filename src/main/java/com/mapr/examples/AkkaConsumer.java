package com.mapr.examples;/* Copyright (c) 2009 & onwards. MapR Tech, Inc., All rights reserved */

import com.google.common.io.Resources;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.Props;

public class AkkaConsumer {
    // Declare a new consumer.
    public static KafkaConsumer consumer;
    private static final ActorSystem system = ActorSystem.create("MyActorSystem");
    private static final ActorRef parser = system.actorOf(Props.create(AkkaPersister.Parser.class), "parser");

    private static void parse(String record) throws ParseException {
        parser.tell(new AkkaPersister.ParseMe(record), ActorRef.noSender());
    }
    public static void main(String[] args) {
        final Inbox inbox = Inbox.create(system);

        Runtime runtime = Runtime.getRuntime();
        if (args.length < 2) {
            System.err.println("ERROR: You must specify a stream:topic to consume data from.");
            System.err.println("USAGE:\n" +
                    "\tjava -cp `mapr classpath`:./nyse-taq-streaming-1.0-jar-with-dependencies.jar com.mapr.examples.Run akkaconsumer [stream:topic]\n" +
                    "Example:\n" +
                    "\tjava -cp `mapr classpath`:./nyse-taq-streaming-1.0-jar-with-dependencies.jar com.mapr.examples.Run akkaconsumer /user/mapr/mystream:mytopic");

        }

        String topic =  args[1] ;
        System.out.println("Subscribed to : "+ topic);

        configureConsumer();

        List<String> topics = new ArrayList<String>();
        topics.add(topic);
        // Subscribe to the topic.
        consumer.subscribe(topics);

        long pollTimeOut = 100;  // milliseconds
        long records_processed = 0L;

        // https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html
        long startTime = System.nanoTime();
        long last_update = 0;
        final int minBatchSize = 200;
        List<String> buffer = new ArrayList<>();


        try {
            while (true) {
                // Request unread messages from the topic.
                ConsumerRecords<String, String> records = consumer.poll(pollTimeOut);
                if (records.count() > 0) {
                    for (ConsumerRecord<String, String> record : records) {
                        buffer.add(record.value());
                    }
                    if (buffer.size() >= minBatchSize) {
                        for (String msg : buffer) {
                            parse(msg);
                        }
                        consumer.commitSync();
                        buffer.clear();
                    }
                    records_processed += records.count();

                    // Print performance stats once per second
                    if ((Math.floor(System.nanoTime() - startTime)/1e9) > last_update) {
                        // Ask the actor to print status
                        inbox.send(parser, new AkkaPersister.Status());
                        last_update++;
                        PerfMonitor.print_status(records_processed, startTime);
                    }
                }

            }
        } catch (Throwable throwable) {
            System.err.printf("%s", throwable.getStackTrace());
        } finally {
            consumer.close();
            System.out.println("Consumed " + records_processed + " messages from stream.");
            System.out.println("Finished.");
        }

    }

    public static void configureConsumer() {
        Properties props = new Properties();
        try {
            props.load(Resources.getResource("consumer.props").openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        props.put("enable.auto.commit","false");
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<String, String>(props);
    }

}
