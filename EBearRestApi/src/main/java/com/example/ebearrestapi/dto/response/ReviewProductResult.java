package com.example.ebearrestapi.dto.response;

import com.example.ebearrestapi.entity.ReviewEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewProductResult {
    private Long reviewId;
    private Integer reviewPoint;
    private String reviewContent;
    private String reviewTitle;
    private String reviewer;
    private String imgUrl;
    private LocalDate regDttm;

    public static ReviewProductResult from(ReviewEntity reviewEntity) {
        return ReviewProductResult.builder()
                .reviewId(reviewEntity.getReviewNo())
                .reviewPoint(reviewEntity.getRating())
                .reviewTitle(reviewEntity.getBoard().getTitle())
                .reviewContent(reviewEntity.getBoard().getContent())
                .reviewer(reviewEntity.getUser().getUserName())
                .imgUrl(reviewEntity.getUser().getFile() == null ? null : reviewEntity.getUser().getFile().getFileLocation() + reviewEntity.getUser().getFile().getSaveFileName())
                .regDttm(reviewEntity.getRegDate().toLocalDate())
                .build();
    }
}
