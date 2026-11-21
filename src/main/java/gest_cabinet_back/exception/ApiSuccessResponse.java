package gest_cabinet_back.exception;

public record ApiSuccessResponse<T>(boolean success, String message, T data) {
}
