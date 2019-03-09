package org.blockchainnative.fabric.test.integrationtest;

import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.fabric.test.TestEnrollment;
import org.blockchainnative.fabric.test.TestUser;
import org.blockchainnative.fabric.typeconverters.IntegerStringConverter;
import org.blockchainnative.test.IntegrationTest;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.experimental.categories.Category;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Matthias Veit
 */
@Category(IntegrationTest.class)
public abstract class FabricIntegrationTest {

    protected static User getFooUser() {
        var enrollment = new TestEnrollment(
                "-----BEGIN PRIVATE KEY-----\n" +
                        "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgzMeSQteLja0mSKIP\n" +
                        "nP+6JuHTk+gH95ySCjblWrsztt+hRANCAASlmk4DkQwJqFMsEdv8xW2rFFJv5wCt\n" +
                        "/dAyCu2eacSD2OOiSnN/aVJPk+ayeknrPk/uH3m1ZvFZpmR8Zjc+8M9B\n" +
                        "-----END PRIVATE KEY-----",
                "-----BEGIN CERTIFICATE-----\n" +
                        "MIICCjCCAbGgAwIBAgIRAOZqpGlH88k9YHnbdIsmmU8wCgYIKoZIzj0EAwIwaTEL\n" +
                        "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\n" +
                        "cmFuY2lzY28xFDASBgNVBAoTC2Zvby5iY24ub3JnMRcwFQYDVQQDEw5jYS5mb28u\n" +
                        "YmNuLm9yZzAeFw0xODA4MjYxMTI2NDNaFw0yODA4MjMxMTI2NDNaMFYxCzAJBgNV\n" +
                        "BAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1TYW4gRnJhbmNp\n" +
                        "c2NvMRowGAYDVQQDDBFVc2VyMUBmb28uYmNuLm9yZzBZMBMGByqGSM49AgEGCCqG\n" +
                        "SM49AwEHA0IABKWaTgORDAmoUywR2/zFbasUUm/nAK390DIK7Z5pxIPY46JKc39p\n" +
                        "Uk+T5rJ6Ses+T+4febVm8VmmZHxmNz7wz0GjTTBLMA4GA1UdDwEB/wQEAwIHgDAM\n" +
                        "BgNVHRMBAf8EAjAAMCsGA1UdIwQkMCKAIMxNGwqNhk1TfVQvxy+uF1PbxKLTODu9\n" +
                        "kzyP/zz2rTM+MAoGCCqGSM49BAMCA0cAMEQCIEpga4VWUL4hMWYZs9Mwg5mwrG6w\n" +
                        "y06HWXjUKVd/Wh9DAiAvyFUxYsgaO94XOvVVtPv/vGe6ROTgbOej/5VqhMHI4g==\n" +
                        "-----END CERTIFICATE-----"
                );
        return new TestUser("user1", new HashSet<>(), null, null, enrollment, "FooMSP");
    }

    protected static User getFooAdmin() {
        var enrollment = new TestEnrollment(
                "-----BEGIN PRIVATE KEY-----\n" +
                        "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgMBmBCoFTmL6A5g9q\n" +
                        "Mx0aDBS4vmHauEgPe/4HrLaHQ5uhRANCAARmEgTnBwDg8abCy8+kcUCiGXPQS108\n" +
                        "CxAc0vBKIdyS6/OUNcKHp6+r6QleyAqUbZa/3ffSeB9i/ajeUm5wM/fD\n" +
                        "-----END PRIVATE KEY-----",
                "-----BEGIN CERTIFICATE-----\n" +
                        "MIICCjCCAbGgAwIBAgIRANjDkFcrz1m3VGRSTjEmS+IwCgYIKoZIzj0EAwIwaTEL\n" +
                        "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\n" +
                        "cmFuY2lzY28xFDASBgNVBAoTC2Zvby5iY24ub3JnMRcwFQYDVQQDEw5jYS5mb28u\n" +
                        "YmNuLm9yZzAeFw0xODA4MjYxMTI2NDNaFw0yODA4MjMxMTI2NDNaMFYxCzAJBgNV\n" +
                        "BAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1TYW4gRnJhbmNp\n" +
                        "c2NvMRowGAYDVQQDDBFBZG1pbkBmb28uYmNuLm9yZzBZMBMGByqGSM49AgEGCCqG\n" +
                        "SM49AwEHA0IABGYSBOcHAODxpsLLz6RxQKIZc9BLXTwLEBzS8Eoh3JLr85Q1woen\n" +
                        "r6vpCV7ICpRtlr/d99J4H2L9qN5SbnAz98OjTTBLMA4GA1UdDwEB/wQEAwIHgDAM\n" +
                        "BgNVHRMBAf8EAjAAMCsGA1UdIwQkMCKAIMxNGwqNhk1TfVQvxy+uF1PbxKLTODu9\n" +
                        "kzyP/zz2rTM+MAoGCCqGSM49BAMCA0cAMEQCIFBSSTWZJ9SPIt2wp9MGPQNBxbFy\n" +
                        "zuQxWl1uNsclDzuvAiAOVV/HNm5sKkM/7q9zV1yTEgRhaM6OU4m7+ms5km2JiA==\n" +
                        "-----END CERTIFICATE-----"
        );
        return new TestUser("admin", new HashSet<>() {{ add("admin"); }}, null, null, enrollment, "FooMSP");
    }

