package me.leoko.advancedban;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Created by Leo on 07.08.2017.
 */

public class DatabaseTest {

    @TempDir
    public static File dataFolder;

    @BeforeAll
    public static void setupUniversal(){
        Universal.get().setup(new TestMethods(dataFolder));
    }

    @Test
    public void shouldAutomaticallyDetectDatabaseType(){
        // DatabaseManagement behaviour changed in 2.1.9
//        assertFalse("MySQL should not be failed as it should not even try establishing any connection", DatabaseManager.get().isFailedMySQL());
//        DatabaseManager.get().shutdown();
//        DatabaseManager.get().setup(true);
//        assertTrue("MySQL should be failed as the connection can not succeed", DatabaseManager.get().isFailedMySQL());
    }

    @AfterAll
    public static void shutdownUniversal(){
        Universal.get().shutdown();
    }
}
