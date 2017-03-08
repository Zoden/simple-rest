package org.java.test.rest.transfers.internal.dao.transation;

import org.java.test.rest.transfers.internal.entity.Transaction;

import java.sql.SQLException;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
public interface TransactionDao {

    void saveTransaction(Transaction transaction);
}
