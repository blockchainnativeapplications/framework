package org.blockchainnative.fabric.test;

import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.fabric.FabricArgumentConverterImpl;
import org.blockchainnative.fabric.FabricContractApi;
import org.blockchainnative.fabric.FabricContractWrapper;
import org.blockchainnative.fabric.builder.FabricContractInfoBuilder;
import org.blockchainnative.fabric.test.contracts.TypeConverterTestContract;
import org.blockchainnative.fabric.typeconverters.FabricDefaultTypeConverters;
import org.blockchainnative.metadata.Result;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Matthias Veit
 */
@SuppressWarnings("unchecked")
public class TypeConverterTests {


    @Test
    public void uuidParameter() throws Exception {
        // arrange
        var contractInfo = new FabricContractInfoBuilder<>(TypeConverterTestContract.class)
                .withChainCodeIdentifier(ChaincodeID.newBuilder().build())
                .build();

        var api = mock(FabricContractApi.class);
        when(api.callChaincode(any(), any(), any(), any())).thenReturn(new Result<>(null));
        var contractWrapper = new FabricContractWrapper(contractInfo, api, new FabricArgumentConverterImpl(getTypeConverters()));

        // act
        var uuid = UUID.randomUUID();
        invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("uuidParameter", UUID.class), uuid);

        // assert
        verify(api, times(1)).callChaincode(any(), argThat(arguments -> {
            var expected = uuid.toString();
            return arguments.length == 1 && expected.equals(arguments[0]);
        }), any(), any());
    }

    @Test
    public void uuidParameterPassAsType() throws Exception {
        // arrange
        var contractInfo = new FabricContractInfoBuilder<>(TypeConverterTestContract.class)
                .withChainCodeIdentifier(ChaincodeID.newBuilder().build())
                .build();

        var api = mock(FabricContractApi.class);
        when(api.callChaincode(any(), any(), any(), any())).thenReturn(new Result<>(null));
        var contractWrapper = new FabricContractWrapper(contractInfo, api, new FabricArgumentConverterImpl(getTypeConverters()));

        // act
        var uuid = UUID.randomUUID();
        invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("uuidParameterPassAsType", UUID.class), uuid);

        // assert
        verify(api, times(1)).callChaincode(any(), argThat(arguments -> {
            var expected = uuid.toString();
            return arguments.length == 1 && expected.equals(arguments[0]);
        }), any(), any());
    }

    @Test
    public void uuidReturnType() throws Exception {
        // arrange
        var contractInfo = new FabricContractInfoBuilder<>(TypeConverterTestContract.class)
                .withChainCodeIdentifier(ChaincodeID.newBuilder().build())
                .build();

        var uuid = UUID.randomUUID();

        var api = mock(FabricContractApi.class);
        when(api.callChaincode(any(), any(), any(), any())).thenReturn(new Result<>(uuid.toString()));

        var contractWrapper = new FabricContractWrapper(contractInfo, api, new FabricArgumentConverterImpl(getTypeConverters()));

        // act
        var result = invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("uuidReturnType"));

        // assert
        verify(api, times(1)).callChaincode(any(), argThat(arguments -> arguments.length == 0), any(), any());
        assertEquals(uuid, result);
    }

    @Test
    public void byteReturnType() throws Exception {
        // arrange
        var contractInfo = new FabricContractInfoBuilder<>(TypeConverterTestContract.class)
                .withChainCodeIdentifier(ChaincodeID.newBuilder().build())
                .build();

        var api = mock(FabricContractApi.class);
        when(api.callChaincode(any(), any(), any(), any())).thenReturn(new Result<>("2"));
        var contractWrapper = new FabricContractWrapper(contractInfo, api, new FabricArgumentConverterImpl(getTypeConverters()));

        // act
        var result = invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("byteReturnType"));

        // assert
        verify(api, times(1)).callChaincode(any(), argThat(arguments -> arguments.length == 0), any(), any());

        assertEquals((byte) 2, result);
    }

    public Object invokeMethod(FabricContractWrapper contractWrapper, Method method, Object... arguments) {
        return contractWrapper.intercept(method, arguments);
    }


    public TypeConverters getTypeConverters() {
        var typeConverters = new TypeConverters(
                new UUIDStringConverter()
        );

        typeConverters.addAll(FabricDefaultTypeConverters.getConverters());
        return typeConverters;
    }

    private class UUIDStringConverter implements TypeConverter<String, UUID> {

        @Override
        public UUID to(String s) {
            return UUID.fromString(s);
        }

        @Override
        public String from(UUID uuid) {
            return uuid.toString();
        }
    }
}
