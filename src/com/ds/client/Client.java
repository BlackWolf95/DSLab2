package com.ds.client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.SerializationUtils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;


public class Client {	
	
	static int idCount = 0;
	private Connection connection;
	private Channel channel;
	private String replyQueueName;
	private static final String EXCHANGE_NAME = "ConnectEx";
	
	public Client() throws Exception{
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    connection = factory.newConnection();
	    channel = connection.createChannel();
	    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
	    replyQueueName = channel.queueDeclare().getQueue();
	}
	
	static BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	
	public boolean call(HashMap<Integer,String> message) throws IOException, InterruptedException {
	    final String corrId = UUID.randomUUID().toString();

	    AMQP.BasicProperties props = new AMQP.BasicProperties
	            .Builder()
	            .correlationId(corrId)
	            .replyTo(replyQueueName)
	            .build();

	    channel.basicPublish(EXCHANGE_NAME, "server", props, SerializationUtils.serialize(message));

	    //final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);
	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    QueueingConsumer.Delivery delivery;

	    channel.basicConsume(replyQueueName, true, consumer);   	
	    delivery = consumer.nextDelivery();
	    System.out.println("HELLOOOOO");
		//@SuppressWarnings("unchecked")
		HashMap<Integer,Boolean> msgMap = (HashMap<Integer,Boolean>) SerializationUtils.deserialize(delivery.getBody());
	    System.out.println(" [x] Received from server '" + msgMap.get(0) + "'");
	    //return msgMap.get(0);
	    return true;
	}
	
	public void close() throws IOException {
	    connection.close();
	}
	
	public static void main(String[] argv) throws Exception {
		Client client = new Client();
	    String uName;
	    
	    HashMap<Integer,String> uMap = new HashMap<Integer,String>();
	    String message = null;
	        
	    System.out.println("Enter the name\n");
	    uName = stdIn.readLine();
	    uMap.put(idCount,uName);
	          
	    System.out.println(" [x] Sent '" + uMap + "'");
//	    /System.out.println(client.call(uMap));
	    if(client.call(uMap)) {
	    	message = getMessage();
	    	System.out.println(message);
	    }
	    else
	    	System.out.println("Already exists\n");
	
	    //channel.basicPublish(EXCHANGE_NAME, "Server", null,SerializationUtils.serialize(message));
	    //System.out.println(" [x] Sent '" + message + "'");
	 }
	
	private static String getMessage() throws IOException{
	   
		String message;
	    System.out.println("Enter the Message\n");
	    message = stdIn.readLine();
	    return message;
	}
}
