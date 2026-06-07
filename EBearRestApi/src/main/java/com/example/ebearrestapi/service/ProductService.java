package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.ProductDeleteDto;
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
    public List<ProductListResultDto> listProductAdmin(Pageable pageable, User user, String type, String kw) {
        UserEntity userEntity = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("User Not Found"));

        String searchType = (type == null || type.isEmpty()) ? "all" : type;
        String keyword = (kw == null) ? "" : kw;

        Page<ProductEntity> productList = productRepository.searchWithFilterAdmin(userEntity, searchType, keyword, pageable);
        return productList.map(data -> ProductListResultDto.builder()
                .productId(data.getProductNo())
                .productName(data.getProductName())
                .seller(data.getUser().getUserName())
                .regDttm(data.getRegDate().toLocalDate())
                .productStatus(data.getProductStatus().getName())
                .build()).getContent();
    }

    @Transactional
    public ProductCategoryResponseDto listProduct(Pageable pageable, String type, String kw, Long categoryId) {
        String searchType = (type == null || type.isEmpty()) ? "all" : type;
        String keyword = (kw == null) ? "" : kw;

        String categoryName = "전체";
        boolean hasCategory = false;
        List<Long> targetCategoryIds = new ArrayList<>();

        if (categoryId != null) {
            CategoryEntity category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                categoryName = category.getCategoryName();
                targetCategoryIds = category.getAllDescendantIds();
                hasCategory = true;
            }
        }

        if (targetCategoryIds.isEmpty()) {
            targetCategoryIds.add(0L);
        }

        Page<ProductEntity> productList = productRepository.searchWithFilter(hasCategory, targetCategoryIds, searchType, keyword, pageable);

        List<ProductItemDto> items = productList.map(data -> {
            String thumbnail = "";
            String priceDisplay = "0";

            if (data.getProductOptionList() != null && !data.getProductOptionList().isEmpty()) {
                int minPrice = data.getProductOptionList().stream()
                        .mapToInt(ProductOptionEntity::getProductOptionPrice)
                        .min()
                        .orElse(0);

                int maxPrice = data.getProductOptionList().stream()
                        .mapToInt(ProductOptionEntity::getProductOptionPrice)
                        .max()
                        .orElse(0);

                if (minPrice == maxPrice) {
                    priceDisplay = String.valueOf(minPrice);
                } else {
                    priceDisplay = minPrice + " ~ " + maxPrice;
                }
            }

            return ProductItemDto.builder()
                    .id(data.getProductNo())
                    .imageUrl(thumbnail)
                    .brand(data.getUser().getUserName())
                    .name(data.getProductName())
                    .price(priceDisplay)
                    .salePercentage(null)
                    .rating(null)
                    .reviewCount(data.getReviewList() != null ? data.getReviewList().size() : 0)
                    .build();
        }).getContent();

        return ProductCategoryResponseDto.builder()
                .category(categoryName)
                .products(items)
                .build();
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
                .productStatus(product.getProductStatus())
                .deliveryDays(product.getDeliveryDays())
                .deliveryPrice(product.getDeliveryPrice())
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

    @Transactional
    public void deleteProduct(ProductDeleteDto productDeleteDto) {
        Long[] productIds = productDeleteDto.getKey();

        for(Long productId : productIds){
            ProductEntity product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
            product.getBoard().setDelYN("Y");
        }
    }
}
