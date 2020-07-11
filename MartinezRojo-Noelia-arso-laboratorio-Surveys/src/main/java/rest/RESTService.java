package rest;

import java.io.IOException;
import rest.exceptions.*;
import rest.patch.JsonPatchDTO;
import rest.patch.OperationDTO;
import rest.patch.PATCH;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
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
import io.swagger.annotations.Api;
import model.Survey;
import model.SurveyResponse;

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
	public Response createSurvey(
			Survey survey, @QueryParam("userId") String userId) 
	
				throws ForbiddenException, IOException, 
					IllegalArgumentException, SurveyException {
		
		if (userId == null) {
			throw new IllegalArgumentException(
					"Missing 'userId' query parameter");
		}

		String id = controller.createSurvey(survey, userId);
		
		survey.setId(id);
		
		return Response
				.status(Response.Status.CREATED)
				.entity(survey)
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
	
	@PATCH
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON) 
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editSurvey(JsonPatchDTO patchDTO, 
			@PathParam("id") String surveyId, @QueryParam("userId") String userId) 
	
				throws SurveyException, ForbiddenException, 
					IOException, ResourceNotFoundException {
	
		if (userId == null) {
			throw new IllegalArgumentException(
					"Missing 'userId' query parameter");
		}
		
		// Convert patch DTO to Java's JsonPatch
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
	    for(OperationDTO op : patchDTO) {
	    	JsonObjectBuilder opBuilder = Json.createObjectBuilder();
	    	opBuilder.add("op", op.getOp().toString());
	    	opBuilder.add("path", op.getPath());
	    	opBuilder.add("value", op.getValue());
	    	builder.add(opBuilder.build());
	    }
	    
	    JsonArray patchAsArray = builder.build();
		
		JsonPatch jsonPatch = Json.createPatch(patchAsArray);

		// Return patched survey
		return Response
				.status(Response.Status.OK)
				.entity(controller.editSurvey(surveyId, userId, jsonPatch))
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeSurvey(@PathParam("id") String surveyId, 
			@QueryParam("userId") String userId) 
	
				throws SurveyException, ForbiddenException, ResourceNotFoundException {
		
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
	public Response getAllSurveys(@QueryParam("userId") String userId) 
	
			throws IOException, ForbiddenException, SurveyException {
		
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
	public Response getSurvey(
			@PathParam("id") String surveyId, @QueryParam("userId") String userId) 
	
				throws ResourceNotFoundException, ForbiddenException, IOException {
		
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
	
	@PATCH
	@Path("/{id}/responses")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response fillSurvey(SurveyResponse response, 
			@PathParam("id") String surveyId, @QueryParam("userId") String userId) 
	
				throws SurveyException, ForbiddenException, 
					IOException, ResourceNotFoundException {
		
		if (userId == null) {
			throw new IllegalArgumentException(
					"Missing 'userId' query parameter");
		}
		
		controller.fillSurvey(surveyId, userId, response);
		
		return Response
				.status(Response.Status.OK)
				.build();
	}
	
	@GET
	@Path("/{id}/results")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getResults(
			@PathParam("id") String surveyId, @QueryParam("userId") String userId) 
				throws IOException, ForbiddenException, 
					SurveyException, ResourceNotFoundException {
		
		if (userId == null) {
			throw new IllegalArgumentException(
					"Missing 'userId' query parameter");
		}

		return Response
				.status(Response.Status.OK)
				.entity(controller.getResults(surveyId, userId))
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
}
