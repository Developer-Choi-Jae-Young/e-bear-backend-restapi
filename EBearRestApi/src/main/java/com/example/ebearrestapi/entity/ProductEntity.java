package com.example.ebearrestapi.entity;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productNo;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private Integer price;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal saleRatio;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer inventory = 0;
    
    @Builder.Default
    private Integer deliveryPrice = 0;
    
    private Integer deliveryDays;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductOptionEntity> productOptionList = new ArrayList<>();
    
    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<CartEntity> cartList = new ArrayList<>();
    
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

    // 비즈니스 로직
    public Integer getDiscountedPrice() {
        // null 체크 및 0인지 확인 (BigDecimal.ZERO와 비교)
        if (saleRatio == null || saleRatio.compareTo(BigDecimal.ZERO) == 0) {
            return price;
        }

        // 계산: price * (1 - saleRatio / 100)
        BigDecimal hundred = new BigDecimal("100");
        BigDecimal one = BigDecimal.ONE;

        // (1 - saleRatio / 100) 계산
        BigDecimal discountFactor = one.subtract(saleRatio.divide(hundred, 4, RoundingMode.HALF_UP));

        // 최종 가격 계산 후 int 변환
        BigDecimal discounted = new BigDecimal(price).multiply(discountFactor);

        return discounted.setScale(0, RoundingMode.HALF_UP).intValue();
    }
    
    public void addProductOption(ProductOptionEntity option) {
        productOptionList.add(option);
        option.setProduct(this);
    }
    
    public void addFile(FileEntity file) {
        fileList.add(file);
        file.setProduct(this);
    }
    
    public boolean isInStock() {
        return inventory > 0;
    }
    
    public void decreaseInventory(int quantity) {
        if (inventory < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.inventory -= quantity;
    }
    
    public void increaseInventory(int quantity) {
        this.inventory += quantity;
    }
}
