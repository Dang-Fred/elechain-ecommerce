document.addEventListener("DOMContentLoaded", function () {
    const formatter = new Intl.NumberFormat('vi-VN');
    const txtBalance = document.getElementById("txt-balance");
    const inputTopup = document.getElementById("input-topup-amount");
    const btnTopup = document.getElementById("btn-topup");

    // ==========================================
    // 1. HÀM LOAD SỐ DƯ HIỆN TẠI
    // ==========================================
    async function loadBalance() {
        try {
            const response = await fetchWithAuth('/api/wallet/balance');
            
            // Lấy text thô để xử lý an toàn
            const textResponse = await response.text();
            let data = {};
            
            try {
                data = JSON.parse(textResponse);
            } catch (err) {
                console.error("Dữ liệu trả về không phải JSON:", textResponse);
            }

            if (response.ok && data.success) {
                txtBalance.innerText = formatter.format(data.data) + " VNĐ";
                txtBalance.classList.replace("text-danger", "text-success");
            } else {
                txtBalance.innerText = "Lỗi tải số dư";
                txtBalance.classList.replace("text-success", "text-danger");
                console.error("API báo lỗi:", data.message || textResponse);
            }
        } catch (error) {
            console.error("Lỗi Fetch Balance:", error);
            txtBalance.innerText = "Mất kết nối";
            txtBalance.classList.replace("text-success", "text-danger");
        }
    }

    // Gọi load số dư ngay khi vào trang
    loadBalance();


    // ==========================================
    // 2. SỰ KIỆN NẠP TIỀN
    // ==========================================
    if (btnTopup) {
        btnTopup.addEventListener("click", async function (e) {
            e.preventDefault();

            // Validate dữ liệu đầu vào
            const amountVal = inputTopup.value.trim();
            const soTien = parseFloat(amountVal);

            if (!amountVal || isNaN(soTien) || soTien <= 0) {
                alert("Vui lòng nhập số tiền hợp lệ (lớn hơn 0)!");
                inputTopup.focus();
                return;
            }

            // Đóng gói payload
            const payload = { soTien: soTien };

            // Trạng thái Loading cho nút
            const originalBtnHtml = btnTopup.innerHTML;
            btnTopup.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Đang xử lý...`;
            btnTopup.disabled = true;

            try {
                const response = await fetchWithAuth('/api/wallet/topup', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });

                // Đọc text thô phòng trường hợp Spring trả về chữ thay vì JSON (lỗi 400 Validation)
                const textResponse = await response.text();
                let responseData = {};
                
                try {
                    responseData = JSON.parse(textResponse);
                } catch(err) {
                    // Nếu lỗi do Spring Boot ném ra plain text
                    responseData = { success: false, message: textResponse }; 
                }

                if (response.ok && responseData.success) {
                    alert("Thành công: " + (responseData.message || "Đã nạp tiền."));
                    
                    // Lấy số dư mới trả về từ Backend đập thẳng lên UI
                    txtBalance.innerText = formatter.format(responseData.data) + " VNĐ";
                    txtBalance.classList.replace("text-danger", "text-success");
                    
                    // Reset input
                    inputTopup.value = "";
                } else {
                    alert("Thất bại: " + (responseData.message || "Đã xảy ra lỗi không xác định."));
                }
                
            } catch (error) {
                console.error("Lỗi gọi API Topup:", error);
                alert("Lỗi kết nối đến máy chủ. Vui lòng thử lại!");
            } finally {
                // Khôi phục nút
                btnTopup.innerHTML = originalBtnHtml;
                btnTopup.disabled = false;
            }
        });
    }
});