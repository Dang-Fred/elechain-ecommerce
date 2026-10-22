document.addEventListener('DOMContentLoaded', function() {
    loadBranchProducts();
});

// Bộ định dạng tiền tệ VNĐ
const vndFormatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });

// Hàm tiện ích để sinh header chứa JWT Token
function getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? 'Bearer ' + token : '' 
    };
}

// ==========================================
// 1. TẢI LƯỚI DỮ LIỆU CHÍNH
// ==========================================
function loadBranchProducts() {
    const tbody = document.getElementById('branch-products-tbody');
    
    fetch('/api/inventory', { 
        method: 'GET', 
        headers: getAuthHeaders()
    })
    .then(res => {
        if (!res.ok) throw new Error("Không thể tải danh sách sản phẩm chi nhánh");
        return res.json();
    })
    .then(data => {
        const productArray = Array.isArray(data) ? data : (data.data || []);
        
        if (productArray.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">Chi nhánh chưa kinh doanh mặt hàng nào.</td></tr>`;
            return;
        }

        tbody.innerHTML = productArray.map(p => `
            <tr>
                <td class="fw-medium text-muted">#${p.maSP}</td>
                <td>
                    <img src="${p.hinhAnh || 'https://placehold.co/40x40?text=No+Img'}" 
                         style="width: 40px; height: 40px; object-fit: cover;" 
                         class="border rounded bg-white">
                </td>
                <td class="fw-bold">${p.tenSP}</td>
                <td class="fw-medium text-danger">${vndFormatter.format(p.giaBan || 0)}</td>
                <td>
                    <span class="badge ${p.soLuongTon > 0 ? 'bg-success' : 'bg-secondary'} rounded-pill px-3">
                        ${p.soLuongTon}
                    </span>
                </td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-danger" onclick="removeProduct(${p.maSP}, ${p.soLuongTon})">
                        <i class="bi bi-x-circle me-1"></i>Gỡ bỏ
                    </button>
                </td>
            </tr>
        `).join('');
    })
    .catch(err => {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger">${err.message}</td></tr>`;
    });
}

// ==========================================
// 2. NGHIỆP VỤ MỞ MODAL & TẢI MASTER DATA
// ==========================================
function openAllocateModal() {
    const tbody = document.getElementById('master-products-tbody');
    tbody.innerHTML = `<tr><td colspan="5" class="text-center py-3"><div class="spinner-border spinner-border-sm"></div> Đang tải kho tổng...</td></tr>`;
    loadMasterProducts();
}

function loadMasterProducts() {
    fetch('/api/inventory/master-products', { 
        method: 'GET', 
        headers: getAuthHeaders() 
    })
    .then(res => {
        if (!res.ok) throw new Error("Lỗi tải danh mục Master Data");
        return res.json();
    })
    .then(data => {
        const productArray = Array.isArray(data) ? data : (data.data || []);
        const tbody = document.getElementById('master-products-tbody');
        
        if (productArray.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-muted">Không có sản phẩm nào trong kho tổng.</td></tr>`;
            return;
        }

        tbody.innerHTML = productArray.map(p => `
            <tr>
                <td class="fw-medium text-muted">#${p.maSP}</td>
                <td>
                    <img src="${p.hinhAnh || 'https://placehold.co/40x40?text=No+Img'}" 
                         style="width: 40px; height: 40px; object-fit: cover;" 
                         class="border rounded bg-white">
                </td>
                <td class="fw-medium">${p.tenSP}</td>
                <td class="fw-medium text-danger">${vndFormatter.format(p.giaBan || 0)}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-primary w-100" onclick="allocateProduct(${p.maSP})">
                        <i class="bi bi-download me-1"></i>Phân bổ
                    </button>
                </td>
            </tr>
        `).join('');
    })
    .catch(err => {
        document.getElementById('master-products-tbody').innerHTML = `<tr><td colspan="5" class="text-center text-danger">${err.message}</td></tr>`;
    });
}

// ==========================================
// 3. NGHIỆP VỤ PHÂN BỔ SẢN PHẨM MỚI
// ==========================================
function allocateProduct(maSP) {
    fetch(`/api/inventory/allocate/${maSP}`, {
        method: 'POST',
        headers: getAuthHeaders()
    })
    .then(async res => {
        if (!res.ok) {
            let errorMsg = "Lỗi khi phân bổ sản phẩm";
            try {
                const errData = await res.json();
                errorMsg = errData.message || errorMsg;
            } catch (e) {
                if (res.status === 400) errorMsg = "Sản phẩm này đã tồn tại trong danh mục của chi nhánh!";
            }
            throw new Error(errorMsg);
        }
        return res.text();
    })
    .then(() => {
        alert("Thêm mặt hàng mới vào chi nhánh thành công!");
        const btnClose = document.querySelector('#allocateModal .btn-close');
        if (btnClose) {
            btnClose.click();
            btnClose.blur();
        }
        loadBranchProducts();
    })
    .catch(err => alert("THẤT BẠI: " + err.message));
}

// ==========================================
// 4. NGHIỆP VỤ GỠ BỎ SẢN PHẨM
// ==========================================
function removeProduct(maSP, soLuongTon) {
    if (soLuongTon > 0) {
        alert(`Không thể gỡ bỏ! Hiện tại vẫn còn tồn ${soLuongTon} sản phẩm trong kho.\nVui lòng xuất hết hàng trước khi thao tác.`);
        return;
    }
    if (!confirm("Xác nhận: Bạn có chắc chắn muốn gỡ mặt hàng này khỏi chi nhánh không?")) {
        return;
    }
    fetch(`/api/inventory/remove/${maSP}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
    })
    .then(async res => {
        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            throw new Error(errData.message || "Xóa thất bại do lỗi hệ thống!");
        }
        alert("Đã gỡ bỏ mặt hàng thành công!");
        loadBranchProducts();
    })
    .catch(err => alert("LỖI: " + err.message));
}