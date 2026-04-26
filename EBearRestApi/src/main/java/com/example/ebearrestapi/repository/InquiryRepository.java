package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.InquiryEntity;
import com.example.ebearrestapi.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
    List<InquiryEntity> findByProduct(ProductEntity product);
}