    protected static User getBarAdmin() {
        var enrollment = new TestEnrollment(
                "-----BEGIN PRIVATE KEY-----\n" +
                        "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgAs/z8KZaZ6nPQL8D\n" +
                        "Pplw5ZvuSBdzl9Eh5wvQRJ+36HehRANCAASXDRjhhVLmGR4D3DbyAkvPe6clLUhZ\n" +
                        "gclM2R7aXQVsJtgrTz3e9tIYr1TlWxWufW4lq9Jht+qj+eSVdRfQ7nsx\n" +
                        "-----END PRIVATE KEY-----",
                "-----BEGIN CERTIFICATE-----\n" +
                        "MIICCjCCAbGgAwIBAgIRAMGTE7QpheXDJzRQu5FpovswCgYIKoZIzj0EAwIwaTEL\n" +
                        "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\n" +
                        "cmFuY2lzY28xFDASBgNVBAoTC2Jhci5iY24ub3JnMRcwFQYDVQQDEw5jYS5iYXIu\n" +
                        "YmNuLm9yZzAeFw0xODA4MjYxMTI2NDNaFw0yODA4MjMxMTI2NDNaMFYxCzAJBgNV\n" +
                        "BAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1TYW4gRnJhbmNp\n" +
                        "c2NvMRowGAYDVQQDDBFBZG1pbkBiYXIuYmNuLm9yZzBZMBMGByqGSM49AgEGCCqG\n" +
                        "SM49AwEHA0IABJcNGOGFUuYZHgPcNvICS897pyUtSFmByUzZHtpdBWwm2CtPPd72\n" +
                        "0hivVOVbFa59biWr0mG36qP55JV1F9DuezGjTTBLMA4GA1UdDwEB/wQEAwIHgDAM\n" +
                        "BgNVHRMBAf8EAjAAMCsGA1UdIwQkMCKAIOUNpinAEbJNNENXpEcxznmsJgl5eG4W\n" +
                        "nTIursvEK+OhMAoGCCqGSM49BAMCA0cAMEQCIA3cfoV/IbUSXA4l1YN+L2HPzV5X\n" +
                        "ZtCDk6KsngOKtW0FAiBmWr81hwfoB9qACJAmZ9v2p1RqEWqne02gZT+6GVenlg==\n" +
                        "-----END CERTIFICATE-----"
        );
        return new TestUser("admin", new HashSet<>() {{ add("ADMIN"); }}, null, null, enrollment, "BarMSP");
    }

    protected static Supplier<HFClient> getClientFactory(){
        return () -> {
            try {
                CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
                var client = HFClient.createNewInstance();
                client.setCryptoSuite(cryptoSuite);
                client.setUserContext(getFooUser());
                return client;
            }catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | InvalidArgumentException | CryptoException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        };
    }

    protected static  Function<HFClient, Channel> getChannelFactory(){
        return (client) -> {
            try {
                var channel = client.loadChannelFromConfig("bcnchannel", NetworkConfig.fromYamlStream(FabricIntegrationTest.class.getClassLoader().getResourceAsStream("bcn-network-config-simplified.yaml")));
                channel.initialize();
                return channel;
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        };
    }

    protected static TypeConverters getTypeConverters(){
        var typeConverters = new TypeConverters();

        typeConverters.add(new IntegerStringConverter());

        return typeConverters;
    }
}
