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

import controlador.ControladorUsuarios;
import controlador.InterfazControladorUsuarios;
import controlador.UsuariosException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import modelo.Rol;
import modelo.Usuario;

@Path("usuarios")
@Api
public class RESTService {
	
	private InterfazControladorUsuarios controlador = ControladorUsuarios.getInstance();
	@Context
	private HttpServletRequest peticion;
	
	@GET
	@Path("/profesores")
	@Produces(MediaType.APPLICATION_XML)
	@ApiOperation(value = "Método de consulta de todos los usuarios con rol profesor", notes = "Retorna una lista de Usuario", response = List.class)
	@ApiResponses(value = { 
		@ApiResponse(code = HttpServletResponse.SC_OK, message = "Consulta con éxito"),
		@ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Error interno del servidor")
	})
	public Response getProfesores() throws UsuariosException {
		
		List<Usuario> profesores = controlador.getProfesores();
		return Response.status(Response.Status.OK).entity(profesores).type(MediaType.APPLICATION_XML).build();
	}
	
	@GET
	@Path("/{id}/rol")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Método de consulta del rol de un usuario", notes = "Retorna una cadena", response = Rol.class)
	@ApiResponses(value = { 
		@ApiResponse(code = HttpServletResponse.SC_OK, message = "Consulta con éxito"),
		@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Usuario no encontrado"), 
		@ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Error interno del servidor")
	})
	public Response getRol(@PathParam("id") String id) throws IOException, UsuariosException {
		
		Rol rol = controlador.getRol(id);
		if (rol == null)
			return Response.status(Response.Status.NOT_FOUND).build();
		return Response.status(Response.Status.OK).entity(rol.toString()).type(MediaType.TEXT_PLAIN).build();
		
	}
}
