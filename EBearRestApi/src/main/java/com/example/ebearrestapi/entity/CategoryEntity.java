package com.example.ebearrestapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CATEGORY")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CategoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryNo;
    
    @Column(nullable = false, length = 100)
    private String categoryName;

    @Column(nullable = false, length = 100)
    private String categoryValue;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentCategoryNo")
    private CategoryEntity parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CategoryEntity> childrenList = new ArrayList<>();
    
    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<CouponEntity> couponList = new ArrayList<>();
    
    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<ProductEntity> productList = new ArrayList<>();

    public void addChildCategory(CategoryEntity child) {
        childrenList.add(child);
        child.setParent(this);
    }

    public void removeChildCategory(CategoryEntity child) {
        childrenList.remove(child);
        child.setParent(null);
    }

    public boolean isRootCategory() {
        return parent == null;
    }

    public int getDepth() {
        int depth = 0;
        CategoryEntity current = this.parent;
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    public List<Long> getAllDescendantIds() {
        List<Long> ids = new ArrayList<>();
        ids.add(this.categoryNo);

        if (this.childrenList != null) {
            for (CategoryEntity child : this.childrenList) {
                ids.addAll(child.getAllDescendantIds());
            }
        }
        return ids;
    }
}
