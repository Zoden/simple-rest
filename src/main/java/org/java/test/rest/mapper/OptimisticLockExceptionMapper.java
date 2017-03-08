package org.java.test.rest.mapper;

import org.java.test.rest.transfers.internal.dao.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by Denis Zolotarev on 08.03.2017.
 */
@Provider
@Priority(Priorities.USER)
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptimisticLockExceptionMapper.class);

    @Override
    public Response toResponse(OptimisticLockException exception) {
        LOGGER.error("", exception);
        // 409 - conflict
        return Response.status(409).build();
    }
}
