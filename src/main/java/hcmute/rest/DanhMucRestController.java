// File: DanhMucRestController.java
package hcmute.rest;

import hcmute.dto.request.DanhMucRequest;
import hcmute.dto.response.DanhMucResponse;
import hcmute.service.DanhMucService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/danh-muc")
public class DanhMucRestController {

    @Autowired
    private DanhMucService danhMucService;

    // Lấy danh sách (Phục vụ hiển thị UC)
    @GetMapping
    public ResponseEntity<List<DanhMucResponse>> getAllDanhMuc() {
        return ResponseEntity.ok(danhMucService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DanhMucResponse> getDanhMucById(@PathVariable Long id) {
        return ResponseEntity.ok(danhMucService.getById(id));
    }

    // UC 22: Thêm mới
    @PostMapping
    public ResponseEntity<DanhMucResponse> createDanhMuc(@Valid @RequestBody DanhMucRequest request) {
        DanhMucResponse response = danhMucService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // UC 23: Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<DanhMucResponse> updateDanhMuc(@PathVariable Long id, @Valid @RequestBody DanhMucRequest request) {
        DanhMucResponse response = danhMucService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // UC 24: Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDanhMuc(@PathVariable Long id) {
        danhMucService.delete(id);
        return ResponseEntity.ok("Xóa danh mục thành công");
    }
}