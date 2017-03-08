package org.java.test.rest;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.java.test.rest.mapper.ConstraintViolationExceptionMapper;
import org.java.test.rest.mapper.OptimisticLockExceptionMapper;
import org.java.test.rest.transfers.internal.InternalTransferController;
import org.java.test.rest.transfers.internal.dao.DaoUtils;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Denis Zolotarev on 05.03.2017.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String SERVER_PORT = "server.port";
    private static final int DEFAULT_PORT = 8080;

    static {
        LOGGER.setLevel(Level.ALL);
    }

    public static void main(String[] args) throws InterruptedException, IOException, SQLException {
        try {
            // redirect jul to sl4j
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
            // start and init DB
            initDataBase();
            // start jersey
            startServer();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting application", e);
        }
    }

    private static void startServer() throws IOException {
        // create a new server listening at port 8080
        final HttpServer server = HttpServer.create(new InetSocketAddress(getBaseURI().getPort()), 0);
        server.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));

        // create application
        Application application = new ResourceConfig()
                // add json Jackson feature
                .register(JacksonFeature.class)
                // add full logging (redirected to sl4j)
                .register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY))
                // custom exception mapper
                .register(ConstraintViolationExceptionMapper.class)
                .register(OptimisticLockExceptionMapper.class)
                // add controller
                .register(InternalTransferController.class);

        // create a handler wrapping the JAX-RS application
        HttpHandler handler = RuntimeDelegate.getInstance().createEndpoint(application, HttpHandler.class);

        // map JAX-RS handler to the server root
        server.createContext(getBaseURI().getPath(), handler);

        // start the server
        server.start();
    }

    private static int getPort() {
        return Integer.getInteger(SERVER_PORT, DEFAULT_PORT);
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/").port(getPort()).build();
    }

    private static void initDataBase() {
        DaoUtils.INSTANCE.doInTx(() -> {
            createDataBaseStructure();
            fillUsers();
            fillAccounts();
            return null;
        });
    }

    private static void createDataBaseStructure() {
        DaoUtils.INSTANCE.doInDb(connection -> {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE USERS (ID VARCHAR(36) NOT NULL, NAME VARCHAR(256) NOT NULL, PRIMARY KEY (ID) )");
            statement.execute("CREATE TABLE ACCOUNTS (ID VARCHAR(36) NOT NULL, USERID VARCHAR(256) NOT NULL, REST NUMERIC, VERSION INTEGER, PRIMARY KEY (ID) )");
            statement.execute("CREATE TABLE TRANSACTIONS (ID VARCHAR(36) NOT NULL, USERID VARCHAR(256) NOT NULL, TRANSACTIONDATE TIMESTAMP NOT NULL, FROMACCOUNTID VARCHAR(256) NOT NULL, TOACCOUNTID VARCHAR(256) NOT NULL, AMOUNT NUMERIC, PRIMARY KEY (ID) )");
            statement.close();
        });
    }

    private static void fillUsers() {
        DaoUtils.INSTANCE.doInDb(connection -> {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO USERS VALUES ('" + TestData.USER_ID_ALICE + "', 'Alice')");
            statement.executeUpdate("INSERT INTO USERS VALUES ('" + TestData.USER_ID_BOB + "', 'Bob')");
            statement.close();
        });
    }

    private static void fillAccounts() {
        DaoUtils.INSTANCE.doInDb(connection -> {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO ACCOUNTS VALUES ('" + TestData.ACCOUNT_ID_ALICE_1 + "', '" + TestData.USER_ID_ALICE + "', 500000, 0)");
            statement.executeUpdate("INSERT INTO ACCOUNTS VALUES ('" + TestData.ACCOUNT_ID_ALICE_2 + "', '" + TestData.USER_ID_ALICE + "', 100, 0)");
            statement.executeUpdate("INSERT INTO ACCOUNTS VALUES ('" + TestData.ACCOUNT_ID_BOB_1 + "', '" + TestData.USER_ID_BOB + "', 1000, 0)");
            statement.close();
        });
    }

}