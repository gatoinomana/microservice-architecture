package rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import rest.exceptions.*;
import rest.patch.JsonPatchDTO;
import rest.patch.OperationDTO;
import rest.patch.PATCH;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonPatch;
import javax.servlet.http.HttpServletResponse;
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
import io.swagger.annotations.*;
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
	@ApiOperation(	value = "Creates a survey", 
					notes = "Returns the created survey, including its id", 
					response = Survey.class) 
	@ApiResponses(value = {
			@ApiResponse(	code = HttpServletResponse.SC_CREATED, 
							message ="Survey created"),
			@ApiResponse(	code = HttpServletResponse.SC_BAD_REQUEST, 
							message ="Missing userId query parameter, invalid field values or trying to set responses"),
			@ApiResponse(	code = HttpServletResponse.SC_FORBIDDEN, 
							message ="User is not a teacher, or doesn't match creator field, or already has a non-past survey with the same title"),
			@ApiResponse(	code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							message ="Some exception occured"),
	})
	public Response createSurvey(
			@ApiParam(value = "Survey", required = true) Survey survey, 
			
			@ApiParam(value = "Id of user performing request", required = true) 
			@QueryParam("userId") String userId) 
	
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
	@ApiOperation(	value = "Edits a survey", 
					notes = "Returns the edited survey", 
					response = Survey.class) 
	@ApiResponses(value = {
		@ApiResponse(	code = HttpServletResponse.SC_OK, 
						message ="Survey edited"),
		@ApiResponse(	code = HttpServletResponse.SC_NOT_FOUND, 
						message ="No survey with that id"),
		@ApiResponse(	code = HttpServletResponse.SC_BAD_REQUEST, 
						message ="Missing userId query parameter, incorrect JSON Patch format, invalid value fields or trying to set responses"),
		@ApiResponse(	code = HttpServletResponse.SC_FORBIDDEN, 
						message ="User is not the creator or it is not a future survey"),
		@ApiResponse(	code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
						message ="Some exception occured"),
	})
	public Response editSurvey(JsonPatchDTO patchDTO, 
			@ApiParam(value = "Survey id", required = true)
			@PathParam("id") String surveyId, 
			
			@ApiParam(value = "Id of user performing request", required = true) 
			@QueryParam("userId") String userId) 
	
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
	@ApiOperation(	value = "Removes a survey" )
	@ApiResponses(value = {
			@ApiResponse(	code = HttpServletResponse.SC_NO_CONTENT, 
							message ="Survey removed"),
			@ApiResponse(	code = HttpServletResponse.SC_NOT_FOUND, 
							message ="No survey with that id"),
			@ApiResponse(	code = HttpServletResponse.SC_BAD_REQUEST, 
							message ="Missing userId query parameter"),
			@ApiResponse(	code = HttpServletResponse.SC_FORBIDDEN, 
							message ="User is not the creator or it is a past survey"),
	})
	public Response removeSurvey(
			@ApiParam(value = "Survey id", required = true)
			@PathParam("id") String surveyId, 
			
			@ApiParam(value = "Id of user performing request", required = true)
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
	@ApiOperation(	value = "Gets all surveys",
					response = List.class) 
	@ApiResponses(value = {
		@ApiResponse(	code = HttpServletResponse.SC_OK, 
						message ="Surveys returned"),
		@ApiResponse(	code = HttpServletResponse.SC_BAD_REQUEST, 
						message ="Missing userId query parameter"),
		@ApiResponse(	code = HttpServletResponse.SC_FORBIDDEN, 
						message ="User is neither a teacher nor a student"),
		@ApiResponse(	code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
						message ="Some exception occurred"),
	})
	public Response getAllSurveys(
			@ApiParam(value = "Id of user performing request", required = true)
			@QueryParam("userId") String userId) 
	
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
	@ApiOperation(	value = "Gets a survey by id",
					response = Survey.class) 
	@ApiResponses(value = {
		@ApiResponse(	code = HttpServletResponse.SC_OK, 
						message ="Survey returned"),
		@ApiResponse(	code = HttpServletResponse.SC_NOT_FOUND, 
						message ="No survey with that id"),
		@ApiResponse(	code = HttpServletResponse.SC_BAD_REQUEST, 
						message ="Missing userId query parameter"),
		@ApiResponse(	code = HttpServletResponse.SC_FORBIDDEN, 
						message ="User is a teacher but not the creator, or is a student but it is a future survey, or it is neither"),
		@ApiResponse(	code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
						message ="Some exception occurred"),
	})
	public Response getSurvey(
			@ApiParam(value = "Survey id", required = true)
			@PathParam("id") String surveyId, 
			
			@ApiParam(value = "Id of user performing request", required = true)
			@QueryParam("userId") String userId) 
	
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
	@ApiOperation(	value = "Adds a response to a Survey" ) 
	@ApiResponses(value = {
		@ApiResponse(	code = HttpServletResponse.SC_OK, 
						message ="Response added"),
		@ApiResponse(	code = HttpServletResponse.SC_NOT_FOUND, 
						message ="No survey with that id"),
		@ApiResponse(	code = HttpServletResponse.SC_BAD_REQUEST, 
						message ="Missing userId query parameter, some option doesn't exist or didn't satisfy minimum or maximum number of selected options"),
		@ApiResponse(	code = HttpServletResponse.SC_FORBIDDEN, 
						message ="User is not a student, or doesn't match student field, or as already filled this survey, or survey is not open"),
		@ApiResponse(	code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
						message ="Some exception occurred"),
	})
	public Response fillSurvey(
			@ApiParam(value = "SurveyResponse", required = true) 
			SurveyResponse response,
			
			@ApiParam(value = "Survey id", required = true)
			@PathParam("id") String surveyId, 
			
			@ApiParam(value = "Id of user performing request", required = true) 
			@QueryParam("userId") String userId) 
	
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
	@ApiOperation(	value = "Gets the results of a Survey", 
					notes = "Returns the % of votes per option as a map",
					response = Map.class ) 
	@ApiResponses(value = {
		@ApiResponse(	code = HttpServletResponse.SC_OK, 
						message ="Results returned"),
		@ApiResponse(	code = HttpServletResponse.SC_NOT_FOUND, 
						message ="No survey with that id"),
		@ApiResponse(	code = HttpServletResponse.SC_BAD_REQUEST, 
						message ="Missing userId query parameter"),
		@ApiResponse(	code = HttpServletResponse.SC_FORBIDDEN, 
						message ="User is not the creator, or is a student but results visibility conditions are not met, or is neither")
	})
	public Response getResults(
			@ApiParam(value = "Survey id", required = true)
			@PathParam("id") String surveyId,
			
			@ApiParam(value = "Id of user performing request", required = true)
			@QueryParam("userId") String userId) 
	
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
