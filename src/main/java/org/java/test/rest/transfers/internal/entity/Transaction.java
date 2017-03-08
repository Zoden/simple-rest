package org.java.test.rest.transfers.internal.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
@Data
@Builder
public class Transaction {

    private String id;

    private Date transactionDate;

    private String userId;

    private String fromAccountId;

    private String toAccountId;

    private BigDecimal amount;
}
