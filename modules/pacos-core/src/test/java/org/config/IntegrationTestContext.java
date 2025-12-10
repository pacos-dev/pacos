package org.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {CoreContextConfiguration.class})
public abstract class IntegrationTestContext extends TransactionalTestExecutionListener {

    static {
        File file = new File(System.getProperty("java.io.tmpdir") + "pacos\\");
        if (file.mkdir()) {
            file.deleteOnExit();
            System.setProperty("workingDir", file.getAbsolutePath());
        } else {
            try {
                File tempDir = Files.createTempDirectory("pacos-test").toFile();
                tempDir.deleteOnExit();
                System.setProperty("workingDir", tempDir.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
