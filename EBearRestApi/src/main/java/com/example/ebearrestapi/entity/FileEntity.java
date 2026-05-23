package com.example.ebearrestapi.entity;

import com.example.ebearrestapi.etc.FileType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FILE")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FileEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileNo;
    
    @Column(nullable = false, length = 500)
    private String fileLocation;
    
    @Column(nullable = false, length = 255)
    private String originalFileName;
    
    @Column(nullable = false, length = 255)
    private String saveFileName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productNo", nullable = false)
    private ProductEntity product;

    private FileType fileType;
}
