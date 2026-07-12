package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.InquiryEntity;
import com.example.ebearrestapi.entity.ProductEntity;
import com.example.ebearrestapi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
    List<InquiryEntity> findByProduct(ProductEntity product);

    // 관리자용
    List<InquiryEntity> findByParentIsNullAndBoard_DelYNOrderByInquiryNoDesc(String delYN);
    // 판매자용
    List<InquiryEntity> findByParentIsNullAndBoard_DelYNAndProduct_UserOrderByInquiryNoDesc(String delYN, UserEntity user);

    @Query("""
        select i
        from InquiryEntity i
        join fetch i.board b
        join fetch i.product p
        join fetch p.user pu
        where b.user.userNo = :userNo
          and i.parent is null
          and b.delYN = 'N'
        order by i.regDate desc
    """)
    List<InquiryEntity> findMyRootInquiries(
            @Param("userNo") Long userNo
    );

    @Query("""
        select i
        from InquiryEntity i
        join fetch i.board b
        where i.parent.inquiryNo in :parentInquiryNos
          and b.delYN = 'N'
    """)
    List<InquiryEntity> findRepliesByParentInquiryNos(
            @Param("parentInquiryNos") List<Long> parentInquiryNos
    );
}
