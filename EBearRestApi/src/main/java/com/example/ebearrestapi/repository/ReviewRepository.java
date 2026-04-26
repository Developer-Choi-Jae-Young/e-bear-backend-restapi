package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.ProductEntity;
import com.example.ebearrestapi.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer> {
    List<ReviewEntity> findByProduct(ProductEntity product);
}
