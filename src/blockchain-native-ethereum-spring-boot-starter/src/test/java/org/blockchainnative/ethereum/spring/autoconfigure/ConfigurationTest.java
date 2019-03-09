package org.blockchainnative.ethereum.spring.autoconfigure;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EthereumAutoConfiguration.class})
@EnableConfigurationProperties(EthereumProperties.class)
@TestPropertySource(locations="classpath:application.properties")
public class ConfigurationTest {

    @Autowired
    private EthereumProperties properties;

    @Test
    public void testPropertiesBasePath() {
        Assert.assertNotNull(properties);
        Assert.assertNotNull(properties.getWallet());
        Assert.assertNotNull(properties.getWallet().getNetworkId());
        Assert.assertNotNull(properties.getWallet().getPassword());
        Assert.assertNotNull(properties.getWallet().getPath());
        Assert.assertNotNull(properties.getClient());
        Assert.assertNotNull(properties.getClient().getAddress());
    }
}
