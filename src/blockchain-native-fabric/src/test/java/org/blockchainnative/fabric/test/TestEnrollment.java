package org.blockchainnative.fabric.test;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.hyperledger.fabric.sdk.Enrollment;

import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @author Matthias Veit
 */
public class TestEnrollment implements Enrollment {
    private String certificate;
    private PrivateKey privateKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public TestEnrollment(String privateKey, String certificate) {
        this.certificate = certificate;
        try {
            PemReader reader = new PemReader(new StringReader(privateKey));
            PemObject obj = reader.readPemObject();
            PrivateKeyInfo info = PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(obj.getContent()));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(obj.getContent());

            KeyFactory keyFact = KeyFactory.getInstance(info.getPrivateKeyAlgorithm().getAlgorithm().getId(), BouncyCastleProvider.PROVIDER_NAME);

            this.privateKey = keyFact.generatePrivate(keySpec);
        } catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PrivateKey getKey() {
        return privateKey;
    }

    @Override
    public String getCert() {
        return certificate;
    }
}
