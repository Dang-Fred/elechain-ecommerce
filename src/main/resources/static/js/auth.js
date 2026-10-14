function processLogin(event, redirectUrl) {
    event.preventDefault();
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const errorAlert = document.getElementById("login-error");
    const submitBtn = document.getElementById("submit-btn");

    errorAlert.classList.add("d-none");
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang xử lý...';

    fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email, matKhau: password })
    })
    .then(response => {
        if (!response.ok) throw new Error("Thông tin đăng nhập không chính xác hoặc tài khoản bị khóa.");
        return response.json();
    })
    .then(data => {
        if (data && data.token) {
            // Lưu Cookie bắt buộc (Path=/ cho phép toàn site đọc token)
            document.cookie = `jwt_token=${data.token}; max-age=86400; path=/; samesite=strict`;
            window.location.href = redirectUrl;
        }
    })
    .catch(error => {
        errorAlert.innerText = error.message;
        errorAlert.classList.remove("d-none");
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Đăng nhập';
    });
}