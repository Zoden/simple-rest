package org.java.test.rest.transfers.internal.dao.transation;

import org.java.test.rest.transfers.internal.dao.DaoUtils;
import org.java.test.rest.transfers.internal.entity.Transaction;

import java.sql.PreparedStatement;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
public class TransactionDaoImpl implements TransactionDao {


    @Override
    public void saveTransaction(Transaction transaction) {
        DaoUtils.INSTANCE.doInDb(connection -> {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO TRANSACTIONS VALUES (?, ?, ?, ?, ?, ?)");
            statement.setString(1, transaction.getId());
            statement.setString(2, transaction.getUserId());
            statement.setDate(3, new java.sql.Date(transaction.getTransactionDate().getTime()));
            statement.setString(4, transaction.getFromAccountId());
            statement.setString(5, transaction.getToAccountId());
            statement.setBigDecimal(6, transaction.getAmount());
            statement.execute();
            statement.close();
        });
    }
}
