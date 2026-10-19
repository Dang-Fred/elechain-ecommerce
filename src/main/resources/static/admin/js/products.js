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
            // FIX LỖI QLCN: Chỉ thêm option khi thẻ select tồn tại (nghĩa là đang ở role ADMIN)
            if (select) {
                // Giữ lại option default đầu tiên, thêm các option từ API
                data.forEach(item => {
                    select.insertAdjacentHTML('beforeend', `<option value="${item.maDM}">${item.tenDM}</option>`);
                });
            }
        }).catch(err => console.error("Lỗi load Danh mục:", err));
}

function loadBrands() {
    fetch('/api/v1/hang-sx', { method: 'GET', ...fetchOptions })
        .then(res => res.json())
        .then(data => {
            const select = document.getElementById('maHang');
            // FIX LỖI QLCN: Chỉ thêm option khi thẻ select tồn tại
            if (select) {
                data.forEach(item => {
                    select.insertAdjacentHTML('beforeend', `<option value="${item.maHang}">${item.tenHang}</option>`);
                });
            }
        }).catch(err => console.error("Lỗi load Hãng SX:", err));
}

function loadProducts() {
    const tbody = document.getElementById('product-tbody');
    if (!tbody) return; // FIX AN TOÀN: Thoát luôn nếu không tìm thấy bảng sản phẩm
    
    fetch('/api/v1/san-pham', { method: 'GET', ...fetchOptions })
        .then(res => {
            if (!res.ok) throw new Error("Không thể tải danh sách sản phẩm");
            return res.json();
        })
        .then(data => {
            globalProducts = data; // Lưu lại để dùng cho nút Sửa
            
            // --- BẮT ĐẦU: LOGIC TÍNH TOÁN STATS VÀ VẼ BIỂU ĐỒ (Chỉ chạy nếu là ADMIN) ---
            if (currentRole === 'ADMIN') {
                const statTotalEl = document.getElementById('stat-total');
                // Kiểm tra element tồn tại (Admin mới có UI này)
                if (statTotalEl) {
                    // 1. Tính toán 4 Thẻ Stats
                    statTotalEl.textContent = data.length; // Tổng SP
                    
                    // Lọc Đang bán (Dựa vào API của anh đang trả về chữ "true")
                    const activeCount = data.filter(p => p.trangThai === "true").length;
                    document.getElementById('stat-active').textContent = activeCount;
                    
                    // Đếm danh mục (Dùng Set để lấy ID duy nhất, loại bỏ null/undefined)
                    const uniqueCats = new Set(data.map(p => p.maDM).filter(Boolean));
                    document.getElementById('stat-categories').textContent = uniqueCats.size;
                    
                    // Tổng giá trị (Cộng dồn giaBan)
                    const totalValue = data.reduce((sum, p) => sum + (Number(p.giaBan) || 0), 0);
                    document.getElementById('stat-value').textContent = vndFormatter.format(totalValue);

                    // 2. Xử lý dữ liệu vẽ Biểu đồ & Danh sách chú thích
                    const chartCanvas = document.getElementById('categoryChart');
                    if (chartCanvas) {
                        // Nhóm mảng data theo tenDM
                        const catCounts = data.reduce((acc, p) => {
                            const name = p.tenDM || 'Khác';
                            acc[name] = (acc[name] || 0) + 1;
                            return acc;
                        }, {});

                        const labels = Object.keys(catCounts);
                        const dataValues = Object.values(catCounts);
                        const bgColors = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796'];
                        const totalProducts = data.length;

                        // --- Đổ dữ liệu ra danh sách bên phải (Legend) ---
                        const legendContainer = document.getElementById('categoryLegend');
                        if (legendContainer) {
                            legendContainer.innerHTML = labels.map((label, index) => {
                                const count = dataValues[index];
                                const percentage = Math.round((count / totalProducts) * 100);
                                const color = bgColors[index % bgColors.length];
                                
                                return `
                                    <div class="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom border-light">
                                        <div class="d-flex align-items-center">
                                            <span style="width: 14px; height: 14px; background-color: ${color}; border-radius: 4px; display: inline-block; margin-right: 12px;"></span>
                                            <span class="small fw-medium">${label}</span>
                                        </div>
                                        <div class="d-flex align-items-center">
                                            <span class="small text-muted me-3">${percentage}%</span>
                                            <span class="small fw-bold text-dark">${count}</span>
                                        </div>
                                    </div>
                                `;
                            }).join('');
                        }

                        // --- Vẽ Chart.js (Hiện số lên mặt bánh) ---
                        const ctx = chartCanvas.getContext('2d');
                        if (window.catChart) window.catChart.destroy(); 
                        
                        window.catChart = new Chart(ctx, {
                            type: 'doughnut',
                            plugins: [ChartDataLabels], // Khai báo dùng plugin hiện chữ
                            data: {
                                labels: labels,
                                datasets: [{ 
                                    data: dataValues, 
                                    backgroundColor: bgColors,
                                    borderWidth: 2,
                                    borderColor: '#ffffff'
                                }]
                            },
                            options: { 
                                maintainAspectRatio: false, 
                                cutout: '55%', 
                                plugins: { 
                                    legend: { display: false }, // Ẩn legend cũ (Vì đã tự code list mới)
                                    datalabels: {
                                        color: '#ffffff', // Chữ trắng trên mặt bánh
                                        font: { weight: 'bold', size: 14 },
                                        formatter: function(value) {
                                            return value; // Hiện số lượng lên bánh
                                        }
                                    }
                                } 
                            }
                        });
                    }
                }
            }
            // --- KẾT THÚC: LOGIC TÍNH TOÁN STATS VÀ VẼ BIỂU ĐỒ ---
            
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
            if (tbody) {
                const colSpan = currentRole === 'ADMIN' ? 6 : 5;
                tbody.innerHTML = `<tr><td colspan="${colSpan}" class="text-center text-danger">${err.message}</td></tr>`;
            }
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


// ==========================================
// 4. QUẢN LÝ DANH MỤC (CRUD)
// ==========================================
let globalCatManager = [];

function openCategoryModal() {
    document.getElementById('categoryForm').reset();
    document.getElementById('cat-maDM').value = "";
    loadCategoriesManager();
    new bootstrap.Modal(document.getElementById('categoryModal')).show();
}

function loadCategoriesManager() {
    fetch('/api/v1/danh-muc', { method: 'GET', ...fetchOptions })
        .then(res => res.ok ? res.json() : [])
        .then(data => {
            globalCatManager = data;
            const tbody = document.getElementById('cat-tbody');
            // FIX LỖI QLCN: Thoát hàm nếu Modal Danh Mục đã bị Thymeleaf xóa
            if (!tbody) return; 

            if (data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Chưa có danh mục nào</td></tr>`;
                return;
            }
            tbody.innerHTML = data.map(item => {
                let imgSrc = 'https://placehold.co/40x40?text=No+Img';
                if (item.logo) {
                    imgSrc = item.logo.startsWith('http') ? item.logo : CLOUDFLARE_URL + item.logo;
                }
                return `
                    <tr>
                        <td>#${item.maDM}</td>
                        <td><img src="${imgSrc}" style="width: 40px; height: 40px; object-fit: contain;" class="border rounded bg-white"></td>
                        <td class="fw-medium">${item.tenDM}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-primary py-0" onclick="editCategory(${item.maDM})" title="Sửa"><i class="bi bi-pencil"></i></button>
                            <button class="btn btn-sm btn-outline-danger py-0" onclick="deleteCategory(${item.maDM})" title="Xóa"><i class="bi bi-trash"></i></button>
                        </td>
                    </tr>
                `;
            }).join('');
        }).catch(err => console.error(err));
}

function saveCategory(event) {
    event.preventDefault();
    const form = document.getElementById('categoryForm');
    const formData = new FormData(form);
    const id = formData.get('maDM');
    const method = id ? 'PUT' : 'POST';
    const url = id ? `/api/v1/danh-muc/${id}` : '/api/v1/danh-muc';

    fetch(url, { method: method, credentials: 'include', body: formData })
        .then(async res => {
            if (!res.ok) throw new Error("Lỗi khi lưu danh mục!");
            form.reset();
            document.getElementById('cat-maDM').value = "";
            loadCategoriesManager(); // Render lại bảng trong Modal
            loadCategories(); // Render lại <select> ở form Sản Phẩm chính (hàm cũ đã viết)
        }).catch(err => alert(err.message));
}

function editCategory(id) {
    const item = globalCatManager.find(c => c.maDM === id);
    if (!item) return;
    document.getElementById('cat-maDM').value = item.maDM;
    document.getElementById('cat-tenDM').value = item.tenDM;
    document.getElementById('cat-logo').value = ''; 
}

function deleteCategory(id) {
    if (!confirm("Xác nhận xóa danh mục này?")) return;
    fetch(`/api/v1/danh-muc/${id}`, { method: 'DELETE', ...fetchOptions })
        .then(async res => {
            if (!res.ok) throw new Error("Không thể xóa (có thể đang chứa SP)!");
            loadCategoriesManager();
            loadCategories();
        }).catch(err => alert(err.message));
}


// ==========================================
// 5. QUẢN LÝ HÃNG SẢN XUẤT (CRUD)
// ==========================================
let globalBrandManager = [];

function openBrandModal() {
    document.getElementById('brandForm').reset();
    document.getElementById('brand-maHang').value = "";
    loadBrandsManager();
    new bootstrap.Modal(document.getElementById('brandModal')).show();
}

function loadBrandsManager() {
    fetch('/api/v1/hang-sx', { method: 'GET', ...fetchOptions })
        .then(res => res.ok ? res.json() : [])
        .then(data => {
            globalBrandManager = data;
            const tbody = document.getElementById('brand-tbody');
            // FIX LỖI QLCN: Thoát hàm nếu Modal Hãng Sản Xuất đã bị Thymeleaf xóa
            if (!tbody) return;

            if (data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Chưa có hãng nào</td></tr>`;
                return;
            }
            tbody.innerHTML = data.map(item => {
                let imgSrc = 'https://placehold.co/40x40?text=No+Img';
                if (item.logo) {
                    imgSrc = item.logo.startsWith('http') ? item.logo : CLOUDFLARE_URL + item.logo;
                }
                return `
                    <tr>
                        <td>#${item.maHang}</td>
                        <td><img src="${imgSrc}" style="width: 40px; height: 40px; object-fit: contain;" class="border rounded bg-white"></td>
                        <td class="fw-medium">${item.tenHang}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-primary py-0" onclick="editBrand(${item.maHang})" title="Sửa"><i class="bi bi-pencil"></i></button>
                            <button class="btn btn-sm btn-outline-danger py-0" onclick="deleteBrand(${item.maHang})" title="Xóa"><i class="bi bi-trash"></i></button>
                        </td>
                    </tr>
                `;
            }).join('');
        }).catch(err => console.error(err));
}

function saveBrand(event) {
    event.preventDefault();
    const form = document.getElementById('brandForm');
    const formData = new FormData(form);
    const id = formData.get('maHang');
    const method = id ? 'PUT' : 'POST';
    const url = id ? `/api/v1/hang-sx/${id}` : '/api/v1/hang-sx';

    fetch(url, { method: method, credentials: 'include', body: formData })
        .then(async res => {
            if (!res.ok) throw new Error("Lỗi khi lưu hãng SX!");
            form.reset();
            document.getElementById('brand-maHang').value = "";
            loadBrandsManager(); 
            loadBrands(); 
        }).catch(err => alert(err.message));
}

function editBrand(id) {
    const item = globalBrandManager.find(c => c.maHang === id);
    if (!item) return;
    document.getElementById('brand-maHang').value = item.maHang;
    document.getElementById('brand-tenHang').value = item.tenHang;
    document.getElementById('brand-logo').value = ''; 
}

function deleteBrand(id) {
    if (!confirm("Xác nhận xóa hãng sản xuất này?")) return;
    fetch(`/api/v1/hang-sx/${id}`, { method: 'DELETE', ...fetchOptions })
        .then(async res => {
            if (!res.ok) throw new Error("Không thể xóa (có thể đang chứa SP)!");
            loadBrandsManager();
            loadBrands();
        }).catch(err => alert(err.message));
}