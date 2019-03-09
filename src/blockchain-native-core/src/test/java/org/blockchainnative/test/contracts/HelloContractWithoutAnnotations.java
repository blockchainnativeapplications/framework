package org.blockchainnative.test.contracts;

import java.util.concurrent.Future;

/**
 * @author Matthias Veit
 */
public interface HelloContractWithoutAnnotations {

    String hello(String name);

    Future<String> helloAsync(String name);

    Future<String> helloReadOnly(String name);
}
