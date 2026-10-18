document.addEventListener("DOMContentLoaded", async function () {
    
    // 1. Lấy mã đơn hàng từ input ẩn
    const maDH = document.getElementById("orderIdStr").value;
    
    if (!maDH) {
        alert("Lỗi: Không tìm thấy mã đơn hàng hợp lệ!");
        window.location.href = "/my-orders";
        return;
    }

    try {
        // 2. Gọi API sử dụng chung hàm fetchWithAuth để kẹp JWT
        const response = await fetchWithAuth('/api/client/orders/' + maDH);
        
        if (response.ok) {
            const data = await response.json();
            const formatter = new Intl.NumberFormat('vi-VN');
            
            // Xử lý format ngày giờ Spring Boot (LocalDateTime array)
            let dateStr = "";
            if (Array.isArray(data.ngayDat)) {
                const pad = (n) => n < 10 ? '0' + n : n;
                dateStr = `${pad(data.ngayDat[2])}/${pad(data.ngayDat[1])}/${data.ngayDat[0]} ${pad(data.ngayDat[3])}:${pad(data.ngayDat[4])}`;
            } else {
                dateStr = new Date(data.ngayDat).toLocaleString('vi-VN');
            }

            // 3. Đổ dữ liệu vào khối Thông tin đơn hàng (Card 1)
            document.getElementById("txt-tenKH").innerText = data.tenKhachHang || "Chưa cập nhật";
            document.getElementById("txt-diaChi").innerText = data.diaChiGiao || "Chưa cập nhật";
            document.getElementById("txt-ngayDat").innerText = dateStr;
            document.getElementById("txt-phuongThucNhan").innerText = data.phuongThucNhan;
            
            // Format màu cho Label Trạng thái
            const statusLabel = document.getElementById("txt-trangThai");
            statusLabel.innerText = data.trangThai;
            const statusText = (data.trangThai || "").toLowerCase();
            if (statusText.includes("chờ") || statusText.includes("pending")) {
                statusLabel.className = "badge bg-warning text-dark fs-6";
            } else if (statusText.includes("hoàn thành") || statusText.includes("đã giao") || statusText.includes("success")) {
                statusLabel.className = "badge bg-success fs-6";
            } else if (statusText.includes("hủy") || statusText.includes("cancel")) {
                statusLabel.className = "badge bg-danger fs-6";
            } else {
                statusLabel.className = "badge bg-secondary fs-6";
            }

            document.getElementById("txt-tongTien").innerText = formatter.format(data.tongTien) + " VNĐ";

            // 4. Lặp dữ liệu vào khối Bảng sản phẩm (Card 2)
            const tbody = document.getElementById("order-items-body");
            tbody.innerHTML = ""; // Xóa icon loading
            
            if (data.chiTietDonHangs && data.chiTietDonHangs.length > 0) {
                data.chiTietDonHangs.forEach(item => {
                    const thanhTien = item.donGia * item.soLuong;
                    const tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td class="py-3">
                            <img src="${item.hinhAnh || '/img/default-product.jpg'}" alt="${item.tenSP}" style="width: 70px; height: 70px; object-fit: cover;" class="rounded img-fluid border">
                        </td>
                        <td class="text-start py-3">${item.tenSP}</td>
                        <td class="py-3">${formatter.format(item.donGia)} đ</td>
                        <td class="py-3">${item.soLuong}</td>
                        <td class="fw-bold text-primary py-3">${formatter.format(thanhTien)} đ</td>
                    `;
                    tbody.appendChild(tr);
                });
            } else {
                tbody.innerHTML = `<tr><td colspan="5" class="text-center py-5">Không có thông tin sản phẩm.</td></tr>`;
            }
            
        } else {
            alert("Đơn hàng không tồn tại hoặc bạn không có quyền truy cập!");
            window.location.href = "/my-orders";
        }
    } catch (error) {
        console.error("Lỗi Fetch Order Detail:", error);
        alert("Lỗi kết nối đến máy chủ.");
        window.location.href = "/my-orders";
    }
});