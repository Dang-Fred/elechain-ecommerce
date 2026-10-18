document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('elechain-login-form');

    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            // Ngăn chặn form submit mặc định (load lại trang)
            e.preventDefault();

            // Lấy giá trị từ các ô input theo ID thực tế trong HTML
            const email = document.getElementById('email').value;
            const matKhau = document.getElementById('password').value;

            // Gọi API đăng nhập
            fetch('/api/admin/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    email: email,
                    matKhau: matKhau // Key này khớp với DTO Backend của bạn
                })
            })
            .then(response => response.json())
            .then(data => {
                if (data.success === true) {
                    // Lưu JWT vào Cookie (đặt path=/ để toàn bộ trang admin đều đọc được)
                    document.cookie = `jwt_token=${data.data.token}; path=/; max-age=86400;`; // Hết hạn sau 1 ngày
                    
                    // Chuyển hướng vào trang Dashboard
                    window.location.href = '/admin/dashboard';
                } else {
                    // Hiển thị thông báo lỗi từ server
                    alert(data.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin!');
                    
                    // (Tùy chọn) Cập nhật thẻ <p id="status"> trong HTML của bạn
                    const statusText = document.getElementById('status');
                    if(statusText) {
                        statusText.textContent = data.message || 'Sai email hoặc mật khẩu!';
                        statusText.style.color = '#c94c0a'; // Màu cam hợp với theme Halloween của bạn
                    }
                }
            })
            .catch(error => {
                console.error('Lỗi khi gọi API đăng nhập:', error);
                alert('Không thể kết nối đến máy chủ. Vui lòng thử lại sau!');
            });
        });
    }
});