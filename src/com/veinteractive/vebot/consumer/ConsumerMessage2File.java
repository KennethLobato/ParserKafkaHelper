package com.veinteractive.vebot.consumer;

/**
 * Created by klobato on 20/10/15.
 */

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConsumerMessage2File implements Runnable{

    private KafkaStream m_stream;
    private int m_threadNumber;
    private static int name = 0;
    private File tempDir;

    public ConsumerMessage2File(KafkaStream a_stream, int a_threadNumber) throws IOException {
        m_threadNumber = a_threadNumber;
        m_stream = a_stream;
        tempDir = new File("messages");
        if(!tempDir.exists())
        {
            tempDir.mkdir();
        }
    }

    public void run(){

        ConsumerIterator<byte[], byte[]>it = m_stream.iterator();

        while(it.hasNext())
        {
            try
            {
                byte[] received_message = it.next().message();
                File inputFile = new File(tempDir, "message-"+name++);
                FileOutputStream fos = new FileOutputStream(inputFile);
                fos.write(received_message, 0, received_message.length);
                fos.flush();
                fos.close();

                System.out.println("Message number: " + name);

            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }
        }
    }
}