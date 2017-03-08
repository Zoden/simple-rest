package org.java.test.rest.transfers.internal.service;

import org.java.test.rest.transfers.internal.model.InternalTransferRequest;

/**
 * Created by Denis Zolotarev on 08.03.2017.
 */
public interface InternalTransferService {

    void internalTransfer(InternalTransferRequest request);
}
