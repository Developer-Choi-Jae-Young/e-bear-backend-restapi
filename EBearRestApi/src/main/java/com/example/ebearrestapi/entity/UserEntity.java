package com.example.ebearrestapi.entity;

import com.example.ebearrestapi.dto.request.SignupDto;
import com.example.ebearrestapi.etc.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "USERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userNo;
    
    @Column(nullable = false, unique = true, length = 50)
    private String userId;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, length = 100)
    private String userName;
    
    private String post;
    
    private String address;
    
    private String addressDetails;
    
    @Column(length = 20)
    private String mobile;
    
    @Column(length = 100)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<AlarmEntity> alarmList = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<BoardEntity> boardList = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<CartEntity> cartList = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<CouponEntity> couponList = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<CurrentViewProductEntity> currentViewProductList = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<MessageEntity> messageList = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<MessageRoomEntity> messageRoomList = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<PointEntity> pointList = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<ProductEntity> productList = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<ReviewEntity> reviewList = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<WishListEntity> wishList = new ArrayList<>();
    
    // 비즈니스 로직
    public String getFullAddress() {
        if (post == null || address == null) {
            return "";
        }
        return String.format("[%s] %s %s", post, address, 
                addressDetails != null ? addressDetails : "");
    }
    
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    public boolean isSeller() {
        return role == Role.SELLER;
    }
}


