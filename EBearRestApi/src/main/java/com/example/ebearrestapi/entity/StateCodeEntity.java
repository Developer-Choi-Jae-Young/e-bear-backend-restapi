package com.example.ebearrestapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "STATE_CODE")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StateCodeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stateCodeNo;
    
    @Column(nullable = false, unique = true, length = 50)
    private String stateName;
    
    @OneToMany(mappedBy = "stateCode")
    @Builder.Default
    private List<AlarmEntity> alarmList = new ArrayList<>();
    
    @OneToMany(mappedBy = "stateCode")
    @Builder.Default
    private List<InquiryEntity> inquiryList = new ArrayList<>();
    
    @OneToMany(mappedBy = "stateCode")
    @Builder.Default
    private List<PointEntity> pointList = new ArrayList<>();
    
    @OneToMany(mappedBy = "stateCode")
    @Builder.Default
    private List<ReportEntity> reportList = new ArrayList<>();
}
