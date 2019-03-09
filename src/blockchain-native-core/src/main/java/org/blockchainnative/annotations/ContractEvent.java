package org.blockchainnative.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a method to be mapped the corresponding event of a smart contract. <br>
 * <br>
 * Depending on the blockchain in use, a smart contract event (if events are supported at all) can consist one or multiple values. <br>
 * In order to function correctly, a method annotated with {@code ContractEvent} must obey to the following rules:
 *
 * <ul>
 *      <li>The event method needs to return {@link io.reactivex.Observable}</li>
 *      <li>An non-generic event type needs to be specified which contains a field for each value in the corresponding event.</li>
 *      <li>The {@code Observable}'s generic parameter is either directly the event type or the event type wrapped in {@link org.blockchainnative.metadata.Event} </li>
 *      <li>Event methods can accept parameters, however all of them are considered to be special arguments that need to be interpreted by the underlying provider (see {@link EventParameter})</li>
 * </ul>
 * <br>
 * <br>
 * The following need to be considered about event types:
 * <ul>
 *      <li>The type needs to define a public parameterless constructor.</li>
 *      <li>The event type's fields are either assigned through their setters if possible, or directly via reflection.</li>
 * </ul>
 * <br>
 * Consider the following example of an event with argument, both methods are valid to be used {@code ContractEvent}:
 *
 * <pre>
 * {@code
 * public interface EventSample {
 *
 *      Observable<HelloEvent> onHelloEvent();
 *
 *      Observable<Event<HelloEvent>> onHelloEventWrapped();
 *
 *      class HelloEvent {
 *          public String greeter;
 *      }
 * }
 * }
 * </pre>
 *
 * @author Matthias Veit
 * @see org.blockchainnative.ContractWrapperGenerator
 * @see org.blockchainnative.metadata.Event
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ContractEvent {

    /**
     * Explicitly specifies the name of the event in the associated smart contract.
     * If no name is specified (i.e. an empty string) the name of the method to which this annotation is applied is used.
     *
     * @return name of the event in the associated smart contract.
     */
    String value() default "";
}
