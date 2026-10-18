const fetchOptions = {
    credentials: 'include' // Dùng chung cho các request GET/DELETE (POST/PUT Form thì không set Content-Type)
};

const vndFormatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });

// Biến toàn cục lưu danh sách sản phẩm để tiện điền vào Modal
let globalProducts = [];

document.addEventListener('DOMContentLoaded', function() {
    // Chạy song song 3 API khi load trang
    loadCategories();
    loadBrands();
    loadProducts();
});

// ==========================================
// 1. CÁC HÀM GET DỮ LIỆU (READ)
// ==========================================

function loadCategories() {
    fetch('/api/v1/danh-muc', { method: 'GET', ...fetchOptions })
        .then(res => res.json())
        .then(data => {
            const select = document.getElementById('maDM');
            // Giữ lại option default đầu tiên, thêm các option từ API
            data.forEach(item => {
                select.insertAdjacentHTML('beforeend', `<option value="${item.maDM}">${item.tenDM}</option>`);
            });
        }).catch(err => console.error("Lỗi load Danh mục:", err));
}

function loadBrands() {
    fetch('/api/v1/hang-sx', { method: 'GET', ...fetchOptions })
        .then(res => res.json())
        .then(data => {
            const select = document.getElementById('maHang');
            data.forEach(item => {
                select.insertAdjacentHTML('beforeend', `<option value="${item.maHang}">${item.tenHang}</option>`);
            });
        }).catch(err => console.error("Lỗi load Hãng SX:", err));
}

function loadProducts() {
    const tbody = document.getElementById('product-tbody');
    
    fetch('/api/v1/san-pham', { method: 'GET', ...fetchOptions })
        .then(res => {
            if (!res.ok) throw new Error("Không thể tải danh sách sản phẩm");
            return res.json();
        })
        .then(data => {
            globalProducts = data; // Lưu lại để dùng cho nút Sửa
            tbody.innerHTML = '';

            if (data.length === 0) {
                const colSpan = currentRole === 'ADMIN' ? 6 : 5;
                tbody.innerHTML = `<tr><td colspan="${colSpan}" class="text-center py-4 text-muted">Chưa có sản phẩm nào</td></tr>`;
                return;
            }

			data.forEach(product => {
			                // 1. Xử lý hiển thị ảnh (Dùng trường 'logo')
			                let imgSrc = 'https://placehold.co/100x100?text=No+Image';
			                
			                if (product.logo) {
			                    imgSrc = product.logo.startsWith('http') 
			                        ? product.logo 
			                        : CLOUDFLARE_URL + product.logo;
			                }

			                // 2. Xử lý Badge Trạng thái (Dựa theo chuỗi "true" hoặc "false")
			                const statusBadge = product.trangThai === "true"
			                    ? '<span class="badge bg-success">Đang bán</span>'
			                    : '<span class="badge bg-secondary">Ngừng bán</span>';

			                // Chỉ vẽ Cột Thao tác nếu là ADMIN
			                let actionTd = '';
			                if (currentRole === 'ADMIN') {
			                    actionTd = `
			                        <td>
			                            <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditModal(${product.maSP})" title="Sửa">
			                                <i class="bi bi-pencil"></i>
			                            </button>
			                            <button class="btn btn-sm btn-outline-danger" onclick="deleteProduct(${product.maSP})" title="Xóa">
			                                <i class="bi bi-trash"></i>
			                            </button>
			                        </td>
			                    `;
			                }

			                const tr = document.createElement('tr');
			                tr.innerHTML = `
			                    <td>
			                        <div class="d-flex align-items-center">
			                            <img src="${imgSrc}" class="product-image me-3" style="width: 48px; height: 48px; object-fit: cover; border-radius: 6px; border: 1px solid #eee;" alt="Logo">
			                            <div>
			                                <div class="fw-medium">${product.tenSP}</div>
			                                <small class="text-muted">ID: #${product.maSP}</small>
			                            </div>
			                        </div>
			                    </td>
			                    <td><span class="badge bg-light text-dark border">${product.tenDM || 'N/A'}</span></td>
			                    <td>${product.tenHang || 'N/A'}</td>
			                    <td class="fw-medium">${vndFormatter.format(product.giaBan)}</td>
			                    <td>${statusBadge}</td>
			                    ${actionTd}
			                `;
			                tbody.appendChild(tr);
			            });           
        }).catch(err => {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger">${err.message}</td></tr>`;
        });
}

