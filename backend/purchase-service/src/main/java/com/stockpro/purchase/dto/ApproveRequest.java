package com.stockpro.purchase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApproveRequest {

    @JsonProperty("approvedBy")
    private Long approvedBy;
}