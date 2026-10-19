const fetchOptions = { credentials: 'include' };
let currentTargetEmpId = null; // Biến toàn cục lưu ID nhân viên đang sửa quyền

document.addEventListener('DOMContentLoaded', function() {
    loadBranches();
    loadEmployees();

    // Lắng nghe sự kiện đổi Vai trò ở cả 2 form để ẩn/hiện nhánh
    document.getElementById('add-vaiTro').addEventListener('change', () => {
        handleRoleLogic('add-vaiTro', 'add-maCN', 'add-branch-container');
    });
    
    document.getElementById('update-vaiTro').addEventListener('change', () => {
        handleRoleLogic('update-vaiTro', 'update-maCN', 'update-branch-container');
    });
});

// ==========================================
// 1. LOGIC ẨN/HIỆN CHI NHÁNH DỰA VÀO VAI TRÒ
// ==========================================
function handleRoleLogic(roleSelectId, branchSelectId, branchContainerId) {
    const role = document.getElementById(roleSelectId).value;
    const branchSelect = document.getElementById(branchSelectId);
    const branchContainer = document.getElementById(branchContainerId);

    if (role === 'ADMIN') {
        // Admin quản lý toàn hệ thống, không gán vào chi nhánh
        branchSelect.value = '';
        branchSelect.removeAttribute('required');
        branchContainer.style.display = 'none';
    } else {
        // NV Kho, Bán hàng, QLCN bắt buộc có chi nhánh
        branchSelect.setAttribute('required', 'required');
        branchContainer.style.display = 'block';
    }
}

// ==========================================
// 2. TẢI DỮ LIỆU BẢNG VÀ SELECT CHI NHÁNH
// ==========================================
function loadBranches() {
    fetch('/api/v1/chi-nhanh/all', { method: 'GET', ...fetchOptions })
        .then(res => res.ok ? res.json() : [])
        .then(data => {
            const addSelect = document.getElementById('add-maCN');
            const updateSelect = document.getElementById('update-maCN');
            let options = '<option value="">-- Chọn chi nhánh --</option>';
            
            data.filter(b => b.trangThai === true).forEach(b => {
                options += `<option value="${b.maCN}">${b.tenCN}</option>`;
            });

            addSelect.innerHTML = options;
            updateSelect.innerHTML = options;
        }).catch(err => console.error("Lỗi nạp DS Chi nhánh:", err));
}

function loadEmployees() {
    const tbody = document.getElementById('employee-tbody');
    
    fetch('/api/admin/employees', { method: 'GET', ...fetchOptions })
        .then(res => {
            if (!res.ok) throw new Error("Không thể tải danh sách nhân viên");
            return res.json();
        })
		.then(data => {
		            // Tự động tìm đúng mảng dữ liệu dù Backend trả về dạng List, Page<T>, hay ApiResponse
		            const empArray = Array.isArray(data) ? data : (data.content || data.data || data.result || []);

		            if (empArray.length === 0) {
		                tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">Chưa có nhân viên nào.</td></tr>`;
		                return;
		            }

		            tbody.innerHTML = empArray.map(emp => {
		                // Màu badge vai trò
		                let roleColor = 'bg-secondary';
		                if (emp.vaiTro === 'ADMIN') roleColor = 'bg-danger';
		                else if (emp.vaiTro === 'QLCN') roleColor = 'bg-warning text-dark';
		                else if (emp.vaiTro === 'NV_KHO') roleColor = 'bg-primary';
		                else if (emp.vaiTro === 'NV_BANHANG') roleColor = 'bg-info text-dark';

		                // Trạng thái khóa / mở
		                const isActive = emp.trangThai === true || String(emp.trangThai).toLowerCase() === "true";
		                const statusBadge = isActive 
		                    ? `<span class="badge bg-success">Hoạt động</span>` 
		                    : `<span class="badge bg-secondary">Bị khóa</span>`;
		                const rowClass = isActive ? "" : "bg-light opacity-75";

		                return `
		                    <tr class="${rowClass}">
		                        <td class="fw-medium text-muted">#${emp.maNV}</td>
		                        <td>
		                            <div class="d-flex align-items-center">
		                                <img src="https://placehold.co/40x40/e9ecef/495057?text=${(emp.tenNV || 'U').charAt(0).toUpperCase()}" 
		                                     class="rounded-circle me-3" style="width: 36px; height: 36px; object-fit: cover;">
		                                <div class="fw-bold">${emp.tenNV}</div>
		                            </div>
		                        </td>
		                        <td>
		                            <div class="small">${emp.email}</div>
		                            <div class="small text-muted">${emp.soDienThoai || 'N/A'}</div>
		                        </td>
		                        <td><span class="badge ${roleColor}">${emp.vaiTro}</span></td>
		                        <td>${emp.tenCN || '<span class="text-muted small">Toàn hệ thống</span>'}</td>
		                        <td>${statusBadge}</td>
		                        <td class="text-center">
		                            <!-- Nút 3 chấm Dropdown -->
		                            <div class="dropdown">
		                                <button class="btn btn-sm btn-light dropdown-toggle" type="button" data-bs-toggle="dropdown">
		                                    <i class="bi bi-three-dots"></i>
		                                </button>
		                                <ul class="dropdown-menu dropdown-menu-end shadow-sm">
		                                    <li>
												<a class="dropdown-item" href="javascript:void(0)" data-bs-toggle="modal" data-bs-target="#updateRoleModal" onclick="openUpdateRoleModal(${emp.maNV}, '${emp.vaiTro}', '${emp.maCN || ''}')">
											    	<i class="bi bi-shield-lock me-2 text-primary"></i>Phân quyền
												</a>
		                                    </li>
		                                    <li><hr class="dropdown-divider"></li>
		                                    <li>
		                                        <a class="dropdown-item ${isActive ? 'text-danger' : 'text-success'}" href="javascript:void(0)" onclick="toggleStatus(${emp.maNV})">
		                                            <i class="bi bi-power me-2"></i>${isActive ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}
		                                        </a>
		                                    </li>
		                                </ul>
		                            </div>
		                        </td>
		                    </tr>
		                `;
		            }).join('');
		        })
        .catch(err => {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger">${err.message}</td></tr>`;
        });
}

