package rest;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import controller.UserController;
import controller.UserControllerInterface;
import controller.UserException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import model.Role;
import model.User;

@Path("users")
@Api
public class RESTService {
	
	private UserControllerInterface controller = UserController.getInstance();
	@Context
	private HttpServletRequest peticion;
	
	@GET
	@Path("/teachers")
	@Produces(MediaType.APPLICATION_XML)
	@ApiOperation(value = "Método de consulta de todos los usuarios con rol profesor", notes = "Retorna una lista de Usuario", response = List.class)
	@ApiResponses(value = { 
		@ApiResponse(code = HttpServletResponse.SC_OK, message = "Consulta con éxito"),
		@ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Error interno del servidor")
	})
	public Response getProfesores() throws UserException {
		
		List<User> profesores = controller.getAllTeachers();
		return Response.status(Response.Status.OK).entity(profesores).type(MediaType.APPLICATION_XML).build();
	}
	
	@GET
	@Path("/{id}/role")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Método de consulta del rol de un usuario", notes = "Retorna una cadena", response = Role.class)
	@ApiResponses(value = { 
		@ApiResponse(code = HttpServletResponse.SC_OK, message = "Consulta con éxito"),
		@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Usuario no encontrado"), 
		@ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Error interno del servidor")
	})
	public Response getRol(@PathParam("id") String id) throws IOException, UserException {
		
		Role rol = controller.getRole(id);
		if (rol == null)
			return Response.status(Response.Status.NOT_FOUND).build();
		return Response.status(Response.Status.OK).entity(rol.toString()).type(MediaType.TEXT_PLAIN).build();
		
	}
}
