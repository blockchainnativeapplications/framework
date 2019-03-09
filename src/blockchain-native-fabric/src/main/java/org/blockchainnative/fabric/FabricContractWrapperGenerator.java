package org.blockchainnative.fabric;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.ExceptionMethod;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.blockchainnative.ContractWrapperGenerator;
import org.blockchainnative.SmartContract;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.exceptions.ContractWrapperCreationException;
import org.blockchainnative.fabric.metadata.FabricContractInfo;
import org.blockchainnative.fabric.typeconverters.FabricDefaultTypeConverters;
import org.blockchainnative.metadata.ContractInfo;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Generates wrapper classes for Hyperledger Fabric chaincodes. <br>
 *
 * <h2>Supported Types</h2>
 *
 * <p>
 * All parameters are passed/retrieved as UTF-8 encoded Strings to/from the chaincode. <br> Java Strings are
 * automatically converted by the wrapper classes, for every other type instance of {@link
 * org.blockchainnative.convert.TypeConverter} is required. <br>
 * However, {@link FabricDefaultTypeConverters#getConverters()} provides type converters for the following types:
 * </p>
 * <ul>
 *     <li>boolean</li>
 *     <li>byte</li>
 *     <li>short</li>
 *     <li>int</li>
 *     <li>long</li>
 *     <li>float</li>
 *     <li>double</li>
 * </ul>
 *
 * <h2>Special Arguments</h2>
 * <p>
 * All contract methods can handle the following special arguments.
 * </p>
 *
 * <h3>Target Peers</h3>
 * Specifies the peers targeted by a method. <br> A parameter declared as such needs to be an {@code Collection} of
 * {@code String} containing the peer names as defined in the network configuration. <br> Target peers can be defined
 * via {@link FabricContractInfo#setTargetPeerNames(Set)} or via a special argument ({@link
 * Constants#TARGET_PEERS_ARGUMENT}). If no target peers are set, every method will target all peers known to the
 * channel.
 *
 * <h3>User</h3>
 * Specifies the user context to be used by a method. <br> A parameter declared as such needs to be of type {@code
 * org.hyperledger.fabric.sdk.User}.
 *
 * <h2>Special Methods</h2>
 *
 * <h3>Install</h3>
 * A method marked as special method and named <i>'install'</i> ({@link Constants#INSTALL_METHOD}) installs the
 * Hyperledger Fabric chaincode on the target peers.  <br> In order to be able to install the chaincode on the target
 * peers and a user ({@link Constants#USER_ARGUMENT}) with the correct permissions is required. Moreover, the following
 * properties of the corresponding contract info need to be set: {@link FabricContractInfo#chaincodeSourceDirectory} and
 * {@link FabricContractInfo#chaincodeLanguage}. <br>
 * The return type of an install method needs to be void.
 *
 * <h3>Instantiate</h3>
 * A method marked as special method and named <i>'instantiate'</i> ({@link Constants#INSTANTIATE_METHOD}) instantiates
 * the Hyperledger Fabric chaincode on the target peers and submits a transaction to the orderer.  <br> In order to be
 * able to instantiate the chaincode on the target peers and a user ({@link Constants#USER_ARGUMENT}) with the correct
 * permissions is required. Moreover, the following properties of the corresponding contract info need to be set: {@link
 * FabricContractInfo#chaincodeSourceDirectory}, {@link FabricContractInfo#chaincodePolicy} and {@link
 * FabricContractInfo#chaincodeLanguage}. <br> All parameters not marked as special arguments are converted and passed to the
 * constructor. <br>
 * The return type of an instantiate method needs to be void.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class FabricContractWrapperGenerator implements ContractWrapperGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricContractWrapperGenerator.class);

    private final Supplier<HFClient> clientFactory;
    private final Function<HFClient, Channel> channelFactory;
    private final TypeConverters typeConverters;

    /** Initializes a new {@code FabricContractWrapperGenerator}.
     * <p>
     * For each contract wrapper, a new instances of {@code HFClient} and {@code Channel} are retrieved via its factories. <br>
     * It is possible for those factories to always return the same instance.
     * </p>
     *
     * @param clientFactory factory producing {@code HFClient} instances for communicating with the Hyperledger Fabric network.
     * @param channelFactory factory producing {@code Channel} instances for authorizing transactions. An instance of {@code HFClient} produced via {@code clientFactory} is passed to this function.
     * @param typeConverters additional {@code TypeConverter}, may be null
     */
    public FabricContractWrapperGenerator(Supplier<HFClient> clientFactory, Function<HFClient, Channel> channelFactory, TypeConverters typeConverters) {
        if(clientFactory == null) throw new IllegalArgumentException("clientFactory must not be null");
        this.clientFactory = clientFactory;

        if(channelFactory == null) throw new IllegalArgumentException("channelFactory must not be null");
        this.channelFactory = channelFactory;

        this.typeConverters = typeConverters == null ? new TypeConverters() : typeConverters;
    }

    /**
     * Generates a new wrapper for the smart contract described by {@code contractInfo}. <br>
     * Although defined otherwise by the interface, {@code contractInfo} needs to be of type {@link FabricContractInfo}.
     *
     * @param contractInfo    contract info representing the chaincode
     * @param <TContractInfo> type of the contract info, required to be {@link FabricContractInfo}
     * @param <TContractType> Java interface declaring the chaincode methods and events
     * @return implementation of the given smart contract interface {@code TContractType}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <TContractInfo extends ContractInfo<TContractType, ?, ?>, TContractType> TContractType generate(TContractInfo contractInfo) {
        if (contractInfo == null) throw new IllegalArgumentException("contractClass must not be null!");
        if (!(contractInfo instanceof FabricContractInfo))
            throw new IllegalArgumentException(String.format("%s requires an instance of %s to generate contract wrappers.", this.getClass().getSimpleName(), FabricContractInfo.class.getSimpleName()));

        // this is safe, contractInfo is an instance of FabricContractInfo,
        // Moreover TContractType is ensured by the generic constraint
        var fabricContractInfo = (FabricContractInfo<TContractType>) contractInfo;

        var contractClass = fabricContractInfo.getContractClass();
        if (!contractClass.isInterface()) throw new IllegalArgumentException("contractClass needs to be an interface!");

        LOGGER.info("Creating smart contract wrapper for contract '{}' ({})", contractInfo.getIdentifier(), contractClass.getName());

        var client = clientFactory.get();
        var channel = channelFactory.apply(client);

        var base = new FabricContractWrapper(fabricContractInfo, client, channel, typeConverters);

        var methodsByNameMatcher = getMethodElementMatcher(fabricContractInfo);
        var eventsByNameMatcher = getEventsElementMatcher(fabricContractInfo);
        var remainingMethodsMatcher = getRemainingMethodsElementMatcher(fabricContractInfo);

        TContractType wrapper;
        try {
            var interceptionMethodName = FabricContractWrapper.class.getMethod("intercept", Method.class, Object[].class).getName();

            wrapper = new ByteBuddy()
                    .subclass(contractClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                    .name(getWrapperName(contractInfo))
                    .method(methodsByNameMatcher)
                    .intercept(MethodDelegation.to(base, FabricContractWrapper.class, interceptionMethodName))
                    .method(eventsByNameMatcher)
                    .intercept(MethodDelegation.to(base, FabricContractWrapper.class, interceptionMethodName))
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
            throw new ContractWrapperCreationException(message, e);
        }
    }

    private ElementMatcher.Junction<? super MethodDescription> getMethodElementMatcher(FabricContractInfo<?> contractInfo) {
        ElementMatcher.Junction<? super MethodDescription> methodsByNameMatcher = ElementMatchers.none();
        for (var methodInfo : contractInfo.getMethodInfos().values()) {
            methodsByNameMatcher = methodsByNameMatcher.or(ElementMatchers.named(methodInfo.getMethod().getName()));
        }

        if(Arrays.stream(contractInfo.getContractClass().getMethods())
                .anyMatch(method -> SmartContract.GET_CONTRACT_INFO_METHOD_NAME.equals(method.getName()))) {
            methodsByNameMatcher = methodsByNameMatcher.or(ElementMatchers.named(SmartContract.GET_CONTRACT_INFO_METHOD_NAME));
        }

        return methodsByNameMatcher;
    }

    private ElementMatcher.Junction<? super MethodDescription> getEventsElementMatcher(FabricContractInfo<?> contractInfo) {
        ElementMatcher.Junction<? super MethodDescription> eventsByNameMatcher = ElementMatchers.none();
        for (var eventInfo : contractInfo.getEventInfos().values()) {
            eventsByNameMatcher = eventsByNameMatcher.or(ElementMatchers.named(eventInfo.getMethod().getName()));
        }
        return eventsByNameMatcher;
    }

    private ElementMatcher.Junction<? super MethodDescription> getRemainingMethodsElementMatcher(FabricContractInfo<?> contractInfo) {
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