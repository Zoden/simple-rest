package org.java.test.rest.transfers.internal.service;

import org.java.test.rest.TestData;
import org.java.test.rest.transfers.internal.dao.account.AccountDao;
import org.java.test.rest.transfers.internal.dao.transation.TransactionDao;
import org.java.test.rest.transfers.internal.dao.user.UserDao;
import org.java.test.rest.transfers.internal.entity.Account;
import org.java.test.rest.transfers.internal.entity.Transaction;
import org.java.test.rest.transfers.internal.entity.User;
import org.java.test.rest.transfers.internal.model.InternalTransferRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Denis Zolotarev on 08.03.2017.
 */
public class InternalTransferServiceImplTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private UserDao userDao;

    @Mock
    private TransactionDao transactionDao;

    @InjectMocks
    private InternalTransferServiceImpl transferService = new InternalTransferServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.when(accountDao.getAccountById(stringArgumentCaptor.capture())).thenAnswer(invocation -> {
            String id = stringArgumentCaptor.getValue();
            if (TestData.ACCOUNT_ID_ALICE_1.equals(id)) {
                return new Account(TestData.ACCOUNT_ID_ALICE_1, TestData.USER_ID_ALICE, BigDecimal.valueOf(100), 0);
            } else if (TestData.ACCOUNT_ID_BOB_1.equals(id)) {
                return new Account(TestData.ACCOUNT_ID_BOB_1, TestData.USER_ID_BOB, BigDecimal.valueOf(100), 0);
            } else {
                return null;
            }
        });

        Mockito.when(userDao.getUserById(stringArgumentCaptor.capture())).thenAnswer(invocation -> {
            String id = stringArgumentCaptor.getValue();
            if (TestData.USER_ID_ALICE.equals(id)) {
                return new User(TestData.USER_ID_ALICE, "Alice");
            } else if (TestData.USER_ID_BOB.equals(id)) {
                return new User(TestData.USER_ID_BOB, "Bob");
            } else {
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        Mockito.reset(accountDao, userDao, transactionDao);
    }

    @Test
    public void testTransfer() {
        InternalTransferRequest request = buildRequest();

        transferService.internalTransfer(request);

        ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        Mockito.verify(transactionDao).saveTransaction(transactionArgumentCaptor.capture());

        Transaction transaction = transactionArgumentCaptor.getValue();
        Assert.assertNotNull(transaction);
        Assert.assertNotNull(transaction.getId());
        Assert.assertEquals(TestData.USER_ID_ALICE, transaction.getUserId());
        Assert.assertEquals(TestData.ACCOUNT_ID_ALICE_1, transaction.getFromAccountId());
        Assert.assertEquals(TestData.ACCOUNT_ID_BOB_1, transaction.getToAccountId());
        Assert.assertEquals(BigDecimal.TEN, transaction.getAmount());

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        Mockito.verify(accountDao, Mockito.times(2)).updateAccount(accountArgumentCaptor.capture());
        List<Account> accounts = accountArgumentCaptor.getAllValues();
        for (Account account : accounts) {
            if (TestData.ACCOUNT_ID_ALICE_1.equals(account.getId())) {
                Assert.assertEquals(new BigDecimal(90), account.getRest());
            } else if (TestData.ACCOUNT_ID_BOB_1.equals(account.getId())) {
                Assert.assertEquals(new BigDecimal(110), account.getRest());
            } else {
                Assert.fail("Unexpected account");
            }
        }
    }

    @Test(expected = NotFoundException.class)
    public void testUserNotExists() {
        InternalTransferRequest request = buildRequest();
        request.setUserId(TestData.uuid());
        transferService.internalTransfer(request);
    }

    @Test(expected = NotFoundException.class)
    public void testAccountFromNotExists() {
        InternalTransferRequest request = buildRequest();
        request.setFromAccountId(TestData.uuid());
        transferService.internalTransfer(request);
    }

    @Test(expected = ForbiddenException.class)
    public void testAccountSecurity() {
        InternalTransferRequest request = buildRequest();
        request.setUserId(TestData.USER_ID_BOB);
        transferService.internalTransfer(request);
    }

    @Test(expected = NotFoundException.class)
    public void testAccountToNotExists() {
        InternalTransferRequest request = buildRequest();
        request.setToAccountId(TestData.uuid());
        transferService.internalTransfer(request);
    }

    @Test(expected = BadRequestException.class)
    public void testNotEnoughMoney() {
        InternalTransferRequest request = buildRequest();
        request.setAmount(BigDecimal.valueOf(Double.MAX_VALUE));
        transferService.internalTransfer(request);
    }


    private InternalTransferRequest buildRequest() {
        return InternalTransferRequest.builder()
                .userId(TestData.USER_ID_ALICE)
                .fromAccountId(TestData.ACCOUNT_ID_ALICE_1)
                .toAccountId(TestData.ACCOUNT_ID_BOB_1)
                .amount(BigDecimal.TEN)
                .build();
    }

}
