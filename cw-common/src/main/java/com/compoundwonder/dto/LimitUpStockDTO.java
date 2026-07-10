package com.compoundwonder.dto;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class LimitUpStockDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // "")
    private Integer lbc;

    // "")
    private Double p;

    // "")
    private BigDecimal cje;

    // "")
    private Double hs;


}
