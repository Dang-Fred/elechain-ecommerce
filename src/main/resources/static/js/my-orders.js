document.addEventListener("DOMContentLoaded", function () {
    // Tải trang 0 (trang đầu tiên) ngay khi mở giao diện
    loadOrders(0);
	
	
});

// Hàm gọi API lấy danh sách đơn hàng
async function loadOrders(pageNumber) {
    try {
        const response = await fetchWithAuth(`/api/client/orders/my-orders?page=${pageNumber}&size=10`);
        
        if (response.ok) {
            const data = await response.json();
            renderOrders(data.content);
            renderPagination(data.totalPages, data.number);
        } else {
            document.getElementById("order-list").innerHTML = 
                `<tr><td colspan="6" class="py-4 text-center text-danger">Lỗi khi tải dữ liệu đơn hàng.</td></tr>`;
        }
    } catch (error) {
        console.error("Lỗi Fetch My Orders:", error);
    }
}

// Hàm vẽ danh sách dữ liệu ra bảng
function renderOrders(orders) {
    const tbody = document.getElementById("order-list");
    tbody.innerHTML = ""; // Xóa loading spinner

    if (!orders || orders.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="py-4 text-center">Bạn chưa có đơn hàng nào.</td></tr>`;
        return;
    }

	
	
    orders.forEach(order => {
        // 1. Format Ngày Đặt (Xử lý mảng LocalDateTime của Spring Boot)
        let dateStr = "";
        if (Array.isArray(order.ngayDat)) {
            const pad = (n) => n < 10 ? '0' + n : n;
            // Format: DD/MM/YYYY HH:mm
            dateStr = `${pad(order.ngayDat[2])}/${pad(order.ngayDat[1])}/${order.ngayDat[0]} ${pad(order.ngayDat[3])}:${pad(order.ngayDat[4])}`;
        } else {
            const d = new Date(order.ngayDat);
            dateStr = d.toLocaleString('vi-VN');
        }

        // 2. Format Tiền tệ
        const formatter = new Intl.NumberFormat('vi-VN');
        const tongTienStr = formatter.format(order.tongTien) + ' VNĐ';

        // 3. Tô màu Trạng thái (Badge Bootstrap)
        let badgeClass = "bg-secondary";
        const status = (order.trangThai || "").toLowerCase();
        if (status.includes("chờ") || status.includes("pending")) {
            badgeClass = "bg-warning text-dark"; // Vàng
        } else if (status.includes("hoàn thành") || status.includes("đã giao") || status.includes("success")) {
            badgeClass = "bg-success"; // Xanh lá
        } else if (status.includes("hủy") || status.includes("cancel")) {
            badgeClass = "bg-danger"; // Đỏ
        }

        // 4. Tạo thẻ <tr>
		const tr = document.createElement("tr");
		        tr.innerHTML = `
		            <td class="py-4"><strong>#${order.maDH}</strong></td>
		            <td class="py-4">${dateStr}</td>
		            <td class="py-4">${order.phuongThucNhan}</td>
		            <td class="py-4 fw-bold text-primary">${tongTienStr}</td>
		            <td class="py-4"><span class="badge ${badgeClass} fs-6">${order.trangThai}</span></td>
		            <td class="py-4">
		                <!-- Đổi thành thẻ a nhảy sang trang chi tiết -->
		                <a href="/my-orders/detail/${order.maDH}" class="btn btn-sm btn-outline-info rounded-pill px-3">
		                    <i class="fas fa-eye me-1"></i> Xem chi tiết
		                </a>
		            </td>
		        `;
		        tbody.appendChild(tr);
    });
}

// Hàm vẽ phân trang (Pagination) chuẩn Template Electro
function renderPagination(totalPages, currentPage) {
    const paginationContainer = document.getElementById("pagination");
    paginationContainer.innerHTML = "";

    // Nếu chỉ có 1 trang thì không hiện thanh phân trang
    if (totalPages <= 1) return;

    // Nút "Lùi" (Previous)
    if (currentPage > 0) {
        const prevBtn = document.createElement("a");
        prevBtn.href = "#";
        prevBtn.className = "rounded";
        prevBtn.innerHTML = "&laquo;";
        prevBtn.addEventListener("click", (e) => { e.preventDefault(); loadOrders(currentPage - 1); });
        paginationContainer.appendChild(prevBtn);
    }

    // Nút số trang (1, 2, 3...)
    for (let i = 0; i < totalPages; i++) {
        const pageBtn = document.createElement("a");
        pageBtn.href = "#";
        pageBtn.className = (i === currentPage) ? "active rounded" : "rounded";
        pageBtn.innerText = i + 1;
        pageBtn.addEventListener("click", (e) => { e.preventDefault(); loadOrders(i); });
        paginationContainer.appendChild(pageBtn);
    }

    // Nút "Tiến" (Next)
    if (currentPage < totalPages - 1) {
        const nextBtn = document.createElement("a");
        nextBtn.href = "#";
        nextBtn.className = "rounded";
        nextBtn.innerHTML = "&raquo;";
        nextBtn.addEventListener("click", (e) => { e.preventDefault(); loadOrders(currentPage + 1); });
        paginationContainer.appendChild(nextBtn);
    }
}