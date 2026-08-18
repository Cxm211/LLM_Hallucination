    public JavaType refineDeserializationType(final MapperConfig<?> config,
            final Annotated a, final JavaType baseType) throws JsonMappingException
    {
        JavaType type = baseType;
        final TypeFactory tf = config.getTypeFactory();

        final JsonDeserialize jsonDeser = _findAnnotation(a, JsonDeserialize.class);
        
        // Ok: start by refining the main type itself; common to all types
        final Class<?> valueClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.as());
        if ((valueClass != null) && !type.hasRawClass(valueClass)) {
            Class<?> currRaw = type.getRawClass();
            // Check for primitive/wrapper pair
            boolean primitiveWrapperPair = false;
            if (currRaw.isPrimitive() != valueClass.isPrimitive()) {
                Class<?> prim = currRaw.isPrimitive() ? currRaw : valueClass;
                Class<?> other = currRaw.isPrimitive() ? valueClass : currRaw;
                primitiveWrapperPair = (prim == Integer.TYPE && other == Integer.class) ||
                    (prim == Long.TYPE && other == Long.class) ||
                    (prim == Double.TYPE && other == Double.class) ||
                    (prim == Float.TYPE && other == Float.class) ||
                    (prim == Boolean.TYPE && other == Boolean.class) ||
                    (prim == Byte.TYPE && other == Byte.class) ||
                    (prim == Short.TYPE && other == Short.class) ||
                    (prim == Character.TYPE && other == Character.class) ||
                    (prim == Void.TYPE && other == Void.class);
            }
            if (!primitiveWrapperPair) {
                try {
                    type = tf.constructSpecializedType(type, valueClass);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException(null,
                            String.format("Failed to narrow type %s with annotation (value %s), from '%s': %s",
                                    type, valueClass.getName(), a.getName(), iae.getMessage()),
                                    iae);
                }
            }
        }
        // Then further processing for container types

        // First, key type (for Maps, Map-like types):
        if (type.isMapLikeType()) {
            JavaType keyType = type.getKeyType();
            final Class<?> keyClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.keyAs());
            if (keyClass != null) {
                Class<?> currRaw = keyType.getRawClass();
                boolean keyPrimitiveWrapperPair = false;
                if (currRaw.isPrimitive() != keyClass.isPrimitive()) {
                    Class<?> prim = currRaw.isPrimitive() ? currRaw : keyClass;
                    Class<?> other = currRaw.isPrimitive() ? keyClass : currRaw;
                    keyPrimitiveWrapperPair = (prim == Integer.TYPE && other == Integer.class) ||
                        (prim == Long.TYPE && other == Long.class) ||
                        (prim == Double.TYPE && other == Double.class) ||
                        (prim == Float.TYPE && other == Float.class) ||
                        (prim == Boolean.TYPE && other == Boolean.class) ||
                        (prim == Byte.TYPE && other == Byte.class) ||
                        (prim == Short.TYPE && other == Short.class) ||
                        (prim == Character.TYPE && other == Character.class) ||
                        (prim == Void.TYPE && other == Void.class);
                }
                if (!keyPrimitiveWrapperPair) {
                    try {
                        keyType = tf.constructSpecializedType(keyType, keyClass);
                        type = ((MapLikeType) type).withKeyType(keyType);
                    } catch (IllegalArgumentException iae) {
                        throw new JsonMappingException(null,
                                String.format("Failed to narrow key type of %s with concrete-type annotation (value %s), from '%s': %s",
                                        type, keyClass.getName(), a.getName(), iae.getMessage()),
                                        iae);
                    }
                }
            }
        }
        JavaType contentType = type.getContentType();
        if (contentType != null) { // collection[like], map[like], array, reference
            // And then value types for all containers:
            final Class<?> contentClass = (jsonDeser == null) ? null : _classIfExplicit(jsonDeser.contentAs());
            if (contentClass != null) {
                Class<?> currRaw = contentType.getRawClass();
                boolean contentPrimitiveWrapperPair = false;
                if (currRaw.isPrimitive() != contentClass.isPrimitive()) {
                    Class<?> prim = currRaw.isPrimitive() ? currRaw : contentClass;
                    Class<?> other = currRaw.isPrimitive() ? contentClass : currRaw;
                    contentPrimitiveWrapperPair = (prim == Integer.TYPE && other == Integer.class) ||
                        (prim == Long.TYPE && other == Long.class) ||
                        (prim == Double.TYPE && other == Double.class) ||
                        (prim == Float.TYPE && other == Float.class) ||
                        (prim == Boolean.TYPE && other == Boolean.class) ||
                        (prim == Byte.TYPE && other == Byte.class) ||
                        (prim == Short.TYPE && other == Short.class) ||
                        (prim == Character.TYPE && other == Character.class) ||
                        (prim == Void.TYPE && other == Void.class);
                }
                if (!contentPrimitiveWrapperPair) {
                    try {
                        contentType = tf.constructSpecializedType(contentType, contentClass);
                        type = type.withContentType(contentType);
                    } catch (IllegalArgumentException iae) {
                        throw new JsonMappingException(null,
                                String.format("Failed to narrow value type of %s with concrete-type annotation (value %s), from '%s': %s",
                                        type, contentClass.getName(), a.getName(), iae.getMessage()),
                                iae);
                    }
                }
            }
        }
        return type;
    }