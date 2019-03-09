package org.blockchainnative.quorum.spring.autoconfigure;

import org.blockchainnative.spring.autoconfigure.Constants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Matthias Veit
 */
@ConfigurationProperties(prefix = QuorumProperties.CONTRACT_REGISTRY_PREFIX_FULL)
public class QuorumProperties {
    public static final String QUORUM_PREFIX = "quorum";
    public static final String CONTRACT_REGISTRY_PREFIX_FULL = Constants.CONFIGURATION_PREFIX + "." + QUORUM_PREFIX;

    private String endpointAddress;
    private Long httpTimeout;

    private Client client;

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
        private String privateFrom;
        private List<String> privateFor;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPrivateFrom() {
            return privateFrom;
        }

        public void setPrivateFrom(String privateFrom) {
            this.privateFrom = privateFrom;
        }

        public List<String> getPrivateFor() {
            return privateFor;
        }

        public void setPrivateFor(List<String> privateFor) {
            this.privateFor = privateFor;
        }
    }
}
