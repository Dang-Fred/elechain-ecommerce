// File: ChiNhanhRestController.java
package hcmute.rest;

import hcmute.dto.request.ChiNhanhRequest;
import hcmute.dto.response.ChiNhanhResponse;
import hcmute.service.ChiNhanhService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chi-nhanh")
public class ChiNhanhRestController {

    @Autowired
    private ChiNhanhService chiNhanhService;

    // UC 31: Thêm mới chi nhánh 
    @PostMapping
    public ResponseEntity<ChiNhanhResponse> createChiNhanh(@Valid @RequestBody ChiNhanhRequest request) {
        ChiNhanhResponse response = chiNhanhService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // UC 32: Cập nhật chi nhánh
    @PutMapping("/{id}")
    public ResponseEntity<ChiNhanhResponse> updateChiNhanh(@PathVariable Long id, @Valid @RequestBody ChiNhanhRequest request) {
        ChiNhanhResponse response = chiNhanhService.update(id, request);
        return ResponseEntity.ok(response);
    }
    

    @GetMapping
    public ResponseEntity<java.util.List<ChiNhanhResponse>> getAllActiveChiNhanh() {
        return ResponseEntity.ok(chiNhanhService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChiNhanhResponse> getChiNhanhById(@PathVariable Long id) {
        return ResponseEntity.ok(chiNhanhService.getActiveById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDeleteChiNhanh(@PathVariable Long id) {
        chiNhanhService.softDelete(id);
        return ResponseEntity.ok("Đã ngừng hoạt động chi nhánh thành công");
    }
    
    @PutMapping("/{id}/restore")
    public ResponseEntity<String> restoreChiNhanh(@PathVariable Long id) {
        chiNhanhService.restore(id);
        return ResponseEntity.ok("Khôi phục hoạt động chi nhánh thành công");
    }
}