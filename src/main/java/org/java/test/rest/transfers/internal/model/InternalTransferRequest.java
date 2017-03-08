package org.java.test.rest.transfers.internal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalTransferRequest {

    @NotNull
    private String userId;

    @NotNull
    private String fromAccountId;

    @NotNull
    private String toAccountId;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal amount;
}
