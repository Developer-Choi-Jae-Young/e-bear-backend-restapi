package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.InquiryWriteDto;
import com.example.ebearrestapi.dto.response.InquiryUserListDto;
import com.example.ebearrestapi.dto.response.InquiryUserListResponseDto;
import com.example.ebearrestapi.entity.*;
import com.example.ebearrestapi.etc.FileType;
import com.example.ebearrestapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryUserService {
    private final InquiryRepository inquiryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final FileRepository fileRepository;

    @Transactional
    public void write(InquiryWriteDto inquiryWriteDto, User user) {
        UserEntity userEntity = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        ProductEntity productEntity = productRepository.findById(inquiryWriteDto.getProductNo()).orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        BoardEntity boardEntity = BoardEntity.builder().title(inquiryWriteDto.getTitle()).content(inquiryWriteDto.getContent()).user(userEntity).build();
        boardRepository.save(boardEntity);

        InquiryEntity inquiryEntity = InquiryEntity.builder().board(boardEntity).product(productEntity).build();
        inquiryRepository.save(inquiryEntity);
    }

    @Transactional(readOnly = true)
    public InquiryUserListResponseDto getMyInquiryList(User user) {
        UserEntity userEntity = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        List<InquiryEntity> inquiries = inquiryRepository.findMyRootInquiries(userEntity.getUserNo());

        if (inquiries.isEmpty()) {
            return new InquiryUserListResponseDto(List.of());
        }

        List<Long> inquiryNos = inquiries.stream().map(InquiryEntity::getInquiryNo).toList();
        List<Long> productNos = inquiries.stream().map(inquiry -> inquiry.getProduct().getProductNo()).distinct().toList();
        List<InquiryEntity> replies = inquiryRepository.findRepliesByParentInquiryNos(inquiryNos);
        List<FileEntity> productFiles = fileRepository.findByProduct_ProductNoInAndFileType(productNos, FileType.THUMBNAIL);
        Map<Long, InquiryEntity> replyMap = replies.stream().collect(Collectors.toMap(
                reply -> reply.getParent().getInquiryNo(),
                reply -> reply
                ));
        Map<Long, String> productImageUrlMap = createProductImageUrlMap(productFiles);
        List<InquiryUserListDto> result = inquiries.stream()
                .map(inquiry -> toInquiryUserListDto(
                        inquiry,
                        replyMap.get(inquiry.getInquiryNo()),
                        productImageUrlMap.get(inquiry.getProduct().getProductNo())
                ))
                .toList();

        return new InquiryUserListResponseDto(result);
    }

    private Map<Long, String> createProductImageUrlMap(
            List<FileEntity> productFiles
    ) {
        Map<Long, String> productImageUrlMap = new HashMap<>();

        for (FileEntity file : productFiles) {
            Long productNo = file.getProduct().getProductNo();

            productImageUrlMap.put(
                    productNo,
                    file.getFileLocation()
            );
        }

        return productImageUrlMap;
    }

    private InquiryUserListDto toInquiryUserListDto(
            InquiryEntity inquiry,
            InquiryEntity reply,
            String productImageUrl
    ) {
        BoardEntity inquiryBoard = inquiry.getBoard();
        ProductEntity product = inquiry.getProduct();

        boolean answered = reply != null;

        String answerContent = null;
        LocalDateTime answerRegDate = null;

        if (answered) {
            BoardEntity answerBoard = reply.getBoard();

            answerContent = answerBoard.getContent();
            answerRegDate = answerBoard.getRegDate();
        }

        return InquiryUserListDto.builder()
                .inquiryNo(inquiry.getInquiryNo())
                .productNo(product.getProductNo())
                .brandName(product.getUser().getUserName())
                .productName(product.getProductName())
                .productImageUrl(productImageUrl)
                .title(inquiryBoard.getTitle())
                .content(inquiryBoard.getContent())
                .regDate(inquiryBoard.getRegDate())
                .answered(answered)
                .answerContent(answerContent)
                .answerRegDate(answerRegDate)
                .build();
    }
}
