package org.blockchainnative.ethereum.spring.autoconfigure;

import org.blockchainnative.spring.autoconfigure.Constants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Optional;

/**
 * @author Matthias Veit
 */
@ConfigurationProperties(prefix = EthereumProperties.CONTRACT_REGISTRY_PREFIX_FULL)
public class EthereumProperties {
    public static final String ETHEREUM_PREFIX = "ethereum";
    public static final String CONTRACT_REGISTRY_PREFIX_FULL = Constants.CONFIGURATION_PREFIX + "." + ETHEREUM_PREFIX;

    private String endpointAddress;
    private Long httpTimeout;

    private Wallet wallet;
    private Client client;

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getEndpointAddress() {
        return endpointAddress;
    }

    public void setEndpointAddress(String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }

    public Long getHttpTimeout() {
        return httpTimeout;
    }

    public void setHttpTimeout(Long httpTimeout) {
        this.httpTimeout = httpTimeout;
    }

    public static class Client {
        private String address;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class Wallet {
        private Byte networkId;
        private String path;
        private String password;

        public Byte getNetworkId() {
            return networkId;
        }

        public void setNetworkId(Byte networkId) {
            this.networkId = networkId;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
