package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity,Long> {

    Page<CategoryEntity> findAllByChildrenListIsEmpty(Pageable pageable);

    List<CategoryEntity> findAllByParentIsNull();
}
