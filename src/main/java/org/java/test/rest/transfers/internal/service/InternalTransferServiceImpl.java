package org.java.test.rest.transfers.internal.service;

import org.java.test.rest.transfers.internal.dao.account.AccountDao;
import org.java.test.rest.transfers.internal.dao.account.AccountDaoImpl;
import org.java.test.rest.transfers.internal.dao.transation.TransactionDao;
import org.java.test.rest.transfers.internal.dao.transation.TransactionDaoImpl;
import org.java.test.rest.transfers.internal.dao.user.UserDao;
import org.java.test.rest.transfers.internal.dao.user.UserDaoImpl;
import org.java.test.rest.transfers.internal.entity.Account;
import org.java.test.rest.transfers.internal.entity.Transaction;
import org.java.test.rest.transfers.internal.entity.User;
import org.java.test.rest.transfers.internal.model.InternalTransferRequest;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Denis Zolotarev on 08.03.2017.
 */
public class InternalTransferServiceImpl implements InternalTransferService {

    private UserDao userDao;

    private AccountDao accountDao;

    private TransactionDao transactionDao;

    public InternalTransferServiceImpl() {
        userDao = new UserDaoImpl();
        accountDao = new AccountDaoImpl();
        transactionDao = new TransactionDaoImpl();
    }

    @Override
    public void internalTransfer(InternalTransferRequest request) {
        User user = getUser(request.getUserId());

        Account accountFrom = getAccountFrom(request.getFromAccountId(), request.getUserId());
        Account accountTo = getAccountTo(request.getToAccountId());

        debitAccount(accountFrom, request.getAmount());
        creditAccount(accountTo, request.getAmount());

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .transactionDate(Date.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant()))
                .fromAccountId(accountFrom.getId())
                .toAccountId(accountTo.getId())
                .amount(request.getAmount())
                .build();
        transactionDao.saveTransaction(transaction);
    }

    private User getUser(String userId) {
        User user = userDao.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("user not found");
        }
        return user;
    }

    private void debitAccount(Account account, BigDecimal amount) {
        // check account has enough money on it to complete transaction
        if (account.getRest().compareTo(amount) < 0) {
            throw new BadRequestException("rest is small");
        }
        account.setRest(account.getRest().subtract(amount));
        accountDao.updateAccount(account);
    }

    private void creditAccount(Account account, BigDecimal amount) {
        account.setRest(account.getRest().add(amount));
        accountDao.updateAccount(account);
    }

    private Account getAccountTo(String accountId) {
        Account accountTo = accountDao.getAccountById(accountId);
        // check account exists
        if (accountTo == null) {
            throw new NotFoundException("account to not found");
        }
        return accountTo;
    }

    private Account getAccountFrom(String accountId, String userId) {
        Account accountFrom = accountDao.getAccountById(accountId);
        // check account exists
        if (accountFrom == null) {
            throw new NotFoundException("account from not found");
        }
        // check account belongs to current user
        if (!accountFrom.getUserId().equals(userId)) {
            throw new ForbiddenException("permissions");
        }
        return accountFrom;
    }
}
