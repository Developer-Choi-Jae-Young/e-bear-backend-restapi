package com.example.ebearrestapi.entity;

import com.example.ebearrestapi.etc.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PRODUCT", indexes = {
    @Index(name = "idx_category", columnList = "categoryNo"),
    @Index(name = "idx_product_user", columnList = "userNo"),
    @Index(name = "idx_reg_date", columnList = "regDate")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productNo;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    private ProductStatus productStatus = ProductStatus.SALE;
    
    @Builder.Default
    private Integer deliveryPrice = 0;
    
    private Integer deliveryDays;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductOptionEntity> productOptionList = new ArrayList<>();
    
    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<CurrentViewProductEntity> currentViewProductList = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FileEntity> fileList = new ArrayList<>();
    
    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<InquiryEntity> inquiryList = new ArrayList<>();
    
    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<ReportEntity> reportList = new ArrayList<>();
    
    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<ReviewEntity> reviewList = new ArrayList<>();
    
    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<WishListEntity> wishList = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userNo", nullable = false)
    private UserEntity user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryNo")
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "boardNo", nullable = false)
    private BoardEntity board;

    public void addProductOption(ProductOptionEntity option) {
        productOptionList.add(option);
        option.setProduct(this);
    }
    
    public void addFile(FileEntity file) {
        fileList.add(file);
        file.setProduct(this);
    }
}
