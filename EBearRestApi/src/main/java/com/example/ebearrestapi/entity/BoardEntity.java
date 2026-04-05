package com.example.ebearrestapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BOARD")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BoardEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardNo;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer viewCnt = 0;

    @Builder.Default
    private String delYN = "N";

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "userNo", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "board")
    @Builder.Default
    private List<EventEntity> eventList = new ArrayList<>();
    
    @OneToMany(mappedBy = "board")
    @Builder.Default
    private List<InquiryEntity> inquiryList = new ArrayList<>();
    
    @OneToMany(mappedBy = "board")
    @Builder.Default
    private List<NotificationEntity> notificationList = new ArrayList<>();
    
    @OneToMany(mappedBy = "board")
    @Builder.Default
    private List<ReportEntity> reportList = new ArrayList<>();
    
    @OneToMany(mappedBy = "board")
    @Builder.Default
    private List<ReviewEntity> reviewList = new ArrayList<>();

    
    // 비즈니스 로직
    public void increaseViewCount() {
        this.viewCnt++;
    }
}
