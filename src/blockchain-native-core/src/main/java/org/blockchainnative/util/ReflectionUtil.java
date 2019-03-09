package org.blockchainnative.util;

import io.reactivex.Observable;
import org.apache.commons.lang3.ClassUtils;
import org.blockchainnative.annotations.ContractEvent;
import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.metadata.Event;
import org.blockchainnative.metadata.Result;

import java.lang.reflect.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Provides static utility methods for extracting information from smart contract interfaces via reflection.
 * <br>
 * The class is not intended to be instantiated as it only provides static methods.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.annotations.SmartContract
 * @see org.blockchainnative.annotations.ContractMethod
 * @see org.blockchainnative.annotations.ContractEvent
 * @since 1.0
 */
public final class ReflectionUtil {

    private ReflectionUtil() {
    }

    /**
     * Checks if the return type of the given method is {@code Void.TYPE} or not.
     *
     * @param method a method
     * @return {@code true} if the return type of the given method is {@code Void.TYPE}, {@code false} otherwise.
     */
    public static boolean isVoidMethod(Method method) {
        return ClassUtils.isAssignable(Void.TYPE, method.getReturnType());
    }

    /**
     * Checks whether the given contract method's return type is wrapped in {@link java.util.concurrent.Future} or {@link java.util.concurrent.CompletableFuture}.
     *
     * @param method contract method
     * @return {@code true} if the return type of the given method is wrapped in {@link java.util.concurrent.Future} or {@link java.util.concurrent.CompletableFuture} {@code false} otherwise.
     */
    public static boolean isAsyncReturnType(Method method) {
        var returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType) {
            var genericReturnType = (ParameterizedType) returnType;
            if (genericReturnType.getRawType() instanceof Class) {
                return isFuture(genericReturnType);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns a type representing the contract method's return type after unwrapping {@link java.util.concurrent.Future}
     * or {@link java.util.concurrent.CompletableFuture}, and {@link org.blockchainnative.metadata.Result}.
     * <p>
     * This type is considered the actual return type of a method annotated with {@link org.blockchainnative.annotations.ContractMethod}
     *
     * @param method contract method
     * @return actual return type of the given contract method
     */
    public static Type getActualReturnType(Method method) {
        var type = method.getGenericReturnType();
        // If the return type is Future<?> or CompletableFuture<?>, genericReturnType is a ParameterizedType
        if (type instanceof ParameterizedType && isFuture((ParameterizedType) type)) {
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        // strip Result<T> if present
        if (type instanceof ParameterizedType && isResult((ParameterizedType) type)) {
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        return type;
    }

    /**
     * Checks whether the given contract method's actual return type is wrapped in {@link org.blockchainnative.metadata.Result}.
     *
     * @param method contract method
     * @return {@code true} if the return type of the given method is wrapped in {@link org.blockchainnative.metadata.Result}, {@code false} otherwise.
     * @see org.blockchainnative.util.ReflectionUtil#getActualReturnType(Method)
     */
    public static boolean usesResultWrapper(Method method) {
        var type = method.getGenericReturnType();

        // strip Future<T> if present
        if (type instanceof ParameterizedType && isFuture((ParameterizedType) type)) {
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        // check whether or not the event's type is wrapped in Event<T>
        return type instanceof ParameterizedType && isResult((ParameterizedType) type);
    }

    /**
     * Extracts the actual event type of a smart contract event method by unwrapping {@link Observable} and {@link Event}.
     *
     * @param method contract event method
     * @return Class representing the actual event type.
     */
    public static Class<?> getEventType(Method method) {
        var type = method.getGenericReturnType();

        // strip Observable<T> if present
        if (type instanceof ParameterizedType && isObservable((ParameterizedType) type)) {
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        // strip Event<T> if present
        if (type instanceof ParameterizedType && isEvent((ParameterizedType) type)) {
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
        }
        if (type instanceof Class) {
            return (Class<?>) type;
        } else {
            throw new IllegalStateException("Unexpected event type " + type);
        }

    }

    /**
     * Checks if the given event method is using the type {@link org.blockchainnative.metadata.Event} to wrap the actual event object.
     *
     * @param method {@code Method} to check. Must not be null.
     * @return {@code true} if the given event method is using the type {@link org.blockchainnative.metadata.Event} to wrap the actual event object, {@code false} otherwise.
     */
    public static boolean usesEventWrapper(Method method) {
        var type = method.getGenericReturnType();

        // strip Observable<T> if present
        if (type instanceof ParameterizedType && isObservable((ParameterizedType) type)) {
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        // check whether or not the event's type is wrapped in Event<T>
        return type instanceof ParameterizedType && isEvent((ParameterizedType) type);
    }

    /**
     * Returns the index of the given parameter as it occurs in the parameter list of the given executable.
     *
     * @param executable {@code Method} or {@code Constructor} containing the parameter. Must not be null.
     * @param parameter  {@code Parameter} to find the index for. Must not be null.
     * @return the parameter index or -1 if the parameter was not found on the given executable.
     */
    public static int getParameterIndex(Executable executable, Parameter parameter) {
        var parameters = executable.getParameters();
        var index = -1;
        for (var i = 0; i < parameters.length; i++) {
            if (parameters[i].equals(parameter)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private static boolean isFuture(ParameterizedType type) {
        return Future.class.equals(type.getRawType()) || CompletableFuture.class.equals(type.getRawType());
    }

    private static boolean isObservable(ParameterizedType type) {
        return Observable.class.equals(type.getRawType());
    }

    private static boolean isResult(ParameterizedType type) {
        return Result.class.equals(type.getRawType());
    }

    private static boolean isEvent(ParameterizedType type) {
        return Event.class.equals(type.getRawType());
    }

}
