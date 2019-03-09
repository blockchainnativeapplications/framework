package org.blockchainnative.ethereum.test;

import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.ethereum.EthereumArgumentConverterImpl;
import org.blockchainnative.ethereum.EthereumContractWrapper;
import org.blockchainnative.ethereum.Web3ContractApiImpl;
import org.blockchainnative.ethereum.builder.EthereumContractInfoBuilder;
import org.blockchainnative.ethereum.test.contracts.TypeConverterTestContract;
import org.blockchainnative.metadata.Result;
import org.blockchainnative.test.contracts.HelloContract;
import org.junit.Test;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.abi.datatypes.generated.StaticArray5;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Matthias Veit
 */
@SuppressWarnings("unchecked")
public class TypeConverterTests {

    @Test
    public void implicitStringConversion() throws Exception {
        // arrange
        var contractInfo = new EthereumContractInfoBuilder<>(HelloContract.class)
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/HelloWorld.abi").getFile()))
                .build();

        var contractConnector = mock(Web3ContractApiImpl.class);
        when(contractConnector.executeFunctionCallTransaction(any(), any(), any(), any())).thenReturn(new Result<>(new ArrayList<>() {{
            add(new Utf8String("Hello Test!"));
        }}, null, null));

        var contractWrapper = new EthereumContractWrapper(contractInfo, contractConnector, new DefaultGasProvider(), new EthereumArgumentConverterImpl(getTypeConverters()));

        // act
        var result = invokeMethod(contractWrapper, HelloContract.class.getDeclaredMethod("hello", String.class), "Test");

        // assert
        verify(contractConnector).executeFunctionCallTransaction(argThat(argument -> {
            var expected = new ArrayList<>() {{
                add(new Utf8String("Test"));
            }};
            return argument.getInputParameters().containsAll(expected) && expected.containsAll(argument.getInputParameters());
        }), any(), any(), any());

        assertEquals("Hello Test!", result);
    }


    @Test
    public void uuidParameter() throws Exception {
        // arrange
        var contractInfo = new EthereumContractInfoBuilder<>(TypeConverterTestContract.class)
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/TypeConverterTestContract.abi").getFile()))
                .build();

        var contractConnector = mock(Web3ContractApiImpl.class);
        when(contractConnector.executeFunctionCallTransaction(any(), any(), any(), any())).thenReturn(new Result<>(new ArrayList<>(), null, null));

        var contractWrapper = new EthereumContractWrapper(contractInfo, contractConnector, new DefaultGasProvider(), new EthereumArgumentConverterImpl(getTypeConverters()));
        var uuid = UUID.randomUUID();

        // act
        invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("uuidParameter", UUID.class), uuid);

