package com.springboot.ecommerce.services;


import com.springboot.ecommerce.entity.*;
import com.springboot.ecommerce.repository.CartRepository;
import com.springboot.ecommerce.repository.ProductRepository;
import com.springboot.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public Cart getCartByUserId(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
    }

    @Override
    public Cart addToCart(UUID userId, UUID productId, Integer quantity) {

        Cart cart = getCartByUserId(userId);
        Product product = getProduct(productId);

        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .build();

            cart.getItems().add(item);
        }

        return cartRepository.save(cart);
    }

    @Override
    public Cart updateQuantity(UUID userId, UUID productId, Integer quantity) {

        Cart cart = getCartByUserId(userId);

        cart.getItems().forEach(item -> {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
            }
        });

        return cartRepository.save(cart);
    }

    @Override
    public void removeFromCart(UUID userId, UUID productId) {

        Cart cart = getCartByUserId(userId);

        cart.getItems().removeIf(
                item -> item.getProduct().getId().equals(productId)
        );

        cartRepository.save(cart);
    }

    @Override
    public void clearCart(UUID userId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // 🔹 Helper Methods

    private Cart createCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = Cart.builder()
                .user(user)
                .build();

        return cartRepository.save(cart);
    }

    private Product getProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}