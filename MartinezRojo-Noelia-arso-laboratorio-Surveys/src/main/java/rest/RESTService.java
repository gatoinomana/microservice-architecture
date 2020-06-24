package rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import persistence.SurveyRepository;

@Path("surveys")
@Api
public class RESTService {

	@Context
	private HttpServletRequest request;
	private static MongoClient client;
	private static SurveyRepository surveyRepository;
	
	/**
	 * Establecer la conexión con la base de datos y cargar las encuestas
	 */	
	private static void initDB() {
		MongoClientURI uri = new MongoClientURI(
			    "mongodb://arso:arso-20@cluster0-shard-00-00-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-01-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-02-xi0ku.azure.mongodb.net:27017/arso?ssl=true&replicaSet=Cluster0-shard-0&authSource=admin&retryWrites=true&w=majority");

		client = new MongoClient(uri);
	    MongoDatabase mongo = client.getDatabase("arso");
	    surveyRepository = new SurveyRepository(mongo.getCollection("surveys"));
	 }
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "r", notes = "", response = Boolean.class)
	@ApiResponses(value = { 
		@ApiResponse(code = HttpServletResponse.SC_OK, message = "Consulta con éxito"),
		@ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Error interno del servidor")
	})
	public Response getAllSurveys() throws IOException {
		initDB();

		// Use the User service to get role of the user
		String response = HTTPUtils.sendGET(
				"http://localhost:8080/api/users/pedro@email.com/role");
		
//		Date now = new Date();
//    			
//        Calendar c = Calendar.getInstance();
//        c.setTime(now);
//        c.add(Calendar.DAY_OF_MONTH, 1);
//        
//        Date nowPlusOne = c.getTime();
//    	
//    	Survey s1 = new Survey("titulo", "instrucciones", 
//    			now, nowPlusOne, 1, 1, Visibility.NEVER);
//    	
//    	Survey s2 = surveyRepository.saveSurvey(s1);
//    	boolean check = s1.equals(s2);
    	
		client.close();
		if (response != null)
			return Response.status(Response.Status.OK).entity(response).type(MediaType.TEXT_PLAIN).build();
		else 
			return Response.status(Response.Status.OK).entity(new String("response es null")).type(MediaType.TEXT_PLAIN).build();
	}
}
