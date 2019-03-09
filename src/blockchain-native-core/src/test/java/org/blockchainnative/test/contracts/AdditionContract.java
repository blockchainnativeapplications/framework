package org.blockchainnative.test.contracts;

import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.annotations.SmartContract;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface AdditionContract {

    @ContractMethod
    int add(int x, int y);

    @ContractMethod("add")
    void addButIgnoreResult(int x, int y);
}
