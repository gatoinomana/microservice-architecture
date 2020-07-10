package rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ForbiddenExceptionTreatment
	implements ExceptionMapper<ForbiddenException>{

	@Override
	public Response toResponse(ForbiddenException exception) {
		return Response.status(Response.Status.FORBIDDEN)
				.entity(exception.getMessage()).build();
	}
}
