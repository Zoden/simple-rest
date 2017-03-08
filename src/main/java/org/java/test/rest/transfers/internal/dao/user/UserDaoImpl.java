package org.java.test.rest.transfers.internal.dao.user;

import org.apache.commons.dbutils.handlers.BeanHandler;
import org.java.test.rest.transfers.internal.dao.DaoUtils;
import org.java.test.rest.transfers.internal.entity.User;

import java.sql.PreparedStatement;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
public class UserDaoImpl implements UserDao {

    @Override
    public User getUserById(String id) {
        DaoUtils.checkNotNull(id);

        return DaoUtils.INSTANCE.doInDb(connection -> {
            PreparedStatement statement = connection.prepareStatement("SELECT * from USERS WHERE ID = ?");
            statement.setString(1, id);
            User user = new BeanHandler<>(User.class).handle(statement.executeQuery());
            statement.close();
            return user;
        });
    }
}
