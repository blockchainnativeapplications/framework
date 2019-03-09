package org.blockchainnative.fabric;

import io.reactivex.Observable;
import org.blockchainnative.exceptions.ContractCallException;
import org.blockchainnative.exceptions.ContractDeploymentException;
import org.blockchainnative.fabric.metadata.ChaincodeLanguage;
import org.blockchainnative.fabric.metadata.FabricContractInfo;
import org.blockchainnative.metadata.Event;
import org.blockchainnative.metadata.Result;
import org.blockchainnative.util.StringUtil;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.helper.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hyperledger.fabric.sdk.TransactionRequest.Type.*;

/**
 * @since 1.0
 * @author Matthias Veit
 */
public class FabricContractApiImpl implements FabricContractApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricContractApiImpl.class);

    private final HFClient client;
    private final Channel channel;

    private FabricContractInfo<?> contractInfo;

    public FabricContractApiImpl(HFClient client, Channel channel, FabricContractInfo<?> contractInfo) {
        this.client = client;
        this.channel = channel;
        this.contractInfo = contractInfo;
    }

    @Override
    public Observable<Event<String>> createChaincodeEventObservable(String eventName) {
        var contractInfo = getContractInfo();

        if(contractInfo.getChaincodeID() == null || StringUtil.isNullOrEmpty(contractInfo.getChaincodeID().getName())){
            throw new IllegalStateException("Chaincode not set in contractInfo");
        }

        var chaincodePattern = Pattern.compile(Pattern.quote(contractInfo.getChaincodeID().getName()));
        var eventNamePattern = Pattern.compile(Pattern.quote(eventName));

        return new ChaincodeEventObservable(client, channel, chaincodePattern, eventNamePattern);
    }

    @Override
    public void installChaincode(Collection<String> targetPeerNames, User user) {
        User previousUser = null;
        try {
            if (user != null) {
                previousUser = this.client.getUserContext();
                setUserContext(user);
            }

            // ensure that contract info is not null
            var contractInfo = getContractInfo();

            var installProposalRequest = prepareInstallProposalRequest(contractInfo);

            var targetPeers = getTargetPeers(targetPeerNames);
            LOGGER.info("Sending install proposal as user '{}' to peers: {}", this.client.getUserContext().getName(), peersToString(targetPeers));

            Collection<ProposalResponse> responses;
            try {
                responses = client.sendInstallProposal(installProposalRequest, targetPeers);
            } catch (ProposalException | InvalidArgumentException e) {
                var message = String.format("Failed to install chaincode '%s:%s': %s", installProposalRequest.getChaincodeName(), installProposalRequest.getChaincodeVersion(), e.getMessage());
                LOGGER.error(message, e);
                throw new ContractDeploymentException(message, e);
            }

            for (var response : responses) {
                if (response.isInvalid()) {
                    var message = String.format("Failed to install chaincode '%s:%s' on peer '%s': %s", installProposalRequest.getChaincodeName(), installProposalRequest.getChaincodeVersion(), response.getPeer().getName(), response.getMessage());
                    LOGGER.error(message);
                    throw new ContractDeploymentException(message);
                }

                // Set the contract to be installed on the target peers
                contractInfo.addInstalledOn(response.getPeer().getName());
                LOGGER.info("Installed chaincode '{}:{}' on peer '{}'.", installProposalRequest.getChaincodeName(), installProposalRequest.getChaincodeVersion(), response.getPeer().getName());
            }

        } finally {
            // restore user
            if (previousUser != null && !previousUser.equals(client.getUserContext())) {
                setUserContext(previousUser);
            }
        }
    }

    @Override
    public void instantiateChaincode(String[] arguments, Collection<String> targetPeerNames, User user) {
        User previousUser = null;
        try {
            if (user != null) {
                previousUser = this.client.getUserContext();
                setUserContext(user);
            }

            // ensure that contract info is not null
            var contractInfo = getContractInfo();

            var instantiateProposalRequest = prepareInstantiateProposalRequest(contractInfo);
            instantiateProposalRequest.setFcn("init");
            instantiateProposalRequest.setArgs(arguments);


            var targetPeers = getTargetPeers(targetPeerNames);
            LOGGER.info("Sending instantiate proposal as user '{}' to peers: {}", this.client.getUserContext().getName(), peersToString(targetPeers));

            Collection<ProposalResponse> responses;
            try {
                responses = channel.sendInstantiationProposal(instantiateProposalRequest, targetPeers);
            } catch (ProposalException | InvalidArgumentException e) {
                var message = String.format("Failed to instantiate chaincode '%s:%s': %s", instantiateProposalRequest.getChaincodeName(), instantiateProposalRequest.getChaincodeVersion(), e.getMessage());
                LOGGER.error(message, e);
                throw new ContractDeploymentException(message, e);
            }

            for (var response : responses) {
                if (response.isInvalid()) {
                    var message = String.format("Failed to instantiate chaincode '%s:%s' on peer '%s': %s", instantiateProposalRequest.getChaincodeName(), instantiateProposalRequest.getChaincodeVersion(), response.getPeer().getName(), response.getMessage());
                    LOGGER.error(message);
                    throw new ContractDeploymentException(message);
                }
            }

            try {
                channel.sendTransaction(responses).get();

                LOGGER.info("Successfully instantiated chaincode '{}:{}' on peers '{}'.", instantiateProposalRequest.getChaincodeName(), instantiateProposalRequest.getChaincodeVersion(), peersToString(targetPeers));
            } catch (InterruptedException | ExecutionException e) {
                var message = String.format("Failed to instantiate chaincode '%s:%s': %s", instantiateProposalRequest.getChaincodeName(), instantiateProposalRequest.getChaincodeVersion(), e.getMessage());
                LOGGER.error(message, e);
                throw new ContractDeploymentException(message);
            }

            // Set the contract to be instantiated on the target peers
            this.contractInfo.setInstantiatedOn(
                    targetPeers.stream()
                            .map(peer -> peer.getName())
                            .collect(Collectors.toSet()));

        } finally {
            // restore user
            if (previousUser != null && !previousUser.equals(client.getUserContext())) {
                setUserContext(previousUser);
            }
        }
    }

    @Override
    public Result<String> callChaincode(String functionName, String[] arguments, Collection<String> targetPeerNames, User user) {
        User previousUser = null;
        try {
            if (user != null) {
                previousUser = this.client.getUserContext();
                setUserContext(user);
            }

            var contractInfo = getContractInfo();
            if(contractInfo.getChaincodeID() == null){
                throw new IllegalStateException("ChaincodeID not set in contractInfo");
            }

            var proposal = client.newTransactionProposalRequest();
            proposal.setChaincodeID(contractInfo.getChaincodeID());
            proposal.setFcn(functionName);
            proposal.setArgs(arguments);


            Collection<ProposalResponse> responses;
            try {
                var targetPeers = getTargetPeers(targetPeerNames);
                LOGGER.info("Sending transaction proposal to peers: {}", peersToString(targetPeers));
                responses = validateProposalResponses(channel.sendTransactionProposal(proposal, targetPeers));
            } catch (ProposalException | InvalidArgumentException e) {
                LOGGER.error("Failed to send transaction proposal to peers.", e);
                throw new ContractCallException(e);
            }

            BlockEvent.TransactionEvent transactionEvent;
            try {
                transactionEvent = channel.sendTransaction(responses).get();
                LOGGER.info("Received block containing transaction {}.", transactionEvent.getTransactionID());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Failed to submit transaction proposals to orderer", e);
                throw new ContractCallException(e);
            }

            try {
                var payload = responses
                        .iterator().next() // take any of the responses (since all are consistent) and return the payload
                        .getChaincodeActionResponsePayload();
                var resultString = new String(payload, StandardCharsets.UTF_8);

                var blockHash = FabricUtil.getBlockHashFromEvent(client, transactionEvent.getBlockEvent());

                return new Result<>(resultString, blockHash, transactionEvent.getTransactionID());

            } catch (InvalidArgumentException e) {
                throw new ContractCallException(e);
            }

        } finally {
            // restore user
            if (previousUser != null && !previousUser.equals(client.getUserContext())) {
                setUserContext(previousUser);
            }
        }
    }

    @Override
    public Result<String> queryChaincode(String functionName, String[] arguments, Collection<String> targetPeerNames, User user) {
        User previousUser = null;
        try {
            if (user != null) {
                previousUser = this.client.getUserContext();
                setUserContext(user);
            }

            var contractInfo = getContractInfo();
            if(contractInfo.getChaincodeID() == null){
                throw new IllegalStateException("ChaincodeID not set in contractInfo");
            }

            var proposal = client.newQueryProposalRequest();
            proposal.setChaincodeID(contractInfo.getChaincodeID());
            proposal.setFcn(functionName);
            proposal.setArgs(arguments);

            Collection<ProposalResponse> responses;
            try {
                var targetPeers = getTargetPeers(targetPeerNames);
                LOGGER.info("Sending query proposal to peers: {}", peersToString(targetPeers));
                responses = validateProposalResponses(channel.queryByChaincode(proposal, targetPeers));
            } catch (ProposalException | InvalidArgumentException e) {
                LOGGER.error("Failed to send query proposal to peers.", e);
                throw new ContractCallException(e);
            }

            try {
                var payload = responses
                        .iterator().next() // take any of the responses (since all are consistent) and return the payload
                        .getChaincodeActionResponsePayload();
                var resultString = new String(payload, StandardCharsets.UTF_8);

                return new Result<>(resultString, null, null);

            } catch (InvalidArgumentException e) {
                throw new RuntimeException(e);
            }
        } finally {
            // restore user
            if (previousUser != null && !previousUser.equals(client.getUserContext())) {
                setUserContext(previousUser);
            }
        }
    }

    private FabricContractInfo<?> getContractInfo(){
        if(this.contractInfo == null){
            throw new IllegalStateException("ContractInfo must not be null");
        }

        return contractInfo;
    }

    @Override
    public void setContractInfo(FabricContractInfo<?> contractInfo) {
        this.contractInfo = contractInfo;
    }

    private InstallProposalRequest prepareInstallProposalRequest(FabricContractInfo<?> contractInfo) {
        LOGGER.info("Preparing install request for contract '{}'.", contractInfo.getIdentifier());
        var installProposalRequest = client.newInstallProposalRequest();
        if (contractInfo.getChaincodeID() == null) {
            var message = "Chaincode ID is not set in contractInfo!";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }
        LOGGER.info("Setting chaincode ID to '{}'", contractInfo.getChaincodeID());
        installProposalRequest.setChaincodeID(contractInfo.getChaincodeID());

        var chaincodeLanguage = contractInfo.getChaincodeLanguage();
        if (chaincodeLanguage == ChaincodeLanguage.Undefined) {
            LOGGER.warn("Chaincode language has not been set in contractInfo, defaulting to 'Go'.");
            chaincodeLanguage = ChaincodeLanguage.Go;
        } else {
            LOGGER.info("Setting chaincode language to '{}'", chaincodeLanguage);
        }
        installProposalRequest.setChaincodeLanguage(convertChaincodeLanguage(chaincodeLanguage));


        if (contractInfo.getChaincodeSourceDirectory() == null) {
            var message = "Chaincode source directory has not been set in contractInfo";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }
        var sourceDirectory = new File(contractInfo.getChaincodeSourceDirectory());
        if (!sourceDirectory.exists()) {
            var message = String.format("Chaincode source directory '%s' does not exist", sourceDirectory);
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }
        if (!sourceDirectory.isDirectory()) {
            var message = String.format("The given chaincode source directory '%s' is not a directory", sourceDirectory);
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }
        var files = sourceDirectory.listFiles();
        if (files == null || files.length == 0) {
            var message = String.format("The given chaincode source directory '%s' is empty", sourceDirectory);
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }
        LOGGER.info("Creating input stream from chaincode source directory '{}'", sourceDirectory);

        // analogous to the Hyperledger Fabric SDK
        String pathPrefix;
        switch (chaincodeLanguage) {
            case Go:
                var chaincodePath = contractInfo.getChaincodeID().getPath();

                if (chaincodePath == null) {
                    LOGGER.error("Path of chaincode ID is not set in contractInfo");
                    throw new IllegalStateException("Path of chaincode ID is not set in contractInfo!");
                }
                LOGGER.info("Setting chaincode path to '{}'", chaincodePath);
                installProposalRequest.setChaincodePath(chaincodePath);

                pathPrefix = Paths.get("src", chaincodePath).toString(); // required by Hyperledger SDK
                break;

            case Java:
            case Node:
                pathPrefix = null; // required to be null by Hyperledger SDK
                break;

            default:
                // this should never happen
                throw new IllegalStateException("Unexpected chaincode language");
        }

        try {
            var data = Utils.generateTarGz(sourceDirectory, pathPrefix, null);
            var inputStream = new ByteArrayInputStream(data);
            installProposalRequest.setChaincodeInputStream(inputStream);
        } catch (IOException | InvalidArgumentException e) {
            var message = String.format("Failed to create input stream from chaincode source directory '%s'", sourceDirectory);
            LOGGER.error(message, e);
            throw new ContractDeploymentException(message, e);
        }

        return installProposalRequest;
    }

    private void setUserContext(User user) {
        try {
            LOGGER.debug("Setting user context to user '{}'", user.getName());
            this.client.setUserContext(user);
        } catch (InvalidArgumentException e) {
            var message = String.format("Failed to set user context to user '%s'", user.getName());
            LOGGER.error(message, e);
            throw new ContractCallException(message, e);
        }
    }

    private Collection<Peer> getTargetPeers(Collection<String> targetPeerNames) {
        if (targetPeerNames == null || targetPeerNames.isEmpty()) {
            LOGGER.info("No target peer addresses registered, defaulting to all peers known at the channel");
            return channel.getPeers();
        }

        var targetPeers = channel.getPeers().stream()
                .filter(peer -> targetPeerNames.contains(peer.getName()))
                .collect(Collectors.toList());

        if (targetPeers.isEmpty()) {
            LOGGER.error("The specified target peer addresses didn't match any of the channels known addresses: \nGiven addresses: {}\nKnown addresses:{}", String.join(", ", targetPeerNames), peersToString(channel.getPeers()));
            throw new ContractCallException("Cannot call function, the specified target peer addresses didn't match any of the channels known addresses");
        }

        return targetPeers;
    }

    private InstantiateProposalRequest prepareInstantiateProposalRequest(FabricContractInfo<?> contractInfo) {
        LOGGER.info("Preparing instantiate request for contract '{}'.", contractInfo.getIdentifier());
        var instantiateProposalRequest = client.newInstantiationProposalRequest();

        if (contractInfo.getChaincodeID() == null) {
            var message = "Chaincode ID is not set in contractInfo!";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }
        LOGGER.info("Setting chaincode ID to '{}'", contractInfo.getChaincodeID());
        instantiateProposalRequest.setChaincodeID(contractInfo.getChaincodeID());

        var chaincodeLanguage = contractInfo.getChaincodeLanguage();
        if (chaincodeLanguage == ChaincodeLanguage.Undefined) {
            LOGGER.warn("Chaincode language has not been set in contractInfo, defaulting to 'Go'.");
            chaincodeLanguage = ChaincodeLanguage.Go;
        } else {
            LOGGER.info("Setting chaincode language to '{}'", chaincodeLanguage);
        }
        instantiateProposalRequest.setChaincodeLanguage(convertChaincodeLanguage(chaincodeLanguage));

        if (contractInfo.getChaincodePolicy() == null) {
            var message = "Chaincode endorsement policy is not set in contractInfo!";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }
        instantiateProposalRequest.setChaincodeEndorsementPolicy(contractInfo.getChaincodePolicy());

        return instantiateProposalRequest;
    }

    private Collection<ProposalResponse> validateProposalResponses(Collection<ProposalResponse> responses) {
        responses.forEach(response -> {
            if (response.isInvalid()) {
                LOGGER.warn("Received invalid response from peer '{}':{}", response.getPeer().getUrl(), response);
            } else {
                LOGGER.debug("Received response from peer '{}':{}", response.getPeer().getUrl(), response);
            }
        });

        Collection<Set<ProposalResponse>> proposalConsistencySets;
        var invalidResponses = new HashSet<ProposalResponse>();
        try {
            proposalConsistencySets = SDKUtils.getProposalConsistencySets(responses, invalidResponses);
        } catch (InvalidArgumentException e) {
            throw new IllegalArgumentException(e);
        }

        if (proposalConsistencySets.size() != 1) {
            LOGGER.error("Received responses are inconsistent from peers");
            throw new ContractCallException("Received inconsistent responses from peers");
        }
        return proposalConsistencySets.iterator().next();
    }

    private static String peersToString(Collection<Peer> targetPeers) {
        return targetPeers.stream().map(Peer::getUrl).collect(Collectors.joining(", "));
    }

    private static TransactionRequest.Type convertChaincodeLanguage(ChaincodeLanguage language) {
        switch (language) {
            case Node:
                return NODE;
            case Java:
                return JAVA;
            case Go:
                return GO_LANG;
            default:
                return GO_LANG;
        }
    }
}
