package org.blockchainnative.spring.autoconfigure;

import org.blockchainnative.metadata.ContractInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.*;

import java.util.Collection;

import static org.blockchainnative.spring.autoconfigure.ContractFactory.CONTRACT_FACTORY_BEAN_NAME;
import static org.blockchainnative.spring.autoconfigure.ContractFactory.CONTRACT_FACTORY_METHOD_NAME;

/**
 * Registers contract info objects and smart contract wrapper for dependency injection. <br>
 * <br>
 * Subclasses need to specify the {@link ContractInfo} objects through {@link AbstractContractConfiguration#getContractInfos()}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class AbstractContractConfiguration implements BeanDefinitionRegistryPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContractConfiguration.class);

    private static final String CONTRACT_INFO_PREFIX = "contractInfo";

    public abstract Collection<? extends ContractInfo> getContractInfos();

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        LOGGER.debug("Registering contract infos for dependency injection");

        var contractInfos = getContractInfos();
        if(contractInfos == null || contractInfos.isEmpty()){
            LOGGER.warn("No contract infos have been registered.");
            return;
        }

        for(var contractInfo : contractInfos) {
            LOGGER.info("Registering contract '{}' ({}) as bean", contractInfo.getIdentifier(), contractInfo.getContractClass());

            var contractBeanDefinition = new RootBeanDefinition();
            contractBeanDefinition.setTargetType(contractInfo.getContractClass());
            contractBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
            contractBeanDefinition.setAutowireCandidate(true);
            contractBeanDefinition.setFactoryBeanName(CONTRACT_FACTORY_BEAN_NAME);
            contractBeanDefinition.setFactoryMethodName(CONTRACT_FACTORY_METHOD_NAME);

            contractBeanDefinition.addQualifier(new AutowireCandidateQualifier(contractInfo.getContractClass()));

            var ctorArgs = new ConstructorArgumentValues();
            ctorArgs.addGenericArgumentValue(contractInfo);
            contractBeanDefinition.setConstructorArgumentValues(ctorArgs);

            registry.registerBeanDefinition(contractInfo.getIdentifier(), contractBeanDefinition);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        var contractInfos = getContractInfos();
        if(contractInfos == null || contractInfos.isEmpty()){
            return;
        }

        for(var contractInfo : contractInfos) {
            LOGGER.info("Registering contract info '{}' as bean", contractInfo.getIdentifier());

            beanFactory.registerSingleton(getContractInfoBeanName(contractInfo.getIdentifier()), contractInfo);
        }
    }

    private static String getContractInfoBeanName(String contractIdentifier){
        return String.format("%s_%s", CONTRACT_INFO_PREFIX, contractIdentifier);
    }
}
