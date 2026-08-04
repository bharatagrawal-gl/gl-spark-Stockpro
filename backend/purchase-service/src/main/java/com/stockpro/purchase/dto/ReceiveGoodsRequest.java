package com.stockpro.purchase.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReceiveGoodsRequest {
    private Long performedBy;  // userId of who is receiving
}