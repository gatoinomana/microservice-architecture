package rest;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonPatch;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import controller.SurveyController;
import controller.SurveyException;
import io.swagger.annotations.Api;
import model.Survey;

@Path("surveys")
@Api
public class RESTService {

	private static SurveyController controller;
    
    public RESTService() throws SurveyException {
    	controller = SurveyController.getInstance();
    }

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createSurvey(Survey survey, @QueryParam("userId") String userId) 
			throws ForbiddenException, IOException, IllegalArgumentException, SurveyException {
		
		if (userId == null) {
			throw new IllegalArgumentException(
					"Missing 'userId' query parameter");
		}

		String id = controller.createSurvey(survey, userId);
		
		return Response
				.status(Response.Status.CREATED)
				.entity(controller.getSurvey(id))
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
	
	@PATCH
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON) 
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editSurvey(JsonPatchDTO patch, 
			@PathParam("id") String surveyId, @QueryParam("userId") String userId) 
					throws SurveyException, ForbiddenException, IOException {
	
		if (userId == null) {
			throw new IllegalArgumentException(
					"Missing 'userId' query parameter");
		}
		
		// Convert patch DTO to Java's JsonPatch
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
	    for(PatchOperationDTO op : patch) {
	    	builder.add(Json.createObjectBuilder()
					.add("op", op.getOp().toString())
					.add("path", op.getPath())
					.add("value", op.getValue().toString())
					.build()
					);
	    }
	    
	    JsonArray patchAsArray = builder.build();
		
		JsonPatch jsonPatch = Json.createPatch(patchAsArray);

		// Return patched survey
		return Response
				.status(Response.Status.CREATED)
				.entity(controller.editSurvey(surveyId, userId, jsonPatch))
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
	
//	@PATCH
//	@Path("/{id}/vote")
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response voteSurvey(Map<String, Boolean> selectedOptions, 
//			@PathParam("id") String surveyId, @QueryParam("userId") String userId) 
//					throws SurveyException {
//		
//		//TODO
//		
//		return null;
//	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteSurvey(@PathParam("id") String surveyId, 
			@QueryParam("userId") String userId) throws SurveyException, ForbiddenException {
		
		if (userId == null) {
			throw new IllegalArgumentException(
					"Missing 'userId' query parameter");
		}
		
		controller.removeSurvey(surveyId, userId);
		return Response.status(Response.Status.NO_CONTENT).build();
	}
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllSurveys(@QueryParam("userId") String userId) throws IOException, ForbiddenException, SurveyException {
		
		if (userId == null) {
			throw new IllegalArgumentException(
					"Missing 'userId' query parameter");
		}

		return Response
				.status(Response.Status.OK)
				.entity(controller.getAllSurveys(userId))
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getSurvey(@PathParam("id") String surveyId, 
			@QueryParam("userId") String userId) 
					throws IOException, ForbiddenException, SurveyException {
		
		if (userId == null) {
			throw new IllegalArgumentException(
					"Missing 'userId' query parameter");
		}

		return Response
				.status(Response.Status.OK)
				.entity(controller.getSurvey(surveyId, userId))
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
	
	
}
