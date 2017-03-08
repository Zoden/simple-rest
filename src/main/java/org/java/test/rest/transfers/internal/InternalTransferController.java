package org.java.test.rest.transfers.internal;

import org.java.test.rest.transfers.internal.dao.DaoUtils;
import org.java.test.rest.transfers.internal.model.InternalTransferRequest;
import org.java.test.rest.transfers.internal.service.InternalTransferService;
import org.java.test.rest.transfers.internal.service.InternalTransferServiceImpl;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
@Path("v1/transfers/internal")
public class InternalTransferController {

    private InternalTransferService internalTransferService;

    public InternalTransferController() {
        internalTransferService = new InternalTransferServiceImpl();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response internalTransfer(@Valid InternalTransferRequest request) {
        DaoUtils.INSTANCE.doInTx(() -> {
            internalTransferService.internalTransfer(request);
            return null;
        });
        return Response.ok().build();
    }
}
