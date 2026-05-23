package com.example.ebearrestapi.dto.response;

import com.example.ebearrestapi.entity.InquiryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QnAProductResult {
    private Long qnaId;
    private String content;
    private String replyContent;
    private String seller;

    public static QnAProductResult from(InquiryEntity inquiryEntity) {
        return QnAProductResult.builder()
                .qnaId(inquiryEntity.getInquiryNo())
                .content(inquiryEntity.getBoard().getContent())
                .replyContent(inquiryEntity.getParent().getBoard().getContent())
                .build();
    }
}
