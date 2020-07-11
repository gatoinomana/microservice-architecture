package rest.exceptions;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class IOExceptionTreatment
	implements ExceptionMapper<IOException>{

	@Override
	public Response toResponse(IOException exception) {
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(exception.getMessage()).build();
	}
}
