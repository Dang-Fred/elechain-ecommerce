package hcmute.service;

import hcmute.dto.request.EmployeeCreateRequest;
import hcmute.dto.request.EmployeeUpdateRequest;
import hcmute.entity.ChiNhanh;
import hcmute.entity.NhanVien;
import hcmute.repository.ChiNhanhRepository;
import hcmute.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private ChiNhanhRepository chiNhanhRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // UC34: Tạo mới nhân viên
    public void createEmployee(EmployeeCreateRequest request) throws Exception {
        // 1. Kiểm tra trùng lặp Email
        if (nhanVienRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new Exception("BAD_REQUEST:Email đã tồn tại trong hệ thống");
        }

        // 2. Kiểm tra trùng lặp Số điện thoại (nếu có nhập)
        if (request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty()) {
            if (nhanVienRepository.findBySoDienThoai(request.getSoDienThoai()).isPresent()) {
                throw new Exception("BAD_REQUEST:Số điện thoại đã được sử dụng");
            }
        }

        // 3. Kiểm tra RBTV 4: Nếu không phải ADMIN thì bắt buộc phải có mã Chi nhánh
        boolean isAdmin = "ADMIN".equalsIgnoreCase(request.getVaiTro());
        if (!isAdmin && request.getMaCN() == null) {
            throw new Exception("BAD_REQUEST:Nhân viên (Khác ADMIN) bắt buộc phải được phân về một Chi nhánh");
        }

        // 4. Tìm Chi nhánh nếu maCN != null
        ChiNhanh chiNhanh = null;
        if (request.getMaCN() != null) {
            chiNhanh = chiNhanhRepository.findById(request.getMaCN())
                    .orElseThrow(() -> new Exception("BAD_REQUEST:Không tìm thấy Chi nhánh với mã: " + request.getMaCN()));
        }

        // 5. Build Entity và Lưu DB
        NhanVien nv = new NhanVien();
        nv.setTenNV(request.getTenNV());
        nv.setEmail(request.getEmail());
        nv.setSoDienThoai(request.getSoDienThoai());
        nv.setVaiTro(request.getVaiTro());
        nv.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        nv.setTrangThai(true);
        nv.setChiNhanh(chiNhanh);

        nhanVienRepository.save(nv);
    }

    // UC35: Cập nhật nhân viên
    public void updateEmployee(Long maNV, EmployeeUpdateRequest request) throws Exception {
        NhanVien nv = nhanVienRepository.findById(maNV)
                .orElseThrow(() -> new Exception("BAD_REQUEST:Không tìm thấy nhân viên"));

        // Kiểm tra RBTV 4 với dữ liệu cập nhật
        boolean isAdmin = "ADMIN".equalsIgnoreCase(request.getVaiTro());
        if (!isAdmin && request.getMaCN() == null) {
            throw new Exception("BAD_REQUEST:Nhân viên (Khác ADMIN) bắt buộc phải được phân về một Chi nhánh");
        }

        ChiNhanh chiNhanh = null;
        if (request.getMaCN() != null) {
            chiNhanh = chiNhanhRepository.findById(request.getMaCN())
                    .orElseThrow(() -> new Exception("BAD_REQUEST:Không tìm thấy Chi nhánh"));
        }

        nv.setVaiTro(request.getVaiTro());
        nv.setChiNhanh(chiNhanh);

        nhanVienRepository.save(nv);
    }

    // UC36: Khóa / Mở khóa tài khoản
    public void toggleStatus(Long maNV) throws Exception {
        NhanVien nv = nhanVienRepository.findById(maNV)
                .orElseThrow(() -> new Exception("BAD_REQUEST:Không tìm thấy nhân viên"));

        // Đảo ngược trạng thái hiện tại (true thành false, false thành true)
        nv.setTrangThai(!nv.getTrangThai());
        
        nhanVienRepository.save(nv);
    }
}