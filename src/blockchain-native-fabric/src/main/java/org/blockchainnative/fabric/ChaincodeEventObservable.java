package org.blockchainnative.fabric;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.blockchainnative.metadata.Event;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents an observable of Hyperledger Fabric chaincode events. <br>
 * <br>
 * An event listener is registered once the first {@code Observer} subscribes to it and unregistered when the last
 * {@code Observer} is disposed.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.fabric.metadata.FabricEventInfo
 * @since 1.0
 */
public class ChaincodeEventObservable extends Observable<Event<String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChaincodeEventObservable.class);
    private final Set<Observer<? super Event<String>>> observers;
    private final HFClient client;
    private final Channel channel;
    private final Pattern chainCodeIdPattern;
    private final Pattern eventNamePattern;
    private String eventHandle;

    /**
     * Creates a new {@code ChaincodeEventObservable}.
     *
     * @param client Client to interact with the Hyperledger Fabric network
     * @param channel Hyperledger channel used
     * @param chainCodeIdPattern regex pattern identifying the chaincode identifier which emits the desired event
     * @param eventNamePattern regex pattern identifying the desired event name
     */
    public ChaincodeEventObservable(HFClient client, Channel channel, Pattern chainCodeIdPattern, Pattern eventNamePattern) {
        this.client = client;
        this.observers = Collections.synchronizedSet(new HashSet<>());
        this.channel = channel;
        this.chainCodeIdPattern = chainCodeIdPattern;
        this.eventNamePattern = eventNamePattern;
    }

    private synchronized void removeObserver(Observer<? super Event<String>> observer) {
        LOGGER.debug("Removing observer '{}'", observer);
        this.observers.remove(observer);
        if (this.observers.isEmpty()) {
            unregisterEventListener();
        }
    }

    private synchronized void addObserver(Observer<? super Event<String>> observer) {
        LOGGER.debug("Adding observer '{}'", observer);
        this.observers.add(observer);
        if (this.observers.size() == 1) {
            registerEventListener();
        }
    }

    private void registerEventListener() {
        try {
            this.eventHandle = this.channel.registerChaincodeEventListener(
                    chainCodeIdPattern,
                    eventNamePattern,
                    (handle, blockEvent, chaincodeEvent) -> {
                        var stringPayload = new String(chaincodeEvent.getPayload(), StandardCharsets.UTF_8);
                        var event = new Event<>(stringPayload, FabricUtil.getBlockHashFromEvent(client, blockEvent), chaincodeEvent.getTxId());

                        onNext(event);
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to register event listener", e);
            onError(e);
        }
    }

    private void unregisterEventListener() {
        try {
            this.channel.unregisterChaincodeEventListener(this.eventHandle);
        } catch (InvalidArgumentException e) {
            LOGGER.error("Failed to unregister event listener with event handle '{}'", eventHandle, e);
            onError(e);
        }
    }

    private void onNext(Event<String> o) {
        for (var observer : this.observers) {
            try {
                observer.onNext(o);
            } catch (Exception e) {
                LOGGER.error("Failed to transmit event to observer '{}'", observer, e);
                removeObserver(observer);
            }
        }
    }

    private void onError(Throwable e) {
        for (var observer : this.observers) {
            try {
                observer.onError(e);
            } catch (Exception e2) {
                LOGGER.error("Failed to transmit exception to observer '{}'", observer, e2);
                removeObserver(observer);
            }
        }
        this.unregisterEventListener();
        this.observers.clear();
    }

    @Override
    protected void subscribeActual(Observer<? super Event<String>> observer) {
        var disposable = new ChaincodeEventObservableDisposable(this, observer);
        observer.onSubscribe(disposable);

        addObserver(observer);

    }

    private static final class ChaincodeEventObservableDisposable implements Disposable {
        private final ChaincodeEventObservable parent;
        private final Observer<? super Event<String>> observer;
        private volatile boolean disposed;

        ChaincodeEventObservableDisposable(ChaincodeEventObservable parent, Observer<? super Event<String>> observer) {
            this.parent = parent;
            this.observer = observer;
        }

        @Override
        public void dispose() {
            this.disposed = true;
            parent.removeObserver(observer);
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
