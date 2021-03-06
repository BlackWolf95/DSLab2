package com.ds.server;

import java.util.HashMap;

import org.apache.commons.lang.SerializationUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ServerNode3 {

	private final static String QUEUE_NAME = "Client3";
	static QueueingConsumer consumer;
	static HashMap<Integer,String> toSend;
	
	  public static void main(String[] argv) throws Exception {
		  ConnectionFactory factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    Connection connection = factory.newConnection();
		    Channel channel = connection.createChannel();
		    
		    QueueingConsumer consumer = new QueueingConsumer(channel);
		    channel.basicConsume("Client2", true, consumer);
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            @SuppressWarnings("unchecked")
			HashMap<Integer,String> msgMap = (HashMap<Integer,String>) SerializationUtils.deserialize(delivery.getBody());
            //String routingKey = delivery.getEnvelope().getRoutingKey();
            System.out.println(" [x] Received '" + "from client 2" + "':'" + msgMap + "'");
            channel.basicPublish("", QUEUE_NAME, null, SerializationUtils.serialize(msgMap));
	  }
}
