// File: static/js/common.js

// 1. Hàm moi JWT Token từ Cookie (Thay vì LocalStorage)
function getJwtToken() {
    let match = document.cookie.match(new RegExp('(^| )jwt_token=([^;]+)'));
    if (match) return match[2];
    return null;
}

// 2. Hàm bọc fetch dùng chung cho toàn hệ thống
async function fetchWithAuth(url, options = {}) {
    const token = getJwtToken();
    
    if (!options.headers) {
        options.headers = {};
    }
    
    // Kẹp token vào Header Authorization
    if (token) {
        options.headers['Authorization'] = 'Bearer ' + token;
    }
    
    // Định dạng JSON mặc định nếu có truyền body
    if (!options.headers['Content-Type'] && options.body && typeof options.body === 'string') {
        options.headers['Content-Type'] = 'application/json';
    }

    try {
        const response = await fetch(url, options);
        
        // Quản lý tập trung lỗi 401/403: Đá văng ra trang Login ngay lập tức
        if (response.status === 401 || response.status === 403) {
            alert("Phiên đăng nhập hết hạn hoặc bạn không có quyền. Vui lòng đăng nhập lại!");
            window.location.href = "/login";
            return Promise.reject("Unauthorized");
        }

        return response;
    } catch (error) {
        console.error("Lỗi Fetch API:", error);
        throw error;
    }
}