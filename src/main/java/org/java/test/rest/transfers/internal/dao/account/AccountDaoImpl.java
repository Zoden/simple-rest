package org.java.test.rest.transfers.internal.dao.account;

import org.apache.commons.dbutils.handlers.BeanHandler;
import org.java.test.rest.transfers.internal.dao.DaoUtils;
import org.java.test.rest.transfers.internal.dao.OptimisticLockException;
import org.java.test.rest.transfers.internal.entity.Account;

import java.sql.PreparedStatement;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
public class AccountDaoImpl implements AccountDao {

    @Override
    public Account getAccountById(String id) {
        DaoUtils.checkNotNull(id);

        return DaoUtils.INSTANCE.doInDb(connection -> {
            PreparedStatement statement = connection.prepareStatement("SELECT * from ACCOUNTS WHERE ID = ?");
            statement.setString(1, id);
            Account account = new BeanHandler<>(Account.class).handle(statement.executeQuery());
            statement.close();
            return account;
        });
    }

    @Override
    public void updateAccount(Account account) {
        DaoUtils.INSTANCE.doInDb(connection -> {
            // use optimistic lock here to prevent simultaneous updates
            PreparedStatement statement = connection.prepareStatement("UPDATE ACCOUNTS SET USERID = ?, REST = ?, VERSION = ? WHERE ID = ? AND VERSION = ?");
            statement.setString(1, account.getUserId());
            statement.setBigDecimal(2, account.getRest());
            statement.setInt(3, account.getVersion() + 1);
            statement.setString(4, account.getId());
            statement.setInt(5, account.getVersion());
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new OptimisticLockException();
            }
            statement.close();
            return account;
        });
    }
}
