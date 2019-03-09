package org.blockchainnative.test;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.blockchainnative.util.ReflectionUtil;
import org.blockchainnative.test.contracts.HelloContract;
import org.blockchainnative.test.contracts.ReflectionTestContractContract;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.util.Set;

/**
 * @author Matthias Veit
 */
public class ReflectionTests {

    @Test
    public void getStringReturnType() throws NoSuchMethodException {
        var method = HelloContract.class.getDeclaredMethod("hello", String.class);
        var returnType = method.getGenericReturnType();

        Assert.assertEquals(String.class, returnType);
    }


    @Test
    public void getSimpleGenericReturnType() throws NoSuchMethodException {
        var method = HelloContract.class.getDeclaredMethod("helloAsync", String.class);
        var returnType = method.getGenericReturnType();

        Assert.assertTrue(returnType instanceof ParameterizedType);

        var genericReturnType = (ParameterizedType)returnType;
        var typeArguments = genericReturnType.getActualTypeArguments();

        Assert.assertEquals(1, typeArguments.length);
        Assert.assertTrue(typeArguments[0] instanceof Class);
        Assert.assertEquals(String.class, typeArguments[0]);
    }

    @Test
    public void getStringReturnTypeReflectionUtil() throws NoSuchMethodException {
        var method = HelloContract.class.getDeclaredMethod("hello", String.class);
        var returnType = ReflectionUtil.getActualReturnType(method);

        Assert.assertEquals(String.class, returnType);
    }

    @Test
    public void getSimpleGenericReturnTypeInterfaceReflectionUtil() throws NoSuchMethodException {
        var method = HelloContract.class.getDeclaredMethod("helloAsync", String.class);
        var returnType = ReflectionUtil.getActualReturnType(method);

        Assert.assertEquals(String.class, returnType);
    }

    @Test
    public void getSimpleGenericReturnTypeClassReflectionUtil() throws NoSuchMethodException {
        var method = ReflectionTestContractContract.class.getDeclaredMethod("getSomeString");
        var returnType = ReflectionUtil.getActualReturnType(method);

        Assert.assertEquals(String.class, returnType);
    }


    @Test
    public void getSubclassedGenericReturnType4ReflectionUtil() throws NoSuchMethodException {
        var method = ReflectionTestContractContract.class.getDeclaredMethod("getSetOfStringArrays");
        var returnType = ReflectionUtil.getActualReturnType(method);

        Assert.assertEquals(TypeUtils.parameterize(Set.class, String[].class), returnType);
    }

    @Test
    public void getSubclassedGenericReturnType5ReflectionUtil() throws NoSuchMethodException {
        var method = ReflectionTestContractContract.class.getDeclaredMethod("getSetOfSetsOfStringArrays");
        var returnType = ReflectionUtil.getActualReturnType(method);

        Assert.assertEquals(TypeUtils.parameterize(Set.class, TypeUtils.parameterize(Set.class, String[].class)), returnType);
    }

    @Test
    public void getSubclassedGenericReturnType6ReflectionUtil() throws NoSuchMethodException {
        var method = ReflectionTestContractContract.class.getDeclaredMethod("getStringArray");
        var returnType = ReflectionUtil.getActualReturnType(method);

        Assert.assertEquals(String[][].class, returnType);
    }

}
