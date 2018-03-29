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
import java.util.ArrayList;
import java.util.HashMap;

public class ServerNode1 {

  private final static String QUEUE_NAME = "Client1";
  private static final String EXCHANGE_NAME = "ConnectEx";
  private static final String EXCHANGE_NAME_MESSAGE = "ConnectExMsg";
  static ArrayList<String> resMap = new ArrayList<String>();

  @SuppressWarnings("unchecked")
public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    Channel channel1 = connection.createChannel();

    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    channel.queueDeclare("Server", false, false, false, null);
    channel.queueDeclare("aClient", false, false, false, null);
    channel.queueDeclare("Client3", false, false, false, null);
    
    int msg = 1;
    ArrayList<String> mp = new ArrayList<String>(); 
    mp.add("user1");
    mp.add("user2");
    mp.add("user3");
    
    QueueingConsumer consumer = new QueueingConsumer(channel);
    QueueingConsumer consumer1 = new QueueingConsumer(channel);
    QueueingConsumer.Delivery delivery;
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
    channel.exchangeDeclare(EXCHANGE_NAME_MESSAGE, BuiltinExchangeType.FANOUT);
    channel.queueBind("Server", EXCHANGE_NAME, "");
    channel.queueBind("aClient", EXCHANGE_NAME_MESSAGE, "");
    String msgMap;
    ArrayList<String> gMap = new ArrayList<String>();
    
    //String message = "Hello World! from client1" + msg;
    //channel.basicPublish("", QUEUE_NAME, null, SerializationUtils.serialize(mp));
    //System.out.println(" [x] Sent '" + message + "'" + mp.get("user1"));
    
   /* channel.basicConsume("Server", true, consumer);   	
    delivery = consumer.nextDelivery();
    msgMap = (ArrayList<String>) SerializationUtils.deserialize(delivery.getBody());
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
        

          try {
        	String gMap = (String) SerializationUtils.deserialize(body);
        	if(gMap.endsWith("1")) {
	        	for(int i=0; i <= resMap.size(); i++) {
	        		System.out.println(resMap);
	        		if(resMap.contains(gMap)) {
	        			response = false;
	        		}
	        		else {
	           		 	resMap.add(gMap);
	           		 	response = true;
	           			System.out.println(response);
	           			break;
	        		}
	        		
				}
	        	HashMap<Integer,Boolean> bMap = new HashMap<Integer,Boolean>();
	            bMap.put(0, response);
	            //System.out.println(bMap);
	            channel.basicPublish( "", properties.getReplyTo(), replyProps, SerializationUtils.serialize(bMap));
	            System.out.println("gmap "+gMap);
	            channel.basicPublish("", QUEUE_NAME, null, SerializationUtils.serialize(gMap));
        	}
        	else if(gMap.endsWith("2")) {
        		System.out.println("in server 2\n");
        		channel.basicPublish(EXCHANGE_NAME_MESSAGE,"publish" ,null, SerializationUtils.serialize(gMap));
        	}
            
          }
          catch (RuntimeException e){
            System.out.println(" [.] " + e.toString());
          }
        }
      };

    channel.basicConsume("Server", true, consume);
    
    
    channel1.basicConsume("Client3", true, consumer1);
    delivery = consumer1.nextDelivery();
    //@SuppressWarnings("unchecked")
	msgMap = (String) SerializationUtils.deserialize(delivery.getBody());
    System.out.println(" [x] Received '" + "from client 3" + "':'" + msgMap + "'");
    
    
    
        
      
  }
}