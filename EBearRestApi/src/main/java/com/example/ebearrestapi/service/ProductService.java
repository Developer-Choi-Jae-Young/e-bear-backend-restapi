package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.ProductOptionDto;
import com.example.ebearrestapi.dto.request.ProductSaveDto;
import com.example.ebearrestapi.dto.request.ProductUpdateDto;
import com.example.ebearrestapi.dto.response.*;
import com.example.ebearrestapi.entity.*;
import com.example.ebearrestapi.etc.FileType;
import com.example.ebearrestapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ReviewRepository reviewRepository;
    private final InquiryRepository inquiryRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public List<ProductListResultDto> listProduct(Pageable pageable) {
        Page<ProductEntity> productList = productRepository.findAll(pageable);
        return productList.map(data -> ProductListResultDto.builder()
                .productId(data.getProductNo())
                .productName(data.getProductName())
                .seller(data.getUser().getUserName())
                .regDttm(data.getRegDate().toLocalDate())
                .productStatus(data.getProductStatus().getName())
                .build()).getContent();
    }

    @Transactional
    public ProductDetailResult detailProduct(Long productId) {
        ProductEntity product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        List<ProductOptionEntity> productOptionList = productOptionRepository.findByProduct(product);
        List<FileEntity> files = fileRepository.findByProduct(product);
        List<InquiryEntity> inquiryList = inquiryRepository.findByProduct(product);
        List<ReviewEntity> reviewList = reviewRepository.findByProduct(product);

        BoardEntity board = product.getBoard();
        FileEntity thumbnailFile = files.stream().filter(data -> data.getFileType() == FileType.THUMBNAIL).findFirst().orElse(null);

        return ProductDetailResult.builder()
                .productId(product.getProductNo())
                .productName(product.getProductName())
                .thumbnail(thumbnailFile == null ? null : thumbnailFile.getFileLocation() + thumbnailFile.getSaveFileName())
                .content(board.getContent())
                .seller(product.getUser().getUserName())
                .sellerImg(product.getUser().getFile() == null ? null : product.getUser().getFile().getFileLocation() + product.getUser().getFile().getSaveFileName())
                .category(CategoryProductResult.from(product.getCategory()))
                .productOptions(productOptionList.stream().map(ProductOptionResult::from).toList())
                .reviews(reviewList.stream().map(ReviewProductResult::from).toList())
                .qnas(inquiryList.stream().map(QnAProductResult::from).toList()).build();
    }

    @Transactional
    public ProductSaveResultDto saveProduct(ProductSaveDto productSaveDto, @AuthenticationPrincipal User user) {
        UserEntity newUser = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        CategoryEntity category = categoryRepository.findById(productSaveDto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));

        BoardEntity board = BoardEntity.builder()
                .title(productSaveDto.getTitle())
                .content(productSaveDto.getContent())
                .user(newUser)
                .build();

        ProductEntity product = ProductEntity.builder()
                .productName(productSaveDto.getProductName())
                .description(productSaveDto.getDescription())
                .deliveryPrice(productSaveDto.getDeliveryPrice())
                .deliveryDays(productSaveDto.getDeliveryDays())
                .productStatus(productSaveDto.getProductStatus())
                .category(category)
                .board(board)
                .user(newUser)
                .build();

        ProductEntity newProduct = productRepository.save(product);

        List<ProductOptionEntity> newProductOption = new ArrayList<>();
        productSaveDto.getProductOptions().forEach(data -> {
            ProductOptionEntity productOption = ProductOptionEntity.builder()
                    .productOptionName(data.getProductOptionName())
                    .productOptionValue(data.getProductOptionValue())
                    .productOptionPrice(data.getProductPrice())
                    .productOptionQuantity(data.getQuantity())
                    .product(product)
                    .build();

            newProductOption.add(productOptionRepository.save(productOption));
        });

        return ProductSaveResultDto.builder()
                .productId(newProduct.getProductNo())
                .productName(newProduct.getProductName())
                .description(newProduct.getDescription())
                .deliveryPrice(newProduct.getDeliveryPrice())
                .deliveryDays(newProduct.getDeliveryDays())
                .productStatus(newProduct.getProductStatus())
                .categoryId(category.getCategoryNo())
                .title(newProduct.getBoard().getTitle())
                .content(newProduct.getBoard().getContent())
                .productOptions(newProductOption.stream().map(data -> ProductOptionDto.builder().productOptionId(data.getProductOptionNo()).productOptionName(data.getProductOptionName()).productOptionValue(data.getProductOptionValue()).productPrice(data.getProductOptionPrice()).quantity(data.getProductOptionQuantity()).build()).toList()).build();
    }

    @Transactional
    public ProductUpdateResultDto updateProduct(ProductUpdateDto productUpdateDto) {
        ProductEntity product = productRepository.findById(productUpdateDto.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));

        product.setProductName(productUpdateDto.getProductName());
        product.setDescription(productUpdateDto.getDescription());
        product.setDeliveryPrice(productUpdateDto.getDeliveryPrice());
        product.setDeliveryDays(productUpdateDto.getDeliveryDays());
        product.setProductStatus(productUpdateDto.getProductStatus());
        product.getCategory().setCategoryNo(productUpdateDto.getCategoryId());
        product.getBoard().setTitle(productUpdateDto.getTitle());
        product.getBoard().setContent(productUpdateDto.getContent());

        List<ProductOptionEntity> productoptionList = productOptionRepository.findByProduct(product);
        productoptionList.forEach(data -> {
           data.setProductOptionName(data.getProductOptionName());
           data.setProductOptionValue(data.getProductOptionValue());
           data.setProductOptionQuantity(data.getProductOptionQuantity());
           data.setProductOptionPrice(data.getProductOptionPrice());
        });

        return ProductUpdateResultDto.builder().productId(product.getProductNo())
                .productName(product.getProductName())
                .description(product.getDescription())
                .deliveryPrice(product.getDeliveryPrice())
                .deliveryDays(product.getDeliveryDays())
                .productStatus(product.getProductStatus())
                .categoryId(product.getCategory().getCategoryNo())
                .title(product.getBoard().getTitle())
                .content(product.getBoard().getContent())
                .productOptions(productoptionList.stream().map(data -> ProductOptionDto.builder().productOptionId(data.getProductOptionNo()).productOptionName(data.getProductOptionName()).productOptionValue(data.getProductOptionValue()).productPrice(data.getProductOptionPrice()).quantity(data.getProductOptionQuantity()).build()).toList()).build();
    }
}
