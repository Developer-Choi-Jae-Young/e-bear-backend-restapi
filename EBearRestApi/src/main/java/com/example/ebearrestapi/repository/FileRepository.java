package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.FileEntity;
import com.example.ebearrestapi.entity.ProductEntity;
import com.example.ebearrestapi.etc.FileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByProduct(ProductEntity product);

    List<FileEntity> findByProduct_ProductNoInAndFileType(List<Long> productNos, FileType fileType);
}
