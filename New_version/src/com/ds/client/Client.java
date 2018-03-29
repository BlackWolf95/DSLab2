package com.ds.client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.server.UID;
import java.util.ArrayList;
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
	private String aClient;
	private static final String EXCHANGE_NAME = "ConnectEx";
	private static final String EXCHANGE_NAME_MESSAGE = "ConnectExMsg";
	
	public Client() throws Exception{
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    connection = factory.newConnection();
	    channel = connection.createChannel();
	    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
	    channel.exchangeDeclare(EXCHANGE_NAME_MESSAGE, BuiltinExchangeType.FANOUT);
	    replyQueueName = channel.queueDeclare().getQueue();
	    aClient = channel.queueDeclare().getQueue();
	}
	
	static BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	
	public boolean call(String message) throws IOException, InterruptedException {
	    final String corrId = UUID.randomUUID().toString();
	    HashMap<Integer,Boolean> msgMap = null;

	    AMQP.BasicProperties props = new AMQP.BasicProperties
	            .Builder()
	            .correlationId(corrId)
	            .replyTo(replyQueueName)
	            .build();
	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    QueueingConsumer.Delivery delivery;
	    channel.queueBind(aClient, EXCHANGE_NAME_MESSAGE, "publish");
	    
	    if(message.endsWith("1")) {
	    	System.out.println("in 1");
		    channel.basicPublish(EXCHANGE_NAME, "server", props, SerializationUtils.serialize(message));
	
		    //final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);
		    
	
		    channel.basicConsume(replyQueueName, true, consumer);   	
		    delivery = consumer.nextDelivery();
		    System.out.println("HELLOOOOO");
			//@SuppressWarnings("unchecked")
			msgMap = (HashMap<Integer,Boolean>) SerializationUtils.deserialize(delivery.getBody());
		    System.out.println(" [x] Received from server '" + msgMap.get(0) + "'");
		    //return msgMap.get(0);
	    }
	    else if((message.endsWith("2"))) {
	    	System.out.println("in 2\n");
	    	
	    	channel.basicPublish(EXCHANGE_NAME, "server", props, SerializationUtils.serialize(message));
	    	//channel.queueBind("server", EXCHANGE_NAME, "");
	    	
	
		    //channel.basicConsume(replyQueueName, true, consumer);   	
		    //delivery = consumer.nextDelivery();
		   // System.out.println("HELLOOOOO");
			//@SuppressWarnings("unchecked")
			//msgMap = (HashMap<Integer,Boolean>) SerializationUtils.deserialize(delivery.getBody());
		    //System.out.println(" [x] Received from server '" + msgMap.get(0) + "'");
	    	//channel.queueBind(aClient, EXCHANGE_NAME_MESSAGE, "publish");
		    System.out.println("grouop chat hi\n");
		    channel.basicConsume(aClient, true, consumer);
		    delivery = consumer.nextDelivery();
		    
		    String sMsg = (String) SerializationUtils.deserialize(delivery.getBody());
		    System.out.println("Received from group chat " + sMsg);
		    
		    return true;
	    }
	    
	    //return true;
		return msgMap.get(0);
	}
	
	public void close() throws IOException {
	    connection.close();
	}
	
	public static void main(String[] argv) throws Exception {
		Client client = new Client();
	    String uName;
	    
	    //ArrayList<String> uMap = new ArrayList<String>();
	    String message = null;
	        
	    System.out.println("Enter the name\n");
	    uName = stdIn.readLine();
	    //UID userId = new UID();
	    //int userid = (int)userId;
	    //uMap.add(uName+"+"+"1");
	    System.out.println(uName);
	          
	    System.out.println(" [x] Sent '" + uName + "'");
//	    /System.out.println(client.call(uMap));
	    if(client.call(uName+"+"+"1")) {
	    	System.out.println("User Registered\n");
	    	message = getMessage();
	    }
	    else {
	    	System.out.println("User Already exists\n");
	    	message = getMessage();
	    }
	    
	    client.call(message);
	    //channel.basicPublish(EXCHANGE_NAME, "Server", null,SerializationUtils.serialize(message));
	    //System.out.println(" [x] Sent '" + message + "'");
	 }
	
	private static String getMessage() throws IOException{
	   
		String message;
	    System.out.println("Enter the Message\n");
	    message = stdIn.readLine();
	    return message+"+"+"2";
	}
	
}
