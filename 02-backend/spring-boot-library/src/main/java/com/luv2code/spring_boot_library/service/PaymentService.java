package com.luv2code.spring_boot_library.service;

import com.luv2code.spring_boot_library.dao.PaymentRepository;
import com.luv2code.spring_boot_library.entity.Payment;
import com.luv2code.spring_boot_library.requestmodels.PaymentInfoRequest;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;

    @Autowired
    public PaymentService(
            PaymentRepository paymentRepository,
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret
    ) throws Exception {
        this.paymentRepository = paymentRepository;
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    public Order createRazorpayOrder(PaymentInfoRequest paymentInfoRequest) throws Exception {

        JSONObject options = new JSONObject();
        options.put("amount", paymentInfoRequest.getAmount());   // amount in paise
        options.put("currency", paymentInfoRequest.getCurrency());
        options.put("payment_capture", 1);  // auto capture

        return razorpayClient.orders.create(options);
    }

    public ResponseEntity<String> razorpayPayment(String userEmail) throws Exception {
        Payment payment = paymentRepository.findByUserEmail(userEmail);

        if (payment == null) {
            throw new Exception("Payment information is missing");
        }

        payment.setAmount(0.00);
        paymentRepository.save(payment);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
