package hcmute.rest;

import hcmute.dto.request.EmployeeCreateRequest;
import hcmute.dto.request.EmployeeUpdateRequest;
import hcmute.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    // Yêu cầu token phải có Role ADMIN
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeCreateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            employeeService.createEmployee(request);
            response.put("success", true);
            response.put("message", "Tạo tài khoản nhân viên thành công");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return handleException(e, response);
        }
    }

    @PutMapping("/{maNV}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateEmployee(@PathVariable Long maNV, @RequestBody EmployeeUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            employeeService.updateEmployee(maNV, request);
            response.put("success", true);
            response.put("message", "Cập nhật phân quyền nhân viên thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, response);
        }
    }

    @PatchMapping("/{maNV}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> toggleStatus(@PathVariable Long maNV) {
        Map<String, Object> response = new HashMap<>();
        try {
            employeeService.toggleStatus(maNV);
            response.put("success", true);
            response.put("message", "Thay đổi trạng thái nhân viên thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, response);
        }
    }

    // Hàm xử lý Exception tái sử dụng
    private ResponseEntity<?> handleException(Exception e, Map<String, Object> response) {
        response.put("success", false);
        String errorMessage = e.getMessage();

        if (errorMessage != null && errorMessage.startsWith("BAD_REQUEST:")) {
            response.put("message", errorMessage.split(":")[1]);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else if (errorMessage != null && errorMessage.startsWith("UNAUTHORIZED:")) {
            response.put("message", errorMessage.split(":")[1]);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        response.put("message", "Đã xảy ra lỗi hệ thống: " + errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}