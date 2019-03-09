package org.blockchainnative.fabric.test;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.util.Set;

/**
 * @author Matthias Veit
 */
public class TestUser implements User {
    private String name;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private Enrollment enrollment;
    private String mspId;

    public TestUser(String name, Set<String> roles, String account, String affiliation, Enrollment enrollment, String mspId) {
        this.name = name;
        this.roles = roles;
        this.account = account;
        this.affiliation = affiliation;
        this.enrollment = enrollment;
        this.mspId = mspId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getAffiliation() {
        return affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        return enrollment;
    }

    @Override
    public String getMspId() {
        return mspId;
    }
}