// ==========================================
// 3. NGHIỆP VỤ THÊM MỚI NHÂN VIÊN
// ==========================================
function openAddModal() {
    document.getElementById('addEmployeeForm').reset();
    handleRoleLogic('add-vaiTro', 'add-maCN', 'add-branch-container'); // Chạy logic hiển thị nhánh
    
}

function saveEmployee(event) {
    event.preventDefault();
    
    const payload = {
        tenNV: document.getElementById('add-tenNV').value.trim(),
        soDienThoai: document.getElementById('add-soDienThoai').value.trim(),
        email: document.getElementById('add-email').value.trim(),
        matKhau: document.getElementById('add-matKhau').value,
        vaiTro: document.getElementById('add-vaiTro').value,
        maCN: document.getElementById('add-vaiTro').value !== 'ADMIN' ? document.getElementById('add-maCN').value : null
    };

    fetch('/api/admin/employees', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
    })
    .then(async res => {
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Lỗi tạo tài khoản!");
        }
        alert("Tạo tài khoản nhân viên thành công!");
		// 1. Trong hàm saveEmployee, thay dòng .click() cũ bằng:
		    const btnCloseAdd = document.querySelector('#addEmployeeModal .btn-close');
		    btnCloseAdd.click();
		    btnCloseAdd.blur(); // Ép nhả focus ngay lập tức
        loadEmployees();
    })
    .catch(err => alert(err.message));
}

// ==========================================
// 4. NGHIỆP VỤ PHÂN QUYỀN
// ==========================================
function openUpdateRoleModal(id, role, branchId) {
    currentTargetEmpId = id;
    document.getElementById('update-vaiTro').value = role;
    
    if (branchId && branchId !== 'null') {
        document.getElementById('update-maCN').value = branchId;
    } else {
        document.getElementById('update-maCN').value = "";
    }

    handleRoleLogic('update-vaiTro', 'update-maCN', 'update-branch-container');
    
}

function updateEmployeeRole(event) {
    event.preventDefault();
    if (!currentTargetEmpId) return;

    const role = document.getElementById('update-vaiTro').value;
    const payload = {
        vaiTro: role,
        maCN: role !== 'ADMIN' ? document.getElementById('update-maCN').value : null
    };

    fetch(`/api/admin/employees/${currentTargetEmpId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
    })
    .then(async res => {
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Lỗi cập nhật phân quyền!");
        }
        alert("Phân quyền thành công!");
		// 2. Trong hàm updateEmployeeRole, thay dòng .click() cũ bằng:
		    const btnCloseUpdate = document.querySelector('#updateRoleModal .btn-close');
		    btnCloseUpdate.click();
		    btnCloseUpdate.blur(); // Ép nhả focus ngay lập tức
        loadEmployees();
    })
    .catch(err => alert(err.message));
}

// ==========================================
// 5. NGHIỆP VỤ KHÓA / MỞ KHÓA TÀI KHOẢN
// ==========================================
function toggleStatus(id) {
    if (!confirm("Bạn có chắc chắn muốn thay đổi trạng thái hoạt động của tài khoản này?")) return;

    fetch(`/api/admin/employees/${id}/status`, {
        method: 'PATCH',
        credentials: 'include'
    })
    .then(async res => {
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Không thể thực hiện hành động này!");
        }
        alert("Thay đổi trạng thái tài khoản thành công!");
        loadEmployees();
    })
    .catch(err => alert(err.message)); // Nếu tự khóa chính mình backend sẽ quăng lỗi ra đây
}