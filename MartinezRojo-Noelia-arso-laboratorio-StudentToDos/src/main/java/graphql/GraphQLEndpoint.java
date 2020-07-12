package graphql;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.annotation.WebServlet;

import com.coxautodev.graphql.tools.SchemaParser;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import graphql.schema.GraphQLSchema;
import graphql.servlet.SimpleGraphQLServlet;
import model.Task;
import persistence.TaskRepository;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/graphql")
public class GraphQLEndpoint extends SimpleGraphQLServlet {

	// MongoDB
	private static TaskRepository taskRepository;
	private static MongoClient client;
	
	// Rabbit MQ
	private static Connection msgConnection;
	private static Channel msgChannel;
    final static String EXCHANGE_NAME = "arso-exchange";
    final static String QUEUE_NAME = "arso-queue";
    final static String ROUTING_KEY = "arso-queue";

	public GraphQLEndpoint() throws TasksException {
		super(buildSchema());
	}

	@Override
	public void destroy() {
		super.destroy();
		client.close();
	}
	
	private static GraphQLSchema buildSchema() throws TasksException {

		initDB();
		
		try {
			initMsgQueue();
		} catch (Exception e) {
			throw new TasksException("Couldn't connect to message queue");
		}
		
		createTasksFromQueue();
		
		return SchemaParser.newParser()
				.file("schema.graphqls")
		        .resolvers(new Query(taskRepository))
		        .build().makeExecutableSchema();
	}
	
	
	private static void initDB() {
		MongoClientURI uri = new MongoClientURI(
			    "mongodb://arso:arso-20@cluster0-shard-00-00-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-01-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-02-xi0ku.azure.mongodb.net:27017/arso?ssl=true&replicaSet=Cluster0-shard-0&authSource=admin&retryWrites=true&w=majority");

		client = new MongoClient(uri);
	    MongoDatabase mongo = client.getDatabase("arso");
	    taskRepository = new TaskRepository(mongo.getCollection("tasks"));
	}

	private static void initMsgQueue() throws IOException, TimeoutException, 
	KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
		
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setUri("amqp://vcqubngz:jyA59K9eMnlB7zuqfh73lr5WeEPLjQ89@stingray.rmq.cloudamqp.com/vcqubngz");

	    msgConnection = factory.newConnection();

	    msgChannel = msgConnection.createChannel();
	   
	    try {
	        boolean durable = true;
	        msgChannel.exchangeDeclare(EXCHANGE_NAME, "direct", durable);

	        boolean exclusive = false;
	        boolean autodelete = false;
	        Map<String, Object> properties = null; // sin propiedades
	        msgChannel.queueDeclare(QUEUE_NAME, durable, exclusive, autodelete, properties);    
	        
	        msgChannel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
	    } catch (IOException e) {

	        String mensaje = e.getMessage() == null ? 
	        		e.getCause().getMessage() : e.getMessage();

	        System.out.println("No se ha podido establecer la conexion con el exchange o la cola: \n\t->" + mensaje);
	    }
	}
	
	private static void createTasksFromQueue() throws TasksException {
		boolean autoAck = false;
		try {
			msgChannel.basicConsume(QUEUE_NAME, autoAck, "arso-consumidor", 
				new DefaultConsumer(msgChannel) {
				    @Override
				    public void handleDelivery(
				    		String consumerTag, 
				    		Envelope envelope, 
				    		AMQP.BasicProperties properties,
				            byte[] body) throws IOException {
				        
				        String routingKey = envelope.getRoutingKey();
				        String contentType = properties.getContentType();
				        long deliveryTag = envelope.getDeliveryTag();
				        
				        if (!routingKey.equals(ROUTING_KEY) || 
				        		!contentType.equals("application/json") )
				        	return;

				        String content = new String(body, "UTF-8");

				        JsonReader jsonReader = Json.createReader(
				        		new StringReader(content.toString()));
				        JsonObject object = jsonReader.readObject();
				        
				        /* If event contains a student's id,
				         * remove their corresponding pending task,
				         * else create new task for all students
				         */
				        boolean newTask = !object.containsKey("studentId");
				        
				        if (newTask)
							try {
								createTask(object);
							} catch (TasksException e) {
								e.printStackTrace();
								
								// Exit to not ack messages
								System.exit(-1);
							}
						else 
				        	removeTask(object);

				        // Acknowledge consumed messages
				        msgChannel.basicAck(deliveryTag, false);
				    }
			});
		} catch (IOException e) {
			throw new TasksException("Couldn't connect to Users service");
		}
	}
	
	private static void removeTask(JsonObject object) {
		String studentId = object.getString("studentId").replace("\"", "");
		String id = object.getString("id").replace("\"", "");
		String service = object.getString("service").replace("\"", "");
		
		taskRepository.remove(studentId, id, service);
	}
	
	private static void createTask(JsonObject object) throws TasksException {
		
		// Get all students
    	List<String> studentIds;
		try {
			studentIds = UsersService.getAllStudents();
		} catch (IOException e) {
			throw new TasksException("Couldn't get students from Users service");
		}
		
		// Save a task in the database for each student
    	for(String studentId : studentIds) {
           
			DateFormat ISOformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			
			try {
        		Task newTask = new Task(
        				studentId,
		    			object.getString("description"),
		    			ISOformat.parse(object.get("deadline")
		    					.toString().replaceAll("\"", "")),
		    			object.getString("id"),
		    			object.getString("service")
        		);
        
        		taskRepository.save(newTask);
        		
			} catch (ParseException e) {
				throw new TasksException("Couldn't parse date to add to task");
			}
    	}
	}

}