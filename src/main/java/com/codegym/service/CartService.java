package com.codegym.service;

import com.codegym.dto.CartDto;
import com.codegym.dto.CartItemDto;
import com.codegym.dto.ProductInCartDto;
import com.codegym.model.Cart;
import com.codegym.model.CartItemDb;
import com.codegym.model.Product;
import com.codegym.model.User;
import com.codegym.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
@Transactional
public class CartService implements ICartService {


    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private static final String SESSION_CART_KEY = "sessionCart";

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
                       UserRepository userRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // --- CÁC PHƯƠNG THỨC PUBLIC CHÍNH ---

    @Override
    public void addToCart(Long productId, int quantity, HttpSession session) {
        User user = getCurrentUser();
        if (user != null) {
            addToDbCart(productId, quantity, user);
        } else {
            addToSessionCart(productId, quantity, session);
        }
    }

    @Override
    public void updateCart(Long productId, int quantity, HttpSession session) {
        User user = getCurrentUser();
        if (user != null) {
            updateDbCart(productId, quantity, user);
        } else {
            updateSessionCart(productId, quantity, session);
        }
    }

    @Override
    public void removeFromCart(Long productId, HttpSession session) {
        User user = getCurrentUser();
        if (user != null) {
            removeFromDbCart(productId, user);
        } else {
            removeFromSessionCart(productId, session);
        }
    }

    @Override
    public void clearCart(HttpSession session) {
        User user = getCurrentUser();
        if (user != null) {
            clearDbCart(user);
        } else {
            clearSessionCart(session);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CartDto getCart(HttpSession session) {
        User user = getCurrentUser();
        if (user != null) {
            return convertCartToDto(getOrCreateDbCart(user));
        } else {
            Map<Long, CartItemDto> sessionItems = getSessionCartMap(session);
            return convertSessionCartToDto(sessionItems);
        }
    }

    @Override
    public int getTotalItems(HttpSession session) {
        User user = getCurrentUser();
        if (user != null) {
            return getOrCreateDbCart(user).getTotalItems();
        } else {
            return getSessionCartMap(session).values().stream().mapToInt(CartItemDto::getQuantity).sum();
        }
    }

    @Override
    public BigDecimal getTotalPrice(HttpSession session) {
        User user = getCurrentUser();
        if (user != null) {
            return getOrCreateDbCart(user).getTotalPrice();
        } else {
            return convertSessionCartToDto(getSessionCartMap(session)).getTotalPrice();
        }
    }

    @Override
    public void mergeSessionCartWithDbCart(HttpSession session) {
        User user = getCurrentUser();
        Map<Long, CartItemDto> sessionItems = getSessionCartMap(session);
        if (user == null || sessionItems.isEmpty()) {
            return;
        }
        for (CartItemDto sessionItem : sessionItems.values()) {
            addToDbCart(sessionItem.getProduct().getId(), sessionItem.getQuantity(), user);
        }
        clearSessionCart(session);
    }

    private Map<Long, CartItemDto> getSessionCartMap(HttpSession session) {
        Map<Long, CartItemDto> cart = (Map<Long, CartItemDto>) session.getAttribute(SESSION_CART_KEY);
        if (cart == null) {
            cart = new java.util.HashMap<>();
            session.setAttribute(SESSION_CART_KEY, cart);
        }
        return cart;
    }

    private void addToSessionCart(Long productId, int quantity, HttpSession session) {
        Map<Long, CartItemDto> cart = getSessionCartMap(session);
        Product product = findProductById(productId);
        CartItemDto existingItem = cart.get(productId);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItemDto newItem = new CartItemDto();
            newItem.setProduct(convertProductToDto(product));
            newItem.setQuantity(quantity);
            cart.put(productId, newItem);
        }
    }

    private void updateSessionCart(Long productId, int quantity, HttpSession session) {
        Map<Long, CartItemDto> cart = getSessionCartMap(session);
        if (cart.containsKey(productId)) {
            if (quantity > 0) {
                cart.get(productId).setQuantity(quantity);
            } else {
                cart.remove(productId);
            }
        }
    }

    private void removeFromSessionCart(Long productId, HttpSession session) {
        Map<Long, CartItemDto> cart = getSessionCartMap(session);
        cart.remove(productId);
    }

    private void clearSessionCart(HttpSession session) {
        session.removeAttribute(SESSION_CART_KEY);
    }

    private void updateSessionCartTotals(HttpSession session, Map<Long, CartItemDto> cart) {
        // Cần cập nhật lại thành tiền cho mỗi item và tổng tiền
        // Đây là điểm phức tạp của session cart, vì nó không tự tính toán
        // Bạn cần một CartDto đầy đủ trong session thay vì chỉ Map
    }


    // --- LOGIC CHO GIỎ HÀNG DATABASE (USER ĐÃ ĐĂNG NHẬP) ---

    private Cart getOrCreateDbCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private void addToDbCart(Long productId, int quantity, User user) {
        Cart cart = getOrCreateDbCart(user);
        Product product = findProductById(productId);
        Optional<CartItemDb> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItemDb item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItemDb newItem = new CartItemDb();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }
        cartRepository.save(cart);
    }

    private void updateDbCart(Long productId, int quantity, User user) {
        Cart cart = getOrCreateDbCart(user);
        Optional<CartItemDb> optionalItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (optionalItem.isPresent()) {
            CartItemDb itemToUpdate = optionalItem.get();
            if (quantity > 0) {
                itemToUpdate.setQuantity(quantity);
            } else {
                cart.getItems().remove(itemToUpdate);
                cartItemRepository.delete(itemToUpdate);
            }
            cartRepository.save(cart);
        }
    }

    private void removeFromDbCart(Long productId, User user) {
        Cart cart = getOrCreateDbCart(user);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cartRepository.save(cart);
    }

    private void clearDbCart(User user) {
        Cart cart = getOrCreateDbCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }


    // --- CÁC PHƯƠNG THỨC CONVERT VÀ HELPER KHÁC ---

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
    }

    private CartDto convertCartToDto(Cart cart) {
        CartDto cartDto = new CartDto();
        cartDto.setTotalItems(cart.getTotalItems());
        cartDto.setTotalPrice(cart.getTotalPrice());
        Map<Long, CartItemDto> dtoItems = cart.getItems().stream()
                .map(this::convertCartItemToDto)
                .collect(Collectors.toMap(itemDto -> itemDto.getProduct().getId(), Function.identity()));
        cartDto.setItems(dtoItems);
        return cartDto;
    }

    private CartDto convertSessionCartToDto(Map<Long, CartItemDto> sessionItems) {
        CartDto cartDto = new CartDto();
        cartDto.setItems(sessionItems);

        int totalItems = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for(CartItemDto item : sessionItems.values()) {
            BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setSubtotal(subtotal);
            totalItems += item.getQuantity();
            totalPrice = totalPrice.add(subtotal);
        }

        cartDto.setTotalItems(totalItems);
        cartDto.setTotalPrice(totalPrice);
        return cartDto;
    }

    private CartItemDto convertCartItemToDto(CartItemDb itemDb) {
        CartItemDto itemDto = new CartItemDto();
        itemDto.setProduct(convertProductToDto(itemDb.getProduct()));
        itemDto.setQuantity(itemDb.getQuantity());
        itemDto.setSubtotal(itemDb.getSubtotal());
        return itemDto;
    }

    private ProductInCartDto convertProductToDto(Product product) {
        ProductInCartDto productDto = new ProductInCartDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setPhotosImagePath(product.getPhotosImagePath());
        productDto.setPrice(product.getPrice());
        return productDto;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

}