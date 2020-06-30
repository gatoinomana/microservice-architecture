package graphql;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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
import persistence.TodoRepository;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/graphql")
public class GraphQLEndpoint extends SimpleGraphQLServlet {

	private static TodoRepository todoRepository;
	private static MongoClient client;
	private static Connection msgConnection;
	private static Channel msgChannel;
    final static String exchangeName = "arso-exchange";
    final static String queueName = "arso-queue";
    final static String routingKey = "arso-queue";
	
	private static void initDB() {
		MongoClientURI uri = new MongoClientURI(
			    "mongodb://arso:arso-20@cluster0-shard-00-00-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-01-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-02-xi0ku.azure.mongodb.net:27017/arso?ssl=true&replicaSet=Cluster0-shard-0&authSource=admin&retryWrites=true&w=majority");

		client = new MongoClient(uri);
	    MongoDatabase mongo = client.getDatabase("arso");
	    todoRepository = new TodoRepository(mongo.getCollection("todos"));
	}

	private static void initMsgQueue() throws IOException, TimeoutException, 
	KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
		
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setUri("amqp://vcqubngz:jyA59K9eMnlB7zuqfh73lr5WeEPLjQ89@stingray.rmq.cloudamqp.com/vcqubngz");

	    msgConnection = factory.newConnection();

	    msgChannel = msgConnection.createChannel();
	   
	    try {
	        boolean durable = true;
	        msgChannel.exchangeDeclare(exchangeName, "direct", durable);

	        boolean exclusive = false;
	        boolean autodelete = false;
	        Map<String, Object> properties = null; // sin propiedades
	        msgChannel.queueDeclare(queueName, durable, exclusive, autodelete, properties);    
	        
	        msgChannel.queueBind(queueName, exchangeName, routingKey);
	    } catch (IOException e) {

	        String mensaje = e.getMessage() == null ? 
	        		e.getCause().getMessage() : e.getMessage();

	        System.out.println("No se ha podido establecer la conexion con el exchange o la cola: \n\t->" + mensaje);
	    }
	}
	
	private static void createTodosFromQueue() throws IOException {
		boolean autoAck = false;
		msgChannel.basicConsume(queueName, autoAck, "arso-consumidor", 
				new DefaultConsumer(msgChannel) {
		    @Override
		    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
		            byte[] body) throws IOException {
		        
		        String routingKey = envelope.getRoutingKey();
		        String contentType = properties.getContentType();
		        long deliveryTag = envelope.getDeliveryTag();

		        String content = new String(body, "UTF-8");
//		        Evento evento (Evento) unmarshaller.unmarshal(new StringReader(contenido));
//		        
//		        System.out.println("Evento: " + evento.getNombre());

//		        JsonReader jsonReader = Json.createReader(new StringReader(content));
//		        JsonObject object = jsonReader.readObject();

		        // ...

		        // Confirma el procesamiento
		        msgChannel.basicAck(deliveryTag, false);
		    }
		});
	}
	
	@Override
	public void destroy() {
		super.destroy();
		client.close();
	}

	public GraphQLEndpoint() throws KeyManagementException, 
	NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
		super(buildSchema());
	}

	private static GraphQLSchema buildSchema() throws KeyManagementException, 
	NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {

		initDB();
		
		initMsgQueue();
		
		createTodosFromQueue();

		return SchemaParser.newParser().file("schema.graphqls")
		        .resolvers(
		            new Query(todoRepository)
		            )
		        .build().makeExecutableSchema();
	}

}