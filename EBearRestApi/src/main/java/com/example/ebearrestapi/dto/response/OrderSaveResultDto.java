package com.example.ebearrestapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderSaveResultDto {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long orderItemId;
}
