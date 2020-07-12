package rest;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import controller.UsersController;
import controller.UsersException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import model.Role;
import model.User;

@Path("users")
@Api
public class RESTService {
	
	private UsersController usersController = UsersController.getInstance();
	
	@GET
	@Path("/teachers")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(	value = "Returns a list of all users with the role \"TEACHER\"",
					notes = "Each user is returned as a JSON Object",
					response = List.class)
	@ApiResponses(value = { 
		@ApiResponse(code = HttpServletResponse.SC_OK, message = "Teachers returned"),
		@ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Some exception occurred")
	})
	public Response getAllTeachers() throws UsersException {
		
		List<User> allTeachers = usersController.getAllTeachers();
		
		JsonObject jsonObject = Json.createObjectBuilder()
				.add("teachers", jsonArrayFromUserList(allTeachers)).build();

		return Response.status(Response.Status.OK).entity(jsonObject.toString())
				.type(MediaType.APPLICATION_JSON).build();
	}
	
	@GET
	@Path("/students")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(	value = "Returns a list of all users with the role \"STUDENT\"",
					notes = "Each user is returned as a JSON Object",
					response = List.class)
	@ApiResponses(value = { 
		@ApiResponse(code = HttpServletResponse.SC_OK, message = "Students returned"),
		@ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Some exception occurred")
	})
	public Response getAllStudents() throws UsersException {
		
		List<User> allStudents = usersController.getAllStudents();
		
		JsonObject jsonObject = Json.createObjectBuilder()
				.add("students", jsonArrayFromUserList(allStudents)).build();

		return Response.status(Response.Status.OK).entity(jsonObject.toString())
				.type(MediaType.APPLICATION_JSON).build();
	}
	
	
	@GET
	@Path("/{id}/role")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(	value = "Returns role of a user, given their id", 
					notes = "Response is a JSON Object with a field 'role'")
	@ApiResponses(value = { 
		@ApiResponse(code = HttpServletResponse.SC_OK, message = "Role returned"),
		@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "No user with that id"), 
		@ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Some exception occurred")
	})
	public Response getRole(
			@ApiParam(value = "Id of user", required = true) 
			@PathParam("id") String id) throws UsersException {
		
		Role role = usersController.getRole(id);
		
		if (role == null)
			return Response.status(Response.Status.NOT_FOUND).build();
		
		JsonObject jsonObject = Json.createObjectBuilder().add("role", role.toString()).build();
		
		return Response.status(Response.Status.OK).entity(jsonObject.toString()).type(MediaType.APPLICATION_JSON).build();
	}
	
	/**
	 * Builds a JSON array from any list of users
	 */
	private static JsonArray jsonArrayFromUserList(List<User> users) {
		
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		
		for(User u : users) {
			arrayBuilder
				.add(Json.createObjectBuilder()
					.add("name", u.getName())
					.add("email", u.getEmail())
					.add("role", u.getRole().toString())
					.build()
				);
		}
		
		return arrayBuilder.build();
	}
}
