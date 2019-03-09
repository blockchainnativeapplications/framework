package org.blockchainnative.fabric;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Set;

/**
 * @author Matthias Veit
 */
public class FabricUser implements User {
    private String name;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private Enrollment enrollment;
    private String mspId;

    public FabricUser(String name, String mspId, Enrollment enrollment) {
        this.name = name;
        this.mspId = mspId;
        this.enrollment = enrollment;
    }

    public FabricUser(String name, Set<String> roles, String account, String affiliation, Enrollment enrollment, String mspId) {
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

    public void setName(String name) {
        this.name = name;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

    public static Enrollment createEnrollment(final String pemCertificateString, final String pemPrivateKeyString) throws IOException {
        final var privateKey = getPrivateKeyFromPemString(pemPrivateKeyString);

        return new Enrollment() {
            @Override
            public PrivateKey getKey() {
                return privateKey;
            }

            @Override
            public String getCert() {
                return pemCertificateString;
            }
        };
    }

    public static Enrollment createEnrollment(File pemCertificateFile, File pemPrivateKeyFile) throws IOException {
        final var pemCertificateString = readPemFileContent(pemCertificateFile);
        final var pemPrivateKeyString = readPemFileContent(pemPrivateKeyFile);

        return createEnrollment(pemCertificateString, pemPrivateKeyString);
    }

    private static String readPemFileContent(File pemFile){
        try (FileInputStream stream = new FileInputStream(pemFile)) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read pem file: '%s'", pemFile.getAbsolutePath()), e);
        }
    }

    private static PrivateKey getPrivateKeyFromPemString(String pemPrivateKey) throws IOException {
        PrivateKeyInfo privateKeyInfo;
        try (var pemParser = new PEMParser(new StringReader(pemPrivateKey))) {
            privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
        }
        return new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
    }
}
