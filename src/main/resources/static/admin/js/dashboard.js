document.addEventListener('DOMContentLoaded', function() {
    
    const fetchOptions = {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include' // Bắt buộc để gửi JWT Cookie
    };

    const vndFormatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });

    // Cấu hình mảng Promise chuẩn bị gọi API
    const apiCalls = [
        fetch('/api/admin/dashboard/summary', fetchOptions).then(handleResponse),
        fetch('/api/admin/dashboard/revenue/monthly?year=2026', fetchOptions).then(handleResponse),
        fetch('/api/admin/dashboard/products/top-selling', fetchOptions).then(handleResponse),
        fetch('/api/admin/orders?page=0&size=5', fetchOptions).then(handleResponse)
    ];

    // Xử lý API 3: Kiểm tra DOM xem Admin có được render block này không
    const branchChartElement = document.getElementById('branchRevenueChart');
    if (branchChartElement) {
        // Tạo chuỗi ngày ISO chuẩn: Bắt đầu từ 01/01 năm nay -> Hiện tại
        const currentYear = new Date().getFullYear();
        const startDate = new Date(currentYear, 0, 1).toISOString(); 
        const endDate = new Date().toISOString();
        
        const branchApiUrl = `/api/admin/dashboard/revenue/by-branch?startDate=${startDate}&endDate=${endDate}`;
        
        // Nhét thêm API 3 vào danh sách gọi đồng thời
        apiCalls.push(
            fetch(branchApiUrl, fetchOptions)
            .then(handleResponse)
            .then(data => {
                console.log("=== DỮ LIỆU API CHI NHÁNH (Để vẽ Pie Chart sau) ===", data);
                branchChartElement.innerHTML = `<span class="badge bg-success">Đã lấy dữ liệu (F12 Console)</span>`;
                return data; // Trả về để luồng Promise.all nắm giữ nếu cần
            })
        );
    }

    // Thực thi toàn bộ API
    Promise.all(apiCalls)
    .then(results => {
        // Tương ứng với thứ tự push vào mảng apiCalls
        const summaryData = results[0];
        const monthlyRevenueData = results[1];
        const topProductsData = results[2];
        const recentOrdersData = results[3]; // Trả về dạng { content: [...] }

        // 1. Render Summary (API 1)
        if (summaryData) {
            document.getElementById('stat-orders').textContent = summaryData.tongSoDonHang.toLocaleString('vi-VN');
            document.getElementById('stat-revenue').textContent = vndFormatter.format(summaryData.tongDoanhThu);
            document.getElementById('stat-products').textContent = summaryData.tongSanPhamDaBan.toLocaleString('vi-VN');
        }

        // 2. Render Chart Doanh thu (API 2)
        if (monthlyRevenueData) {
            renderRevenueChart(monthlyRevenueData);
        }

        // 3. Render Table Top Products (API 4)
        if (topProductsData) {
            renderTopProducts(topProductsData);
        }

        // 4. Render Table Recent Orders (API 5)
        if (recentOrdersData && recentOrdersData.content) {
            renderRecentOrders(recentOrdersData.content); // Truyền đúng mảng content
        }
		
		// Nếu apiCalls có 5 phần tử, tức là API chi nhánh đã được gọi
		if (results.length > 4 && results[4]) {
		    renderBranchChart(results[4]);
		}
    })
    .catch(error => console.error("Lỗi Dashboard API:", error));

    // ==============================================
    // CÁC HÀM TIỆN ÍCH & RENDER UI
    // ==============================================

    function handleResponse(response) {
        if (response.status === 401 || response.status === 403) {
            alert("Bạn không có quyền hoặc phiên đăng nhập đã hết hạn!");
            window.location.href = '/admin/login';
            throw new Error("Unauthorized");
        }
        return response.json();
    }

    // Render Table: Top 10 Sản phẩm
    function renderTopProducts(products) {
        const tbody = document.getElementById('top-products-tbody');
        tbody.innerHTML = ''; 

        if (products.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-muted">Chưa có dữ liệu</td></tr>'; return;
        }

        products.forEach(item => {
            const imgSrc = item.hinhAnh || '/admin/assets/images/placeholder.png';
            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td><img src="${imgSrc}" alt="SP" style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px;"></td>
                    <td class="text-start fw-medium text-truncate" style="max-width: 180px;" title="${item.tenSP}">${item.tenSP}</td>
                    <td class="fw-bold text-success">${item.tongSoLuongBan}</td>
                </tr>
            `);
        });
    }

    // Render Table: 5 Đơn hàng mới nhất
    function renderRecentOrders(orders) {
        const tbody = document.getElementById('recent-orders-tbody');
        tbody.innerHTML = '';

        if (orders.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Chưa có đơn hàng nào</td></tr>'; return;
        }

        orders.forEach(order => {
            // Định dạng ngày (Ví dụ: "15/10/2026")
            let dateStr = "N/A";
            if(order.ngayDat) {
                const d = new Date(order.ngayDat);
                dateStr = d.toLocaleDateString('vi-VN');
            }

            // Chọn màu badge cho trạng thái
            let badgeClass = "bg-secondary";
            if (order.trangThai === 'Hoàn thành') badgeClass = "bg-success";
            else if (order.trangThai === 'Đang xử lý') badgeClass = "bg-warning text-dark";
            else if (order.trangThai === 'Đã hủy') badgeClass = "bg-danger";

            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td class="fw-bold text-primary">#${order.maDH}</td>
                    <td>${order.tenKhachHang || 'Khách vãng lai'}</td>
                    <td class="fw-medium">${vndFormatter.format(order.tongTien)}</td>
                    <td><span class="badge ${badgeClass}">${order.trangThai}</span></td>
                    <td class="text-muted small">${dateStr}</td>
                </tr>
            `);
        });
    }

    // Render Chart: Doanh thu theo tháng
    function renderRevenueChart(monthlyData) {
        const chartElement = document.querySelector("#elechainRevenueChart");
        if (!chartElement) return;

        const labels = monthlyData.map(item => item.mocThoiGian);
        const dataValues = monthlyData.map(item => item.tongDoanhThu);

        const options = {
            series: [{ name: 'Doanh thu', data: dataValues }],
            chart: { type: 'area', height: 320, toolbar: { show: false }, fontFamily: 'inherit' },
            xaxis: { categories: labels, tooltip: { enabled: false } },
            yaxis: { labels: { formatter: val => vndFormatter.format(val) } },
            dataLabels: { enabled: false },
            stroke: { curve: 'smooth', width: 3 },
            colors: ['#0d6efd'],
            fill: { type: 'gradient', gradient: { shadeIntensity: 1, opacityFrom: 0.4, opacityTo: 0.05, stops: [0, 90, 100] } },
            markers: { size: 4, colors: ['#fff'], strokeColors: '#0d6efd', strokeWidth: 2 }
        };

        chartElement.innerHTML = "";
        const chart = new ApexCharts(chartElement, options);
        chart.render();
    }
	
	
	// Render Chart: Tỷ trọng doanh thu Chi nhánh (Biểu đồ Donut)
	    function renderBranchChart(branchData) {
	        const chartElement = document.getElementById('branchRevenueChart');
	        if (!chartElement) return;

	        // Nếu mảng rỗng
	        if (branchData.length === 0) {
	            chartElement.innerHTML = '<span class="text-muted">Chưa có doanh thu</span>';
	            return;
	        }

	        // Tách nhãn (Tên CN) và Dữ liệu (Doanh thu)
	        const labels = branchData.map(item => item.mocThoiGian);
	        const dataValues = branchData.map(item => item.tongDoanhThu);

	        const options = {
	            series: dataValues,
	            labels: labels,
	            chart: { 
	                type: 'donut', // Anh có thể đổi thành 'pie' nếu thích biểu đồ tròn đặc
	                height: 320, 
	                fontFamily: 'inherit' 
	            },
	            tooltip: {
	                y: {
	                    formatter: function (value) {
	                        return vndFormatter.format(value); // Format tiền VNĐ khi hover
	                    }
	                }
	            },
	            dataLabels: { enabled: false }, // Ẩn số trên mặt bánh cho đỡ rối
	            plotOptions: {
	                pie: {
	                    donut: { size: '65%' } // Độ dày của viền bánh
	                }
	            },
	            legend: { 
	                position: 'bottom', // Đưa chú thích xuống dưới
	                markers: { radius: 12 }
	            }
	        };

	        chartElement.innerHTML = ""; // Xóa chữ "Đang phân tích..."
	        const chart = new ApexCharts(chartElement, options);
	        chart.render();
	    }
});