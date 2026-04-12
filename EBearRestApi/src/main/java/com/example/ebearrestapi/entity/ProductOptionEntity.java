package com.example.ebearrestapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PRODUCT_OPTION")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductOptionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productOptionNo;
    
    @Column(nullable = false, length = 100)
    private String productOptionName;

    private String productOptionValue;
    
    private Integer productOptionPrice;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer productOptionQuantity = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productNo", nullable = false)
    private ProductEntity product;

    public int decreaseProductOptionQuantity(int quantity) {
        if (this.productOptionQuantity < quantity) {
            throw new RuntimeException("재고가 부족합니다. (현재 재고: " + this.productOptionQuantity + ")");
        }
        this.productOptionQuantity -= quantity;
        return  this.productOptionQuantity;
    }

    public void increaseProductOptionQuantity(int quantity) {
        this.productOptionQuantity += quantity;
    }
}
