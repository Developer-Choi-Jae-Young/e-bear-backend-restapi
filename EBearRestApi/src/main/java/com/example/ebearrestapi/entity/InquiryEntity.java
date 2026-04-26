package com.example.ebearrestapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "INQUIRY")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InquiryEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryNo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stateCodeNo")
    private StateCodeEntity stateCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardNo")
    private BoardEntity board;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productNo")
    private ProductEntity product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentInquiryNo")
    private InquiryEntity parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InquiryEntity> childrenList = new ArrayList<>();
    
    // 비즈니스 로직
    public void addReply(InquiryEntity reply) {
        childrenList.add(reply);
        reply.setParent(this);
    }
    
    public boolean isAnswered() {
        return !childrenList.isEmpty();
    }
}
