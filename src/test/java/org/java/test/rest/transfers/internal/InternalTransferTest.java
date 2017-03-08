package org.java.test.rest.transfers.internal;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.java.test.rest.Main;
import org.java.test.rest.TestData;
import org.java.test.rest.transfers.internal.model.InternalTransferRequest;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;


/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
public class InternalTransferTest {

    private static Invocation.Builder invocationBuilder;

    @BeforeClass
    public static void init() throws InterruptedException, SQLException, IOException {
        Main.main(new String[0]);
        Client client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);
        WebTarget webTarget = client.target("http://localhost:8080/v1");
        WebTarget webTarget1 = webTarget.path("transfers/internal");
        invocationBuilder = webTarget1.request(MediaType.APPLICATION_JSON);
    }

    @Test
    public void testSuccess() {
        Response response = invocationBuilder.post(Entity.entity(buildRequest(), MediaType.APPLICATION_JSON_TYPE));
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testAccountFromNotExists() {
        InternalTransferRequest request = buildRequest();
        request.setFromAccountId(TestData.uuid());
        Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testAccountSecurity() {
        InternalTransferRequest request = buildRequest();
        request.setUserId(TestData.USER_ID_BOB);
        Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testAccountToNotExists() {
        InternalTransferRequest request = buildRequest();
        request.setToAccountId(TestData.uuid());
        Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testNotEnoughMoney() {
        InternalTransferRequest request = buildRequest();
        request.setAmount(BigDecimal.valueOf(Double.MAX_VALUE));
        Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testUserNotExists() {
        InternalTransferRequest request = buildRequest();
        request.setUserId(TestData.uuid());
        Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testBadRequests() {
        InternalTransferRequest request = buildRequest();
        request.setUserId(null);
        Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(400, response.getStatus());

        request = buildRequest();
        request.setFromAccountId(null);
        response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(400, response.getStatus());

        request = buildRequest();
        request.setToAccountId(null);
        response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(400, response.getStatus());

        request = buildRequest();
        request.setAmount(null);
        response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(400, response.getStatus());

        request = buildRequest();
        request.setAmount(BigDecimal.ZERO);
        response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(400, response.getStatus());

        request = buildRequest();
        request.setAmount(BigDecimal.TEN.negate());
        response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(400, response.getStatus());
    }

    // just test to check optimistic locking
    @Test
    public void loadTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            executor.execute(() -> invocationBuilder.post(Entity.entity(buildRequest(), MediaType.APPLICATION_JSON_TYPE)));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    private static InternalTransferRequest buildRequest() {
        return InternalTransferRequest.builder()
                .userId(TestData.USER_ID_ALICE)
                .fromAccountId(TestData.ACCOUNT_ID_ALICE_1)
                .toAccountId(TestData.ACCOUNT_ID_BOB_1)
                .amount(BigDecimal.TEN)
                .build();
    }
}
