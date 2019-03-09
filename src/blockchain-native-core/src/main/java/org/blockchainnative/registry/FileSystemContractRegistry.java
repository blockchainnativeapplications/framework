package org.blockchainnative.registry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.blockchainnative.metadata.ContractInfo;
import org.blockchainnative.metadata.MethodInfo;
import org.blockchainnative.serialization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ContractRegistry} which stores {@code ContractInfo} objects as JSON files on the filesystem
 * using {@link ObjectMapper}.
 * <p>
 * The user is responsible for making sure that a {@code ContractInfo} registered with this registry can be successfully
 * serialized through the use of {@code com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class FileSystemContractRegistry extends AbstractContractRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemContractRegistry.class);
    private static final String FILE_EXTENSION = ".json";

    private ObjectMapper mapper;
    private final List<Module> modules;
    private final Path basePath;


    /**
     * Creates a new {@code FileSystemContractRegistry} with a given base path.
     *
     * @param basePath path to which the {@code ContractInfo} objects are stored to/loaded from.
     * @throws IOException in case the basePath exists but is no directory, or the creation of the directory fails
     */
    public FileSystemContractRegistry(Path basePath) throws IOException {
        this.basePath = basePath;
        ensureBaseDirectory(basePath);
        this.modules = new ArrayList<>();
    }

    /**
     * Adds a {@link Module} to the object mapper used by the registry.
     *
     * @param module module to be added
     * @return the {@code FileSystemContractRegistry} itself
     */
    public FileSystemContractRegistry registerObjectMapperModule(Module module) {
        modules.add(module);
        this.mapper = null; // reset mapper
        return this;
    }

    private void ensureBaseDirectory(Path location) throws IOException {
        if (Files.exists(location)) {
            if (!Files.isDirectory(location)) {
                throw new IllegalArgumentException(String.format("Given path '%s' exists but is no directory", location));
            } else {
                // everything is fine
            }
        } else {
            Files.createDirectories(location);
        }
    }

    private ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper()
                    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                    .registerModule(new Jdk8Module())
                    .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                    .registerModule(
                            new SimpleModule()
                                    .setMixInAnnotation(MethodInfo.class, MethodInfoMixin.class)
                                    .addKeySerializer(Method.class, new MethodKeySerializer())
                                    .addKeyDeserializer(Method.class, new MethodKeyDeserializer())
                                    .addSerializer(Method.class, new MethodSerializer())
                                    .addSerializer(Parameter.class, new ParameterSerializer())
                                    .addDeserializer(Method.class, new MethodDeserializer())
                                    .addDeserializer(Parameter.class, new ParameterDeserializer())
                                    .addSerializer(Field.class, new FieldSerializer())
                                    .addDeserializer(Field.class, new FieldDeserializer()))
                    .registerModules(modules)
                    .enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, JsonTypeInfo.As.WRAPPER_ARRAY);
        }
        return mapper;
    }

    private String getContractInfoFileName(ContractInfo contractInfo) {
        return contractInfo.getIdentifier() + FILE_EXTENSION;
    }


    /**
     * Saves all registered {@code ContractInfo} objects to the base path of the registry.
     * Each {@code ContractInfo} is stored to a separate file named after its identifier.
     *
     * @throws IOException in case an error occurs while writing to the file system.
     */
    @Override
    public void persist() throws IOException {
        var mapper = getMapper();

        for (var contractInfo : super.contractInfos.values()) {
            try {
                var contractPath = basePath.resolve(getContractInfoFileName(contractInfo));

                mapper.writerWithDefaultPrettyPrinter()
                        .writeValue(contractPath.toFile(), contractInfo);
            } catch (IOException e) {
                LOGGER.error("Failed to persist contract info '{}': {}", contractInfo.getIdentifier(), e.getMessage(), e);
                throw e;
            }
        }
    }

    /**
     * Loads all {@code ContractInfo} objects from the base path of the registry
     *
     * @throws IOException in case an error occurs while reading from the file system.
     */
    @Override
    public void load() throws IOException {
        var mapper = getMapper();
        var contractInfoFiles = Files.list(basePath)
                .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(FILE_EXTENSION))
                .map(Path::toFile)
                .collect(Collectors.toList());

        for (var file : contractInfoFiles) {
            try {
                var contractInfo = (ContractInfo) mapper.readValue(file, Object.class);

                this.addContractInfo(contractInfo);
            } catch (IOException e) {
                LOGGER.error("Failed to load contract info file '{}': {}", file, e.getMessage(), e);
                throw e;
            }
        }
    }
}
