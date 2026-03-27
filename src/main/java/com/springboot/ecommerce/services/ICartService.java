package com.springboot.ecommerce.services;

import com.springboot.ecommerce.entity.Cart;

import java.util.UUID;

public interface ICartService {

    Cart getCartByUserId(UUID userId);

    Cart addToCart(UUID userId, UUID productId, Integer quantity);

    Cart updateQuantity(UUID userId, UUID productId, Integer quantity);

    void removeFromCart(UUID userId, UUID productId);

    void clearCart(UUID userId);
}