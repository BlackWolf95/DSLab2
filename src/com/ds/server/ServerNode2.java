package com.ds.server;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class ServerNode2 {

	private final static String QUEUE_NAME = "Client2";

	  public static void main(String[] argv) throws Exception {
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    
	    Consumer consumer = new DefaultConsumer(channel) {
		      @Override
		      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
		          throws IOException {
		        String message = new String(body, "UTF-8");
		        System.out.println(" [x] Received in Client2'" + message + "'");
		        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
		      }
		    };
		    channel.basicConsume("Client1", true, consumer);


	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    
	    channel.close();
	    connection.close();
	  }
}
