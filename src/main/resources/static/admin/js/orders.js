const fetchOptions = {
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include'
};

const vndFormatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });

document.addEventListener('DOMContentLoaded', function() {
    loadSummary();
    loadOrders(0);
});

// 1. GỌI API SUMMARY
function loadSummary() {
    const totalEl = document.getElementById('summary-total');
    // Chỉ gọi API nếu thẻ card tồn tại trên DOM (Nghĩa là Role ADMIN hoặc QLCN)
    if (!totalEl) return;

    fetch('/api/admin/orders/summary', { ...fetchOptions, method: 'GET' })
        .then(res => res.json())
        .then(data => {
            if (data) {
                totalEl.textContent = data.totalOrders || 0;
                document.getElementById('summary-pending').textContent = data.pendingOrders || 0;
                document.getElementById('summary-delivering').textContent = data.deliveringOrders || 0;
                document.getElementById('summary-completed').textContent = data.completedOrders || 0;
            }
        })
        .catch(err => console.error("Lỗi load summary:", err));
}

// 2. GỌI API DANH SÁCH ĐƠN HÀNG
function loadOrders(page) {
    fetch(`/api/admin/orders?page=${page}&size=20`, { ...fetchOptions, method: 'GET' })
        .then(res => res.json())
        .then(pageData => {
            renderOrdersTable(pageData.content || []);
        })
        .catch(err => console.error("Lỗi load đơn hàng:", err));
}

// 3. RENDER BẢNG & STATE MACHINE
function renderOrdersTable(orders) {
    const tbody = document.getElementById('orders-tbody');
    tbody.innerHTML = '';

    if (orders.length === 0) {
        // Căn colspan linh hoạt dựa vào việc có cột Thao tác hay không
        const colSpan = (currentRole === 'QLCN' || currentRole === 'NV_BANHANG') ? 6 : 5;
        tbody.innerHTML = `<tr><td colspan="${colSpan}" class="text-muted py-4">Không có đơn hàng nào</td></tr>`;
        return;
    }

    orders.forEach(order => {
        // Format Ngày tháng
        let dateStr = "N/A";
        if (order.ngayDat) {
            const d = new Date(order.ngayDat);
            dateStr = `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
        }

        // Xử lý Badge Trạng thái
        let badgeHtml = '';
        let isPending = false;
        let isDelivering = false;

        // Cover cả trường hợp Backend trả về Tiếng Việt hoặc ENUM
        if (order.trangThai === 'PENDING' || order.trangThai === 'Chờ duyệt') {
            badgeHtml = '<span class="badge bg-warning text-dark">Chờ duyệt</span>';
            isPending = true;
        } else if (order.trangThai === 'DELIVERING' || order.trangThai === 'Đang giao') {
            badgeHtml = '<span class="badge bg-info">Đang giao</span>';
            isDelivering = true;
        } else if (order.trangThai === 'COMPLETED' || order.trangThai === 'Hoàn thành') {
            badgeHtml = '<span class="badge bg-success">Hoàn thành</span>';
        } else if (order.trangThai === 'CANCELLED' || order.trangThai === 'Đã hủy') {
            badgeHtml = '<span class="badge bg-danger">Đã hủy</span>';
        } else {
            badgeHtml = `<span class="badge bg-secondary">${order.trangThai}</span>`;
        }

        // Xử lý Cột Thao tác (Chỉ cho QLCN hoặc NV_BANHANG)
        let actionTd = '';
        if (currentRole === 'QLCN' || currentRole === 'NV_BANHANG') {
            let buttonsHtml = '';
            
            if (isPending) {
                buttonsHtml = `
                    <button class="btn btn-sm btn-primary me-1" onclick="updateStatus('${order.maDH}', 'DELIVERING')">Giao hàng</button>
                    <button class="btn btn-sm btn-outline-danger" onclick="updateStatus('${order.maDH}', 'CANCELLED')">Hủy</button>
                `;
            } else if (isDelivering) {
                buttonsHtml = `
                    <button class="btn btn-sm btn-success" onclick="updateStatus('${order.maDH}', 'COMPLETED')">Hoàn thành</button>
                `;
            } else {
                buttonsHtml = `<i class="bi bi-lock text-muted" title="Đã chốt"></i>`;
            }
            
            actionTd = `<td>${buttonsHtml}</td>`;
        }

        // Đổ HTML
        tbody.insertAdjacentHTML('beforeend', `
            <tr>
                <td class="fw-bold text-primary">#${order.maDH}</td>
                <td class="small text-muted">${dateStr}</td>
                <td class="text-start">${order.khachHang || 'Khách vãng lai'}</td>
                <td class="fw-medium">${vndFormatter.format(order.tongTien)}</td>
                <td>${badgeHtml}</td>
                ${actionTd}
            </tr>
        `);
    });
}

// 4. GỌI API CẬP NHẬT TRẠNG THÁI
function updateStatus(maDH, newStatus) {
    // Tùy chỉnh câu hỏi Confirm
    let actionName = newStatus === 'DELIVERING' ? 'Giao đơn hàng này' 
                   : newStatus === 'COMPLETED' ? 'Chốt Hoàn thành đơn này' 
                   : 'HỦY đơn hàng này';
                   
    if (!confirm(`Xác nhận: Bạn muốn ${actionName}?`)) return;

    fetch(`/api/admin/orders/${maDH}/status`, {
        ...fetchOptions,
        method: 'PUT',
        body: JSON.stringify({ status: newStatus })
    })
    .then(async res => {
        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.message || "Cập nhật thất bại!");
        }
        return res.json();
    })
    .then(data => {
        // Cập nhật thành công -> Load lại dữ liệu
        loadOrders(0);
        loadSummary();
    })
    .catch(err => {
        alert(err.message);
    });
}