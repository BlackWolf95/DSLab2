package com.ds.server;

import java.util.HashMap;
import org.apache.commons.lang.SerializationUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ServerNode2 {

	private final static String QUEUE_NAME = "Client2";
	
	  public static void main(String[] argv) throws Exception {
		  
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    channel.queueDeclare("Client2", false, false, false, null);
	    
	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume("Client1", true, consumer);
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        String msgMap = (String) SerializationUtils.deserialize(delivery.getBody());
        //String routingKey = delivery.getEnvelope().getRoutingKey();
        System.out.println(" [x] Received '" + "from client1" + "':'" + msgMap + "'");
        channel.basicPublish("", QUEUE_NAME, null, SerializationUtils.serialize(msgMap));
	  }
}
