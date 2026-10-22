//File: InventoryService.java
package hcmute.service;

import hcmute.dto.request.ChiTietNhapRequest;
import hcmute.dto.response.BranchStockResponse;
import hcmute.dto.request.PhieuNhapRequest;
import hcmute.dto.response.PhieuNhapResponse;
import hcmute.entity.*;
import hcmute.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import hcmute.dto.response.InventoryItemResponse;
import java.util.stream.Collectors;
import hcmute.dto.response.MasterProductResponse;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import hcmute.dto.response.LowStockAlertResponse;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final PhieuNhapRepository phieuNhapRepository;
    private final ChiTietPhieuNhapRepository chiTietPhieuNhapRepository;
    private final KhoHangRepository khoHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final SanPhamRepository sanPhamRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createPhieuNhap(String email, PhieuNhapRequest request) {
        try {
            // Bước 1: Tìm nhân viên đang đăng nhập
            NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên (Email: " + email + ")"));

            if (nhanVien.getChiNhanh() == null) {
                throw new RuntimeException("Nhân viên chưa được phân bổ về chi nhánh nào!");
            }

            // Bước 2: Khởi tạo và lưu Phiếu Nhập
            PhieuNhap phieuNhap = new PhieuNhap();
            phieuNhap.setNhanVien(nhanVien);
            phieuNhap.setChiNhanh(nhanVien.getChiNhanh());
            phieuNhap.setNgayLap(LocalDateTime.now()); // Tuân thủ RBTV15: Ngày lập <= Ngày hiện tại
            PhieuNhap savedPhieuNhap = phieuNhapRepository.save(phieuNhap);

            // Bước 3: Xử lý danh sách chi tiết phiếu nhập
            for (ChiTietNhapRequest ctReq : request.getDanhSachSanPham()) {
                SanPham sanPham = sanPhamRepository.findById(ctReq.getMaSP())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + ctReq.getMaSP()));

                // Lưu Chi Tiết Phiếu Nhập
                ChiTietPhieuNhap ctPhieuNhap = new ChiTietPhieuNhap();
                ctPhieuNhap.setId(new ChiTietPhieuNhapKey(savedPhieuNhap.getMaPN(), sanPham.getMaSP()));
                ctPhieuNhap.setPhieuNhap(savedPhieuNhap);
                ctPhieuNhap.setSanPham(sanPham);
                ctPhieuNhap.setSoLuongNhap(ctReq.getSoLuongNhap());
                chiTietPhieuNhapRepository.save(ctPhieuNhap);

             // Cập nhật Kho Hàng (Cộng dồn số lượng tồn)
                KhoHang khoHang = khoHangRepository.findById_MaSPAndId_MaCN(sanPham.getMaSP(), nhanVien.getChiNhanh().getMaCN())
                        .orElseGet(() -> {
                            KhoHang newKho = new KhoHang();
                            newKho.setId(new KhoHangKey(sanPham.getMaSP(), nhanVien.getChiNhanh().getMaCN()));
                            
                            // BỔ SUNG 2 DÒNG NÀY ĐỂ FIX LỖI (Gán Association cho Hibernate)
                            newKho.setSanPham(sanPham);
                            newKho.setChiNhanh(nhanVien.getChiNhanh());
                            
                            newKho.setSoLuongTon(0);
                            return newKho;
                        });
                
                khoHang.setSoLuongTon(khoHang.getSoLuongTon() + ctReq.getSoLuongNhap());
                khoHangRepository.save(khoHang);
            }
        } catch (Exception e) {
            // Ném lỗi ra để Spring Boot tự động Rollback lại toàn bộ transaction (bảng PhieuNhap, ChiTiet, KhoHang)
            throw new RuntimeException("Lỗi hệ thống khi nhập kho: " + e.getMessage(), e);
        }
    }

    public List<PhieuNhapResponse> getLichSuNhapKho(String email) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        List<PhieuNhap> danhSachPhieu = phieuNhapRepository.findByChiNhanh_MaCN(nhanVien.getChiNhanh().getMaCN());

        return danhSachPhieu.stream().map(pn -> {
            PhieuNhapResponse response = new PhieuNhapResponse();
            response.setMaPN(pn.getMaPN());
            response.setNgayLap(pn.getNgayLap());
            response.setTenNhanVien(pn.getNhanVien().getTenNV());

            List<PhieuNhapResponse.ChiTietSanPhamResponse> chiTietList = pn.getChiTietPhieuNhaps().stream()
                    .map(ct -> new PhieuNhapResponse.ChiTietSanPhamResponse(
                            ct.getSanPham().getMaSP(),
                            ct.getSanPham().getTenSP(),
                            ct.getSoLuongNhap()
                    )).collect(Collectors.toList());

            response.setDanhSachSanPham(chiTietList);
            return response;
        }).collect(Collectors.toList());
    }
    
    
    public List<BranchStockResponse> checkStockByProduct(Long maSP) {
        // Gọi repository với soLuongMin = 0 để lấy các chi nhánh CÒN HÀNG
        List<KhoHang> khoHangList = khoHangRepository.findBySanPham_MaSPAndSoLuongTonGreaterThan(maSP, 0);

        // Map dữ liệu từ Entity sang DTO
        return khoHangList.stream().map(kho -> {
            BranchStockResponse response = new BranchStockResponse();
            response.setMaCN(kho.getChiNhanh().getMaCN());
            response.setTenCN(kho.getChiNhanh().getTenCN());
            response.setDiaChi(kho.getChiNhanh().getDiaChi());
            response.setHotline(kho.getChiNhanh().getHotline());
            response.setSoLuongTon(kho.getSoLuongTon());
            return response;
        }).collect(Collectors.toList());
    }
    
    
    public List<LowStockAlertResponse> getLowStockAlert(String email) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));

        if (nhanVien.getChiNhanh() == null) {
            throw new RuntimeException("Nhân viên chưa được phân bổ về chi nhánh nào!");
        }

        final int ALERT_THRESHOLD = 5;

        // Gọi repository lấy danh sách sản phẩm có số lượng tồn < 5
        List<KhoHang> lowStockList = khoHangRepository.findLowStockByChiNhanh(
                nhanVien.getChiNhanh().getMaCN(), ALERT_THRESHOLD);

        // Map sang DTO
        return lowStockList.stream().map(kho -> {
            LowStockAlertResponse response = new LowStockAlertResponse();
            response.setMaSP(kho.getSanPham().getMaSP());
            response.setTenSP(kho.getSanPham().getTenSP());
            response.setHinhAnh(kho.getSanPham().getLogo());
            response.setGiaBan(kho.getSanPham().getGiaBan());
            response.setSoLuongTon(kho.getSoLuongTon());
            return response;
        }).collect(Collectors.toList());
    }
    
    public List<InventoryItemResponse> getAllStock(String email) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));

        if (nhanVien.getChiNhanh() == null) {
            throw new RuntimeException("Nhân viên chưa được phân bổ về chi nhánh nào!");
        }

        // Lấy toàn bộ hàng trong kho theo mã chi nhánh
        List<KhoHang> stockList = khoHangRepository.findByChiNhanh_MaCN(nhanVien.getChiNhanh().getMaCN());

        // Map sang DTO
        return stockList.stream().map(kho -> {
            InventoryItemResponse response = new InventoryItemResponse();
            response.setMaSP(kho.getSanPham().getMaSP());
            response.setTenSP(kho.getSanPham().getTenSP());
            response.setHinhAnh(kho.getSanPham().getLogo());
            response.setGiaBan(kho.getSanPham().getGiaBan());
            response.setSoLuongTon(kho.getSoLuongTon());
            return response;
        }).collect(Collectors.toList());
    }
    
    
 // --- TICKET 3.3: Lấy danh sách sản phẩm gốc (Master Data) ---
    public List<MasterProductResponse> getMasterProducts() {
        // Lấy trực tiếp từ bảng SanPham, bỏ qua KhoHang để thấy cả SP chưa phân bổ
        List<SanPham> masterList = sanPhamRepository.findAll();
        
        return masterList.stream()
                // Nếu dự án anh định nghĩa trạng thái Active là "Hoạt động" hoặc "1" thì sửa ở đây
                .filter(sp -> sp.getTrangThai() != null) 
                .map(sp -> new MasterProductResponse(
                        sp.getMaSP(), 
                        sp.getTenSP(), 
                        sp.getLogo(), 
                        sp.getGiaBan(), 
                        sp.getTrangThai()
                )).collect(Collectors.toList());
    }

    // --- TICKET 3.3: Phân bổ sản phẩm về Chi nhánh ---
    @Transactional(rollbackFor = Exception.class)
    public void allocateProductToBranch(String email, Long maSP) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));
        
        ChiNhanh chiNhanh = nhanVien.getChiNhanh();
        if (chiNhanh == null) {
            throw new RuntimeException("Nhân viên chưa được phân bổ về chi nhánh nào!");
        }

        SanPham sanPham = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new RuntimeException("Sản phẩm gốc không tồn tại (Mã SP: " + maSP + ")"));

        // Kiểm tra xem sản phẩm đã có trong danh mục của chi nhánh chưa
        Optional<KhoHang> existingKho = khoHangRepository.findById_MaSPAndId_MaCN(maSP, chiNhanh.getMaCN());
        if (existingKho.isPresent()) {
            throw new RuntimeException("Sản phẩm này đã nằm trong danh mục kinh doanh của chi nhánh.");
        }

        // Khởi tạo tồn kho ban đầu = 0
        KhoHang newKho = new KhoHang();
        newKho.setId(new KhoHangKey(maSP, chiNhanh.getMaCN()));
        newKho.setSanPham(sanPham);
        newKho.setChiNhanh(chiNhanh);
        newKho.setSoLuongTon(0); // Bắt buộc = 0 theo nghiệp vụ

        khoHangRepository.save(newKho);
    }

    // --- TICKET 3.3: Gỡ bỏ sản phẩm khỏi Chi nhánh ---
    @Transactional(rollbackFor = Exception.class)
    public void removeAllocatedProduct(String email, Long maSP) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));
        
        ChiNhanh chiNhanh = nhanVien.getChiNhanh();
        if (chiNhanh == null) {
            throw new RuntimeException("Nhân viên chưa được phân bổ về chi nhánh nào!");
        }

        KhoHang khoHang = khoHangRepository.findById_MaSPAndId_MaCN(maSP, chiNhanh.getMaCN())
                .orElseThrow(() -> new RuntimeException("Sản phẩm chưa có trong danh mục của chi nhánh."));

        // RBTV Nghiệp vụ: Tồn kho phải = 0 mới được xóa
        if (khoHang.getSoLuongTon() > 0) {
            throw new RuntimeException("Không thể gỡ bỏ mặt hàng này khỏi chi nhánh. Bắt buộc phải xuất hết tồn kho (Tồn kho = 0) trước khi thao tác.");
        }

        // Xóa vật lý record KhoHang
        khoHangRepository.delete(khoHang);
    }
    
    
    
    
    
    
    
    
    
}