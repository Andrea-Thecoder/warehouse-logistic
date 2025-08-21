package it.warehouse.optimization.exception;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMapper {

    @ServerExceptionMapper
    public RestResponse<ExceptionResponse> mapException(RuntimeException ex) {
        return ResponseBuilder
                .create(Status.BAD_REQUEST, new ExceptionResponse(Status.BAD_REQUEST.getStatusCode(), ex))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @ServerExceptionMapper
    public RestResponse<ExceptionResponse> mapException(InvalidFormatException ex) {
        return ResponseBuilder
                .create(Status.BAD_REQUEST, new ExceptionResponse(Status.BAD_REQUEST.getStatusCode(), ex))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
    
    @ServerExceptionMapper
    public RestResponse<ExceptionResponse> mapException(NotFoundException ex) {
        return ResponseBuilder
                .create(Status.NOT_FOUND, new ExceptionResponse(Status.NOT_FOUND.getStatusCode(), ex))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @ServerExceptionMapper
    public RestResponse<ExceptionResponse> mapException(JsonParseException ex) {
        return ResponseBuilder
                .create(Status.BAD_REQUEST, new ExceptionResponse(Status.BAD_REQUEST.getStatusCode(), ex))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @ServerExceptionMapper
    public RestResponse<ExceptionResponse> mapException(UnauthorizedException ex) {
        return ResponseBuilder
                .create(Status.UNAUTHORIZED, new ExceptionResponse(Status.UNAUTHORIZED.getStatusCode(), ex))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @ServerExceptionMapper
    public RestResponse<ExceptionResponse> mapException(ForbiddenException ex) {
        return ResponseBuilder
                .create(Status.FORBIDDEN, new ExceptionResponse(Status.FORBIDDEN.getStatusCode(), ex))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}

