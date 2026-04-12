package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.ProductEntity;
import com.example.ebearrestapi.entity.ProductOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOptionRepository extends JpaRepository<ProductOptionEntity,Long> {
    List<ProductOptionEntity> findByProduct(ProductEntity product);
}
