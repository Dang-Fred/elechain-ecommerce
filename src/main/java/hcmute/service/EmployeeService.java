package hcmute.service;

import hcmute.dto.request.EmployeeCreateRequest;

import hcmute.dto.request.EmployeeUpdateRequest;
import hcmute.entity.ChiNhanh;
import hcmute.entity.NhanVien;
import hcmute.repository.ChiNhanhRepository;
import hcmute.repository.NhanVienRepository;
import hcmute.dto.response.EmployeeResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
   
 // UC37: Lấy danh sách nhân viên theo phân quyền
    public List<EmployeeResponse> getEmployeeList(String email) throws Exception {
        NhanVien currentStaff = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("UNAUTHORIZED:Không tìm thấy thông tin tài khoản"));

        String role = currentStaff.getVaiTro();
        List<NhanVien> nhanVienList;

        if ("ADMIN".equalsIgnoreCase(role)) {
            nhanVienList = nhanVienRepository.findAll();
        } else if ("QLCN".equalsIgnoreCase(role)) {
            if (currentStaff.getChiNhanh() == null) {
                throw new Exception("BAD_REQUEST:Quản lý chi nhánh này chưa được phân bổ về chi nhánh nào");
            }
            nhanVienList = nhanVienRepository.findByChiNhanh_MaCN(currentStaff.getChiNhanh().getMaCN());
        } else {
            throw new Exception("FORBIDDEN:Bạn không có quyền xem danh sách nhân sự");
        }

        return nhanVienList.stream().map(nv -> {
            EmployeeResponse dto = new EmployeeResponse();
            dto.setMaNV(nv.getMaNV());
            dto.setTenNV(nv.getTenNV());
            dto.setEmail(nv.getEmail());
            dto.setSoDienThoai(nv.getSoDienThoai());
            dto.setVaiTro(nv.getVaiTro());
            dto.setTrangThai(nv.getTrangThai());
            
            if (nv.getChiNhanh() != null) {
                dto.setMaCN(nv.getChiNhanh().getMaCN());
                // Lấy thêm tên chi nhánh (Giả sử trong entity ChiNhanh có hàm getTenCN())
                dto.setTenCN(nv.getChiNhanh().getTenCN()); 
            }
            return dto;
        }).collect(Collectors.toList());
    }
    
    
 // UC36: Khóa / Mở khóa tài khoản- Chặn tự khóa chính mình(admin khong bi chan chinh minh)
    public void toggleStatus(Long maNV) throws Exception {
        NhanVien nv = nhanVienRepository.findById(maNV)
                .orElseThrow(() -> new Exception("BAD_REQUEST:Không tìm thấy nhân viên"));

        // Lấy email của người đang gọi API
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Kiểm tra chặn nếu thao tác lên chính mình
        if (nv.getEmail().equals(currentEmail)) {
            throw new Exception("BAD_REQUEST:Bạn không thể tự khóa/mở khóa tài khoản của chính mình!");
        }

        nv.setTrangThai(!nv.getTrangThai());
        nhanVienRepository.save(nv);
    }
    
    
    
    
    
    
    
    
    
}