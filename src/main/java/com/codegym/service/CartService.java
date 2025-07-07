package com.codegym.service;

import com.codegym.dto.CartDto;
import com.codegym.dto.CartItemDto;
import com.codegym.model.Cart;
import com.codegym.model.CartItemDb;
import com.codegym.model.Product;
import com.codegym.model.User;
import com.codegym.repository.CartItemRepository;
import com.codegym.repository.CartRepository;
import com.codegym.repository.ProductRepository;
import com.codegym.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class CartService implements ICartService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);
    private static final String SESSION_CART_NAME = "sessionCart";

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
                       UserRepository userRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // === PHƯƠNG THỨC CÔNG KHAI (IMPLEMENT TỪ INTERFACE) ===

    @Override
    public void addToCart(Long productId, int quantity, HttpSession session) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            addToDbCart(productId, quantity, currentUser);
        } else {
            addToSessionCart(productId, quantity, session);
        }
    }

    @Override
    public void updateCart(Long productId, int quantity, HttpSession session) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            updateDbCart(productId, quantity, currentUser);
        } else {
            updateSessionCart(productId, quantity, session);
        }
    }

    @Override
    public void removeFromCart(Long productId, HttpSession session) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            removeFromDbCart(productId, currentUser);
        } else {
            removeFromSessionCart(productId, session);
        }
    }

    @Override
    public void clearCart(HttpSession session) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            clearDbCart(currentUser);
        } else {
            clearSessionCart(session);
        }
    }

    @Override
    public CartDto getCart(HttpSession session) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return getDbCartDto(currentUser);
        } else {
            return getSessionCart(session);
        }
    }

    @Override
    public int getTotalItems(HttpSession session) {
        return getCart(session).getTotalItems();
    }

    @Override
    public BigDecimal getTotalPrice(HttpSession session) {
        return getCart(session).getTotalPrice();
    }

    @Override
    @Transactional
    public void mergeSessionCartWithDbCart(HttpSession session) {
        User currentUser = getCurrentUser();
        CartDto sessionCart = getSessionCart(session);

        if (currentUser == null || sessionCart.getItems().isEmpty()) {
            return; // Không có user hoặc không có gì trong giỏ hàng session để hợp nhất
        }

        LOGGER.info("Merging session cart for user: {}", currentUser.getUsername());
        for (CartItemDto sessionItem : sessionCart.getItems().values()) {
            // Dùng lại logic addToDbCart để thêm từng sản phẩm
            addToDbCart(sessionItem.getProduct().getId(), sessionItem.getQuantity(), currentUser);
        }

        // Xóa giỏ hàng khỏi session sau khi đã hợp nhất
        session.removeAttribute(SESSION_CART_NAME);
        LOGGER.info("Session cart merged and removed.");
    }

    // === CÁC PHƯƠNG THỨC LOGIC RIÊNG (PRIVATE) CHO TỪNG LOẠI GIỎ HÀNG ===

    // --- Logic cho Giỏ hàng trong Session (Khách) ---

    private CartDto getSessionCart(HttpSession session) {
        CartDto cart = (CartDto) session.getAttribute(SESSION_CART_NAME);
        if (cart == null) {
            cart = new CartDto();
            session.setAttribute(SESSION_CART_NAME, cart);
        }
        return cart;
    }

    private void addToSessionCart(Long productId, int quantity, HttpSession session) {
        CartDto cart = getSessionCart(session);
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));

        CartItemDto existingItem = cart.getItems().get(productId);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            cart.getItems().put(productId, new CartItemDto(product, quantity));
        }
    }

    private void updateSessionCart(Long productId, int quantity, HttpSession session) {
        CartDto cart = getSessionCart(session);
        CartItemDto item = cart.getItems().get(productId);
        if (item != null) {
            if (quantity > 0) {
                item.setQuantity(quantity);
            } else {
                cart.getItems().remove(productId);
            }
        }
    }

    private void removeFromSessionCart(Long productId, HttpSession session) {
        CartDto cart = getSessionCart(session);
        cart.getItems().remove(productId);
    }

    private void clearSessionCart(HttpSession session) {
        session.removeAttribute(SESSION_CART_NAME);
    }

    // --- Logic cho Giỏ hàng trong CSDL (Người dùng đã đăng nhập) ---

    private void addToDbCart(Long productId, int quantity, User user) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Optional<CartItemDb> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItemDb item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItemDb newItem = new CartItemDb();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
            cartRepository.save(cart);
        }
    }

    private void updateDbCart(Long productId, int quantity, User user) {
        Cart cart = getOrCreateCart(user);
        Optional<CartItemDb> optionalItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (optionalItem.isPresent()) {
            CartItemDb itemToUpdate = optionalItem.get();
            if (quantity > 0) {
                itemToUpdate.setQuantity(quantity);
                cartItemRepository.save(itemToUpdate);
            } else {
                cart.getItems().remove(itemToUpdate);
                cartRepository.save(cart);
            }
        }
    }

    private void removeFromDbCart(Long productId, User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cartRepository.save(cart);
    }

    private void clearDbCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartDto getDbCartDto(User user) {
        Cart cart = getOrCreateCart(user);
        CartDto cartDto = new CartDto();
        for (CartItemDb itemDb : cart.getItems()) {
            CartItemDto itemDto = new CartItemDto(itemDb.getProduct(), itemDb.getQuantity());
            cartDto.getItems().put(itemDb.getProduct().getId(), itemDto);
        }
        return cartDto;
    }

    // --- Phương thức tiện ích ---

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}