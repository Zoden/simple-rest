package org.java.test.rest.transfers.internal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private String id;

    private String userId;

    private BigDecimal rest;

    private Integer version;
}
