// ==========================================================
// main.js - PHIÊN BẢN HOÀN CHỈNH VÀ DUY NHẤT
// ==========================================================

document.addEventListener("DOMContentLoaded", function() {

    // === PHẦN CHUNG: Luôn chạy trên mọi trang ===
    const token = document.querySelector("meta[name='_csrf']")?.content;
    const header = document.querySelector("meta[name='_csrf_header']")?.content;

    // 1. Animation on Scroll
    const animateOnScrollObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const delay = entry.target.dataset.animationDelay || 0;
                setTimeout(() => {
                    entry.target.classList.add('is-visible');
                }, delay);
                animateOnScrollObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    document.querySelectorAll('.animate-on-scroll').forEach(element => {
        animateOnScrollObserver.observe(element);
    });

    // === PHẦN RIÊNG: Chỉ chạy trên trang tương ứng ===

    // 2. LOGIC CHO TRANG CHI TIẾT SẢN PHẨM
    const productDetailContent = document.querySelector('.product-gallery');
    if (productDetailContent) {
        // a. Image Gallery
        const mainImage = document.getElementById('mainProductImage');
        const thumbnails = document.querySelectorAll('.thumbnail-item');
        if (mainImage && thumbnails.length > 0) {
            thumbnails.forEach(thumb => {
                thumb.addEventListener('click', function() {
                    thumbnails.forEach(item => item.classList.remove('active'));
                    this.classList.add('active');
                    const newImageSrc = this.querySelector('img').src;
                    mainImage.src = newImageSrc;
                });
            });
        }

        // b. Quantity Selector (phiên bản không-AJAX cho trang detail)
        const quantitySelector = document.querySelector('.quantity-selector');
        if (quantitySelector) {
            const decreaseBtn = quantitySelector.querySelector('[data-action="decrease"]');
            const increaseBtn = quantitySelector.querySelector('[data-action="increase"]');
            const input = quantitySelector.querySelector('.quantity-input');

            decreaseBtn?.addEventListener('click', () => {
                let currentValue = parseInt(input.value, 10);
                if (currentValue > 1) {
                    input.value = currentValue - 1;
                }
            });

            increaseBtn?.addEventListener('click', () => {
                let currentValue = parseInt(input.value, 10);
                input.value = currentValue + 1;
            });
        }
    }

    // 3. LOGIC CHO TRANG GIỎ HÀNG
    const cartContent = document.getElementById('cart-content');
    if (cartContent) {
        const loader = document.getElementById('cart-loader');

        const showLoader = () => loader.style.display = 'flex';
        const hideLoader = () => loader.style.display = 'none';

        const updateCartView = (cartData) => {
            if (cartData.totalItems === 0) {
                const emptyCartHtml = `
                    <div class="cart-loader-overlay" id="cart-loader" style="display: none;"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>
                    <div id="empty-cart-message">
                        <div class="alert alert-info text-center p-5">
                            <h4>Giỏ hàng của bạn đang trống.</h4>
                            <p class="lead">Hãy khám phá các sản phẩm tuyệt vời của chúng tôi!</p>
                            <a href="/products" class="btn btn-primary mt-3">Tiếp tục mua sắm</a>
                        </div>
                    </div>`;
                cartContent.innerHTML = emptyCartHtml;
                return;
            }

            document.getElementById('summary-item-count').innerText = cartData.totalItems;
            document.getElementById('cart-header-item-count').innerText = cartData.totalItems;
            document.getElementById('summary-subtotal').innerText = cartData.formattedTotalPrice;
            document.getElementById('summary-total-price').innerText = cartData.formattedTotalPrice;

            const currentItemElements = document.querySelectorAll('.cart-item');
            const itemIdsFromServer = new Set(cartData.items.map(item => item.product.id));

            currentItemElements.forEach(el => {
                const productId = parseInt(el.dataset.productId, 10);
                if (!itemIdsFromServer.has(productId)) {
                    el.remove();
                }
            });

            cartData.items.forEach(item => {
                const subtotalEl = document.getElementById(`subtotal-${item.product.id}`);
                if (subtotalEl) {
                    subtotalEl.innerText = `${item.subtotal.toLocaleString('vi-VN')} ₫`;
                }
                const inputEl = document.querySelector(`#cart-item-${item.product.id} .quantity-input`);
                if(inputEl) {
                    inputEl.value = item.quantity;
                }
            });
        };

        const callCartApi = (url, body = null) => {
            showLoader();

            const headers = { 'X-Requested-With': 'XMLHttpRequest' };
            if (token && header) {
                headers[header] = token; // Thêm CSRF token
            }

            const fetchOptions = {
                method: 'POST', // Luôn dùng POST
                headers: headers
            };

            if (body) {
                headers['Content-Type'] = 'application/x-www-form-urlencoded';
                fetchOptions.body = body;
            }

            fetch(url, fetchOptions)
                .then(response => {
                    if (!response.ok) {
                        console.error('Lỗi từ server:', response.status, response.statusText);
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(data => { updateCartView(data); })
                .catch(error => {
                    console.error('Lỗi khi cập nhật giỏ hàng:', error);
                    alert('Đã có lỗi xảy ra, vui lòng tải lại trang.');
                })
                .finally(() => { hideLoader(); });
        };

        cartContent.addEventListener('click', (e) => {
            const target = e.target;
            const cartItem = target.closest('.cart-item');

            if (target.matches('.quantity-btn') && cartItem) {
                const productId = cartItem.dataset.productId;
                const input = cartItem.querySelector('.quantity-input');
                let quantity = parseInt(input.value, 10);

                if (target.dataset.action === 'increase') quantity++;
                else if (quantity > 1) quantity--;

                input.value = quantity;
                const body = `productId=${productId}&quantity=${quantity}`;
                callCartApi('/api/cart/update', body);
            }

            if (target.closest('.remove-item-btn') && cartItem) {
                if (confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
                    const productId = cartItem.dataset.productId;
                    // Gọi đến URL có chứa productId và không cần gửi body
                    callCartApi(`/api/cart/remove/${productId}`);
                }
            }

            if (target.id === 'clear-cart-btn') {
                if (confirm('Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng?')) {
                    callCartApi('/api/cart/clear');
                }
            }
        });
    }

});