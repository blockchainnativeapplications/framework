package org.blockchainnative.fabric.spring.autoconfigure;

import org.blockchainnative.spring.autoconfigure.Constants;
import org.hyperledger.fabric.sdk.Enrollment;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Optional;
import java.util.Set;

/**
 * @author Matthias Veit
 */
@ConfigurationProperties(prefix = FabricProperties.CONTRACT_REGISTRY_PREFIX_FULL)
public class FabricProperties {
    public static final String FABRIC_PREFIX = "fabric";
    public static final String CONTRACT_REGISTRY_PREFIX_FULL = Constants.CONFIGURATION_PREFIX + "." + FABRIC_PREFIX;

    private String networkConfigFile;
    private String channel;

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNetworkConfigFile() {
        return networkConfigFile;
    }

    public void setNetworkConfigFile(String networkConfigFile) {
        this.networkConfigFile = networkConfigFile;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public static class User {
        private String name;
        private Set<String> roles;
        private String account;
        private String affiliation;
        private String mspId;
        private String certificateFile;
        private String privateKeyFile;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Set<String> getRoles() {
            return roles;
        }

        public void setRoles(Set<String> roles) {
            this.roles = roles;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getAffiliation() {
            return affiliation;
        }

        public void setAffiliation(String affiliation) {
            this.affiliation = affiliation;
        }

        public String getMspId() {
            return mspId;
        }

        public void setMspId(String mspId) {
            this.mspId = mspId;
        }

        public String getCertificateFile() {
            return certificateFile;
        }

        public void setCertificateFile(String certificateFile) {
            this.certificateFile = certificateFile;
        }

        public String getPrivateKeyFile() {
            return privateKeyFile;
        }

        public void setPrivateKeyFile(String privateKeyFile) {
            this.privateKeyFile = privateKeyFile;
        }
    }
}
