package com.luv2code.spring_boot_library.controller;

import com.luv2code.spring_boot_library.requestmodels.PaymentInfoRequest;
import com.luv2code.spring_boot_library.service.PaymentService;
import com.razorpay.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("https://localhost:3000")
@RestController
@RequestMapping("/api/payment/secure")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<String> createRazorpayOrder(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PaymentInfoRequest paymentInfoRequest) throws Exception {

        String userEmail = jwt.getClaim("email");
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing user email in token");
        }

        Order order = paymentService.createRazorpayOrder(paymentInfoRequest);
        String orderJson = order.toString();

        return new ResponseEntity<>(orderJson, HttpStatus.OK);
    }

    @PutMapping("/payment-complete")
    public ResponseEntity<String> razorpayPaymentComplete(
            @AuthenticationPrincipal Jwt jwt) throws Exception {

        String userEmail = jwt.getClaim("email");
        if (userEmail == null) {
            throw new Exception("User email is missing");
        }

        return paymentService.razorpayPayment(userEmail);
    }
}
