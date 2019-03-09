package org.blockchainnative.fabric.typeconverters;

import org.blockchainnative.convert.TypeConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Provides default {@code TypeConverter} for Hyperledger Fabric.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public final class FabricDefaultTypeConverters {

    public FabricDefaultTypeConverters() {
    }

    private static HashSet<TypeConverter<?,?>> typeConverters = new HashSet<>() {{
        add(new BooleanStringConverter());
        add(new ByteStringConverter());
        add(new ShortStringConverter());
        add(new IntegerStringConverter());
        add(new FloatStringConverter());
        add(new DoubleStringConverter());
        add(new LongStringConverter());
    }};

    /**
     * Provides an unmodifiable collection of {@code TypeConverter} for converting to following types to {@code String}.
     * <ul>
     *     <li>boolean</li>
     *     <li>byte</li>
     *     <li>short</li>
     *     <li>int</li>
     *     <li>long</li>
     *     <li>float</li>
     *     <li>double</li>
     * </ul>
     *
     * @return unmodifiable collection of {@code TypeConverter}
     */
    public static Collection<TypeConverter<?,?>> getConverters(){
        return Collections.unmodifiableCollection(typeConverters);
    }


}
