package org.blockchainnative.ethereum.transactions;

import io.reactivex.Observable;
import org.blockchainnative.transactions.Blockchain;
import org.blockchainnative.transactions.exceptions.BlockchainException;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author Matthias Veit
 */
public abstract class EthereumBaseBlockchain<TBlock extends EthereumBaseBlock<TTransaction>, TTransaction extends EthereumBaseTransaction> implements Blockchain<TBlock, TTransaction> {
    protected Web3j web3j;

    public EthereumBaseBlockchain(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public TBlock getBlockByNumber(BigInteger blockNumber) {

        if(blockNumber == null || blockNumber.compareTo(BigInteger.ZERO) < 0){
            throw new BlockchainException(String.format("Failed to retrieve block '%s': Invalid block number", blockNumber));
        }

        try {
            var request = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
                    .send();

            if(request.hasError()){
                throw new BlockchainException(String.format("Failed to retrieve block '%s': %s", blockNumber, request.getError().getMessage()));
            }

            if(request.getBlock() == null){
                throw new BlockchainException(String.format("Failed to retrieve block '%s': Block not found", blockNumber));
            }

            return mapBlock(request.getBlock());
        } catch (IOException e) {
            throw new BlockchainException(e);
        }
    }

    @Override
    public TBlock getBlockByHash(String blockHash) {
        try {
            var request = web3j.ethGetBlockByHash(blockHash, true)
                    .send();

            if(request.hasError()){
                throw new BlockchainException(String.format("Failed to retrieve block '%s': %s", blockHash, request.getError().getMessage()));
            }

            if(request.getBlock() == null){
                throw new BlockchainException(String.format("Failed to retrieve block '%s': Block not found", blockHash));
            }

            return mapBlock(request.getBlock());
        } catch (IOException e) {
            throw new BlockchainException(e);
        }
    }

    @Override
    public TBlock getLatestBlock() {
        try {
            var request = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, true)
                    .send();

            if(request.hasError()){
                throw new BlockchainException(String.format("Failed to retrieve latest block: %s", request.getError().getMessage()));
            }

            if(request.getBlock() == null){
                throw new BlockchainException("Failed to retrieve latest block: Block not found");
            }

            return mapBlock(request.getBlock());
        } catch (IOException e) {
            throw new BlockchainException(e);
        }
    }

    @Override
    public TTransaction getTransactionByHash(String transactionHash) {
        try {
            var request = web3j.ethGetTransactionByHash(transactionHash)
                    .send();

            if(request.hasError()){
                throw new BlockchainException(String.format("Failed to retrieve transaction '%s': %s", transactionHash, request.getError().getMessage()));
            }

            var transaction = request.getTransaction();

            if(transaction.isPresent()) {
                return mapTransaction(transaction.get());
            }else {
                throw new BlockchainException(String.format("Failed to retrieve transaction '%s': Transaction not found", transactionHash));
            }
        } catch (IOException e) {
            throw new BlockchainException(e);
        }
    }

    @Override
    public Observable<TBlock> getBlockObservable() {
        return web3j.blockFlowable(true).toObservable().map(ethBlock -> mapBlock(ethBlock.getBlock()));
    }

    @Override
    public Observable<TTransaction> getTransactionObservable() {
        return web3j.transactionFlowable().toObservable().map(ethTransaction -> mapTransaction(ethTransaction));
    }

    protected abstract TBlock newBlock();

    protected abstract TTransaction newTransaction();

    protected TBlock mapBlock(EthBlock.Block ethBlock) {
        var block = newBlock();

        block.setBlockNumber(ethBlock.getNumber());
        block.setBlockHash(ethBlock.getHash());
        block.setTimestamp(
                Instant.ofEpochMilli(ethBlock.getTimestamp().longValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());

        var transactions = ethBlock.getTransactions()
                .stream()
                .map(transactionResult -> {
                    var payload = transactionResult.get();
                    if (!(payload instanceof EthBlock.TransactionObject)) {
                        throw new IllegalStateException(String.format("Unexpected payload returned from JSON RPC, full transaction object (%s) is required but got '%s'.", EthBlock.TransactionObject.class.getName(), payload != null ? payload.getClass().getName() : "<null>"));
                    }
                    return (EthBlock.TransactionObject) payload;
                })
                .sorted(Comparator.comparing(Transaction::getTransactionIndex))
                .map(this::mapTransaction)

                .map(t -> {
                    t.setTimestamp(block.getTimestamp());
                    return t;
                })
                .collect(Collectors.toList());

        block.setTransactions(transactions);

        return block;
    }

    protected TTransaction mapTransaction(Transaction ethTransaction) {
        var transaction = newTransaction();

        transaction.setBlockNumber(ethTransaction.getBlockNumber());
        transaction.setBlockHash(ethTransaction.getBlockHash());
        transaction.setHash(ethTransaction.getHash());
        transaction.setSender(ethTransaction.getFrom());
        transaction.setRecipient(ethTransaction.getTo());
        transaction.setValue(ethTransaction.getValue());

        var input = ethTransaction.getInput();
        var decoded = decodeHexDataString(input);
        if(decoded != null){
            transaction.setData(decoded);
        }

        return transaction;
    }

    protected byte[] decodeHexDataString(String hexString){
        if(hexString != null && hexString.startsWith("0x") && hexString.length() > 2){
            return Hex.decode(hexString.substring(2));
        }
        return null;
    }
}
