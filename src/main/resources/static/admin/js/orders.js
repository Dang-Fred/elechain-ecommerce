// Đảm bảo DOM load xong mới thực thi gọi API
document.addEventListener("DOMContentLoaded", function() {
    fetchOrders();
});

function fetchOrders() {
    const tableBody = document.getElementById('order-table-body');

    // Gọi lên Backend API REST
    // Gửi JWT Cookie lên Server thông qua credentials: 'include'
    fetch('/api/admin/orders?page=0&size=20', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include'
    })
    .then(response => {
        // Xử lý báo lỗi bảo mật nếu Token hết hạn hoặc sai Role
        if (response.status === 401 || response.status === 403) {
            alert("Phiên đăng nhập đã hết hạn hoặc bạn không có quyền truy cập. Vui lòng đăng nhập lại!");
            // window.location.href = '/auth/login';
            throw new Error("Unauthorized");
        }
        if (!response.ok) {
            throw new Error("Lỗi máy chủ hoặc API không tồn tại.");
        }
        return response.json();
    })
    .then(pageData => {
        // Dữ liệu Spring Data JPA Page<T> thường nằm trong mảng 'content'
        const orders = pageData.content;
        
        tableBody.innerHTML = ''; // Xoá dòng "Đang tải dữ liệu..."

        if (!orders || orders.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Không có đơn hàng nào!</td></tr>';
            return;
        }

        // Lặp qua mảng dữ liệu và tạo Element DOM
        orders.forEach(order => {
            // Chuẩn hóa định dạng tiền tệ và thời gian
            const formatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
            const dateStr = new Date(order.ngayDat).toLocaleString('vi-VN');
            
            // Giả định backend trả về field diaChiGiao hoặc tên lấy từ DTO, mình dùng order.nguoiNhan (tùy biến theo DTO)
            const recipientName = order.nguoiNhan ? order.nguoiNhan : "Khách hàng"; 
            const statusBadgeColor = getStatusBadge(order.trangThai);

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-medium">${order.maDH}</td>
                <td>${dateStr}</td>
                <td>${recipientName}</td>
                <td class="text-danger fw-bold">${formatter.format(order.tongTien)}</td>
                <td><span class="badge ${statusBadgeColor}">${order.trangThai}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-info" onclick="viewOrderDetails('${order.maDH}')">
                        Xem chi tiết
                    </button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    })
    .catch(error => {
        console.error("Lỗi khi tải đơn hàng:", error);
        tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-danger">Lỗi kết nối hoặc tải dữ liệu thất bại!</td></tr>`;
    });
}

// Hàm format màu badge Bootstrap tùy theo trạng thái
function getStatusBadge(status) {
    if (!status) return "bg-secondary";
    const stt = status.toLowerCase();
    if (stt.includes("chờ")) return "bg-warning text-dark"; // Chờ xác nhận
    if (stt.includes("đang")) return "bg-primary";          // Đang giao
    if (stt.includes("hoàn thành")) return "bg-success";    // Hoàn thành
    if (stt.includes("hủy")) return "bg-danger";            // Đã hủy
    return "bg-secondary";
}

// Ví dụ hàm bắt sự kiện click xem chi tiết
function viewOrderDetails(maDH) {
    console.log("Xem chi tiết đơn: " + maDH);
    // Logic gọi API chi tiết đơn hàng hoặc bật Modal
}