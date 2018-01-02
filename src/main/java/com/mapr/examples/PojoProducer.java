package com.mapr.examples;

        import com.google.common.io.Resources;
        import org.apache.kafka.clients.producer.Callback;
        import org.apache.kafka.clients.producer.KafkaProducer;
        import org.apache.kafka.clients.producer.ProducerRecord;
        import org.apache.kafka.clients.producer.RecordMetadata;
        import org.apache.log4j.Level;
        import org.apache.log4j.Logger;

        import java.io.*;
        import java.util.LinkedList;
        import java.util.List;
        import java.util.Properties;

/************
 PURPOSE:

 Shows how to stream plain old java objects (POJOs). Also shows how to invoke a synchronous callback after the pojo is sent.

 **********/

public class PojoProducer {

    public static KafkaProducer producer;

    public static void main(String[] args) throws IOException {

        if (args.length != 2 && args.length != 3) {
            System.err.println("USAGE:\n" +
                    "\tjava -cp `mapr classpath`:./nyse-taq-streaming-1.0-jar-with-dependencies.jar com.mapr.examples.Run pojoproducer stream:topic\n" +
                    "Example:\n" +
                    "\tjava -cp `mapr classpath`:./nyse-taq-streaming-1.0-jar-with-dependencies.jar com.mapr.examples.Run pojoproducer /user/mapr/mystream:mytopic");

        }

        String topic =  args[1] ;
        System.out.println("Publishing to topic: "+ topic);
        configureProducer();

        try {
            // generate POJO data
            Person person1 = new Person();
            person1.setName("Ian");
            person1.setAddress("123 Main St");
            person1.setAge(34);
            List<String> hobbies = new LinkedList<>();
            hobbies.add("ski");
            hobbies.add("boat");
            hobbies.add("fly");
            person1.setHobbies(hobbies);

            //Prepare bytes to send:
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(person1);
            out.flush();
            final byte[] value = bos.toByteArray();

            final String key = Long.toString(System.nanoTime());
            ProducerRecord<String, byte[]> rec = new ProducerRecord<String, byte[]>(topic,key,value);
            producer.send(rec,
                    new Callback() {
                        public void onCompletion(RecordMetadata metadata, Exception e) {
                            long current_time = System.nanoTime();

                            System.out.print(".");

                            System.out.printf("\tSent: '%s'\n" +
                                            "\t\tdelay = %.2f\n" +
                                            "\t\ttopic = %s\n" +
                                            "\t\tpartition = %d\n" +
                                            "\t\toffset = %d\n",
                                    value,
                                    (current_time - Long.valueOf(key))/1e9,
                                    metadata.topic(),
                                    metadata.partition(), metadata.offset());
                        }
                    });
            producer.flush();
            producer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /* Set the value for a configuration parameter.
     This configuration parameter specifies which class
     to use to serialize the value of each message.*/
    public static void configureProducer() {
        Properties props = new Properties();
        try {
            props.load(Resources.getResource("producer.props").openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.ByteArraySerializer");
        producer = new KafkaProducer<String, String>(props);
    }
}