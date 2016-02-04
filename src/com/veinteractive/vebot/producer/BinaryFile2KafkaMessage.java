package com.veinteractive.vebot.producer;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

/**
 * Created by klobato on 20/10/15.
 */

public class BinaryFile2KafkaMessage {

    public void producer(String brokerList, String dirPath, String topic) throws IOException
    {
        Properties props = new Properties();
        props.put("metadata.broker.list", brokerList);
        props.put("key.serializer", "kafka.serializer.StringEnconder");
        props.put("value.serializer", "kafka.serializer.ByteArraySerializer");
        //props.put("serializer.class", "kafka.serializer.DefaultEncoder");
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, byte[]> producer = new Producer<String, byte[]>(config);
        Random rnd = new Random();
        File folder = new File(dirPath);

        for(final File fileEntry : folder.listFiles())
        {
            int i = rnd.nextInt();
            byte[] data = new byte[(int)fileEntry.length()];
            FileInputStream fis = new FileInputStream(fileEntry);
            fis.read(data, 0, data.length);
            fis.close();
            //ProducerRecord<String, byte[]> message = new ProducerRecord<String,byte[]>(topic, Integer.toString(i), data);
            KeyedMessage<String, byte[]> message = new KeyedMessage<String, byte[]>(topic, data);
            producer.send(message);
        }

        producer.close();
    }

    public static void main(String[] args) throws IOException
    {
        if(args.length != 3)
        {
            System.out.println("Usage: BinaryFile2KafkaMessage <strBrokerList> <strMessageDir> <strTopicName>");
            System.exit(1);
        }

        BinaryFile2KafkaMessage test = new BinaryFile2KafkaMessage();
        test.producer(args[0], args[1], args[2]);
    }
}
