package org.blockchainnative.test.contracts;

import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.annotations.SmartContract;

import java.util.concurrent.Future;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface HelloContract {

    @ContractMethod
    String hello(String name);

    @ContractMethod("hello")
    Future<String> helloAsync(String name);

    @ContractMethod(value = "hello", isReadOnly = true)
    Future<String> helloReadOnly(String name);
}
