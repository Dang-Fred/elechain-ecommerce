// File: HangSXRestController.java
package hcmute.rest;

import hcmute.dto.request.HangSXRequest;
import hcmute.dto.response.HangSXResponse;
import hcmute.service.HangSXService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hang-sx")
public class HangSXRestController {

    @Autowired
    private HangSXService hangSXService;

    @GetMapping
    public ResponseEntity<List<HangSXResponse>> getAllHangSX() {
        return ResponseEntity.ok(hangSXService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HangSXResponse> getHangSXById(@PathVariable Long id) {
        return ResponseEntity.ok(hangSXService.getById(id));
    }

    @PostMapping
    public ResponseEntity<HangSXResponse> createHangSX(@Valid @RequestBody HangSXRequest request) {
        HangSXResponse response = hangSXService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HangSXResponse> updateHangSX(@PathVariable Long id, @Valid @RequestBody HangSXRequest request) {
        HangSXResponse response = hangSXService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHangSX(@PathVariable Long id) {
        hangSXService.delete(id);
        return ResponseEntity.ok("Xóa hãng sản xuất thành công");
    }
}