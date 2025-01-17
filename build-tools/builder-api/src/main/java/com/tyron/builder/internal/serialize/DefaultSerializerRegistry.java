package com.tyron.builder.internal.serialize;

import com.google.common.base.Objects;
import com.tyron.builder.api.internal.Cast;
import com.tyron.builder.api.internal.serialize.AbstractSerializer;
import com.tyron.builder.api.internal.serialize.Decoder;
import com.tyron.builder.api.internal.serialize.DefaultSerializer;
import com.tyron.builder.api.internal.serialize.Encoder;
import com.tyron.builder.api.internal.serialize.Serializer;
import com.tyron.builder.api.internal.serialize.SerializerRegistry;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Default implementation of {@link SerializerRegistry}.
 *
 * This class must be thread-safe because multiple tasks may be registering serializable classes concurrently, while other tasks are calling {@link #build(Class)}.
 */
@ThreadSafe
public class DefaultSerializerRegistry implements SerializerRegistry {
    private static final Comparator<Class<?>> CLASS_COMPARATOR = new Comparator<Class<?>>() {
        @Override
        public int compare(Class<?> o1, Class<?> o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };
    private final Map<Class<?>, Serializer<?>> serializerMap = new ConcurrentSkipListMap<Class<?>, Serializer<?>>(CLASS_COMPARATOR);

    // We are using a ConcurrentHashMap here because:
    //   - We don't want to use a Set with CLASS_COMPARATOR, since that would treat two classes with the same name originating from different classloaders as identical, allowing only one in the Set.
    //   - ConcurrentHashMap.newKeySet() isn't available on Java 6, yet, and that is where this code needs to run.
    //   - CopyOnWriteArraySet has slower insert performance, since it is not hash based.
    private final Map<Class<?>, Boolean> javaSerialization = new ConcurrentHashMap<Class<?>, Boolean>();
    private final SerializerClassMatcherStrategy classMatcher;

    public DefaultSerializerRegistry() {
        this(true);
    }

    public DefaultSerializerRegistry(boolean supportClassHierarchy) {
        this.classMatcher = supportClassHierarchy ? SerializerClassMatcherStrategy.HIERARCHY : SerializerClassMatcherStrategy.STRICT;
    }

    @Override
    public <T> void register(Class<T> implementationType, Serializer<T> serializer) {
        serializerMap.put(implementationType, serializer);
    }

    @Override
    public <T> void useJavaSerialization(Class<T> implementationType) {
        javaSerialization.put(implementationType, Boolean.TRUE);
    }

    @Override
    public boolean canSerialize(Class<?> baseType) {
        for (Class<?> candidate : serializerMap.keySet()) {
            if (classMatcher.matches(baseType, candidate)) {
                return true;
            }
        }
        for (Class<?> candidate : javaSerialization.keySet()) {
            if (classMatcher.matches(baseType, candidate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> Serializer<T> build(Class<T> baseType) {
        Map<Class<?>, Serializer<?>> matches = new LinkedHashMap<Class<?>, Serializer<?>>();
        for (Map.Entry<Class<?>, Serializer<?>> entry : serializerMap.entrySet()) {
            if (baseType.isAssignableFrom(entry.getKey())) {
                matches.put(entry.getKey(), entry.getValue());
            }
        }
        Set<Class<?>> matchingJavaSerialization = new LinkedHashSet<Class<?>>();
        for (Class<?> candidate : javaSerialization.keySet()) {
            if (baseType.isAssignableFrom(candidate)) {
                matchingJavaSerialization.add(candidate);
            }
        }
        if (matches.isEmpty() && matchingJavaSerialization.isEmpty()) {
            throw new IllegalArgumentException(String.format("Don't know how to serialize objects of type %s.", baseType.getName()));
        }
        if (matches.size() == 1 && matchingJavaSerialization.isEmpty()) {
            return Cast.uncheckedNonnullCast(matches.values().iterator().next());
        }
        return new TaggedTypeSerializer<T>(matches, matchingJavaSerialization);
    }

    private static class TypeInfo {
        final int tag;
        final boolean useForSubtypes;
        final Serializer<?> serializer;

        private TypeInfo(int tag, boolean useForSubtypes, Serializer<?> serializer) {
            this.tag = tag;
            this.useForSubtypes = useForSubtypes;
            this.serializer = serializer;
        }
    }

    private static class TaggedTypeSerializer<T> extends AbstractSerializer<T> {
        private static final int JAVA_TYPE = 1; // Reserve 0 for null (to be added later)
        private static final TypeInfo JAVA_SERIALIZATION = new TypeInfo(JAVA_TYPE, true, new DefaultSerializer<Object>());
        private final Map<Class<?>, TypeInfo> serializersByType = new HashMap<Class<?>, TypeInfo>();
        private final Map<Class<?>, TypeInfo> typeHierarchies = new HashMap<Class<?>, TypeInfo>();
        private final TypeInfo[] serializersByTag;

        TaggedTypeSerializer(Map<Class<?>, Serializer<?>> serializerMap, Set<Class<?>> javaSerialization) {
            serializersByTag = new TypeInfo[2 + serializerMap.size()];
            serializersByTag[JAVA_TYPE] = JAVA_SERIALIZATION;
            int nextTag = 2;
            for (Map.Entry<Class<?>, Serializer<?>> entry : serializerMap.entrySet()) {
                add(nextTag, entry.getKey(), entry.getValue());
                nextTag++;
            }
            for (Class<?> type : javaSerialization) {
                serializersByType.put(type, JAVA_SERIALIZATION);
                typeHierarchies.put(type, JAVA_SERIALIZATION);
            }
        }

        private void add(int tag, Class<?> type, Serializer<?> serializer) {
            TypeInfo typeInfo = new TypeInfo(tag, type.equals(Throwable.class), serializer);
            serializersByType.put(type, typeInfo);
            serializersByTag[typeInfo.tag] = typeInfo;
            if (typeInfo.useForSubtypes) {
                typeHierarchies.put(type, typeInfo);
            }
        }

        @Override
        public T read(Decoder decoder) throws Exception {
            int tag = decoder.readSmallInt();
            TypeInfo typeInfo = tag >= serializersByTag.length ? null : serializersByTag[tag];
            if (typeInfo == null) {
                throw new IllegalArgumentException(String.format("Unexpected type tag %d found.", tag));
            }
            return Cast.uncheckedNonnullCast(typeInfo.serializer.read(decoder));
        }

        @Override
        public void write(Encoder encoder, T value) throws Exception {
            TypeInfo typeInfo = map(value.getClass());
            encoder.writeSmallInt(typeInfo.tag);
            Cast.<Serializer<T>>uncheckedNonnullCast(typeInfo.serializer).write(encoder, value);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            TaggedTypeSerializer<?> rhs = (TaggedTypeSerializer<?>) obj;
            return Objects.equal(serializersByType, rhs.serializersByType)
                   && Objects.equal(typeHierarchies, rhs.typeHierarchies)
                   && Arrays.equals(serializersByTag, rhs.serializersByTag);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), serializersByType, typeHierarchies, Arrays.hashCode(serializersByTag));
        }

        private TypeInfo map(Class<?> valueType) {
            TypeInfo typeInfo = serializersByType.get(valueType);
            if (typeInfo != null) {
                return typeInfo;
            }
            for (Map.Entry<Class<?>, TypeInfo> entry : typeHierarchies.entrySet()) {
                if (entry.getKey().isAssignableFrom(valueType)) {
                    return entry.getValue();
                }
            }
            throw new IllegalArgumentException(String.format("Don't know how to serialize an object of type %s.", valueType.getName()));
        }
    }

    private interface SerializerClassMatcherStrategy {
        SerializerClassMatcherStrategy STRICT = new StrictSerializerMatcher();
        SerializerClassMatcherStrategy HIERARCHY = new HierarchySerializerMatcher();

        boolean matches(Class<?> baseType, Class<?> candidate);

    }

    private static final class HierarchySerializerMatcher implements SerializerClassMatcherStrategy {
        @Override
        public boolean matches(Class<?> baseType, Class<?> candidate) {
            return baseType.isAssignableFrom(candidate);
        }
    }

    private static class StrictSerializerMatcher implements SerializerClassMatcherStrategy {
        @Override
        public boolean matches(Class<?> baseType, Class<?> candidate) {
            return baseType.equals(candidate);
        }
    }
}
