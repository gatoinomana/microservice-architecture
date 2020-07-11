package rest.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ResourceNotFoundExceptionTreatment implements ExceptionMapper<ResourceNotFoundException>{

	@Override
	public Response toResponse(ResourceNotFoundException exception) {
		return Response.status(Response.Status.NOT_FOUND).entity(exception.getMessage()).build();
	}

}
