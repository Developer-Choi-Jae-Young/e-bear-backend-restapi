package com.example.ebearrestapi.dto.request;

import com.example.ebearrestapi.etc.Validate;
import lombok.Data;

@Data
public class AlarmDto {
    private Long alarmNo;
    private String alarmContent;
    private Validate validate = Validate.UNCHECK;
    private Long userNo;
    private Long stateCodeNo;
}
