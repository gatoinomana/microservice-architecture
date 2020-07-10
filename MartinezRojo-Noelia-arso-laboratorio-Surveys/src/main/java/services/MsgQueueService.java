package services;

import java.io.IOException;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import controller.SurveyException;

public class MsgQueueService {
	
	final private static String URI = "amqp://vcqubngz:jyA59K9eMnlB7zuqfh73lr5WeEPLjQ89@stingray.rmq.cloudamqp.com/vcqubngz";
	final private static String EXCHANGE_NAME = "arso-exchange";
    final private static String QUEUE_NAME = "arso-queue";
    final private static String ROUTING_KEY = "arso-queue";
	
	private static MsgQueueService instance;
	private static Connection msgConnection;
	private static Channel msgChannel;

	public static MsgQueueService getInstance() throws SurveyException {
		if (instance == null)
			instance = new MsgQueueService();
		return instance;
	}
	
	private MsgQueueService() throws SurveyException {
		initMsgQueue();
	}
	
	public void publishMessage(String jsonString) throws SurveyException {
		try {
			msgChannel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, 
			      new AMQP.BasicProperties.Builder()
			          .contentType("application/json")
			          .build()                
			      , jsonString.getBytes());
		} catch (IOException e) {
			throw new SurveyException("Couldn't publish message to queue");
		}
	}
	
	private static void initMsgQueue() throws SurveyException {
	    try {
	    	// Connect to exchange
		    ConnectionFactory factory = new ConnectionFactory();
		    factory.setUri(URI);
		    msgConnection = factory.newConnection();
		    msgChannel = msgConnection.createChannel();
	        boolean durable = true;
	        
	        msgChannel.exchangeDeclare(EXCHANGE_NAME, "direct", durable);
	        
	        // Connect to queue
	        boolean exclusive = false;
	        boolean autodelete = false;
	        Map<String, Object> properties = null; // without properties
	        
	        msgChannel.queueDeclare(QUEUE_NAME, durable, exclusive, autodelete, properties);    
	        msgChannel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
	        
	    } catch (Exception e) {
	       throw new SurveyException("Couldn't connect to exchange or queue");
	    }
	}

}
