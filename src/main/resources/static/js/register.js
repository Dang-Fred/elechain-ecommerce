document.addEventListener("DOMContentLoaded", function () {
    const step1Form = document.getElementById("register-step1");
    const step2Form = document.getElementById("register-step2");
    
    if (!step1Form || !step2Form) return;

    const btnStep1 = document.getElementById("btn-step1");
    const btnStep2 = document.getElementById("btn-step2");
    const statusStep1 = document.getElementById("status-step1");
    const statusStep2 = document.getElementById("status-step2");
    const displayEmail = document.getElementById("display-email");
    const btnBack = document.getElementById("btn-back");

    let registeredEmail = "";

    // BƯỚC 1: GỬI THÔNG TIN LẤY MÃ OTP
    step1Form.addEventListener("submit", function (e) {
        e.preventDefault();
        const tenKH = document.getElementById("tenKH").value;
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        // Kiểm tra mật khẩu khớp
        if (password !== confirmPassword) {
            statusStep1.style.color = "#ff6b6b"; // Màu đỏ rùng rợn
            statusStep1.textContent = "Cảnh báo: Hai mật khẩu không khớp nhau!";
            return;
        }

        // Cập nhật UI lúc Loading
        statusStep1.style.color = "#a1e4e9";
        statusStep1.textContent = "Đang triệu hồi bồ câu đưa thư (Gửi OTP)...";
        btnStep1.style.opacity = "0.7";
        btnStep1.style.pointerEvents = "none";

        // Fetch API
        fetch("/api/auth/register/send-otp", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                tenKH: tenKH, 
                email: email, 
                matKhau: password, 
                xacNhanMatKhau: confirmPassword 
            })
        })
        .then(response => {
            if (!response.ok) throw new Error("Tài khoản đã tồn tại hoặc hệ thống đang bận!");
            return response.json();
        })
        .then(data => {
            // LƯU STATE VÀ CHUYỂN SANG FORM 2 (Không reload trang)
            registeredEmail = email;
            displayEmail.textContent = email;
            
            step1Form.style.display = "none";
            step2Form.style.display = "block";

            // Ẩn con cú đi để khỏi bị vướng vào layout OTP
            const owl = document.getElementById("email-owl");
            if (owl) owl.style.opacity = "0"; 
            
            // Khôi phục nút bấm Form 1
            btnStep1.style.opacity = "1";
            btnStep1.style.pointerEvents = "auto";
            statusStep1.textContent = "";
        })
        .catch(error => {
            statusStep1.style.color = "#ff6b6b";
            statusStep1.textContent = error.message;
            btnStep1.style.opacity = "1";
            btnStep1.style.pointerEvents = "auto";
        });
    });

    // BƯỚC 2: XÁC NHẬN MÃ OTP
    step2Form.addEventListener("submit", function (e) {
        e.preventDefault();
        const otp = document.getElementById("otp").value;

        statusStep2.style.color = "#a1e4e9";
        statusStep2.textContent = "Đang giải mã OTP...";
        btnStep2.style.opacity = "0.7";
        btnStep2.style.pointerEvents = "none";

        fetch("/api/auth/register/verify", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: registeredEmail, otp: otp })
        })
        .then(response => {
            if (!response.ok) throw new Error("Mã OTP bị sai hoặc đã hết phép thuật (hết hạn)!");
            return response.json(); // Hoặc response.text() tùy Backend
        })
        .then(data => {
            statusStep2.style.color = "#4ade80"; // Màu xanh lá
            statusStep2.textContent = "Tuyệt vời! Gia nhập thành công. Đang mở cổng...";
            
            // Redirect về Login sau 1.5s
            setTimeout(() => {
                window.location.href = "/login";
            }, 1500);
        })
        .catch(error => {
            statusStep2.style.color = "#ff6b6b";
            statusStep2.textContent = error.message;
            btnStep2.style.opacity = "1";
            btnStep2.style.pointerEvents = "auto";
        });
    });

    // NÚT QUAY LẠI (SỬA EMAIL)
    btnBack.addEventListener("click", function () {
        step2Form.style.display = "none";
        step1Form.style.display = "block";
        statusStep2.textContent = "";
        document.getElementById("otp").value = "";
    });
});