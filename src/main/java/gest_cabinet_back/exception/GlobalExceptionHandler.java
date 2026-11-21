package gest_cabinet_back.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.core.MethodParameter;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String INTERNAL_ERROR_MESSAGE = "Une erreur interne est survenue.";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
        return error(exception.getStatus(), exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException() {
        return error(HttpStatus.UNAUTHORIZED, "Authentification requise ou identifiants invalides.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException() {
        return error(HttpStatus.FORBIDDEN, "Vous n'êtes pas autorisé à effectuer cette action.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " : " + Objects.requireNonNullElse(
                        fieldError.getDefaultMessage(), "valeur invalide"))
                .distinct()
                .toList();
        return error(HttpStatus.UNPROCESSABLE_CONTENT, "Les données fournies ne sont pas valides.", errors);
    }

    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(Exception exception) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, "Les paramètres fournis ne sont pas valides.");
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "La requête contient des données ou paramètres invalides.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported() {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Cette méthode HTTP n'est pas autorisée pour cette ressource.");
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound() {
        return error(HttpStatus.NOT_FOUND, "La ressource demandée est introuvable.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = status.is5xxServerError()
                ? INTERNAL_ERROR_MESSAGE
                : Objects.requireNonNullElse(exception.getReason(), status.getReasonPhrase());
        return error(status, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation() {
        return error(HttpStatus.CONFLICT, "L'opération entre en conflit avec des données existantes.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
        log.error("Erreur interne lors du traitement de la requête", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE);
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null || body instanceof ApiSuccessResponse<?> || body instanceof ApiErrorResponse
                || !MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)
                || !request.getURI().getPath().startsWith("/api/")) {
            return body;
        }
        return new ApiSuccessResponse<>(true, "Opération effectuée avec succès", body);
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message) {
        return error(status, message, List.of());
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status, String message, List<String> errors) {
        String safeMessage = (message == null || message.isBlank())
                ? status.getReasonPhrase()
                : message;
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(false, safeMessage, errors, status.value()));
    }
}
