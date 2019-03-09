package org.blockchainnative.quorum;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.ExceptionMethod;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.blockchainnative.ContractWrapperGenerator;
import org.blockchainnative.SmartContract;
import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.quorum.metadata.QuorumContractInfo;
import org.blockchainnative.exceptions.ContractWrapperCreationException;
import org.blockchainnative.metadata.ContractInfo;
import org.blockchainnative.metadata.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.quorum.Quorum;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Generates wrapper classes for Ethereum smart contracts. <br>
 *
 * <h2>Supported Types</h2>
 *
 * <p>
 * Classes built by this generator can natively convert the following application binary interface (ABI) types to their
 * corresponding Java type and vice versa. However, be careful of potential precision loss when using primitive value
 * types for big numbers.
 * </p>
 *
 * <table border="1">
 * <thead>
 * <tr>
 * <th>ABI Type</th>
 * <th>Java Type</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>bool</td>
 * <td>boolean</td>
 * </tr>
 * <tr>
 * <td>bytes, bytes8 - bytes256</td>
 * <td>byte[]</td>
 * </tr>
 * <tr>
 * <td>uint, uint8 - unit256, int, int8 - int256</td>
 * <td>BigInteger, byte, short, int, long</td>
 * </tr>
 * <tr>
 * <td>string, address</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>static array, dynamic array</td>
 * <td>[], List&lt;&gt;</td>
 * </tr>
 * </tbody>
 * <caption>ABI to Java type mapping</caption>
 * </table>
 *
 * <p>
 * For every other type, type converters need to be registered. However, in order to convert lists and arrays of custom
 * objects, only a type converter for the specific object is required. <br> Consider the following example. Assume the
 * smart contract's method 'getSpecialObject' returns a uint[]. In order to be able to convert the smart contract result
 * to {@code List<SpecialObject>}, only a type converter is needed that is able to handle objects of type {@code
 * SpecialObject}, arrays and lists are unrolled automatically. The same applies for arguments.
 * </p>
 * <pre>{@code
 *     public interface MyContract {
 *
 *         List<SpecialObject> getSpecialObjects();
 *
 *         public class SpecialObject {
 *              public int number;
 *         }
 *     }
 *     }</pre>
 *
 * <h2>Special Arguments</h2>
 * <p>
 * All contract methods can handle the following special arguments, however, passing them to methods which do not result
 * in a transaction (i.e. methods marked as readonly {@link ContractMethod#isReadOnly()}, {@link
 * MethodInfo#isReadOnly()}) has no effect.
 *
 * <h3>Gas Limit</h3>
 *
 * <p>
 * Specifies the gas limit used for a transaction. <br> If no such parameter is declared or its value is passed as null,
 * a default value will be chosen. <br> A parameter declared as such needs to be of type {@code BigInteger} or {@code
 * Number}.
 * </p>
 *
 * <h3>Value</h3>
 * <p>
 * Specifies the amount of Ether (in Wei) to be sent in a transaction. <br> If no such parameter is declared or its
 * value is passed as null, no Ether will be sent.<br> A parameter declared as such needs to be of type {@code
 * BigInteger} or {@code Number}.
 * </p>
 *
 * <h3>Private For</h3>
 * <p>
 * Specifies the recipients targeted by a transaction. <br> A parameter declared as such needs to be an {@code
 * Collection} of {@code String} containing the base64 encoded public keys of the targeted nodes. <br> The recipients
 * can be defined via {@link QuorumContractInfo#setPrivateFor(List)} or via a special argument ({@link
 * Constants#PRIVATE_FOR_ARGUMENT}). If no recipients are set, every transaction will be public.
 * </p>
 * <h2>Special Methods</h2>
 *
 * <h3>Deploy</h3>
 * <p>
 * A method marked as special method and named <i>'deploy'</i> ({@link Constants#DEPLOYMENT_METHOD}) is mapped to the
 * constructor of the Ethereum smart contract. All parameters not marked as special arguments are converted and passed
 * to the constructor. <br> Such method can either be {@code void} or return {@code String}. In the later case, the
 * address to which the contract has been deployed is returned. <br> In order to be able to deploy the smart contract,
 * its {@code QuorumContractInfo} needs to specify the contracts binary.
 * </p>
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class QuorumContractWrapperGenerator implements ContractWrapperGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuorumContractWrapperGenerator.class);
    private final Supplier<Quorum> clientFactory;
    private final Function<Quorum, TransactionManager> transactionManagerFactory;
    private final TypeConverters typeConverters;

    /**
     * Initializes a new {@code QuorumContractWrapperGenerator}.
     * <p>
     * For each contract wrapper, a new instances of {@code Web3j} and {@code TransactionManager} are retrieved via its
     * factories. <br> It is possible for those factories to always return the same instance.
     * </p>
     *
     * @param clientFactory             factory producing {@code Web3j} instances for communicating with an Ethereum
     *                                  node.
     * @param transactionManagerFactory factory producing {@code TransactionManager} instances for authorizing
     *                                  transactions. An instance of {@code Web3j} produced via {@code clientFactory} is
     *                                  passed to this function.
     * @param typeConverters            additional {@code TypeConverter}, may be null
     */
    public QuorumContractWrapperGenerator(Supplier<Quorum> clientFactory, Function<Quorum, TransactionManager> transactionManagerFactory, TypeConverters typeConverters) {
        if (clientFactory == null) throw new IllegalArgumentException("clientFactory must not be null");
        this.clientFactory = clientFactory;

        if (transactionManagerFactory == null)
            throw new IllegalArgumentException("transactionManagerFactory must not be null");
        this.transactionManagerFactory = transactionManagerFactory;

        this.typeConverters = typeConverters == null ? new TypeConverters() : typeConverters;
    }

    /**
     * Generates a new wrapper for the smart contract described by {@code contractInfo}. <br> Although defined otherwise
     * by the interface, {@code contractInfo} needs to be of type {@link QuorumContractInfo}.
     *
     * @param contractInfo    contract info representing the smart contract
     * @param <TContractInfo> type of the contract info, required to be {@link QuorumContractInfo}
     * @param <TContractType> Java interface declaring the smart contracts methods and events
     * @return implementation of the given smart contract interface {@code TContractType}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <TContractInfo extends ContractInfo<TContractType, ?, ?>, TContractType> TContractType generate(TContractInfo contractInfo) {
        if (contractInfo == null) throw new IllegalArgumentException("contractClass must not be null!");
        if (!(contractInfo instanceof QuorumContractInfo))
            throw new IllegalArgumentException(String.format("%s requires an instance of %s to generate contract wrappers.", this.getClass().getSimpleName(), QuorumContractInfo.class.getSimpleName()));

        // this is safe, contractInfo is an instance of QuorumContractInfo,
        // Moreover TContractType is ensured by the generic constraint
        var quorumContractInfo = (QuorumContractInfo<TContractType>) contractInfo;

        var contractClass = quorumContractInfo.getContractClass();
        if (!contractClass.isInterface()) throw new IllegalArgumentException("contractClass needs to be an interface!");

        LOGGER.info("Creating smart contract wrapper for contract '{}' ({})", contractInfo.getIdentifier(), contractClass.getName());

        var client = clientFactory.get();
        var transactionManager = transactionManagerFactory.apply(client);

        var base = new QuorumContractWrapper(quorumContractInfo, new Web3jQuorumContractApiImpl(client, transactionManager, quorumContractInfo), new DefaultGasProvider(), typeConverters);

        var methodsByNameMatcher = getMethodElementMatcher(quorumContractInfo);
        var eventsByNameMatcher = getEventsElementMatcher(quorumContractInfo);
        var remainingMethodsMatcher = getRemainingMethodsElementMatcher(quorumContractInfo);

        TContractType wrapper;
        try {
            var interceptionMethodName = QuorumContractWrapper.class.getMethod("intercept", Method.class, Object[].class).getName();

            wrapper = new ByteBuddy()
                    .subclass(contractClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                    .name(getWrapperName(contractInfo))
                    .method(methodsByNameMatcher)
                    .intercept(MethodDelegation.to(base, QuorumContractWrapper.class, interceptionMethodName))
                    .method(eventsByNameMatcher)
                    .intercept(MethodDelegation.to(base, QuorumContractWrapper.class, interceptionMethodName))
                    .method(remainingMethodsMatcher)
                    .intercept(ExceptionMethod.throwing(UnsupportedOperationException.class))
                    .make()
                    .load(getClass().getClassLoader())
                    .getLoaded()
                    .asSubclass(contractClass)
                    .getDeclaredConstructor().newInstance();

            return wrapper;

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            var message = String.format("Failed to create smart contract wrapper for contract '%s' (%s): %s", contractInfo.getIdentifier(), contractClass.getName(), e.getMessage());
            LOGGER.error(message, e);
            throw new ContractWrapperCreationException(message, e);
        }
    }

    private ElementMatcher.Junction<? super MethodDescription> getMethodElementMatcher(QuorumContractInfo<?> contractInfo) {
        ElementMatcher.Junction<? super MethodDescription> methodsByNameMatcher = ElementMatchers.none();
        for (var methodInfo : contractInfo.getMethodInfos().values()) {
            methodsByNameMatcher = methodsByNameMatcher.or(ElementMatchers.named(methodInfo.getMethod().getName()));
        }

        if (Arrays.stream(contractInfo.getContractClass().getMethods())
                .anyMatch(method -> SmartContract.GET_CONTRACT_INFO_METHOD_NAME.equals(method.getName()))) {
            methodsByNameMatcher = methodsByNameMatcher.or(ElementMatchers.named(SmartContract.GET_CONTRACT_INFO_METHOD_NAME));
        }

        return methodsByNameMatcher;
    }

    private ElementMatcher.Junction<? super MethodDescription> getEventsElementMatcher(QuorumContractInfo<?> contractInfo) {
        ElementMatcher.Junction<? super MethodDescription> eventsByNameMatcher = ElementMatchers.none();
        for (var eventInfo : contractInfo.getEventInfos().values()) {
            eventsByNameMatcher = eventsByNameMatcher.or(ElementMatchers.named(eventInfo.getMethod().getName()));
        }
        return eventsByNameMatcher;
    }

    private ElementMatcher.Junction<? super MethodDescription> getRemainingMethodsElementMatcher(QuorumContractInfo<?> contractInfo) {
        ElementMatcher.Junction<? super MethodDescription> allInterfaceMethods = ElementMatchers.none();
        for (var method : contractInfo.getContractClass().getDeclaredMethods()) {
            allInterfaceMethods = allInterfaceMethods.or(ElementMatchers.is(method));
        }

        return allInterfaceMethods.and(
                ElementMatchers.not(
                        getMethodElementMatcher(contractInfo)
                                .or(getEventsElementMatcher(contractInfo))));
    }
}
