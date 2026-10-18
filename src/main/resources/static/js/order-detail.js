document.addEventListener("DOMContentLoaded", async function () {
    const maDH = document.getElementById("orderIdStr").value;
    
    if (!maDH) {
        alert("Lỗi: Không tìm thấy mã đơn hàng hợp lệ!");
        window.location.href = "/my-orders";
        return;
    }

    try {
        const response = await fetchWithAuth('/api/client/orders/' + maDH);
        
        if (response.ok) {
            const data = await response.json();
            const formatter = new Intl.NumberFormat('vi-VN');
            
            // Format ngày tháng
            let dateStr = "";
            if (Array.isArray(data.ngayDat)) {
                const pad = (n) => n < 10 ? '0' + n : n;
                dateStr = `${pad(data.ngayDat[2])}/${pad(data.ngayDat[1])}/${data.ngayDat[0]} ${pad(data.ngayDat[3])}:${pad(data.ngayDat[4])}`;
            } else {
                dateStr = new Date(data.ngayDat).toLocaleString('vi-VN');
            }

            // Đổ dữ liệu text
            document.getElementById("txt-tenKH").innerText = data.tenKhachHang || "Chưa cập nhật";
            document.getElementById("txt-diaChi").innerText = data.diaChiGiao || "Chưa cập nhật";
            document.getElementById("txt-ngayDat").innerText = dateStr;
            document.getElementById("txt-phuongThucNhan").innerText = data.phuongThucNhan;
            
            // Xử lý Trạng thái & Xác định xem có được đánh giá hay không
            const statusLabel = document.getElementById("txt-trangThai");
            statusLabel.innerText = data.trangThai;
            const statusText = (data.trangThai || "").toLowerCase();
            
            let isCompleted = false; // Biến cờ kiểm tra đơn đã hoàn thành
            
            if (statusText.includes("chờ") || statusText.includes("pending")) {
                statusLabel.className = "badge bg-warning text-dark fs-6";
            } else if (statusText.includes("hoàn thành") || statusText.includes("đã giao") || statusText.includes("completed")) {
                statusLabel.className = "badge bg-success fs-6";
                isCompleted = true; // Bật cờ cho phép đánh giá
            } else if (statusText.includes("hủy") || statusText.includes("cancel")) {
                statusLabel.className = "badge bg-danger fs-6";
            } else {
                statusLabel.className = "badge bg-secondary fs-6";
            }

            document.getElementById("txt-tongTien").innerText = formatter.format(data.tongTien) + " VNĐ";

            // Render Bảng sản phẩm
            const tbody = document.getElementById("order-items-body");
            tbody.innerHTML = ""; 
            
            if (data.chiTietDonHangs && data.chiTietDonHangs.length > 0) {
                data.chiTietDonHangs.forEach(item => {
                    const thanhTien = item.donGia * item.soLuong;
                    
                    // Logic: Chỉ hiện nút "Đánh giá" nếu đơn hàng đã hoàn thành
                    let actionHtml = `<span class="text-muted small">Không khả dụng</span>`;
                    if (isCompleted) {
                        actionHtml = `<button class="btn btn-warning btn-sm btn-review text-dark fw-bold" data-masp="${item.maSP}"><i class="fas fa-star"></i> Đánh giá</button>`;
                    }

                    const tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td class="py-3">
                            <img src="${item.hinhAnh || '/img/default-product.jpg'}" alt="${item.tenSP}" style="width: 70px; height: 70px; object-fit: cover;" class="rounded img-fluid border">
                        </td>
                        <td class="text-start py-3">${item.tenSP}</td>
                        <td class="py-3">${formatter.format(item.donGia)} đ</td>
                        <td class="py-3">${item.soLuong}</td>
                        <td class="fw-bold text-primary py-3">${formatter.format(thanhTien)} đ</td>
                        <td class="py-3">${actionHtml}</td>
                    `;
                    tbody.appendChild(tr);
                });
            } else {
                tbody.innerHTML = `<tr><td colspan="6" class="text-center py-5">Không có thông tin sản phẩm.</td></tr>`;
            }
            
        } else {
            alert("Đơn hàng không tồn tại hoặc bạn không có quyền truy cập!");
            window.location.href = "/my-orders";
        }
    } catch (error) {
        console.error("Lỗi Fetch Order Detail:", error);
    }

    // ==========================================
    // LOGIC XỬ LÝ SỰ KIỆN REVIEW (MODAL)
    // ==========================================
    
    let currentReviewBtn = null; // Biến lưu tạm nút vừa click để lát nữa ẩn đi
    const orderItemsBody = document.getElementById("order-items-body");
    
    // 1. Mở Modal khi click "Đánh giá"
    orderItemsBody.addEventListener("click", function (e) {
        const btn = e.target.closest('.btn-review');
        if (!btn) return;

        e.preventDefault();
        currentReviewBtn = btn;
        
        // Reset form
        document.getElementById("review-soSao").value = "5";
        document.getElementById("review-binhLuan").value = "";
        
        // Lưu maSP vào input ẩn
        document.getElementById("review-maSP").value = btn.getAttribute("data-masp");

        // Gọi Bootstrap Modal lên
		const modalEl = document.getElementById('reviewModal');
		        const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
		        modalInstance.show();
    });

    // 2. Submit Đánh giá lên Server
    document.getElementById("btn-submit-review").addEventListener("click", async function () {
        const maSP = document.getElementById("review-maSP").value;
        const soSao = document.getElementById("review-soSao").value;
        const binhLuan = document.getElementById("review-binhLuan").value.trim();

        if (!soSao) {
            alert("Vui lòng chọn số sao!");
            return;
        }

        const payload = {
            maDH: parseInt(maDH),
            maSP: parseInt(maSP),
            soSao: parseInt(soSao),
            binhLuan: binhLuan
        };

        const submitBtn = this;
        submitBtn.disabled = true;
        submitBtn.innerText = "Đang gửi...";

		try {
		            const response = await fetchWithAuth('/api/reviews', {
		                method: 'POST',
		                body: JSON.stringify(payload)
		            });

		            // Đọc dữ liệu thô (Text) trước để không bị lỗi parse
		            const textResponse = await response.text();
		            let errorMessage = "Không thể gửi đánh giá lúc này.";
		            
		            // Cố gắng bóc tách xem nó là JSON hay Text thuần
		            try {
		                const data = JSON.parse(textResponse);
		                errorMessage = data.message || data.error || textResponse;
		            } catch (err) {
		                // Nếu không phải JSON thì lấy nguyên câu Text của Backend
		                errorMessage = textResponse; 
		            }

		            if (response.ok) {
		                alert("Đánh giá thành công! Cảm ơn bạn.");
		                
		                // Đóng modal
		                const modalEl = document.getElementById('reviewModal');
		                const modalInstance = bootstrap.Modal.getInstance(modalEl);
		                if (modalInstance) modalInstance.hide();

		                // Đổi nút Đánh giá thành "Đã đánh giá" và khóa lại
		                if (currentReviewBtn) {
		                    currentReviewBtn.className = "btn btn-outline-success btn-sm disabled";
		                    currentReviewBtn.innerHTML = '<i class="fas fa-check"></i> Đã đánh giá';
		                }
		            } else {
		                // In chính xác câu báo lỗi từ Backend ra màn hình (ví dụ: "Đã đánh giá rồi")
		                alert("Thông báo: " + errorMessage);
		            }
		        } catch (error) {
		            console.error("Lỗi gửi đánh giá:", error);
		            alert("Lỗi kết nối mạng, vui lòng thử lại.");
		        } finally {
		            submitBtn.disabled = false;
		            submitBtn.innerText = "Gửi đánh giá";
		        }
    });
});