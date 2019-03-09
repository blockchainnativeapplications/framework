package org.blockchainnative.fabric.spring.autoconfigure; /**
 * @author Matthias Veit
 */

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FabricAutoConfiguration.class})
@EnableConfigurationProperties(FabricProperties.class)
@TestPropertySource(locations="classpath:application.properties")
public class ConfigurationTest {

    @Autowired
    private FabricProperties properties;

    @Test
    public void testPropertiesBasePath() {
        Assert.assertNotNull(properties);
    }
}
