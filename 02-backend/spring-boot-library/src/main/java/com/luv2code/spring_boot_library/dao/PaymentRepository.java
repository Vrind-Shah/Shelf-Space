package com.luv2code.spring_boot_library.dao;

import com.luv2code.spring_boot_library.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@CrossOrigin("https://localhost:3000")
@RepositoryRestResource(path = "payment")
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserEmail(@Param("userEmail") String userEmail);

}
