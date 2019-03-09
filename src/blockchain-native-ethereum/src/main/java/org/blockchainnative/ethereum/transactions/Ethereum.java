package org.blockchainnative.ethereum.transactions;

import org.web3j.protocol.Web3j;

/**
 * @author Matthias Veit
 */
public class Ethereum extends EthereumBaseBlockchain<EthereumBlock, EthereumTransaction> {

    public Ethereum(Web3j web3j) {
        super(web3j);
    }

    @Override
    protected EthereumBlock newBlock() {
        return new EthereumBlock();
    }

    @Override
    protected EthereumTransaction newTransaction() {
        return new EthereumTransaction();
    }
}
