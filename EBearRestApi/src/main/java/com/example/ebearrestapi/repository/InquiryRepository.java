package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.InquiryEntity;
import com.example.ebearrestapi.entity.ProductEntity;
import com.example.ebearrestapi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
    List<InquiryEntity> findByProduct(ProductEntity product);

    // 관리자용
    List<InquiryEntity> findByParentIsNullAndBoard_DelYNOrderByInquiryNoDesc(String delYN);
    // 판매자용
    List<InquiryEntity> findByParentIsNullAndBoard_DelYNAndProduct_UserOrderByInquiryNoDesc(String delYN, UserEntity user);
}