        // assert
        verify(contractConnector, times(1)).executeFunctionCallTransaction(argThat(argument -> {
            var expected = new ArrayList<>() {{
                add(new Utf8String(uuid.toString()));
            }};
            return argument.getInputParameters().containsAll(expected) && expected.containsAll(argument.getInputParameters());
        }), any(), any(), any());
    }

    @Test
    public void uuidReturnType() throws Exception {
        // arrange
        var contractInfo = new EthereumContractInfoBuilder<>(TypeConverterTestContract.class)
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/TypeConverterTestContract.abi").getFile()))
                .build();

        var uuid = UUID.randomUUID();
        var convert = new UUIDByteConverter();

        var contractConnector = mock(Web3ContractApiImpl.class);
        when(contractConnector.executeFunctionCallTransaction(any(), any(), any(), any())).thenReturn(new Result<>(new ArrayList<>() {{
            add(new Bytes16(convert.to(uuid)));
        }}, null, null));

        var contractWrapper = new EthereumContractWrapper(contractInfo, contractConnector, new DefaultGasProvider(), new EthereumArgumentConverterImpl(getTypeConverters()));

        // act
        var result = invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("uuidReturnType"));

        // assert
        verify(contractConnector, times(1)).executeFunctionCallTransaction(argThat(argument -> argument.getInputParameters().isEmpty()), any(), any(), any());

        assertEquals(uuid, result);
    }

    @Test
    public void uuidParameterPassAsType() throws Exception {
        // arrange
        var contractInfo = new EthereumContractInfoBuilder<>(TypeConverterTestContract.class)
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/TypeConverterTestContract.abi").getFile()))
                .build();

        var contractConnector = mock(Web3ContractApiImpl.class);

        when(contractConnector.executeFunctionCallTransaction(any(), any(), any(), any())).thenReturn(new Result<>(new ArrayList<>(), null, null));

        var contractWrapper = new EthereumContractWrapper(contractInfo, contractConnector, new DefaultGasProvider(), new EthereumArgumentConverterImpl(getTypeConverters()));
        var uuid = UUID.randomUUID();

        // act
        invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("uuidParameterPassAsType", UUID.class), uuid);

        // assert
        verify(contractConnector, times(1)).executeFunctionCallTransaction(argThat(argument -> {
            var expected = new ArrayList<>() {{
                add(new Utf8String(uuid.toString()));
            }};
            return argument.getInputParameters().containsAll(expected) && expected.containsAll(argument.getInputParameters());
        }), any(), any(), any());
    }

    @Test
    public void byteReturnType() throws Exception {
        // arrange
        var contractInfo = new EthereumContractInfoBuilder<>(TypeConverterTestContract.class)
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/TypeConverterTestContract.abi").getFile()))
                .build();

        var contractConnector = mock(Web3ContractApiImpl.class);

        when(contractConnector.executeFunctionCallTransaction(any(), any(), any(), any())).thenReturn(new Result<>(new ArrayList<>() {{
            add(new Uint8(2));
        }}, null, null));

        var contractWrapper = new EthereumContractWrapper(contractInfo, contractConnector, new DefaultGasProvider(), new EthereumArgumentConverterImpl(getTypeConverters()));

        // act
        var result = invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("byteReturnType"));

        // assert
        verify(contractConnector, times(1)).executeFunctionCallTransaction(argThat(argument -> argument.getInputParameters().isEmpty()), any(), any(), any());

        assertEquals((byte) 2, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listParameter() throws Exception {
        // arrange
        var contractInfo = new EthereumContractInfoBuilder<>(TypeConverterTestContract.class)
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/TypeConverterTestContract.abi").getFile()))
                .build();

        var contractConnector = mock(Web3ContractApiImpl.class);
        when(contractConnector.executeFunctionCallTransaction(any(), any(), any(), any())).thenReturn(new Result<>(new ArrayList<>(), null, null));

        var uuids = IntStream.range(0, 5).mapToObj(i -> UUID.randomUUID()).collect(Collectors.toList());

        var contractWrapper = new EthereumContractWrapper(contractInfo, contractConnector, new DefaultGasProvider(), new EthereumArgumentConverterImpl(getTypeConverters()));

        // act
        invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("listParameter", List.class), uuids);

        // assert
        verify(contractConnector, times(1)).executeFunctionCallTransaction(argThat(argument -> {
            var convert = new UUIDByteConverter();
            var expected = new ArrayList<>(uuids.stream().map(uuid -> new Bytes16(convert.to(uuid))).collect(Collectors.toList()));

            return argument.getInputParameters().size() == 1 &&
                    argument.getInputParameters().get(0) instanceof DynamicArray &&
                    expected.containsAll(((DynamicArray) argument.getInputParameters().get(0)).getValue()) && ((DynamicArray) argument.getInputParameters().get(0)).getValue().containsAll(expected);
        }), any(), any(), any());
    }

    @Test
    public void uuidListReturnType() throws Exception {
        // arrange
        var contractInfo = new EthereumContractInfoBuilder<>(TypeConverterTestContract.class)
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/TypeConverterTestContract.abi").getFile()))
                .build();

        var uuids = IntStream.range(0, 5).mapToObj(i -> UUID.randomUUID()).collect(Collectors.toList());
        var convert = new UUIDByteConverter();

        var contractConnector = mock(Web3ContractApiImpl.class);
        when(contractConnector.executeFunctionCallTransaction(any(), any(), any(), any())).thenReturn(new Result<>(
                new ArrayList<>(){{
                    add(new DynamicArray(uuids.stream().map(uuid -> new Bytes16(convert.to(uuid))).collect(Collectors.toList())));
                }}, null, null));

        var contractWrapper = new EthereumContractWrapper(contractInfo, contractConnector, new DefaultGasProvider(), new EthereumArgumentConverterImpl(getTypeConverters()));

        // act
        var result = invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("uuidListReturnType"));

        // assert
        verify(contractConnector, times(1)).executeFunctionCallTransaction(argThat(argument -> argument.getInputParameters().isEmpty()), any(), any(), any());

        assertEquals(uuids, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void twoDimensionalListParameter() throws Exception {
        // arrange
        var contractInfo = new EthereumContractInfoBuilder<>(TypeConverterTestContract.class)
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/TypeConverterTestContract.abi").getFile()))
                .build();

        var contractConnector = mock(Web3ContractApiImpl.class);

        when(contractConnector.executeFunctionCallTransaction(any(), any(), any(), any())).thenReturn(new Result<>(new ArrayList<>(), null, null));

        var uuids = IntStream.range(0, 5)
                .mapToObj(i -> IntStream.range(0, 5)
                        .mapToObj(j -> UUID.randomUUID())
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        var contractWrapper = new EthereumContractWrapper(contractInfo, contractConnector, new DefaultGasProvider(), new EthereumArgumentConverterImpl(getTypeConverters()));

        // act
        invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("twoDimensionalListParameter", List.class), uuids);

        // assert
        verify(contractConnector, times(1)).executeFunctionCallTransaction(argThat(argument -> {
            var convert = new UUIDByteConverter();
            var expected = new ArrayList<>(
                    uuids.stream()
                            .map(uuidList -> uuidList.stream()
                                    .map(uuid -> new Bytes16(convert.to(uuid)))
                                    .collect(Collectors.toList()))
                            .collect(Collectors.toList()));

            if(argument.getInputParameters().size() != 1 && !(argument.getInputParameters().get(0) instanceof DynamicArray))
                return false;

            var equal = true;
            var outer = (DynamicArray<?>)argument.getInputParameters().get(0);

            if(outer.getValue().size() != expected.size())
                return false;

            for(var i = 0; i < outer.getValue().size(); i++){
                var inner = outer.getValue().get(i);
                if(!(inner instanceof StaticArray5)) {
                    return false;
                }
                equal &= ((StaticArray5) inner).getValue().containsAll(expected.get(i))
                        && expected.get(i).containsAll(((StaticArray5) inner).getValue());
            }

            return equal;
        }), any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void arrayParameter() throws Exception {
        // arrange
        var contractInfo = new EthereumContractInfoBuilder<>(TypeConverterTestContract.class)
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/TypeConverterTestContract.abi").getFile()))
                .build();

        var contractConnector = mock(Web3ContractApiImpl.class);

        when(contractConnector.executeFunctionCallTransaction(any(), any(), any(), any())).thenReturn(new Result<>(new ArrayList<>(), null, null));

        var values = IntStream.range(0, 5).mapToObj(i -> (short)i).collect(Collectors.toList());

        var contractWrapper = new EthereumContractWrapper(contractInfo, contractConnector, new DefaultGasProvider(), new EthereumArgumentConverterImpl(getTypeConverters()));

        // act
        invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("arrayParameter", short[].class), values);

        // assert
        verify(contractConnector, times(1)).executeFunctionCallTransaction(argThat(argument -> {
            var expected = new ArrayList<>(values.stream().map(value -> new Uint16(value)).collect(Collectors.toList()));

            return argument.getInputParameters().size() == 1 &&
                    argument.getInputParameters().get(0) instanceof DynamicArray &&
                    expected.containsAll(((DynamicArray) argument.getInputParameters().get(0)).getValue()) && ((DynamicArray) argument.getInputParameters().get(0)).getValue().containsAll(expected);
        }), any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void twoDimensionalArrayParameter() throws Exception {
        // arrange
        var contractInfo = new EthereumContractInfoBuilder<>(TypeConverterTestContract.class)
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/TypeConverterTestContract.abi").getFile()))
                .build();

        var contractConnector = mock(Web3ContractApiImpl.class);

        when(contractConnector.executeFunctionCallTransaction(any(), any(), any(), any())).thenReturn(new Result<>(new ArrayList<>(), null, null));

        var values = IntStream.range(0, 5)
                .mapToObj(i -> IntStream.range(0, 5)
                        .mapToObj(j -> (short)j)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        var contractWrapper = new EthereumContractWrapper(contractInfo, contractConnector, new DefaultGasProvider(), new EthereumArgumentConverterImpl(getTypeConverters()));

        // act
        invokeMethod(contractWrapper, TypeConverterTestContract.class.getDeclaredMethod("twoDimensionalArrayParameter", short[][].class), values);

        // assert
        verify(contractConnector, times(1)).executeFunctionCallTransaction(argThat(argument -> {
            var expected = new ArrayList<>(
                    values.stream()
                            .map(ol -> ol.stream()
                                    .map(val -> new Uint16(val))
                                    .collect(Collectors.toList()))
                            .collect(Collectors.toList()));

            if(argument.getInputParameters().size() != 1 && !(argument.getInputParameters().get(0) instanceof DynamicArray))
                return false;

            var equal = true;
            var outer = (DynamicArray<?>)argument.getInputParameters().get(0);

            if(outer.getValue().size() != expected.size())
                return false;

            for(var i = 0; i < outer.getValue().size(); i++){
                var inner = outer.getValue().get(i);
                if(!(inner instanceof StaticArray5)) {
                    return false;
                }
                equal &= ((StaticArray5) inner).getValue().containsAll(expected.get(i))
                        && expected.get(i).containsAll(((StaticArray5) inner).getValue());
            }

            return equal;
        }), any(), any(), any());
    }

    public Object invokeMethod(EthereumContractWrapper contractWrapper, Method method, Object... arguments) {
        return contractWrapper.intercept(method, arguments);
    }

    public TypeConverters getTypeConverters() {
        return new TypeConverters(
                new UUIDStringConverter(), new UUIDByteConverter()
        );
    }

    private class UUIDByteConverter implements TypeConverter<UUID, byte[]> {

        @Override
        public byte[] to(UUID s) {
            var buffer = ByteBuffer.wrap(new byte[16]);
            buffer.putLong(s.getLeastSignificantBits());
            buffer.putLong(s.getMostSignificantBits());
            return buffer.array();
        }

        @Override
        public UUID from(byte[] bytes) {
            var buffer = ByteBuffer.wrap(bytes);
            var lsb = buffer.getLong();
            var msb = buffer.getLong();

            return new UUID(msb, lsb);
        }
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
