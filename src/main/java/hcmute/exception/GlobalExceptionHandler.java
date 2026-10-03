// File: GlobalExceptionHandler.java
package hcmute.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, ex.getStatus());
    }

    // Bắt thêm lỗi DataIntegrityViolationException từ DB nếu bắt trượt ở Logic Service
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Dữ liệu đang được tham chiếu, không thể thao tác (Conflict DB)!");
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}