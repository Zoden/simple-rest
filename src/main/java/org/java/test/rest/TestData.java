package org.java.test.rest;

import java.util.UUID;

/**
 * Created by Denis Zolotarev on 06.03.2017.
 */
public final class TestData {

    public static final String USER_ID_ALICE = "ffd93d33-2182-462c-b564-0360172d4571";
    public static final String USER_ID_BOB = "cee30ce1-27c5-4227-823f-67ce430c6d97";
    public static final String ACCOUNT_ID_ALICE_1 = "a7128a24-7c01-4cd4-873f-d6e742102572";
    public static final String ACCOUNT_ID_ALICE_2 = "b0295038-f3cc-4e34-9d09-731e0a7cfa79";
    public static final String ACCOUNT_ID_BOB_1 = "f6431e3c-bde2-4807-8850-fdee7c112ec5";

    public static String uuid() {
        return UUID.randomUUID().toString();
    }
}
