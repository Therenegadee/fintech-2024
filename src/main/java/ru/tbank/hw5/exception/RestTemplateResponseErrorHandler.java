package ru.tbank.hw5.exception;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        return httpResponse.getStatusCode().is5xxServerError() ||
                httpResponse.getStatusCode().is4xxClientError();
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {
        HttpStatusCode statusCode = httpResponse.getStatusCode();
        if (statusCode.is5xxServerError()) {
            switch ((HttpStatus) statusCode) {
                case INTERNAL_SERVER_ERROR -> throw new IntegrationException(
                        "Произошла ошибка на стороне вызываемого сервиса.");
                default -> throw new HttpClientErrorException(httpResponse.getStatusCode());
            }
        } else if (httpResponse.getStatusCode().is4xxClientError()) {
            switch ((HttpStatus) statusCode) {
                case BAD_REQUEST -> throw new BadRequestException(httpResponse.getStatusText());
                case NOT_FOUND -> throw new NotFoundException(httpResponse.getStatusText());
                default ->  throw new HttpClientErrorException(httpResponse.getStatusCode());
            }
        }
    }
}
