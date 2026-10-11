// File: OrderService.java
package hcmute.service;

import hcmute.dto.request.CheckoutRequestDTO;
import hcmute.dto.response.CheckoutResponseDTO;

public interface OrderService {
    CheckoutResponseDTO checkout(String email, CheckoutRequestDTO request);
}