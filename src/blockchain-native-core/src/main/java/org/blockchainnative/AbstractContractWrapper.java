package org.blockchainnative;

import io.reactivex.Observable;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.blockchainnative.exceptions.ContractCallException;
import org.blockchainnative.metadata.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Base class for smart contract wrappers. <br>
 * <br>
 * Using this class is optional. However, it provides many potentially useful functions found in smart contract wrappers.
 *
 * @param <TContractInfo>       Concrete type of the {@link ContractInfo} expected by this contract wrapper
 * @param <TMethodInfo>         Concrete type of the {@link MethodInfo} used by {@code TContractInfo}
 * @param <TParameterInfo>      Concrete type of the {@link ParameterInfo} used by {@code TContractInfo}
 * @param <TEventInfo>          Concrete type of the {@link EventInfo} used by {@code TContractInfo}
 * @param <TEventFieldInfo>     Concrete type of the {@link EventFieldInfo} used by {@code TContractInfo}
 * @param <TEventParameterInfo> Concrete type of the {@link EventParameterInfo} used by {@code TContractInfo}
 * @author Matthias Veit
 * @since 1.0
 */
public abstract class AbstractContractWrapper<TContractInfo extends ContractInfo<?, TMethodInfo, TEventInfo>,
        TMethodInfo extends MethodInfo<TParameterInfo>,
        TParameterInfo extends ParameterInfo,
        TEventInfo extends EventInfo<TEventFieldInfo, TEventParameterInfo>,
        TEventFieldInfo extends EventFieldInfo,
        TEventParameterInfo extends EventParameterInfo> {
    protected final TContractInfo contractInfo;

    /**
     * Creates a new instance of the contract wrapper
     *
     * @param contractInfo contract info describing the smart contract to be wrapped
     */
    public AbstractContractWrapper(TContractInfo contractInfo) {
        this.contractInfo = contractInfo;
    }

    /**
     * Returns the contract info of the wrapper.
     *
     * @return contract info of the wrapper.
     */
    public TContractInfo getContractInfo(){
        return contractInfo;
    }

    /**
     * Extracts a special argument from a contract method.
     *
     * @param methodInfo {@code MethodInfo} describing the contract method.
     * @param name       name of the special argument to be extracted.
     * @param arguments  arguments passed to the contract method.
     * @return value of the special argument or null if no matching special arguemnt has been registered in the method info
     */
    protected Object extractSpecialArgument(TMethodInfo methodInfo, String name, Object[] arguments) {
        for (var i = 0; i < arguments.length; i++) {
            var parameterInfo = methodInfo.getParameterInfos().get(i);
            if (parameterInfo.isSpecialArgument() && name.equalsIgnoreCase(parameterInfo.getSpecialArgumentName())) {
                return arguments[i];
            }
        }
        return null;
    }

    /**
     * Extracts a special argument from an event method.
     *
     * @param eventInfo {@code EventInfo} describing the event method.
     * @param name      name of the special argument to be extracted.
     * @param arguments arguments passed to the event method.
     * @return value of the special argument or null if no matching special arguemnt has been registered in the event info
     */
    protected Object extractSpecialArgument(TEventInfo eventInfo, String name, Object[] arguments) {
        for (var i = 0; i < arguments.length; i++) {
            var parameterInfo = eventInfo.getEventParameterInfos().get(i);
            if (name.equalsIgnoreCase(parameterInfo.getSpecialArgumentName())) {
                return arguments[i];
            }
        }
        return null;
    }

    /**
     * Extracts the arguments that need to be passed to the corresponding smart contract method, i.e. the arguments which are not marked as special arguments.
     *
     * @param methodInfo {@code MethodInfo} describing the contract method
     * @param arguments  arguments passed to contract method
     * @return array containing only the arguments that need to be passed to the corresponding smart contract method
     */
    protected Object[] extractContractMethodParameters(TMethodInfo methodInfo, Object[] arguments) {
        var relevantArguments = new ArrayList<>();
        for (var i = 0; i < arguments.length; i++) {
            if (!methodInfo.getParameterInfos().get(i).isSpecialArgument()) {
                relevantArguments.add(arguments[i]);
            }
        }
        return relevantArguments.toArray();
    }

    /**
     * Extracts the parameter info objects that correspond to the arguments that need to be passed to the smart contract method, i.e. the parameter info objects that describe the arguments which are not marked as special arguments.
     *
     * @param methodInfo {@code MethodInfo} describing the contract method
     * @return array containing only the parameter info objects that correspond to the arguments that need to be passed to the smart contract method
     */
    protected List<TParameterInfo> extractContractMethodParameterInfos(TMethodInfo methodInfo) {
        return methodInfo.getParameterInfos().stream()
                .filter(p -> !p.isSpecialArgument())
                .collect(Collectors.toList());
    }

    /**
     * Declares the delegates to be executed when a method marked as special method is called.
     *
     * @return Mapping of special method names to {@code SpecialMethodDelegate}
     */
    protected abstract Map<String, SpecialMethodDelegate<TMethodInfo>> getSpecialMethods();

    /**
     * Interceptor to be called when a method of the smart contract interface {@code TContractInfo} is invoked.
     *
     * @param method    smart contract interface method which has been invoked
     * @param arguments arguments of the smart contract interface method
     * @return result of the smart contract method converted to the correct declared return type
     */
    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] arguments) {

        if(SmartContract.GET_CONTRACT_INFO_METHOD_NAME.equals(method.getName())){
            if(TypeUtils.isInstance(contractInfo, method.getGenericReturnType())) {
                return this.getContractInfo();
            } else {
                throw new IllegalStateException(String.format("Method '%s' is declared with an invalid return type. It needs to return '%s' or one of its super types.", method.getName(), contractInfo.getClass().getName()));
            }
        }

        var methodInfo = contractInfo.getMethodInfo(method);
        if (methodInfo == null) {
            var eventInfo = contractInfo.getEventInfo(method);
            if (eventInfo == null) {
                throw new IllegalStateException(String.format("No method or event information registered for method '%s' in contract info of contract '%s' (%s)", method.getName(), contractInfo.getIdentifier(), contractInfo.getClass()));
            } else {
                return createEventObservable(eventInfo, arguments);
            }
        } else {
            return invoke(methodInfo, arguments);
        }
    }

    /**
     * Utility method for calling smart contract methods and special methods.
     *
     * @param methodInfo {@code MethodInfo} describing the corresponding smart contract method.
     * @param arguments  arguments of the smart contract interface method
     * @return actual smart contract method result converted to the expected type
     * @see org.blockchainnative.AbstractContractWrapper#intercept(Method, Object[])
     * @see org.blockchainnative.AbstractContractWrapper#invokeMethod(MethodInfo, Object[])
     * @see org.blockchainnative.AbstractContractWrapper#invokeReadOnlyMethod(MethodInfo, Object[])
     * @see org.blockchainnative.AbstractContractWrapper#getMethodReturnValue(MethodInfo, Future)
     */
    protected Object invoke(TMethodInfo methodInfo, Object[] arguments) {
        Future<?> future;
        if (methodInfo.isSpecialMethod()) {
            var specialMethods = getSpecialMethods();
            var methodName = methodInfo.getContractMethodName();
            if (specialMethods.containsKey(methodName)) {
                future = specialMethods.get(methodName).invoke(methodInfo, arguments);
            } else {
                throw new IllegalStateException(String.format("Unexpected special method '%s' found!", methodInfo.getContractMethodName()));
            }

        } else {
            if (methodInfo.isReadOnly()) {
                future = invokeReadOnlyMethod(methodInfo, arguments);
            } else {
                future = invokeMethod(methodInfo, arguments);
            }
        }

        return getMethodReturnValue(methodInfo, future);
    }

    /**
     * Excutes the given {@link java.util.concurrent.Future} containing the expected method result or directly returns it depending on the smart contrac method's return type..
     *
     * @param methodInfo methodInfo {@code MethodInfo} describing the corresponding smart contract method.
     * @param future     {@code Future} containing the smart contract method result
     * @return actual smart contract method result converted to the expected type
     */
    protected Object getMethodReturnValue(TMethodInfo methodInfo, Future<?> future) {
        if (methodInfo.isAsync()) {
            return future;
        } else {
            Object result;
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new ContractCallException(e);
            }
            if (methodInfo.isVoidReturnType()) {
                return Void.TYPE;
            } else {
                return result;
            }
        }
    }

    /**
     * Calling the actual smart contract method and converts its result to the expected type.
     *
     * @param methodInfo {@code MethodInfo} describing the corresponding smart contract method.
     * @param arguments  arguments of the smart contract interface method
     * @return actual smart contract method result converted to the expected type and wrapped as {@link java.util.concurrent.Future}
     */
    protected abstract Future<Object> invokeMethod(TMethodInfo methodInfo, Object[] arguments);

    /**
     * Calling the actual smart contract method without modifying the blockchain's state and converts its result to the expected type.
     *
     * @param methodInfo {@code MethodInfo} describing the corresponding smart contract method.
     * @param arguments  arguments of the smart contract interface method
     * @return actual smart contract method result converted to the expected type and wrapped as {@link java.util.concurrent.Future}
     */
    protected abstract Future<Object> invokeReadOnlyMethod(TMethodInfo methodInfo, Object[] arguments);

    /**
     * Creates an {@link io.reactivex.Observable} containing the events emitted by the smart contract
     *
     * @param eventInfo {@code EventInfo} describing the corresponding smart contract event.
     * @param arguments arguments of the smart contract interface method
     * @return observable containing the events emitted by the smart contract converted to the expected type
     */
    protected abstract Observable<Object> createEventObservable(TEventInfo eventInfo, Object[] arguments);
}
