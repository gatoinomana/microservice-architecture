package rest;

import java.io.IOException;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import controller.SurveyController;
import controller.SurveyControllerInterface;
import controller.SurveyException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import model.Survey;
import model.Visibility;
import persistence.SurveyRepository;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

@Path("surveys")
@Api
public class RESTService {

	@Context
	private HttpServletRequest request;
	private static MongoClient client;
	private static SurveyRepository surveyRepository;
	private static SurveyControllerInterface controller;
	private static Connection msgConnection;
	private static Channel msgChannel;
    final static String exchangeName = "arso-exchange";
    final static String queueName = "arso-queue";
    final static String routingKey = "arso-queue";
	
	// TODO: Devolver JSON
	/**
	 * Establecer la conexión con la base de datos y cargar las encuestas
	 */	
	private static void initDB() {
		MongoClientURI uri = new MongoClientURI(
			    "mongodb://arso:arso-20@cluster0-shard-00-00-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-01-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-02-xi0ku.azure.mongodb.net:27017/arso?ssl=true&replicaSet=Cluster0-shard-0&authSource=admin&retryWrites=true&w=majority");
		
		client = new MongoClient(uri);
		MongoDatabase mongo = client.getDatabase("arso");
		surveyRepository = SurveyRepository.getInstance(mongo.getCollection("surveys"));
	    controller = SurveyController.getInstance(surveyRepository);
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
	
	@POST
	@ApiOperation(	value = "Creación de una encuesta", 
					notes = "Retorna el identificador de la encuesta creada",
					response = String.class)
	@ApiResponses(value = { 
			@ApiResponse(	code = HttpServletResponse.SC_CREATED,
							message = "Encuesta creada"),
			@ApiResponse(	code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							message = "Error interno del servidor")
	})
	public Response createSurvey(
			@QueryParam("userId") String userId,
			@FormParam("title") String title,
			@FormParam("instructions") String instructions,
			@FormParam("starts") String _starts,
			@FormParam("ends") String _ends,
			@FormParam("minOptions") String _minOptions,
			@FormParam("maxOptions") String _maxOptions,
			@FormParam("visibility") String _visibility) throws SurveyException, IOException, KeyManagementException, NoSuchAlgorithmException, TimeoutException, URISyntaxException {
		
		// Comprobar que ningún parámetro sea nulo
		if (userId == null || title == null || instructions == null || 
				_starts == null || _ends == null || _minOptions == null || 
				_maxOptions == null || _visibility == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		
		// Comprobar que todos los parámetros se puedan parsear
		Date starts, ends;
		int minOptions, maxOptions;
		Visibility visibility;
		try {
			DateFormat format = new SimpleDateFormat("d-M-yyyy_HH:mm", 
					new Locale("es", "ES"));
			starts = format.parse(_starts);
			ends = format.parse(_ends);
			minOptions = Integer.parseInt(_minOptions);
			maxOptions = Integer.parseInt(_maxOptions);
			visibility = Visibility.valueOf(_visibility);
		} catch (ParseException e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		
		// Comprobar que el usuario tiene el rol profesor
		String urlGetRole = "http://localhost:8080/api/users/" + userId + "/role";
		String role = HTTPUtils.sendGET(urlGetRole);
		
		if (role == null)
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		else if (role.equals("STUDENT"))
			return Response.status(Response.Status.FORBIDDEN).build();
		
		// Conectar con base de datos
		initDB();
		
		// Delegar en el controlador
		String surveyId = controller.createSurvey(
				userId, title, instructions, starts, ends, 
				minOptions, maxOptions, visibility);
		
		client.close();
		
		if (surveyId == null)
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		
		// Informar al servicio StudentToDos
		initMsgQueue();
		
        String mensaje = "creada survey"; // Objeto JSON en formato cadena
        
    	msgChannel.basicPublish(exchangeName, routingKey, 
                new AMQP.BasicProperties.Builder()
                    .contentType("text/plain")
                    .build()                
                , mensaje.getBytes());
		
    	msgChannel.close();
    	msgConnection.close();
    	
		return Response.status(Response.Status.OK).entity(surveyId).type(MediaType.TEXT_PLAIN).build();
	}
	
	// TODO: Completar JSON con resto de atributos y comprobar formato HAL
	@GET
	@Produces("application/hal+json")
	@ApiOperation(value = "r", notes = "", response = List.class)
	@ApiResponses(value = { 
		@ApiResponse(code = HttpServletResponse.SC_OK, message = "Consulta con éxito"),
		@ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Error interno del servidor")
	})
	public Response getAllSurveys() throws IOException, SurveyException {
		initDB();

		List<Survey> surveys = controller.getAllSurveys();
		String surveysJSON = JSONUtils.jsonFromSurveys(surveys);	

		client.close();
		if (surveys != null)
			return Response.status(Response.Status.OK).entity(surveysJSON).type("application/hal+json").build();
		else 
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}
	
	// TODO: return JSON
	// TODO: Si opcion ya existe y quiere añadirla
	// TODO: Si opcion no existe y quiere borrarla
	@PATCH
	@Path("/{surveyId}/options")
	@Produces(MediaType.TEXT_PLAIN)
	public Response patchOption(
			@PathParam("surveyId") String surveyId,
			@FormParam("option") String option,
			@FormParam("action") String action) throws SurveyException {
		
		if (action.equals("add")) {
			initDB();
			controller.addOption(surveyId, option);
		}	
		else if(action.equals("remove")) {
			initDB();
			controller.removeOption(surveyId, option);
		}	
		else 
			return Response.status(Response.Status.BAD_REQUEST).build();
		
		client.close();
		return null;
	}
	
	// TODO: Comprobar que la edita el mismo usuario que la creo
	@PATCH
	@Path("/{surveyId}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response editSurvey(
			@QueryParam("userId") String userId,
			@PathParam("surveyId") String surveyId,
			@FormParam("title") String title,
			@FormParam("instructions") String instructions,
			@FormParam("starts") String _starts,
			@FormParam("ends") String _ends,
			@FormParam("minOptions") String _minOptions,
			@FormParam("maxOptions") String _maxOptions,
			@FormParam("visibility") String _visibility) throws SurveyException {
		
		// Comprobar que ningún parámetro sea nulo
		if (userId == null || title == null || instructions == null || 
				_starts == null || _ends == null || _minOptions == null || 
				_maxOptions == null || _visibility == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		
		// Comprobar que todos los parámetros se puedan parsear
		Date starts, ends;
		int minOptions, maxOptions;
		Visibility visibility;
		try {
			DateFormat format = new SimpleDateFormat("d-M-yyyy_HH:mm", 
					new Locale("es", "ES"));
			starts = format.parse(_starts);
			ends = format.parse(_ends);
			minOptions = Integer.parseInt(_minOptions);
			maxOptions = Integer.parseInt(_maxOptions);
			visibility = Visibility.valueOf(_visibility);
		} catch (ParseException e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		
		/* Comprobar que el usuario que quiere editarla
		 * es el mismo que la creó */
		
		// Conectar con base de datos
		initDB();
		
		String creatorId = surveyRepository.findCreatorById(surveyId);
		if (!userId.equals(creatorId))
			return Response.status(Response.Status.FORBIDDEN).build();
		
		boolean success = controller.editSurvey(surveyId, title, instructions, starts, ends, minOptions, maxOptions, visibility);
		
		client.close();
		
		if (!success)
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			
		return Response.status(Response.Status.OK).entity(surveyId).type(MediaType.TEXT_PLAIN).build();
	}
	
	@PATCH
	@Path("/{surveyId}/results")
	@Consumes("application/x-www-form-urlencoded")
	public void vote(MultivaluedMap<String, String> formParams) {
		
	}
	
	
//	@DELETE
//	@Path("/{surveyId}")
//	@Produces(MediaType.TEXT_PLAIN)
//	public Response removeSurvey(
//			@PathParam("surveyId") String surveyId) throws SurveyException {
//		
//		controller.removeSurvey();
//		return null;
//	}
	
}