// ==========================================
// 2. CÁC HÀM THAO TÁC MODAL (ADD / EDIT)
// ==========================================

function openAddModal() {
    document.getElementById('productForm').reset();
    document.getElementById('maSP').value = ""; // Đảm bảo ID rỗng để hệ thống hiểu là THÊM MỚI
    document.getElementById('modalTitle').textContent = "Thêm Sản Phẩm Mới";
    
    // Mở modal bằng Bootstrap 5 JS API
    const modal = new bootstrap.Modal(document.getElementById('productModal'));
    modal.show();
}

function openEditModal(id) {
    const product = globalProducts.find(p => p.maSP === id);
    if (!product) return;

    // Viết hàm helper nhỏ để gán giá trị an toàn, chặn đứng lỗi "null"
    const setSafeValue = (elementId, value) => {
        const el = document.getElementById(elementId);
        if (el) el.value = value;
    };

    // Đổ dữ liệu vào Form
    setSafeValue('maSP', product.maSP);
    setSafeValue('tenSP', product.tenSP);
    setSafeValue('giaBan', product.giaBan);
    setSafeValue('baoHanh', product.baoHanh || '');
    setSafeValue('cauHinh', product.cauHinh || '');
    setSafeValue('moTa', product.moTa || '');
    
    // Select Option
    if (product.maDM) setSafeValue('maDM', product.maDM);
    if (product.maHang) setSafeValue('maHang', product.maHang);

    // Xóa file cũ
    const logoInput = document.getElementById('logoFile');
    if (logoInput) logoInput.value = ''; 
    
    // Đổi tiêu đề
    const titleEl = document.getElementById('modalTitle');
    if (titleEl) titleEl.textContent = "Cập Nhật Sản Phẩm";

    const modal = new bootstrap.Modal(document.getElementById('productModal'));
    modal.show();
}

// ==========================================
// 3. CÁC HÀM LƯU VÀ XÓA (CREATE / UPDATE / DELETE)
// ==========================================

function saveProduct(event) {
    event.preventDefault(); // Ngăn trình duyệt reload trang
    
    const form = document.getElementById('productForm');
    const formData = new FormData(form);
    const productId = formData.get('maSP');
    
    // Nếu có ID thì là Sửa (PUT), không có thì là Thêm (POST)
    const isUpdate = productId && productId.trim() !== "";
    const method = isUpdate ? 'PUT' : 'POST';
    const url = isUpdate ? `/api/v1/san-pham/${productId}` : `/api/v1/san-pham`;

    fetch(url, {
        method: method,
        credentials: 'include',
        body: formData // Truyền nguyên FormData, trình duyệt sẽ tự set header multipart/form-data
    })
    .then(async res => {
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Có lỗi xảy ra khi lưu!");
        }
        return res.json();
    })
    .then(data => {
        alert(isUpdate ? "Cập nhật thành công!" : "Thêm sản phẩm thành công!");
        
        // Đóng modal
        const modalInstance = bootstrap.Modal.getInstance(document.getElementById('productModal'));
        if(modalInstance) modalInstance.hide();
        
        // Load lại bảng
        loadProducts();
    })
    .catch(err => alert(err.message));
}

function deleteProduct(id) {
    // Tìm tên sản phẩm để hiển thị thông báo cho rõ ràng
    const product = globalProducts.find(p => p.maSP === id);
    const tenSP = product ? product.tenSP : 'này';

    if (!confirm(`XÁC NHẬN: Bạn có chắc chắn muốn xóa sản phẩm "${tenSP}" không?\nThao tác này không thể hoàn tác!`)) {
        return;
    }

    fetch(`/api/v1/san-pham/${id}`, {
        method: 'DELETE',
        credentials: 'include'
    })
    .then(async res => {
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Xóa thất bại!");
        }
        alert("Đã xóa sản phẩm thành công!");
        loadProducts();
    })
    .catch(err => alert(err.message));
}