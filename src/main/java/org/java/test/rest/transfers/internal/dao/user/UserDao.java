package org.java.test.rest.transfers.internal.dao.user;

import org.java.test.rest.transfers.internal.entity.User;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
public interface UserDao {

    User getUserById(String id);
}
