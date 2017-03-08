package org.java.test.rest.transfers.internal.dao.account;

import org.java.test.rest.transfers.internal.entity.Account;

import java.sql.SQLException;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
public interface AccountDao {

    Account getAccountById(String id);

    void updateAccount(Account account);
}
