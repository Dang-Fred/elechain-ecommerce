const fetchOptions = { credentials: 'include' };

document.addEventListener('DOMContentLoaded', function() {
    loadBranches();
});

// ==========================================
// 1. TẢI VÀ RENDER DANH SÁCH CHI NHÁNH
// ==========================================
function loadBranches() {
    const tbody = document.getElementById('branch-tbody');
    
    fetch('/api/v1/chi-nhanh/all', { method: 'GET', ...fetchOptions })
        .then(res => {
            if (!res.ok) throw new Error("Lỗi khi tải danh sách chi nhánh!");
            return res.json();
        })
        .then(data => {
            if (data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">Chưa có chi nhánh nào trong hệ thống.</td></tr>`;
                return;
            }

            tbody.innerHTML = data.map(branch => {
                // Kiểm tra trạng thái an toàn (chuẩn bị cho trường hợp Backend trả về boolean hoặc chuỗi "true")
                const isActive = branch.trangThai === true || String(branch.trangThai).toLowerCase() === "true";
                
                // Giao diện thay đổi theo trạng thái
                const rowClass = isActive ? "" : "opacity-50 text-muted bg-light";
                const statusBadge = isActive 
                    ? `<span class="badge bg-success">Đang hoạt động</span>`
                    : `<span class="badge bg-secondary">Ngừng hoạt động</span>`;
                
                // Format string cẩn thận để nhúng vào function JS (chống lỗi dấu nháy đơn trong chuỗi)
                const escapeStr = (str) => (str || '').replace(/'/g, "\\'");

                let actionButtons = "";
                if (isActive) {
                    actionButtons = `
                        <button class="btn btn-sm btn-outline-warning me-1" 
                                onclick="openEditModal(${branch.maCN}, '${escapeStr(branch.tenCN)}', '${escapeStr(branch.diaChi)}', '${escapeStr(branch.hotline)}')" 
                                title="Sửa">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" 
                                onclick="deleteBranch(${branch.maCN})" 
                                title="Ngừng hoạt động">
                            <i class="bi bi-power"></i>
                        </button>
                    `;
                } else {
                    actionButtons = `
                        <button class="btn btn-sm btn-outline-success" 
                                onclick="restoreBranch(${branch.maCN})" 
                                title="Khôi phục">
                            <i class="bi bi-arrow-counterclockwise"></i> Khôi phục
                        </button>
                    `;
                }

                return `
                    <tr class="${rowClass}">
                        <td class="fw-medium">#${branch.maCN}</td>
                        <td class="fw-bold">${branch.tenCN}</td>
                        <td>${branch.diaChi}</td>
                        <td class="fw-medium text-dark">${branch.hotline}</td>
                        <td>${statusBadge}</td>
                        <td>${actionButtons}</td>
                    </tr>
                `;
            }).join('');
        })
        .catch(err => {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger">${err.message}</td></tr>`;
        });
}

// ==========================================
// 2. MỞ MODAL THÊM / SỬA
// ==========================================
function openAddModal() {
    document.getElementById('branchForm').reset();
    document.getElementById('maCN').value = "";
    document.getElementById('branchModalTitle').textContent = "Thêm Chi nhánh";
    
    const modal = new bootstrap.Modal(document.getElementById('branchModal'));
    modal.show();
}

function openEditModal(id, tenCN, diaChi, hotline) {
    document.getElementById('branchForm').reset();
    
    document.getElementById('maCN').value = id;
    document.getElementById('tenCN').value = tenCN;
    document.getElementById('diaChi').value = diaChi;
    document.getElementById('hotline').value = hotline;
    
    document.getElementById('branchModalTitle').textContent = "Cập nhật Chi nhánh";
    
    const modal = new bootstrap.Modal(document.getElementById('branchModal'));
    modal.show();
}

// ==========================================
// 3. XỬ LÝ LƯU (THÊM / CẬP NHẬT)
// ==========================================
function saveBranch(event) {
    event.preventDefault();
    
    const maCN = document.getElementById('maCN').value;
    const isUpdate = maCN && maCN.trim() !== "";
    
    // Đóng gói data dạng JSON thay vì FormData vì API không upload file
    const requestPayload = {
        tenCN: document.getElementById('tenCN').value.trim(),
        diaChi: document.getElementById('diaChi').value.trim(),
        hotline: document.getElementById('hotline').value.trim()
    };

    const method = isUpdate ? 'PUT' : 'POST';
    const url = isUpdate ? `/api/v1/chi-nhanh/${maCN}` : `/api/v1/chi-nhanh`;

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(requestPayload)
    })
    .then(async res => {
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Lỗi hệ thống khi lưu chi nhánh!");
        }
        return res.json();
    })
    .then(data => {
        alert(isUpdate ? "Cập nhật chi nhánh thành công!" : "Thêm chi nhánh thành công!");
        
        // Đóng modal
        const modalInstance = bootstrap.Modal.getInstance(document.getElementById('branchModal'));
        if (modalInstance) modalInstance.hide();
        
        // Tải lại bảng
        loadBranches();
    })
    .catch(err => alert(err.message));
}

// ==========================================
// 4. NGỪNG HOẠT ĐỘNG (XÓA MỀM) VÀ KHÔI PHỤC
// ==========================================
function deleteBranch(id) {
    if (!confirm("XÁC NHẬN: Bạn có chắc chắn muốn ngừng hoạt động chi nhánh này không?")) return;

    fetch(`/api/v1/chi-nhanh/${id}`, {
        method: 'DELETE',
        credentials: 'include'
    })
    .then(async res => {
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Lỗi khi ngừng hoạt động chi nhánh!");
        }
        alert("Đã ngừng hoạt động chi nhánh thành công!");
        loadBranches();
    })
    .catch(err => alert(err.message));
}

function restoreBranch(id) {
    if (!confirm("XÁC NHẬN: Bạn muốn khôi phục lại hoạt động cho chi nhánh này?")) return;

    fetch(`/api/v1/chi-nhanh/${id}/restore`, {
        method: 'PUT',
        credentials: 'include'
    })
    .then(async res => {
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Lỗi khi khôi phục chi nhánh!");
        }
        alert("Khôi phục chi nhánh thành công!");
        loadBranches();
    })
    .catch(err => alert(err.message));
}