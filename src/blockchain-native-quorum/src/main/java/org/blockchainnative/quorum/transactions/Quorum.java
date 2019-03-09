package org.blockchainnative.quorum.transactions;

import org.blockchainnative.ethereum.transactions.EthereumBaseBlockchain;
import org.blockchainnative.transactions.exceptions.TransactionException;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Transaction;

import java.io.IOException;

/**
 * Quorum blockchain interface
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class Quorum extends EthereumBaseBlockchain<QuorumBlock, QuorumTransaction> {
    private final org.web3j.quorum.Quorum quorum;

    public Quorum(org.web3j.quorum.Quorum quorum) {
        super(quorum);
        this.quorum = quorum;
    }

    public Quorum(Web3j web3j) {
        super(web3j);
        if(!(web3j instanceof org.web3j.quorum.Quorum)){
            throw new IllegalArgumentException("An instance of org.web3j.quorum.Quorum is required!");
        }
        this.quorum = (org.web3j.quorum.Quorum)web3j;
    }

    @Override
    protected QuorumBlock newBlock() {
        return new QuorumBlock();
    }

    @Override
    protected QuorumTransaction newTransaction() {
        return new QuorumTransaction();
    }

    @Override
    protected QuorumTransaction mapTransaction(Transaction ethTransaction) {
        var transaction = new QuorumTransaction();

        transaction.setBlockNumber(ethTransaction.getBlockNumber());
        transaction.setBlockHash(ethTransaction.getBlockHash());
        transaction.setHash(ethTransaction.getHash());
        transaction.setSender(ethTransaction.getFrom());
        transaction.setRecipient(ethTransaction.getTo());
        transaction.setValue(ethTransaction.getValue());
        transaction.setPrivate(isPrivateTransaction(ethTransaction)); // set by quorum to indicate private transactions

        var input = ethTransaction.getInput();
        if(transaction.isPrivate()){
            transaction.setPayloadHash(input);
            try {
                var privatePayload = quorum.quorumGetPrivatePayload(input).send();
                if (privatePayload.hasError()) {
                    throw new TransactionException(
                            String.format("Failed to retrieve private data for transaction '%s': %s",
                                    transaction.getHash(),
                                    privatePayload.getError().getMessage()));
                }

                var payload = privatePayload.getPrivatePayload();
                var decoded = decodeHexDataString(payload);
                if(decoded != null){
                    transaction.setData(decoded);
                }

            }catch (IOException e){
                throw new TransactionException(
                        String.format("Failed to retrieve private data for transaction '%s': %s",
                                transaction.getHash(),
                                e.getMessage()), e);
            }
        } else {
            var decoded = decodeHexDataString(input);
            if(decoded != null){
                transaction.setData(decoded);
            }
        }

        return transaction;
    }

    private boolean isPrivateTransaction(Transaction ethTransaction){
        return ethTransaction.getV() == 37 || ethTransaction.getV() == 38;
    }
}
