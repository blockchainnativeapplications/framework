package org.blockchainnative.builder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Comparator used to order {@code Parameter} objects by the order in which to occur in a method. <br>
 * Only works for parameters of the same method used to construct the {@code Comparator}. <br>
 * <br>
 * <b>For internal use only.</b>
 *
 * @author Matthias Veit
 * @since 1.0
 */
class ParameterByIndexComparator implements Comparator<Parameter> {
    private final Method method;
    private final Map<Parameter, Integer> orderMap;

    public ParameterByIndexComparator(Method method) {
        this.method = method;
        this.orderMap = new HashMap<>();

        var parameters = method.getParameters();
        for (var i = 0; i < parameters.length; i++) {
            this.orderMap.put(parameters[i], i);
        }
    }

    @Override
    public int compare(Parameter o1, Parameter o2) {
        return orderMap.get(o1).compareTo(orderMap.get(o2));
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
