document.addEventListener("DOMContentLoaded", function () {
    
    // Logic ẩn/hiện ô nhập địa chỉ tùy theo Phương thức nhận hàng
    const ptNhanSelect = document.getElementById("phuongThucNhan");
    const addressInput = document.getElementById("address");
    const cityInput = document.getElementById("city");

    ptNhanSelect.addEventListener("change", function () {
        if (this.value === "Nhận tại cửa hàng") {
            addressInput.disabled = true;
            cityInput.disabled = true;
            addressInput.value = "";
            cityInput.value = "";
        } else {
            addressInput.disabled = false;
            cityInput.disabled = false;
        }
    });

    // Lắng nghe sự kiện click Chốt đơn
    const btnPlaceOrder = document.getElementById("btn-place-order");
    if (btnPlaceOrder) {
        btnPlaceOrder.addEventListener("click", async function (e) {
            e.preventDefault();

            // 1. Thu thập dữ liệu form
            const maCN = document.getElementById("chiNhanh").value;
            const phuongThucNhan = ptNhanSelect.value;
            const address = addressInput.value.trim();
            const city = cityInput.value.trim();

            let diaChiGiao = "";

            // 2. Validate dữ liệu
            if (!maCN) {
                alert("Vui lòng chọn chi nhánh!");
                return;
            }

            if (phuongThucNhan === "Giao tận nơi") {
                if (!address || !city) {
                    alert("Vui lòng nhập đầy đủ Số nhà và Thành phố để chúng tôi giao hàng!");
                    return;
                }
                diaChiGiao = address + ", " + city;
            }

            // Vô hiệu hóa nút để tránh click 2 lần
            btnPlaceOrder.disabled = true;
            btnPlaceOrder.innerText = "Đang xử lý...";

            // 3. Đóng gói payload
            const payload = {
                maCN: parseInt(maCN),
                phuongThucNhan: phuongThucNhan,
                diaChiGiao: diaChiGiao
            };

            // 4. Gọi API
            try {
                // Lưu ý: Dùng fetchWithAuth từ common.js
                const response = await fetchWithAuth('/api/client/order/checkout', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });

                // Phân tích response (Giả định Backend trả về JSON: {message: "..."})
                let responseData;
                try {
                    responseData = await response.json();
                } catch (err) {
                    responseData = { message: "Lỗi không xác định từ máy chủ." };
                }

                if (response.ok) {
                    // HTTP 200 OK
                    alert("Đặt hàng thành công! Cảm ơn bạn đã mua sắm.");
                    window.location.href = "/"; // Hoặc chuyển sang trang /history (Lịch sử đơn hàng)
                } else {
                    // Lỗi 400 hoặc 500
                    alert("Lỗi đặt hàng: " + (responseData.message || responseData.error || "Không thể đặt hàng lúc này."));
                    
                    // Khôi phục nút
                    btnPlaceOrder.disabled = false;
                    btnPlaceOrder.innerText = "CHỐT ĐƠN";
                }
            } catch (error) {
                console.error("Lỗi Fetch Checkout:", error);
                // Nếu bị ném ra do 401/403 (từ common.js), common.js đã tự redirect ra login
                btnPlaceOrder.disabled = false;
                btnPlaceOrder.innerText = "CHỐT ĐƠN";
            }
        });
    }
});