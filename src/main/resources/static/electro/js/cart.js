document.addEventListener("DOMContentLoaded", function () {
    
    // 1. Hàm format tiền tệ VNĐ để in ra HTML
    function formatCurrency(number) {
        return new Intl.NumberFormat('vi-VN').format(number) + " VNĐ";
    }

    // 2. Hàm tính toán và cập nhật lại TỔNG TIỀN của toàn bộ giỏ hàng
    function updateCartTotal() {
        let total = 0;
        // Quét tất cả các ô input số lượng và nhân với giá lấy từ nút
        document.querySelectorAll('.qty-input').forEach(function(input) {
            let qty = parseInt(input.value);
            // Lấy giá từ nút btn-plus nằm ngay cạnh
            let price = parseFloat(input.parentElement.querySelector('.btn-plus').getAttribute('data-price'));
            total += (qty * price);
        });

        // Đổ ra màn hình
        const subtotalEl = document.getElementById('cart-subtotal');
        const totalEl = document.getElementById('cart-total');
        
        if (subtotalEl && totalEl) {
            subtotalEl.innerText = formatCurrency(total);
            totalEl.innerText = formatCurrency(total);
        }
    }

    // Gọi lần đầu khi vừa vào trang
    updateCartTotal();

    // 3. Hàm gọi API Cập nhật số lượng (PUT)
    async function updateQuantityAPI(maSP, newQty) {
        try {
            const response = await fetchWithAuth('/api/cart/update', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    maSP: maSP,
                    soLuong: newQty
                })
            });
            return response.ok; // Trả về true nếu HTTP 200
        } catch (error) {
            console.error("Lỗi gọi API Update Cart:", error);
            return false;
        }
    }

    // 4. Bắt sự kiện Click vào Nút CỘNG
    document.querySelectorAll('.btn-plus').forEach(btn => {
        btn.addEventListener('click', async function (e) {
            e.preventDefault();
            let maSP = this.getAttribute('data-masp');
            let price = parseFloat(this.getAttribute('data-price'));
            let inputEl = this.parentElement.parentElement.querySelector('.qty-input');
            
            let currentQty = parseInt(inputEl.value);
            let maxQty = parseInt(inputEl.getAttribute('data-max')); // Tồn kho

            if (currentQty < maxQty) {
                let newQty = currentQty + 1;
                // Gọi API                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
                let isSuccess = await updateQuantityAPI(maSP, newQty);
                
                if (isSuccess) {
                    // Update HTML số lượng
                    inputEl.value = newQty;
                    // Update HTML Thành tiền dòng
                    document.getElementById('row-total-' + maSP).innerText = formatCurrency(newQty * price);
                    // Update HTML Tổng tiền giỏ
                    updateCartTotal();
                } else {
                    alert("Có lỗi xảy ra khi cập nhật giỏ hàng.");
                }
            } else {
                alert("Số lượng yêu cầu vượt quá tồn kho hiện tại!");
            }
        });
    });

    // 5. Bắt sự kiện Click vào Nút TRỪ
    document.querySelectorAll('.btn-minus').forEach(btn => {
        btn.addEventListener('click', async function (e) {
            e.preventDefault();
            let maSP = this.getAttribute('data-masp');
            let price = parseFloat(this.getAttribute('data-price'));
            let inputEl = this.parentElement.parentElement.querySelector('.qty-input');
            
            let currentQty = parseInt(inputEl.value);

            // Giới hạn không cho trừ dưới 1
            if (currentQty > 1) {
                let newQty = currentQty - 1;
                // Gọi API
                let isSuccess = await updateQuantityAPI(maSP, newQty);
                
                if (isSuccess) {
                    inputEl.value = newQty;
                    document.getElementById('row-total-' + maSP).innerText = formatCurrency(newQty * price);
                    updateCartTotal();
                } else {
                    alert("Có lỗi xảy ra khi cập nhật giỏ hàng.");
                }
            }
        });
    });

    // 6. Bắt sự kiện Click vào Nút XÓA (DELETE)
    document.querySelectorAll('.btn-delete').forEach(btn => {
        btn.addEventListener('click', async function (e) {
            e.preventDefault();
            
            if (confirm("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?")) {
                let maSP = this.getAttribute('data-masp');
                
                try {
                    const response = await fetchWithAuth('/api/cart/remove/' + maSP, {
                        method: 'DELETE'
                    });
                    
                    if (response.ok) {
                        // Xóa nguyên thẻ <tr> chứa dòng sản phẩm đó khỏi DOM
                        const trElement = document.getElementById('cart-row-' + maSP);
                        if (trElement) {
                            trElement.remove();
                        }
                        
                        // Tính lại tổng tiền
                        updateCartTotal();
                        
                        // Nếu giỏ hàng trống trơn (không còn input số lượng nào), reload lại trang để hiện thông báo trống
                        if (document.querySelectorAll('.qty-input').length === 0) {
                            window.location.reload();
                        }
                    } else {
                        alert("Lỗi khi xóa sản phẩm.");
                    }
                } catch (error) {
                    console.error("Lỗi gọi API Delete Cart:", error);
                }
            }
        });
    });

});