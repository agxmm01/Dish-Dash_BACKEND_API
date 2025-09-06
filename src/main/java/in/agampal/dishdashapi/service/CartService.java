package in.agampal.dishdashapi.service;

import in.agampal.dishdashapi.io.CartRequest;
import in.agampal.dishdashapi.io.CartResponse;

public interface CartService {

    CartResponse addToCart(CartRequest request);

    CartResponse getCart();

    void clearCart();

    CartResponse removeFromCart(CartRequest cartRequest);
}
