package com.example.ebearrestapi.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossCancelDto {
    private String cancelReason;
}
