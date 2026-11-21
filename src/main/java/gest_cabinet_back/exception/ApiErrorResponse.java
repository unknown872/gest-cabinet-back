package gest_cabinet_back.exception;

import java.util.List;

public record ApiErrorResponse(boolean success, String message, List<String> errors, int statusCode) {
}
