package rest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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

@Path("surveys")
@Api
public class RESTService {

	@Context
	private HttpServletRequest request;
	private static MongoClient client;
	private static SurveyRepository surveyRepository;
	private static SurveyControllerInterface controller;
	
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
			@FormParam("visibility") String _visibility) throws SurveyException, IOException {
		
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
		String surveyId = controller.createSurvey(title, instructions, starts, ends, 
				minOptions, maxOptions, visibility);
		
		client.close();
		
		if (surveyId == null)
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		
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
	
	// TODO: Cambiar a JSON
	@PATCH
	@Path("/{idSurvey}/options")
	@Produces(MediaType.TEXT_PLAIN)
	public Response addOption(
			@PathParam("idSurvey") String idSurvey,
			@FormParam("option") String option) throws SurveyException {
		initDB();
		controller.addOption(idSurvey, option);
		return null;
	}
	
}
