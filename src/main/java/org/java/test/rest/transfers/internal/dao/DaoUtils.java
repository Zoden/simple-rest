package org.java.test.rest.transfers.internal.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
public enum DaoUtils {

    INSTANCE;

    public static final Logger LOGGER = LoggerFactory.getLogger(DaoUtils.class);

    // in memory hsql db
    private static final String DB_URL = "jdbc:hsqldb:mem:db";
    private static final String DBU_USER_NAME = "SA";
    private static final String DB_USER_PASS = "";
    private static final int DB_POOL_SIZE = 50;

    private BasicDataSource dataSource;

    // store current connection do db
    private ThreadLocal<Connection> txConnection = new ThreadLocal<>();

    DaoUtils() {
        dataSource = createDataSource(DB_URL, DBU_USER_NAME, DB_USER_PASS, DB_POOL_SIZE);
    }

    /**
     * do job in transaction
     *
     * @param handler job to do
     * @param <T>     result class
     * @return object of T
     */
    public <T> T doInTx(Supplier<T> handler) {
        T result;
        try {
            // start transaction
            DaoUtils.INSTANCE.begin();
            // do our job
            result = handler.get();
            // commit
            DaoUtils.INSTANCE.commit();
        } catch (Exception e) {
            LOGGER.error("", e);
            // rollback on exception
            DaoUtils.INSTANCE.rollback();
            throw e;
        } finally {
            // cleanup
            DaoUtils.INSTANCE.end();
        }
        return result;
    }

    public void begin() {
        Connection connection;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            txConnection.set(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void end() {
        Connection connection = txConnection.get();
        if (connection != null) {
            txConnection.remove();
            try {
                connection.close();
            } catch (SQLException closeException) {
                throw new RuntimeException(closeException);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public void commit() {
        Connection connection = txConnection.get();
        if (connection != null) {
            try {
                connection.commit();
            } catch (SQLException commitException) {
                throw new RuntimeException(commitException);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public void rollback() {
        Connection connection = txConnection.get();
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                throw new RuntimeException(rollbackException);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Some work in db without result
     *
     * @param call lambda do deal with given connection
     */
    public void doInDb(Consumer<Connection> call) {
        Connection connection = txConnection.get();
        if (connection != null) {
            try {
                call.accept(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Some work in db with result
     *
     * @param call lambda do deal with given connection
     * @param <R>  result class
     * @return work result
     */
    public <R> R doInDb(Function<Connection, R> call) {
        Connection connection = txConnection.get();
        if (connection != null) {
            try {
                return call.apply(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public static void checkNotNull(String s) {
        if (s == null || s.trim().length() == 0) {
            throw new IllegalArgumentException();
        }
    }

    private static BasicDataSource createDataSource(String url, String userName, String password, Integer poolSize) {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        basicDataSource.setDefaultAutoCommit(false);
        basicDataSource.setInitialSize(poolSize / 10);
        basicDataSource.setMaxActive(poolSize);
        basicDataSource.setMaxIdle(poolSize / 4);
        basicDataSource.setMinEvictableIdleTimeMillis(120000);
        basicDataSource.setTimeBetweenEvictionRunsMillis(60000);
        basicDataSource.setPoolPreparedStatements(true);
        basicDataSource.setTestOnBorrow(true);
        basicDataSource.setTestOnReturn(false);
        basicDataSource.setTestWhileIdle(false);

        basicDataSource.setUsername(userName);
        basicDataSource.setPassword(password);
        basicDataSource.setUrl(url);

        basicDataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        basicDataSource.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
        basicDataSource.setValidationQueryTimeout(5000);
        return basicDataSource;
    }

    // hack interfaces to use methods, that throws checked exceptions, in lambdas
    @FunctionalInterface
    public interface Consumer<T> {
        void accept(T t) throws SQLException;
    }

    @FunctionalInterface
    public interface Function<T, R> {
        R apply(T t) throws SQLException;
    }
}
