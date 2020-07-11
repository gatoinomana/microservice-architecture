package rest.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import rest.exceptions.SurveyException;

@Provider
public class SurveyExceptionTreatment
	implements ExceptionMapper<SurveyException>{

	@Override
	public Response toResponse(SurveyException exception) {
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(exception.getMessage()).build();
	}
}
