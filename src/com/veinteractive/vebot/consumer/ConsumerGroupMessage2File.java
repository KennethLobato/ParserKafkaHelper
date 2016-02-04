package com.veinteractive.vebot.consumer;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by klobato on 20/10/15.
 */

public class ConsumerGroupMessage2File {
    private final ConsumerConnector consumer;
    private final String topic;
    private ExecutorService executor;

    public ConsumerGroupMessage2File(String a_zookeeper, String a_groupId, String a_topic, String smallestLargest) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(createConsumerConfig(a_zookeeper, a_groupId, smallestLargest));
        this.topic = a_topic;
    }

    private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId, String smallestLargest) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("auto.offset.reset", smallestLargest);
        props.put("zookeeper.session.timeout.ms", "5000");
        props.put("zookeeper.sync.time.ms", "1000");
        props.put("auto.commit.interval.ms", "2000");

        return new ConsumerConfig(props);
    }

    public void shutdown() {
        if (consumer != null) consumer.shutdown();
        if (executor != null) executor.shutdown();
        System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
        try {
            if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {

            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }

    }


    public void run(int a_numThreads) throws IOException{
        //Make a map of topic as key and no. of threads for that topic
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(a_numThreads));
        //Create message streams for each topic
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        //initialize thread pool
        executor = Executors.newFixedThreadPool(a_numThreads);
        //start consuming from thread
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new ConsumerMessage2File(stream, threadNumber));
            threadNumber++;
        }
    }

    public static void main(String[] args)
    {
        if(args.length != 5)
        {
            System.out.println("Usage: ConsumerGroup2MessageFile <strZookeeperAddress:port> <strConsumerGroupName> <strTopicName> <\"smallest\"|\"largest\"> <iNumberOfThreads>");
            System.exit(1);
        }

        String zooKeeper = args[0];
        String groupId = args[1];
        String topic = args[2];
        String smallestLargest = args[3];
        int threads = Integer.parseInt(args[4]);

        try
        {
            ConsumerGroupMessage2File example = new ConsumerGroupMessage2File(zooKeeper, groupId, topic, smallestLargest);
            example.run(threads);
            Thread.sleep(1200000);

            example.shutdown();
        }
        catch (InterruptedException ie)
        {
            ie.printStackTrace();
            System.err.println(ie.toString());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            System.err.println(ioex.toString());
        }
    }
}