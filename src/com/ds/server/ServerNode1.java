package com.ds.server;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.util.HashMap;

public class ServerNode1 {

  private final static String QUEUE_NAME = "Client1";
  private static final String EXCHANGE_NAME = "ConnectEx";

  @SuppressWarnings("unchecked")
public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    Channel channel1 = connection.createChannel();

    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    channel.queueDeclare("Server", false, false, false, null);
    channel.queueDeclare("Client3", false, false, false, null);
    int msg = 1;
    HashMap<Integer,String> mp = new HashMap<Integer,String>(); 
    mp.put(1,"user1");
    mp.put(2,"user2");
    mp.put(3,"user3");
    
    QueueingConsumer consumer = new QueueingConsumer(channel);
    QueueingConsumer consumer1 = new QueueingConsumer(channel);
    QueueingConsumer.Delivery delivery;
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
    channel.queueBind("Server", EXCHANGE_NAME, "");
    HashMap<Integer,String> msgMap;
    HashMap<Integer,String> gMap = new HashMap<Integer,String>();
    
    //String message = "Hello World! from client1" + msg;
    //channel.basicPublish("", QUEUE_NAME, null, SerializationUtils.serialize(mp));
    //System.out.println(" [x] Sent '" + message + "'" + mp.get("user1"));
    
   /* channel.basicConsume("Server", true, consumer);   	
    delivery = consumer.nextDelivery();
    msgMap = (HashMap<Integer,String>) SerializationUtils.deserialize(delivery.getBody());
    System.out.println(" [x] Received from client '" + msgMap + "'");
    channel.basicPublish("", QUEUE_NAME, null, SerializationUtils.serialize(msgMap));*/
    
    
    Consumer consume = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
          AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                  .Builder()
                  .correlationId(properties.getCorrelationId())
                  .build();

          boolean response=false;
          HashMap<Integer,String> resMap;

          try {
        	resMap = (HashMap<Integer,String>) SerializationUtils.deserialize(body);
        	for(Integer i : resMap.keySet()) {
        		if(gMap.containsKey(i)) {
        			response = false;
        		}
        		else {
           		 	gMap.putAll(resMap);
           		 	response = true;
        		}
        		
			}
            HashMap<Integer,Boolean> bMap = new HashMap<Integer,Boolean>();
            bMap.put(0, response);
            //System.out.println(bMap);
            channel.basicPublish( "", properties.getReplyTo(), replyProps, SerializationUtils.serialize(bMap));
            channel.basicPublish("", QUEUE_NAME, null, SerializationUtils.serialize(gMap));
          }
          catch (RuntimeException e){
            System.out.println(" [.] " + e.toString());
          }
        }
      };

    channel.basicConsume("Server", true, consume);
    
    System.out.println("gmap "+gMap);
    channel1.basicConsume("Client3", true, consumer1);
    delivery = consumer1.nextDelivery();
    //@SuppressWarnings("unchecked")
	msgMap = (HashMap<Integer,String>) SerializationUtils.deserialize(delivery.getBody());
    System.out.println(" [x] Received '" + "from client 3" + "':'" + msgMap + "'");
    
    
    
        
      
  }
}