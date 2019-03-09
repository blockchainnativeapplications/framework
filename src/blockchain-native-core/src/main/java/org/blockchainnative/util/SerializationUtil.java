package org.blockchainnative.util;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.blockchainnative.exceptions.SerializationException;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * Provides static utility methods for extracting serializing java native types to JSON. <br>
 * <br>
 * The class is not intended to be instantiated as it only provides static methods.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public final class SerializationUtil {

    /**
     * The class is not intended to be instantiated as it only provides static methods.
     */
    private SerializationUtil() {
    }

    /**
     * Tries to retrieve a class object from its name.
     *
     * @param className name of the class
     * @return the class object representing the given {@code className}.
     * @throws SerializationException if no class with the given name is found
     */
    public static Class<?> parseClass(String className) {
        if (StringUtil.isNullOrEmpty(className)) {
            throw new SerializationException("Cannot parse empty class name");
        }

        try {
            return ClassUtils.getClass(className);
        } catch (ClassNotFoundException e) {
            throw new SerializationException(String.format("Failed to get class for name '%s'", className, e));
        }
    }

    /**
     * Serializes a {@code Parameter} to {@code String}. <br>
     * <br>
     * The resulting {@code String} is of the format &lt;class name&gt;.&lt;method name&gt;(&lt;parameter type&gt;,
     * ...)[parameter index]
     *
     * @param parameter {@code Parameter} to be serialized
     * @return {@code String} representation of the given {@code Parameter}.
     * @see org.blockchainnative.util.SerializationUtil#buildMethodDescription(Executable)
     */
    public static String buildParameterDescription(Parameter parameter) {
        if (parameter == null) {
            throw new SerializationException("Parameter must not be null");
        }

        var parameterIndex = getParameterIndex(parameter.getDeclaringExecutable(), parameter);
        var methodDescription = buildMethodDescription(parameter.getDeclaringExecutable());

        return methodDescription + ("[" + parameterIndex + "]");
    }

    /**
     * Deserializes a {@code Parameter} from its {@code String} representation built by {@link
     * org.blockchainnative.util.SerializationUtil#buildParameterDescription(Parameter)}.
     *
     * @param parameterDescription {@code String}  representation of the {@code Parameter}
     * @return {@code Parameter} object represented by the given {@code String}
     * @throws org.blockchainnative.exceptions.SerializationException in case the {@code parameterDescription} is
     *                                                                invalid with respect to {@link org.blockchainnative.util.SerializationUtil#buildParameterDescription(Parameter)}
     * @see org.blockchainnative.util.SerializationUtil#buildParameterDescription(Parameter)
     */
    public static Parameter parseParameterDescription(String parameterDescription) {
        if (StringUtil.isNullOrEmpty(parameterDescription)) {
            throw new SerializationException("Cannot parse empty parameter description");
        }

        var splitIndex = parameterDescription.lastIndexOf('[');
        if (splitIndex < 0 || splitIndex == parameterDescription.length() - 1) {
            throw new SerializationException(String.format("Invalid field parameter '%s'", parameterDescription));
        }

        var methodDescription = parameterDescription.substring(0, splitIndex);
        var parameterIndex = parameterDescription.substring(splitIndex + 1, parameterDescription.length() - 1);


        var method = parseMethodDescription(methodDescription);
        var parameters = method.getParameters();

        try {
            var i = Integer.parseInt(parameterIndex);

            if (i < 0 || i > parameters.length - 1) {
                throw new SerializationException(String.format("Invalid parameter index '%s' for method '%s'  of class '%s'", i, method.getName(), method.getDeclaringClass().getCanonicalName()));
            }

            return parameters[i];

        } catch (NumberFormatException e) {
            throw new SerializationException(String.format("Failed to get parameter at index '%s' of method '%s' of class '%s'", parameterIndex, method.getName(), method.getDeclaringClass().getCanonicalName()), e);
        }

    }

    /**
     * Serializes an {@code Executable} to {@code String}. <br>
     * <br>
     * The resulting {@code String} is of the format &lt;class name&gt;.&lt;executable name&gt;(&lt;parameter type&gt;,
     * ...)
     *
     * @param executable {@code Executable} to be serialized
     * @return {@code String} representation of the given {@code Executable}.
     */
    public static String buildMethodDescription(Executable executable) {
        if (executable == null) {
            throw new SerializationException("Executable must not be null");
        }

        var builder = new StringBuilder();
        var parameterJoiner = new StringJoiner(", ");
        Arrays.stream(executable.getParameters()).forEach(parameter -> parameterJoiner.add(parameter.getType().getCanonicalName()));
        builder.append(executable.getDeclaringClass().getCanonicalName());
        builder.append(".");
        builder.append(executable.getName());
        builder.append("(");
        builder.append(parameterJoiner.toString());
        builder.append(")");
        return builder.toString();
    }


    /**
     * Deserializes a {@code Method} from its {@code String} representation built by {@link
     * org.blockchainnative.util.SerializationUtil#buildMethodDescription(Executable)}.
     *
     * @param methodDescription {@code String}  representation of the {@code Method}
     * @return {@code Method} object represented by the given {@code String}
     * @throws org.blockchainnative.exceptions.SerializationException in case the {@code methodDescription} is invalid
     *                                                                with respect to {@link org.blockchainnative.util.SerializationUtil#buildMethodDescription(Executable)}
     * @see org.blockchainnative.util.SerializationUtil#buildMethodDescription(Executable)
     */
    public static Method parseMethodDescription(String methodDescription) {
        if (StringUtil.isNullOrEmpty(methodDescription)) {
            throw new SerializationException("Cannot parse empty method description");
        }

        var pattern = Pattern.compile("(?<classname>([^\\.]+\\.)*(.*?))\\.(?<methodname>.*?)\\((?<parameters>.*)\\)");
        var match = pattern.matcher(methodDescription);
        if (!match.matches()) {
            throw new SerializationException(String.format("Invalid method description '%s'", methodDescription));
        }

        var className = match.group("classname");
        var methodName = match.group("methodname");
        var parameterTypes = Arrays.stream(match.group("parameters").split(","))
                .map(s -> s.trim())
                .filter(s -> !StringUtil.isNullOrEmpty(s))
                .map(s -> parseClass(s))
                .toArray(Class[]::new);

        var method = MethodUtils.getMatchingMethod(parseClass(className), methodName, parameterTypes);
        if (method != null) {
            return method;
        } else {
            throw new SerializationException(String.format("Failed to get method '%s' of class '%s'", methodName, className));
        }
    }

    /**
     * Serializes an {@code Field} to {@code String}. <br>
     * <br>
     * The resulting {@code String} is of the format &lt;class name&gt;.&lt;field name&gt;
     *
     * @param field {@code Field} to be serialized
     * @return {@code String} representation of the given {@code Field}.
     */
    public static String buildFieldDescription(Field field) {
        if (field == null) {
            throw new SerializationException("Field must not be null");
        }

        var builder = new StringBuilder();
        builder.append(field.getDeclaringClass().getCanonicalName());
        builder.append(".");
        builder.append(field.getName());
        return builder.toString();
    }

    /**
     * Deserializes a {@code Field} from its {@code String} representation built by {@link
     * org.blockchainnative.util.SerializationUtil#buildFieldDescription(Field)}.
     *
     * @param fieldDescription {@code String} representation of the {@code Field}
     * @return {@code Field} object represented by the given {@code String}
     * @throws org.blockchainnative.exceptions.SerializationException in case the {@code fieldDescription} is invalid
     *                                                                with respect to {@link org.blockchainnative.util.SerializationUtil#buildFieldDescription(Field)}
     * @see org.blockchainnative.util.SerializationUtil#buildFieldDescription(Field)
     */
    public static Field parseFieldDescription(String fieldDescription) {
        if (StringUtil.isNullOrEmpty(fieldDescription)) {
            throw new SerializationException("Cannot parse empty field description");
        }

        var splitIndex = fieldDescription.lastIndexOf('.');
        if (splitIndex < 0 || splitIndex == fieldDescription.length() - 1) {
            throw new SerializationException(String.format("Invalid field description '%s'", fieldDescription));
        }

        var className = fieldDescription.substring(0, splitIndex);
        var fieldName = fieldDescription.substring(splitIndex + 1);

        var field = FieldUtils.getField(parseClass(className), fieldName, true);
        if (field != null) {
            return field;
        } else {
            throw new SerializationException(String.format("Failed to get field '%s' of class '%s'", fieldName, className));
        }

    }

    private static int getParameterIndex(Executable method, Parameter parameter) {
        var index = ReflectionUtil.getParameterIndex(method, parameter);

        if (index > -1) {
            return index;
        }

        throw new SerializationException(String.format("Could not find field '%s' on method '%s'", parameter.getName(), method.toGenericString()));
    }
}
