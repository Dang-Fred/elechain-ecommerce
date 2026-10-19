const fetchOptions = { credentials: 'include' };
const CLOUDFLARE_URL = "https://pub-xxxxxx.r2.dev/"; // TODO: Thay bằng URL thật của anh
const vndFormatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });

// State lưu giỏ hàng tạm cho Phiếu Nhập
let receiptItems = [];

document.addEventListener('DOMContentLoaded', function() {
    loadInventory();
    
    // Chỉ QLCN và NV_KHO mới cần load Alerts và Products cho form Nhập
    if (currentRole === 'QLCN' || currentRole === 'NV_KHO') {
        loadAlerts();
        initReceiptForm();
    }
    
    loadHistory();
});

// =====================================
// 1. TAB TỒN KHO & CẢNH BÁO
// =====================================

function loadInventory() {
    fetch('/api/inventory', { method: 'GET', ...fetchOptions })
        .then(res => res.ok ? res.json() : [])
        .then(data => {
            // Đổ số liệu lên thẻ Stats
            const statTotal = document.getElementById('stat-total-items');
            if (statTotal) statTotal.textContent = data.length;

            // Render bảng
            const tbody = document.getElementById('inventory-tbody');
            if (data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="4" class="text-center py-4 text-muted">Kho hiện tại đang trống.</td></tr>`;
                return;
            }

            tbody.innerHTML = data.map(item => {
                // Xử lý link ảnh R2
                let imgSrc = 'https://placehold.co/100x100?text=No+Image';
                if (item.hinhAnh) {
                    imgSrc = item.hinhAnh.startsWith('http') ? item.hinhAnh : CLOUDFLARE_URL + item.hinhAnh;
                }

                // Cảnh báo trạng thái < 5 thì màu đỏ
                const stockQty = item.soLuongTon || 0;
                const statusBadge = stockQty < 5 
                    ? `<span class="badge bg-danger">Sắp hết hàng</span>` 
                    : `<span class="badge bg-success">An toàn</span>`;

                return `
                    <tr>
                        <td>
                            <div class="d-flex align-items-center">
                                <img src="${imgSrc}" class="product-image me-3" style="width: 48px; height: 48px; object-fit: cover; border-radius: 6px; border: 1px solid #eee;" alt="${item.tenSP}">
                                <div>
                                    <div class="fw-medium">${item.tenSP}</div>
                                    <small class="text-muted">ID: #${item.maSP}</small>
                                </div>
                            </div>
                        </td>
                        <td class="fw-medium">${vndFormatter.format(item.giaBan || 0)}</td>
                        <td class="fw-bold ${stockQty < 5 ? 'text-danger' : 'text-success'}">${stockQty}</td>
                        <td>${statusBadge}</td>
                    </tr>
                `;
            }).join('');
        })
        .catch(err => console.error("Lỗi load Inventory:", err));
}

function loadAlerts() {
    fetch('/api/inventory/alert', { method: 'GET', ...fetchOptions })
        .then(res => res.ok ? res.json() : [])
        .then(data => {
            const statAlert = document.getElementById('stat-low-stock');
            if (statAlert) statAlert.textContent = data.length;
        })
        .catch(err => console.error("Lỗi load Alerts:", err));
}

// =====================================
// 2. TAB LỊCH SỬ NHẬP KHO
// =====================================

function loadHistory() {
    fetch('/api/inventory/history', { method: 'GET', ...fetchOptions })
        .then(res => res.ok ? res.json() : [])
        .then(data => {
            const tbody = document.getElementById('history-tbody');
            if (data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="4" class="text-center py-4 text-muted">Chưa có lịch sử nhập kho nào.</td></tr>`;
                return;
            }

            tbody.innerHTML = data.map(record => {
                // Xử lý Ngày lập
                const dateStr = record.ngayLap ? new Date(record.ngayLap).toLocaleString('vi-VN') : 'N/A';
                
                // Lặp lồng mảng chi tiết
                const detailsHtml = (record.danhSachSanPham || []).map(dt => 
                    `<li>[ID: ${dt.maSP}] ${dt.tenSP || 'SP Không tên'}: <strong>+${dt.soLuongNhap}</strong></li>`
                ).join('');

                return `
                    <tr>
                        <td class="fw-bold text-primary">#${record.maPN}</td>
                        <td class="small text-muted">${dateStr}</td>
                        <td>${record.tenNhanVien || 'N/A'}</td>
                        <td><ul class="mb-0 ps-3 text-muted small">${detailsHtml || '<li>Không có chi tiết</li>'}</ul></td>
                    </tr>
                `;
            }).join('');
        })
        .catch(err => console.error("Lỗi load History:", err));
}

// =====================================
// 3. LOGIC MODAL LẬP PHIẾU NHẬP
// =====================================

function initReceiptForm() {
    fetch('/api/v1/san-pham', { method: 'GET', ...fetchOptions })
        .then(res => res.ok ? res.json() : [])
        .then(data => {
            const select = document.getElementById('product-select');
            select.innerHTML = '<option value="">-- Chọn một sản phẩm --</option>';
            data.forEach(p => {
                select.insertAdjacentHTML('beforeend', `<option value="${p.maSP}" data-name="${p.tenSP}">[#${p.maSP}] ${p.tenSP}</option>`);
            });
        }).catch(err => console.error("Lỗi nạp DS SP:", err));
}

function openReceiptModal() {
    receiptItems = []; // Reset mảng
    renderReceiptGrid(); // Reset lưới HTML
    document.getElementById('receipt-qty').value = 1; // Reset số lượng
    document.getElementById('product-select').value = ""; // Reset dropdown
    
    const modal = new bootstrap.Modal(document.getElementById('receiptModal'));
    modal.show();
}

function addReceiptItem() {
    const select = document.getElementById('product-select');
    const qtyInput = document.getElementById('receipt-qty');
    
    const maSP = select.value;
    const qty = parseInt(qtyInput.value);

    if (!maSP) return alert("Vui lòng chọn sản phẩm!");
    if (isNaN(qty) || qty < 1) return alert("Số lượng phải lớn hơn 0!");

    // Trích xuất tên từ thuộc tính data-name
    const tenSP = select.options[select.selectedIndex].getAttribute('data-name');

    // Kiểm tra trùng
    const isExist = receiptItems.find(item => item.maSP == maSP);
    if (isExist) {
        // Nếu đã có thì cộng dồn số lượng
        isExist.soLuongNhap += qty;
    } else {
        // Thêm mới vào mảng
        receiptItems.push({ maSP: parseInt(maSP), tenSP: tenSP, soLuongNhap: qty });
    }

    renderReceiptGrid();
    qtyInput.value = 1; // Reset ô nhập số lượng cho tiện nhập món tiếp theo
}

function removeReceiptItem(maSP) {
    receiptItems = receiptItems.filter(item => item.maSP != maSP);
    renderReceiptGrid();
}

function renderReceiptGrid() {
    const tbody = document.getElementById('receipt-grid');
    if (receiptItems.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted py-3">Chưa có sản phẩm nào trong phiếu.</td></tr>`;
        return;
    }

    tbody.innerHTML = receiptItems.map(item => `
        <tr>
            <td class="fw-medium">#${item.maSP}</td>
            <td>${item.tenSP}</td>
            <td class="fw-bold text-success">${item.soLuongNhap}</td>
            <td class="text-center">
                <button class="btn btn-sm btn-outline-danger" onclick="removeReceiptItem(${item.maSP})" title="Xóa">
                    <i class="bi bi-x-lg"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function saveReceipt() {
    if (receiptItems.length === 0) return alert("Phiếu nhập chưa có sản phẩm nào!");
    
    if (!confirm("Xác nhận lưu Phiếu Nhập này vào hệ thống?")) return;

    // Body payload theo đúng cấu trúc Backend yêu cầu
    const requestPayload = {
        danhSachSanPham: receiptItems.map(item => ({
            maSP: item.maSP,
            soLuongNhap: item.soLuongNhap
        }))
    };

    fetch('/api/inventory/receipt', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(requestPayload)
    })
    .then(async res => {
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Lỗi lưu Phiếu nhập!");
        }
        alert("Lưu Phiếu nhập kho thành công!");
        
        // Đóng Modal
        const modalInstance = bootstrap.Modal.getInstance(document.getElementById('receiptModal'));
        if (modalInstance) modalInstance.hide();
        
        // Cập nhật lại giao diện
        loadInventory();
        loadAlerts();
        loadHistory();
    })
    .catch(err => alert(err.message));
}